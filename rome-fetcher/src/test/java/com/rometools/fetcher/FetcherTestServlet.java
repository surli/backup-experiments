/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.rometools.fetcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

public class FetcherTestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String ETAG_1 = "ETAG-1";
    public static final String ETAG_2 = "ETAG-2";

    public static final String DELTA_FEED_TITLE = "Delta Encoded Feed";
    public static final String DELTA_FEED_ENTRY_TITLE = "Delta Encoded Feed Entry";

    public static final String SERVLET_MAPPING = "/FetcherTestServlet/*";
    public static final String SERVLET_MAPPING2 = "/FetcherTestServlet2/*";

    /**
     * @throws IOException
     * @throws
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        if ("TRUE".equalsIgnoreCase(request.getParameter("redirect"))) {
            // testing redirection support
            response.sendRedirect("/rome/FetcherTestServlet2/");
            return;
        } else if (request.getParameter("error") != null) {
            // response.sendError(HttpServletResponse.SC_NOT_FOUND);
            final int errorToThrow = Integer.parseInt(request.getParameter("error"));
            response.sendError(errorToThrow);
            return;
        } else {

            // We manually set the date headers using strings
            // instead of the get/setDateHeader methods because
            // those methods return longs, which has too much
            // precision for the real date headers
            // this is just a random date
            String lastModifiedDate = "Thu, 08 Jan 2009 23:06:39 GMT";
            String eTag = ETAG_1;

            if ("TRUE".equalsIgnoreCase(request.getParameter("refreshfeed"))) {
                lastModifiedDate = "Fri, 09 Jan 2009 12:06:39 GMT";
                eTag = ETAG_2;
            }

            final boolean serveFeed = checkModified(request, lastModifiedDate, eTag) || "TRUE".equalsIgnoreCase(request.getParameter("deltaencode"));
            final boolean gzip = "TRUE".equalsIgnoreCase(request.getParameter("gzipfeed"));

            if (serveFeed) {
                final String aimHeader = request.getHeader("A-IM");
                final boolean serveDeltaEncodedFeed = aimHeader != null && aimHeader.indexOf("feed") >= 0
                        && "TRUE".equalsIgnoreCase(request.getParameter("deltaencode"));
                if (serveDeltaEncodedFeed) {
                    try {
                        sendDeltaEncodedData(response, lastModifiedDate, request.getHeader("If-None-Match"), eTag, gzip);
                    } catch (final FeedException e) {
                        throw new ServletException(e);
                    }
                } else {
                    sendFeedData(response, lastModifiedDate, eTag, gzip);
                }
                return;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }
    }

    private boolean checkModified(final HttpServletRequest request, final String lastModifiedDate, final String eTag) {

        final String requestedETag = request.getHeader("If-None-Match");
        final String requestedLastModified = request.getHeader("If-Modified-Since");
        boolean modified = true;
        boolean mustServer = false;
        if (requestedETag != null) {
            if (eTag.equals(requestedETag)) {
                modified = false;
            } else {
                modified = true;
                mustServer = true;
            }
        }
        if (requestedLastModified != null) {
            if (lastModifiedDate.equals(requestedLastModified)) {
                modified = false;
            } else {
                modified = true;
                mustServer = true;
            }
        }
        final boolean serveFeed = modified || mustServer;
        return serveFeed;
    }

    /**
     * @param request
     * @param lastModifiedDate
     * @param tag
     * @param gzip
     * @throws IOException
     * @throws FeedException
     */
    private void sendDeltaEncodedData(final HttpServletResponse response, final String lastModifiedDate, final String requestedETag, final String responseETag,
            final boolean gzip) throws IOException, FeedException {
        if (ETAG_1.equals(requestedETag) || ETAG_2.equals(requestedETag)) {
            OutputStream out = null;
            if (gzip) {
                response.setHeader("Content-Encoding", "gzip");
                out = new GZIPOutputStream(response.getOutputStream());
            } else {
                out = response.getOutputStream();
            }

            response.setContentType("text/xml");
            response.setStatus(226);
            if (gzip) {
                response.setHeader("IM", "feed, gzip");
            } else {
                response.setHeader("IM", "feed");
            }

            if (responseETag != null) {
                response.setHeader("ETag", responseETag);
            }
            if (lastModifiedDate != null) {
                response.setHeader("Last-Modified", lastModifiedDate);
            }

            final SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType("atom_1.0");

            feed.setTitle(DELTA_FEED_TITLE);
            feed.setLink("http://rome.dev.java.net");
            feed.setDescription("This tests using rfc3229 delta encoding.");

            final List<SyndEntry> entries = new ArrayList<SyndEntry>();
            SyndEntry entry;
            SyndContent description;

            entry = new SyndEntryImpl();
            entry.setTitle(DELTA_FEED_ENTRY_TITLE);
            entry.setLink("http://bobwyman.pubsub.com/main/2004/09/using_rfc3229_w.html");
            try {
                final DateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
                entry.setPublishedDate(dateParser.parse("2004-11-25"));
            } catch (final ParseException ex) {
                //
            }
            description = new SyndContentImpl();
            description.setType("text/plain");
            description.setValue("Test for RFC3229 Delta Encoding");
            entry.setDescription(description);
            entries.add(entry);

            feed.setEntries(entries);

            final SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, new OutputStreamWriter(out));
        } else {
            sendFeedData(response, lastModifiedDate, responseETag, gzip);
        }
    }

    private void sendFeedData(final HttpServletResponse response, final String lastModifiedDate, final String eTag, final boolean gzip) throws IOException {
        OutputStream out = null;
        if (gzip) {
            response.setHeader("Content-Encoding", "gzip");
            out = new GZIPOutputStream(response.getOutputStream());
        } else {
            out = response.getOutputStream();
        }

        response.setContentType("text/xml");
        if (eTag != null) {
            response.setHeader("ETag", eTag);
        }
        if (lastModifiedDate != null) {
            response.setHeader("Last-Modified", lastModifiedDate);
        }

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/atom_1.0.xml");
        if (inputStream == null) {
            inputStream = this.getClass().getResourceAsStream("/atom_1.0.xml");
        }

        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                out.write(line.getBytes());
                line = null;
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        out.close();
    }
}
