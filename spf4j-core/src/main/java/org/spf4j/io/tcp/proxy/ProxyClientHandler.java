/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.io.tcp.proxy;

import com.google.common.net.HostAndPort;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.base.AbstractRunnable;
import org.spf4j.base.Closeables;
import org.spf4j.ds.UpdateablePriorityQueue;
import org.spf4j.io.tcp.ClientHandler;
import org.spf4j.io.tcp.DeadlineAction;

/**
 *
 * @author zoly
 */
@ParametersAreNonnullByDefault
public final class ProxyClientHandler implements ClientHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyClientHandler.class);

    private final HostAndPort fwdDestination;
    private final int proxyBufferSize;
    private final int connectTimeoutMillis;
    private final SnifferFactory c2sSnifferFact;
    private final SnifferFactory s2cSnifferFact;

    /**
     * TCP proxy client handler.
     * @param fwdDestination - the destination all connections will be forwarded to.
     * @param c2sSnifferFact - create sniffer to be invoked when data is received from client.
     * @param s2cSnifferFact - create sniffer to be invoked when data is received from server.
     * @param proxyBufferSize - the transmission buffer sizes.
     * @param connectTimeoutMillis - The connection timeout.
     */

    public ProxyClientHandler(final HostAndPort fwdDestination,
        @Nullable final SnifferFactory c2sSnifferFact, @Nullable final SnifferFactory s2cSnifferFact,
        final int proxyBufferSize, final int connectTimeoutMillis) {
        this.fwdDestination = fwdDestination;
        this.proxyBufferSize = proxyBufferSize;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.c2sSnifferFact = c2sSnifferFact;
        this.s2cSnifferFact = s2cSnifferFact;
    }

    @Override
    public void handle(final Selector serverSelector, final SocketChannel clientChannel,
            final ExecutorService exec, final BlockingQueue<Runnable> tasksToRunBySelector,
            final UpdateablePriorityQueue<DeadlineAction> deadlineActions)  throws IOException {
        final InetSocketAddress socketAddress = new InetSocketAddress(
                fwdDestination.getHost(), fwdDestination.getPort());
        final SocketChannel proxyChannel = SocketChannel.open();
        try {
            proxyChannel.configureBlocking(false);
            proxyChannel.connect(socketAddress);
            TransferBuffer c2s = new TransferBuffer(proxyBufferSize);
            if (c2sSnifferFact != null) {
                c2s.setIncomingSniffer(c2sSnifferFact.get(clientChannel));
            }
            TransferBuffer s2c = new TransferBuffer(proxyBufferSize);
            final long connectDeadline = System.currentTimeMillis() + connectTimeoutMillis;
            UpdateablePriorityQueue.ElementRef daction = deadlineActions.add(new DeadlineAction(connectDeadline,
                    new CloseChannelsOnTimeout(proxyChannel, clientChannel)));
            new ProxyBufferTransferHandler(c2s, s2c, null, clientChannel,
                    serverSelector, exec, tasksToRunBySelector, daction).initialInterestRegistration();
            new ProxyBufferTransferHandler(s2c, c2s, s2cSnifferFact, proxyChannel,
                    serverSelector, exec, tasksToRunBySelector, daction).initialInterestRegistration();
        } catch (IOException ex) {
            Exception exs = Closeables.closeAll(proxyChannel, clientChannel);
            ex.addSuppressed(exs);
            throw ex;
        }

    }

    static final class CloseChannelsOnTimeout extends AbstractRunnable {

        private final SocketChannel proxyChannel;
        private final SocketChannel clientChannel;

        CloseChannelsOnTimeout(final SocketChannel proxyChannel, final SocketChannel clientChannel) {
            super(true);
            this.proxyChannel = proxyChannel;
            this.clientChannel = clientChannel;
        }

        @Override
        public void doRun() throws IOException {
            LOG.warn("Timed out connecting to {}", proxyChannel);
            try {
                clientChannel.close();
            } finally {
                proxyChannel.close();
            }
        }
    }

    @Override
    public String toString() {
        return "ProxyClientHandler{" + "fwdDestination=" + fwdDestination + ", proxyBufferSize="
                + proxyBufferSize + ", connectTimeoutMillis=" + connectTimeoutMillis + ", c2sSnifferFact="
                + c2sSnifferFact + ", s2cSnifferFact=" + s2cSnifferFact + '}';
    }



}
