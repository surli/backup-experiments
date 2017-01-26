/**
 * Copyright 2016 Yahoo Inc.
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
 */
package com.yahoo.pulsar.websocket.service;

import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletException;
import javax.websocket.DeploymentException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.yahoo.pulsar.broker.PulsarServerException;
import com.yahoo.pulsar.broker.ServiceConfiguration;
import com.yahoo.pulsar.client.api.PulsarClientException;
import com.yahoo.pulsar.common.util.SecurityUtility;

import io.netty.util.concurrent.DefaultThreadFactory;

public class ProxyServer {
    private final ExecutorService executorService;
    private final Server server;
    private final List<Handler> handlers = Lists.newArrayList();
    private final ServiceConfiguration conf;

    public ProxyServer(ServiceConfiguration config)
            throws PulsarClientException, MalformedURLException, PulsarServerException {
        this.conf = config;
        this.executorService = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors(),
                new DefaultThreadFactory("pulsar-websocket-web"));
        this.server = new Server(new ExecutorThreadPool(executorService));
        List<ServerConnector> connectors = new ArrayList<>();

        ServerConnector connector = new ServerConnector(server);

        connector.setPort(config.getWebServicePort());
        connectors.add(connector);

        // TLS enabled connector
        if (config.isTlsEnabled()) {
            SslContextFactory sslCtxFactory = new SslContextFactory(true);
            try {
                SSLContext sslCtx = SecurityUtility.createSslContext(false, null, config.getTlsCertificateFilePath(),
                        config.getTlsKeyFilePath());
                sslCtxFactory.setSslContext(sslCtx);

            } catch (GeneralSecurityException e) {
                throw new PulsarServerException(e);
            }

            sslCtxFactory.setWantClientAuth(true);
            ServerConnector tlsConnector = new ServerConnector(server, -1, -1, sslCtxFactory);
            tlsConnector.setPort(config.getWebServicePortTls());
            connectors.add(tlsConnector);

        }

        // Limit number of concurrent HTTP connections to avoid getting out of
        // file descriptors
        connectors.stream().forEach(c -> c.setAcceptQueueSize(1024 / connectors.size()));
        server.setConnectors(connectors.toArray(new ServerConnector[connectors.size()]));
    }

    public void addWebSocketServlet(String basePath, WebSocketServlet socketServlet)
            throws ServletException, DeploymentException {
        ServletHolder servletHolder = new ServletHolder("ws-events", socketServlet);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(basePath);
        context.addServlet(servletHolder, "/*");
        handlers.add(context);
    }

    public void start() throws PulsarServerException {
        log.info("Starting web socket proxy at port {}", conf.getWebServicePort());
        try {
            RequestLogHandler requestLogHandler = new RequestLogHandler();
            Slf4jRequestLog requestLog = new Slf4jRequestLog();
            requestLog.setExtended(true);
            requestLog.setLogTimeZone("GMT");
            requestLog.setLogLatency(true);
            requestLogHandler.setRequestLog(requestLog);
            handlers.add(0, new ContextHandlerCollection());
            handlers.add(requestLogHandler);

            ContextHandlerCollection contexts = new ContextHandlerCollection();
            contexts.setHandlers(handlers.toArray(new Handler[handlers.size()]));

            HandlerCollection handlerCollection = new HandlerCollection();
            handlerCollection.setHandlers(new Handler[] { contexts, new DefaultHandler(), requestLogHandler });
            server.setHandler(handlerCollection);

            server.start();
        } catch (Exception e) {
            throw new PulsarServerException(e);
        }
    }

    public void stop() throws Exception {
        server.stop();
    }

    private static final Logger log = LoggerFactory.getLogger(ProxyServer.class);
}
