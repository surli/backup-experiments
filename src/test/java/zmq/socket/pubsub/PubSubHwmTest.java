package zmq.socket.pubsub;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import zmq.Ctx;
import zmq.SocketBase;
import zmq.ZError;
import zmq.ZMQ;
import zmq.util.Utils;

public class PubSubHwmTest
{
    @Test
    public void testDefaults()
    {
        // send 1000 msg on hwm 1000, receive 1000
        int count = testDefaults(1000, 1000);
        assertThat(count, is(1000));
    }

    @Test
    public void testBlocking()
    {
        // send 6000 msg on hwm 2000, drops above hwm, only receive hwm
        int count = testBlocking(2000, 6000);
        assertThat(count, is(6000));
    }

    private int testDefaults(int sendHwm, int msgCnt)
    {
        Ctx ctx = ZMQ.createContext();

        // Set up bind socket
        SocketBase pub = ctx.createSocket(ZMQ.ZMQ_PUB);
        boolean rc = ZMQ.bind(pub, "inproc://a");
        assertThat(rc, is(true));

        // Set up connect socket
        SocketBase sub = ctx.createSocket(ZMQ.ZMQ_SUB);
        rc = ZMQ.connect(sub, "inproc://a");
        assertThat(rc, is(true));

        //set a hwm on publisher
        rc = ZMQ.setSocketOption(pub, ZMQ.ZMQ_SNDHWM, sendHwm);
        assertThat(rc, is(true));

        rc = ZMQ.setSocketOption(sub, ZMQ.ZMQ_SUBSCRIBE, new byte[0]);
        assertThat(rc, is(true));

        // Send until we block
        int sendCount = 0;
        while (sendCount < msgCnt && ZMQ.send(pub, "", ZMQ.ZMQ_DONTWAIT) == 0) {
            ++sendCount;
        }

        // Now receive all sent messages
        int recvCount = 0;
        while (null != ZMQ.recv(sub, ZMQ.ZMQ_DONTWAIT)) {
            ++recvCount;
        }
        assertThat(sendCount, is(recvCount));

        // Clean up
        ZMQ.close(sub);
        ZMQ.close(pub);
        ZMQ.term(ctx);

        return recvCount;
    }

    private int receive(SocketBase socket)
    {
        int recvCount = 0;
        // Now receive all sent messages
        while (null != ZMQ.recv(socket, ZMQ.ZMQ_DONTWAIT)) {
            ++recvCount;
        }

        return recvCount;
    }

    private int testBlocking(int sendHwm, int msgCnt)
    {
        Ctx ctx = ZMQ.createContext();

        // Set up bind socket
        SocketBase pub = ctx.createSocket(ZMQ.ZMQ_PUB);
        boolean rc = ZMQ.bind(pub, "inproc://a");
        assertThat(rc, is(true));

        // Set up connect socket
        SocketBase sub = ctx.createSocket(ZMQ.ZMQ_SUB);
        rc = ZMQ.connect(sub, "inproc://a");
        assertThat(rc, is(true));

        //set a hwm on publisher
        rc = ZMQ.setSocketOption(pub, ZMQ.ZMQ_SNDHWM, sendHwm);
        assertThat(rc, is(true));

        rc = ZMQ.setSocketOption(pub, ZMQ.ZMQ_XPUB_NODROP, true);
        assertThat(rc, is(true));

        rc = ZMQ.setSocketOption(sub, ZMQ.ZMQ_SUBSCRIBE, new byte[0]);
        assertThat(rc, is(true));

        // Send until we block
        int sendCount = 0;
        int recvCount = 0;
        while (sendCount < msgCnt) {
            int ret = ZMQ.send(pub, "", ZMQ.ZMQ_DONTWAIT);
            if (ret == 0) {
                ++sendCount;
            }
            else if (ret == -1) {
                assertThat(pub.errno(), is(ZError.EAGAIN));
                recvCount += receive(sub);

                assertThat(sendCount, is(recvCount));
            }
        }

        recvCount += receive(sub);

        // Clean up
        ZMQ.close(sub);
        ZMQ.close(pub);
        ZMQ.term(ctx);

        return recvCount;
    }

    @Test
    public void testResetHwm() throws IOException
    {
        // hwm should apply to the messages that have already been received
        // with hwm 11024: send 9999 msg, receive 9999, send 1100, receive 1100

        int firstCount = 9999;
        int secondCount = 1100;
        int hwm = 11024;

        int port = Utils.findOpenPort();
        Ctx ctx = ZMQ.createContext();

        // Set up bind socket
        SocketBase pub = ctx.createSocket(ZMQ.ZMQ_PUB);
        boolean rc = ZMQ.setSocketOption(pub, ZMQ.ZMQ_SNDHWM, hwm);
        assertThat(rc, is(true));

        rc = ZMQ.bind(pub, "tcp://localhost:" + port);
        assertThat(rc, is(true));

        // Set up connect socket
        SocketBase sub = ctx.createSocket(ZMQ.ZMQ_SUB);

        rc = ZMQ.setSocketOption(sub, ZMQ.ZMQ_RCVHWM, hwm);
        assertThat(rc, is(true));

        rc = ZMQ.connect(sub, "tcp://localhost:" + port);
        assertThat(rc, is(true));

        rc = ZMQ.setSocketOption(sub, ZMQ.ZMQ_SUBSCRIBE, new byte[0]);
        assertThat(rc, is(true));

        ZMQ.sleep(1);

        // Send messages
        int sendCount = 0;
        while (sendCount < firstCount && ZMQ.send(pub, "1", ZMQ.ZMQ_DONTWAIT) == 1) {
            ++sendCount;
        }
        assertThat(sendCount, is(firstCount));

        ZMQ.msleep(100);

        // Now receive all sent messages
        int recvCount = 0;
        while (null != ZMQ.recv(sub, ZMQ.ZMQ_DONTWAIT)) {
            ++recvCount;
        }
        assertThat(recvCount, is(firstCount));

        ZMQ.msleep(100);

        sendCount = 0;
        while (sendCount < secondCount && ZMQ.send(pub, "2", ZMQ.ZMQ_DONTWAIT) == 1) {
            ++sendCount;
        }
        assertThat(sendCount, is(secondCount));

        ZMQ.msleep(200);

        // Now receive all sent messages
        recvCount = 0;
        while (null != ZMQ.recv(sub, ZMQ.ZMQ_DONTWAIT)) {
            ++recvCount;
        }
        assertThat(recvCount, is(secondCount));

        // Clean up
        ZMQ.close(sub);
        ZMQ.close(pub);
        ZMQ.term(ctx);
    }
}
