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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import static com.yahoo.pulsar.broker.admin.AdminResource.jsonMapper;
import com.yahoo.pulsar.websocket.data.ProducerMessage;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class SimpleProducerSocket {

    private final CountDownLatch closeLatch;
    private Session session;
    private ArrayList<String> producerBuffer;

    public SimpleProducerSocket() {
        this.closeLatch = new CountDownLatch(1);
        producerBuffer = new ArrayList<String>();
    }

    private static String getTestJsonPayload(int index) throws JsonProcessingException {
        ProducerMessage msg = new ProducerMessage();
        msg.payload = Base64.getEncoder().encodeToString(("test" + index).getBytes());
        msg.key = Integer.toString(index);
        return jsonMapper().writeValueAsString(msg);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        log.info("Connection closed: {} - {}", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws InterruptedException, IOException, JsonParseException {
        log.info("Got connect: {}", session);
        this.session = session;
        for (int i = 0; i < 10; i++) {
            this.session.getRemote().sendString(getTestJsonPayload(i));
        }

    }

    @OnWebSocketMessage
    public void onMessage(String msg) throws JsonParseException {
        JsonObject ack = new Gson().fromJson(msg, JsonObject.class);
        producerBuffer.add(ack.get("messageId").getAsString());
    }

    public RemoteEndpoint getRemote() {
        return this.session.getRemote();
    }

    public Session getSession() {
        return this.session;
    }

    public ArrayList<String> getBuffer() {
        return producerBuffer;
    }

    private static final Logger log = LoggerFactory.getLogger(SimpleProducerSocket.class);

}
