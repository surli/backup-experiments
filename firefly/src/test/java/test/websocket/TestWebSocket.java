package test.websocket;

import com.firefly.$;
import com.firefly.client.websocket.SimpleWebSocketClient;
import com.firefly.server.websocket.SimpleWebSocketServer;
import com.firefly.utils.RandomUtils;
import com.firefly.utils.io.BufferUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author Pengtao Qiu
 */
@RunWith(Parameterized.class)
public class TestWebSocket {

    @Parameterized.Parameter
    public Run r;

    static class Run {
        int port;
        int maxMsg;
        String testName;
        SimpleWebSocketServer server;
        SimpleWebSocketClient client;

        @Override
        public String toString() {
            return testName;
        }
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Run> data() {
        List<Run> data = new ArrayList<>();
        Run run = new Run();
        run.port = (int) RandomUtils.random(3000, 65534);
        run.maxMsg = 10;
        run.testName = "Test the WebSocket";
        run.server = $.createWebSocketServer();
        run.client = $.createWebSocketClient();
        data.add(run);

        run = new Run();
        run.port = (int) RandomUtils.random(3000, 65534);
        run.maxMsg = 10;
        run.testName = "Test the secure WebSocket";
        run.server = $.createSecureWebSocketServer();
        run.client = $.createSecureWebSocketClient();
        data.add(run);
        return data;
    }

    @Test
    public void test() throws InterruptedException {
        _test(Collections.emptyList());
    }

    @Test
    public void testDeflate() throws InterruptedException {
        _test(Collections.singletonList("deflate-frame"));
    }

    public void _test(List<String> extensions) throws InterruptedException {
        SimpleWebSocketServer server = r.server;
        SimpleWebSocketClient client = r.client;
        String host = "localhost";
        int port = r.port;
        int count = r.maxMsg;

        CountDownLatch latch = new CountDownLatch(count * 2 + 1);
        server.webSocket("/helloWebSocket")
              .onConnect(conn -> {
                  for (int i = 0; i < count; i++) {
                      conn.sendText("Msg: " + i);
                      conn.sendData(("Data: " + i).getBytes(StandardCharsets.UTF_8));
                  }
              })
              .onText((text, conn) -> {
                  System.out.println("Server received: " + text);
                  latch.countDown();
              })
              .listen(host, port);

        client.webSocket("http://" + host + ":" + port + "/helloWebSocket")
              .putExtension(extensions)
              .onText((text, conn) -> {
                  System.out.println("Client received: " + text);
                  latch.countDown();
              })
              .onData((buf, conn) -> {
                  System.out.println("Client received: " + BufferUtils.toString(buf));
                  latch.countDown();
              })
              .connect()
              .thenAccept(conn -> conn.sendText("Hello Websocket"));

        latch.await();
        server.stop();
        client.stop();
    }
}
