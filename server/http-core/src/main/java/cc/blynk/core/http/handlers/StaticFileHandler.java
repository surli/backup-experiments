package cc.blynk.core.http.handlers;

import cc.blynk.server.core.protocol.handlers.DefaultExceptionHandler;
import cc.blynk.utils.ContentTypeUtil;
import cc.blynk.utils.ServerProperties;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 10.12.15.
 */
public class StaticFileHandler extends ChannelInboundHandlerAdapter implements DefaultExceptionHandler {

    private static final Logger log = LogManager.getLogger(StaticFileHandler.class);

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

    /**
     * Used for case when server started from IDE and static files wasn't unpacked from jar.
     */
    private final boolean isUnpacked;
    private final StaticFile[] staticPaths;

    public StaticFileHandler(boolean isUnpacked, StaticFile... staticPaths) {
        this.staticPaths = staticPaths;
        this.isUnpacked = isUnpacked;
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", StandardCharsets.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
     *
     * @param ctx
     *            Context
     */
    private static void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);
        setDateHeader(response);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Sets the Date header for the HTTP response
     *
     * @param response
     *            HTTP response
     */
    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param fileToCache
     *            file to extract content type
     */
    private static void setDateAndCacheHeaders(io.netty.handler.codec.http.HttpResponse response, File fileToCache, StaticFile staticFile) {
        if (staticFile.doCaching) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

            // Date header
            Calendar time = new GregorianCalendar();
            response.headers().set(DATE, dateFormatter.format(time.getTime()));

            // Add cache headers
            time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
            response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
            response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
            response.headers().set(
                    LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
        } else {
            response.headers().set(CACHE_CONTROL, "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            return;
        }

        FullHttpRequest req = (FullHttpRequest) msg;

        StaticFile staticFile = getStaticPath(req.uri());
        if (staticFile != null) {
            try {
                serveStatic(ctx, req, staticFile);
            } finally {
                ReferenceCountUtil.release(req);
            }
            return;
        }

        ctx.fireChannelRead(req);
    }

    private StaticFile getStaticPath(String path) {
        for (StaticFile staticPath : staticPaths) {
            if (staticPath.isStatic(path)) {
                return staticPath;
            }
        }
        return null;
    }

    private void serveStatic(ChannelHandlerContext ctx, FullHttpRequest request, StaticFile staticFile) throws Exception {
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        if (request.method() != HttpMethod.GET) {
            return;
        }

        Path path;
        String uri = request.uri();
        //running from jar
        if (isUnpacked) {
            if (staticFile instanceof StaticFileEdsWith) {
                StaticFileEdsWith staticFileEdsWith = (StaticFileEdsWith) staticFile;
                path = Paths.get(staticFileEdsWith.folderPathForStatic, uri);
            } else {
                path = ServerProperties.getFileInCurrentDir(uri);
            }
        } else {
            //for local mode / running from ide
            path = getPathForLocalRun(uri);
        }

        if (Files.isHidden(path) || Files.notExists(path)) {
            sendError(ctx, NOT_FOUND);
            return;
        }

        if (Files.isDirectory(path)) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        File file = path.toFile();

        // Cache Validation
        String ifModifiedSince = request.headers().get(IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx);
                return;
            }
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            sendError(ctx, NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        io.netty.handler.codec.http.HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, fileLength);

        //setting content type
        response.headers().set(CONTENT_TYPE, ContentTypeUtil.getContentType(file.getName()));

        //todo setup caching for files.
        setDateAndCacheHeaders(response, file, staticFile);
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the initial line and the header.
        ctx.write(response);

        // Write the content.
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            // Write the end marker.
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } else {
            sendFileFuture =
                    ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                            ctx.newProgressivePromise());
            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;
        }

        // Decide whether to close the connection or not.
        if (!HttpUtil.isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private Path getPathForLocalRun(String uri) {
        Path path = Paths.get("./server/http-admin/target/classes", uri);

        if (Files.exists(path)) {
            return path;
        }

        //path for integration tests
        path = Paths.get("../server/http-admin/target/classes" , uri);

        if (Files.exists(path)) {
            return path;
        }

        //path for integration tests
        path = Paths.get("../server/http-core/target/classes" , uri);

        if (Files.exists(path)) {
            return path;
        }

        if (uri.endsWith(".csv.gz")) {
            return Paths.get("/tmp/blynk", uri);
        }

        //last hope
        return Paths.get("./server/http-api/target/classes", uri);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause.getMessage() != null && cause.getMessage().contains("unknown_ca")) {
            log.warn("Self-generated certificate.");
        } else {
            handleGeneralException(ctx, cause);
        }
    }

}
