package zmq.socket.pubsub;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZMQ;
import zmq.util.Utils;

public class TestPubsubTcp
{
    @Test
    public void testPubsubTcp() throws Exception
    {
        int port = Utils.findOpenPort();
        Ctx ctx = ZMQ.createContext();
        assertThat(ctx, notNullValue());

        SocketBase pubBind = ZMQ.socket(ctx, ZMQ.ZMQ_PUB);
        assertThat(pubBind, notNullValue());
        ZMQ.setSocketOption(pubBind, ZMQ.ZMQ_XPUB_NODROP, true);

        boolean rc = ZMQ.bind(pubBind, "tcp://127.0.0.1:" + port);
        assertThat(rc, is(true));

        SocketBase subConnect = ZMQ.socket(ctx, ZMQ.ZMQ_SUB);
        assertThat(subConnect, notNullValue());

        subConnect.setSocketOpt(ZMQ.ZMQ_SUBSCRIBE, "topic");
        rc = ZMQ.connect(subConnect, "tcp://127.0.0.1:" + port);
        assertThat(rc, is(true));

        ZMQ.sleep(1);

        pubBind.send(new Msg("topic abc".getBytes(ZMQ.CHARSET)), 0);
        pubBind.send(new Msg("topix defg".getBytes(ZMQ.CHARSET)), 0);
        pubBind.send(new Msg("topic defgh".getBytes(ZMQ.CHARSET)), 0);

        Msg msg = subConnect.recv(0);
        assertThat(msg.size(), is(9));

        msg = subConnect.recv(0);
        assertThat(msg.size(), is(11));

        ZMQ.close(subConnect);
        ZMQ.close(pubBind);
        ZMQ.term(ctx);
    }
}
