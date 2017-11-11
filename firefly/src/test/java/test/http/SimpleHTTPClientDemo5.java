package test.http;

import com.firefly.$;
import com.firefly.client.http2.SimpleHTTPClient;
import com.firefly.client.http2.SimpleHTTPClientConfiguration;
import com.firefly.client.http2.SimpleResponse;
import com.firefly.codec.http2.model.HttpStatus;
import com.firefly.net.tcp.TcpConfiguration;
import com.firefly.net.tcp.secure.SelfSignedCertificateOpenSSLContextFactory;
import com.firefly.utils.heartbeat.Result;
import com.firefly.utils.heartbeat.Task;
import com.firefly.utils.io.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Pengtao Qiu
 */
public class SimpleHTTPClientDemo5 {

    private final static Logger log = LoggerFactory.getLogger(SimpleHTTPClientDemo5.class);

    public static void main(String[] args) {
        SimpleHTTPClientConfiguration httpClientConfiguration = new SimpleHTTPClientConfiguration();
        httpClientConfiguration.setSecureConnectionEnabled(true);
        httpClientConfiguration.setSecureSessionFactory(new SelfSignedCertificateOpenSSLContextFactory());
        $.createHTTPClient(httpClientConfiguration).get("https://www.jd.com").submit()
         .thenAccept(resp -> System.out.println(resp.getStringBody()));
    }

    public static void main6(String[] args) throws Exception {
//        Task task = new Task();
//        task.setTask(() -> $.httpsClient().head("https://github.com").submit()
//                            .thenApply(res -> res.getStatus() == HttpStatus.OK_200? Result.SUCCESS : Result.FAILURE));
//        task.setName("https://github.com");
//        task.setResultListener((name, result, ex) -> System.out.println("the " + name + " health check result -> " + result));
//        $.httpsClient().registerHealthCheck(task);

//        $.httpsClient().head("https://github.com")
//         .submit()
//         .thenAccept(resp -> {
//             System.out.println(resp.getStatus());
//             System.out.println(resp.getFields());
//             System.out.println(resp.getStringBody());
//         });

        $.httpsClient().get("https://login.taobao.com").submit()
         .thenAccept(resp -> System.out.println(resp.getStringBody("GBK")));

//        System.out.println($.httpsClient().get("https://www.taobao.com").submit().get().getStringBody());
//        System.out.println($.httpsClient().get("https://github.com").submit().get().getStringBody());
//        System.out.println($.httpsClient().get("https://segmentfault.com").submit().get().getStringBody());
    }

    public static void main5(String[] args) {
        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < 25; i++) { // tls.ctf.network
                long start = System.currentTimeMillis();
                $.httpsClient().get("https://www.taobao.com").submit()
                 .thenApply(SimpleResponse::getStringBody)
                 .thenAccept(System.out::println)
                 .thenAccept(res -> {
                     System.out.print("------------------------");
                     System.out.println("time: " + (System.currentTimeMillis() - start));
                 });
            }
            $.thread.sleep(5000L);
        }
    }

    public static void main4(String[] args) throws ExecutionException, InterruptedException {
        for (int j = 0; j < 1000; j++) {
            for (int i = 0; i < 25; i++) {
                long start = System.currentTimeMillis();
                $.httpsClient().get("https://login.taobao.com")
                 .submit()
                 .thenApply(res -> res.getStringBody("GBK"))
                 .thenAccept(v -> {
                     System.out.println("----login time: " + (System.currentTimeMillis() - start) + "| body size: " + v.length());
                     log.info("----------------> login body -> {}", v);
                 });

                $.httpsClient().get("https://www.taobao.com/")
                 .submit()
                 .thenApply(res -> res.getStringBody("UTF-8"))
                 .thenAccept(v -> {
                     System.out.println("----index time: " + (System.currentTimeMillis() - start) + "| body size: " + v.length());
                     log.info("----------------> index body -> {}", v);
                 });
            }
            Thread.sleep(5000L);
        }
    }

    public static void main3(String[] args) {
        SimpleHTTPClient client = new SimpleHTTPClient();
        for (int i = 0; i < 200; i++) {
            try {
                long start = System.currentTimeMillis();
                CompletableFuture<SimpleResponse> future = client.get("http://www.csdn.net").submit();
                SimpleResponse response = future.get(2, TimeUnit.SECONDS);
                long end = System.currentTimeMillis();
                System.out.println(response.getResponse());
                System.out.println(response.getResponse().getFields());
                System.out.println(response.getResponse().getContentLength() + "|" + (end - start));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main2(String[] args) {
        SimpleHTTPClient client = new SimpleHTTPClient();
        client.get("http://www.csdn.net")
              .headerComplete(res -> {
                  System.out.println(res.toString());
                  System.out.println(res.getFields());
              })
              .content(buf -> System.out.println(BufferUtils.toUTF8String(buf)))
              .contentComplete(res -> System.out.println("content complete"))
              .messageComplete(res -> System.out.println("ok"))
              .end();

    }

    public static void main1(String[] args) {
        SimpleHTTPClientConfiguration httpConfiguration = new SimpleHTTPClientConfiguration();
        httpConfiguration.setSecureConnectionEnabled(true);

        SimpleHTTPClient client = new SimpleHTTPClient(httpConfiguration);
        client.get("https://login.taobao.com/")
              .headerComplete(res -> {
                  System.out.println(res.toString());
                  System.out.println(res.getFields());
              })
              .content(buf -> System.out.println(BufferUtils.toString(buf, Charset.forName("GBK"))))
              .contentComplete(res -> System.out.println("content complete"))
              .messageComplete(res -> System.out.println("ok"))
              .end();
    }
}
