package cc.blynk.integration.tcp;

import cc.blynk.integration.IntegrationBase;
import cc.blynk.integration.model.tcp.TestAppClient;
import cc.blynk.integration.model.tcp.TestHardClient;
import cc.blynk.server.application.AppServer;
import cc.blynk.server.core.BaseServer;
import cc.blynk.server.core.model.DashBoard;
import cc.blynk.server.core.protocol.model.messages.ResponseMessage;
import cc.blynk.server.core.protocol.model.messages.appllication.GetServerMessage;
import cc.blynk.server.core.protocol.model.messages.hardware.ConnectRedirectMessage;
import cc.blynk.server.hardware.HardwareServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;

import static cc.blynk.server.core.protocol.enums.Response.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 5/09/2016.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadBalancingTest extends IntegrationBase {

    private BaseServer appServer;
    private BaseServer hardwareServer;

    @Before
    public void init() throws Exception {
        hardwareServer = new HardwareServer(holder).start();
        appServer = new AppServer(holder).start();

        try (Jedis jedis = holder.redisClient.getTokenPool().getResource()) {
            jedis.flushDB();
        }
        try (Jedis jedis = holder.redisClient.getUserPool().getResource()) {
            jedis.flushDB();
        }
    }

    @After
    public void shutdown() {
        appServer.close();
        hardwareServer.close();
    }

    @Test
    public void testCreateFewAccountWithDifferentApp() throws Exception {
        TestAppClient appClient1 = new TestAppClient("localhost", tcpAppPort, properties);
        appClient1.start();

        String email = "test@gmmail.com";
        String pass = "a";
        String appName = "Blynk";

        appClient1.send("getServer");
        verify(appClient1.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(1, ILLEGAL_COMMAND)));

        appClient1.send("getServer " + email + "\0" + appName);
        verify(appClient1.responseMock, timeout(1000)).channelRead(any(), eq(new GetServerMessage(2, "127.0.0.1")));

        appClient1.send("register " + email + " " + pass + " " + appName);
        verify(appClient1.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(3, OK)));
        appClient1.send("login " + email + " " + pass + " Android 1.10.4 " + appName);
        //we should wait until login finished. Only after that we can send commands
        verify(appClient1.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(4, OK)));

        appClient1.send("getServer " + email + "\0" + appName);
        verify(appClient1.responseMock, timeout(1000).times(0)).channelRead(any(), eq(new GetServerMessage(5, "127.0.0.1")));
    }

    @Test
    @Ignore
    public void testConnectRedirect() throws Exception {
        TestHardClient hardClient = new TestHardClient("localhost", tcpHardPort);
        hardClient.start();

        hardClient.send("login 123");
        verify(hardClient.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(1, INVALID_TOKEN)));

        holder.redisClient.assignServerToToken("123", "123.123.123.123");
        hardClient.send("login 123");
        verify(hardClient.responseMock, timeout(1000)).channelRead(any(), eq(new ConnectRedirectMessage(2, b("123.123.123.123 9442"))));
    }

    @Test
    public void testNoRedirectAsTokenIsWrong() throws Exception {
        TestHardClient hardClient = new TestHardClient("localhost", tcpHardPort);
        hardClient.start();

        hardClient.send("login 123");
        verify(hardClient.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(1, INVALID_TOKEN)));

        holder.redisClient.assignServerToToken("123", "127.0.0.1");
        hardClient.send("login 123");
        verify(hardClient.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(2, INVALID_TOKEN)));
    }

    @Test
    public void testNewUserStoredInRedis() throws Exception {
        TestAppClient appClient1 = new TestAppClient("localhost", tcpAppPort, properties);
        appClient1.start();

        String email = "test_new@gmail.com";
        String pass = "a";
        String appName = "Blynk";

        appClient1.send("getServer " + email + "\0" + appName);
        verify(appClient1.responseMock, timeout(1000)).channelRead(any(), eq(new GetServerMessage(1, "127.0.0.1")));

        appClient1.reset();

        String token = workflowForUser(appClient1, email, pass, appName);
        assertEquals( "127.0.0.1", holder.redisClient.getServerByToken(token));
        assertEquals( "127.0.0.1", holder.redisClient.getServerByUser(email));
    }

    @Test
    public void testUserIsOnOtherServer() throws Exception {
        TestAppClient appClient1 = new TestAppClient("localhost", tcpAppPort, properties);
        appClient1.start();

        String email = "test_new@gmail.com";
        String appName = "Blynk";

        holder.redisClient.assignServerToUser(email, "100.100.100.100");

        appClient1.send("getServer " + email + "\0" + appName);
        verify(appClient1.responseMock, timeout(1000)).channelRead(any(), eq(new GetServerMessage(1, "100.100.100.100")));
    }

    @Test
    public void testDeleteTokenFromRedis() throws Exception {
        TestAppClient appClient1 = new TestAppClient("localhost", tcpAppPort, properties);
        appClient1.start();

        String email = "test_new@gmail.com";
        String pass = "a";
        String appName = "Blynk";

        appClient1.send("getServer " + email + "\0" + appName);
        verify(appClient1.responseMock, timeout(1000)).channelRead(any(), eq(new GetServerMessage(1, "127.0.0.1")));

        appClient1.reset();

        String token = workflowForUser(appClient1, email, pass, appName);
        assertEquals( "127.0.0.1", holder.redisClient.getServerByToken(token));
        assertEquals( "127.0.0.1", holder.redisClient.getServerByUser(email));

        appClient1.reset();
        appClient1.send("deleteDash 1");
        verify(appClient1.responseMock, timeout(1000)).channelRead(any(), eq(ok(1)));
        assertNull(holder.redisClient.getServerByToken(token));
        assertEquals( "127.0.0.1", holder.redisClient.getServerByUser(email));
    }

    private String workflowForUser(TestAppClient appClient, String email, String pass, String appName) throws Exception{
        appClient.send("register " + email + " " + pass + " " + appName);
        verify(appClient.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(1, OK)));
        appClient.send("login " + email + " " + pass + " Android 1.10.4 " + appName);
        //we should wait until login finished. Only after that we can send commands
        verify(appClient.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(2, OK)));

        DashBoard dash = new DashBoard();
        dash.id = 1;
        dash.name = "test";
        appClient.send("createDash " + dash.toString());
        verify(appClient.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(3, OK)));
        appClient.send("activate 1");
        verify(appClient.responseMock, timeout(1000)).channelRead(any(), eq(new ResponseMessage(4, DEVICE_NOT_IN_NETWORK)));

        appClient.reset();
        appClient.send("getToken 1");

        String token = appClient.getBody();
        assertNotNull(token);
        return token;
    }

}
