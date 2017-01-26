
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
package com.yahoo.pulsar.websocket.proxy;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.bookkeeper.test.PortManager;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.yahoo.pulsar.broker.ServiceConfiguration;
import com.yahoo.pulsar.broker.auth.MockedPulsarServiceBaseTest;
import com.yahoo.pulsar.websocket.WebSocketService;

public class ProxyAuthenticationTest extends MockedPulsarServiceBaseTest {
    protected String methodName;
    private static final int TEST_PORT = PortManager.nextFreePort();
    private static final String CONSUME_URI = "ws://localhost:" + TEST_PORT
            + "/consume/persistent/my-property/cluster1/my-ns/my-topic/my-sub";
    private static final String PRODUCE_URI = "ws://localhost:" + TEST_PORT
            + "/produce/persistent/my-property/cluster1/my-ns/my-topic/";
    private WebSocketService service;

    @BeforeClass
    public void setup() throws Exception {
        super.internalSetup();

        ServiceConfiguration config = new ServiceConfiguration();
        config.setWebServicePort(TEST_PORT);
        config.setAuthenticationEnabled(true);
        config.setAuthenticationProviders(
                Sets.newHashSet("com.yahoo.pulsar.websocket.proxy.MockAuthenticationProvider"));
        service = spy(new WebSocketService(config));
        doReturn(mockZooKeeperClientFactory).when(service).getZooKeeperClientFactory();
        service.start();
        log.info("Proxy Server Started");
    }

    @Override
    protected void cleanup() throws Exception {
        super.internalCleanup();
        service.close();
        log.info("Finished Cleaning Up Test setup");

    }

    @Test
    public void socketTest() throws InterruptedException {
        URI consumeUri = URI.create(CONSUME_URI);
        URI produceUri = URI.create(PRODUCE_URI);

        WebSocketClient consumeClient = new WebSocketClient();
        SimpleConsumerSocket consumeSocket = new SimpleConsumerSocket();
        WebSocketClient produceClient = new WebSocketClient();
        SimpleProducerSocket produceSocket = new SimpleProducerSocket();

        try {
            consumeClient.start();
            ClientUpgradeRequest consumeRequest = new ClientUpgradeRequest();
            consumeClient.connect(consumeSocket, consumeUri, consumeRequest);
            log.info("Connecting to : %s%n", consumeUri);

            ClientUpgradeRequest produceRequest = new ClientUpgradeRequest();
            produceClient.start();
            produceClient.connect(produceSocket, produceUri, produceRequest);

            consumeSocket.awaitClose(1, TimeUnit.SECONDS);
            produceSocket.awaitClose(1, TimeUnit.SECONDS);

            Assert.assertEquals(produceSocket.getBuffer(), consumeSocket.getBuffer());
        } catch (Throwable t) {
            log.error(t.getMessage());
        } finally {
            try {
                consumeClient.stop();
                produceClient.stop();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ProxyAuthenticationTest.class);
}
