package zmq.poll;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import zmq.Ctx;
import zmq.ZError;

public class Poller extends PollerBase
{
    private static class PollSet
    {
        protected final IPollEvents handler;
        protected int               ops;
        protected boolean           cancelled;

        protected PollSet(IPollEvents handler)
        {
            this.handler = handler;
            ops = 0;
        }

        @Override
        public String toString()
        {
            return "" + handler;
        }

        public boolean register(Selector selector, SelectableChannel ch)
        {
            SelectionKey key = ch.keyFor(selector);

            if (cancelled || !ch.isOpen()) {
                if (key != null) {
                    key.cancel();
                }
                return false;
            }
            if (key == null) {
                try {
                    key = ch.register(selector, ops, this);
                }
                catch (ClosedSelectorException | CancelledKeyException | ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
            else {
                if (key.isValid()) {
                    key.interestOps(ops);
                }
            }
            return true;
        }

        public void cancel(Selector selector, SelectableChannel ch)
        {
            cancelled = true;
        }
    }

    // Reference to ZMQ context.
    private final Ctx ctx;

    //  This table stores data for registered descriptors.
    private final Map<Handle, PollSet> fdTable;

    //  If true, there's at least one retired event source.
    private boolean retired = false;

    //  If true, thread is in the process of shutting down.
    private final AtomicBoolean  stopping = new AtomicBoolean();
    private final CountDownLatch stopped  = new CountDownLatch(1);

    private Selector selector;

    public Poller(Ctx ctx, String name)
    {
        super(name);
        this.ctx = ctx;

        fdTable = new HashMap<>();
        selector = ctx.createSelector();
    }

    public void destroy()
    {
        try {
            stop();
            stopped.await();
        }
        catch (InterruptedException e) {
        }
        finally {
            ctx.closeSelector(selector);
        }
    }

    public static final class Handle
    {
        private final SelectableChannel fd;

        public Handle(SelectableChannel fd)
        {
            this.fd = fd;
        }

        @Override
        public String toString()
        {
            return "Handle-" + fd;
        }
    }

    public final Handle addHandle(SelectableChannel fd, IPollEvents events)
    {
        assert (Thread.currentThread() == worker || !worker.isAlive());

        Handle handle = new Handle(fd);
        fdTable.put(handle, new PollSet(events));
        retired = true;

        //  Increase the load metric of the thread.
        adjustLoad(1);
        return handle;
    }

    public final void removeHandle(Handle handle)
    {
        assert (Thread.currentThread() == worker || !worker.isAlive());

        //  Mark the fd as unused.
        PollSet pollSet = fdTable.get(handle);
        pollSet.cancel(selector, handle.fd);
        retired = true;

        //  Decrease the load metric of the thread.
        adjustLoad(-1);
    }

    public final void setPollIn(Handle handle)
    {
        register(handle, SelectionKey.OP_READ, true);
    }

    public final void resetPollIn(Handle handle)
    {
        register(handle, SelectionKey.OP_READ, false);
    }

    public final void setPollOut(Handle handle)
    {
        register(handle, SelectionKey.OP_WRITE, true);
    }

    public final void resetPollOut(Handle handle)
    {
        register(handle, SelectionKey.OP_WRITE, false);
    }

    public final void setPollConnect(Handle handle)
    {
        register(handle, SelectionKey.OP_CONNECT, true);
    }

    public final void setPollAccept(Handle handle)
    {
        register(handle, SelectionKey.OP_ACCEPT, true);
    }

    private final void register(Handle handle, int ops, boolean add)
    {
        assert (Thread.currentThread() == worker || !worker.isAlive());

        PollSet pollset = fdTable.get(handle);

        if (add) {
            pollset.ops = pollset.ops | ops;
        }
        else {
            pollset.ops = pollset.ops & ~ops;
        }
        retired = true;
    }

    public void start()
    {
        worker.start();
    }

    public void stop()
    {
        stopping.set(true);
        retired = false;
        selector.wakeup();
    }

    @Override
    public void run()
    {
        int returnsImmediately = 0;

        while (!stopping.get()) {
            //  Execute any due timers.
            long timeout = executeTimers();

            try {
                if (!selector.keys().isEmpty()) {
                    //  Wait for events.
                    int rc = 0;
                    long start = System.currentTimeMillis();
                    rc = selector.select(timeout);

                    //  If there are no events (i.e. it's a timeout) there's no point
                    //  in checking the pollset.
                    if (rc == 0) {
                        returnsImmediately = maybeRebuildSelector(returnsImmediately, timeout, start);
                        continue;
                    }

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        PollSet pollset = (PollSet) key.attachment();
                        if (pollset.cancelled) {
                            continue;
                        }
                        try {
                            if (key.isValid() && key.isAcceptable()) {
                                pollset.handler.acceptEvent();
                            }
                            if (key.isValid() && key.isConnectable()) {
                                pollset.handler.connectEvent();
                            }
                            if (key.isValid() && key.isWritable()) {
                                pollset.handler.outEvent();
                            }
                            if (key.isValid() && key.isReadable()) {
                                pollset.handler.inEvent();
                            }
                        }
                        catch (RuntimeException e) {
                            // avoid the thread death by continuing to iterate
                            e.printStackTrace();
                        }
                        iterator.remove();
                    }
                }
            }
            catch (IOException | ClosedSelectorException e) {
                rebuildSelector();
                e.printStackTrace();
                ctx.errno().set(ZError.EINTR);
                continue;
            }

            //  Clean up the pollset and update the fd_table accordingly.
            if (retired == true && !stopping.get()) {
                retired = false;
                Iterator<Map.Entry<Handle, PollSet>> it = fdTable.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Handle, PollSet> entry = it.next();

                    Handle handle = entry.getKey();
                    PollSet pollset = entry.getValue();
                    if (!pollset.register(selector, handle.fd)) {
                        it.remove();
                    }
                }
            }
        }
        stopped.countDown();
    }

    private int maybeRebuildSelector(int returnsImmediately, long timeout, long start)
    {
        //  Guess JDK epoll bug
        if (timeout == 0 || System.currentTimeMillis() - start < timeout / 2) {
            returnsImmediately++;
        }
        else {
            returnsImmediately = 0;
        }

        if (returnsImmediately > 10) {
            rebuildSelector();
            returnsImmediately = 0;
        }
        return returnsImmediately;
    }

    private void rebuildSelector()
    {
        System.out.println("rebuilding selector");
        Selector newSelector = ctx.createSelector();
        Selector oldSelector = selector;

        selector = newSelector;
        retired = true;

        ctx.closeSelector(oldSelector);
    }
}
