package zmq.io.coder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import zmq.Helper;
import zmq.Msg;
import zmq.ZMQ;
import zmq.util.Errno;
import zmq.util.ValueReference;

public class CustomEncoderTest
{
    private Helper.DummySocketChannel sock = new Helper.DummySocketChannel();

    private int write(ByteBuffer out)
    {
        try {
            return sock.write(out);
        }
        catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    static class CustomEncoder extends EncoderBase
    {
        public static final boolean RAW_ENCODER = true;
        private final Runnable      readHeader  = new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        readHeader();
                                                    }
                                                };
        private final Runnable      readBody    = new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        readBody();
                                                    }
                                                };

        ByteBuffer header = ByteBuffer.allocate(10);

        public CustomEncoder(int bufsize)
        {
            super(new Errno(), bufsize);
            initStep(readBody, true);
        }

        private void readHeader()
        {
            nextStep(inProgress, readBody, !inProgress.hasMore());
        }

        private void readBody()
        {
            if (inProgress == null) {
                return;
            }
            header.clear();
            header.put("HEADER".getBytes(ZMQ.CHARSET));
            header.putInt(inProgress.size());
            header.flip();
            nextStep(header, header.limit(), readHeader, false);
        }

    }

    @Test
    public void testCustomEncoder()
    {
        CustomEncoder cencoder = new CustomEncoder(32);

        Msg msg = new Msg("12345678901234567890".getBytes(ZMQ.CHARSET));
        cencoder.loadMsg(msg);
        ValueReference<ByteBuffer> ref = new ValueReference<>();
        int outsize = cencoder.encode(ref, 0);
        assertThat(outsize, is(30));
        ByteBuffer out = ref.get();
        out.flip();
        write(out);
        byte[] data = sock.data();

        assertThat(new String(data, 0, 6, ZMQ.CHARSET), is("HEADER"));
        assertThat((int) data[9], is(20));
        assertThat(new String(data, 10, 20, ZMQ.CHARSET), is("12345678901234567890"));
    }
}
