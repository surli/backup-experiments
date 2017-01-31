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
import com.google.gson.JsonPrimitive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class SimpleConsumerSocket {
    private static final String X_PULSAR_MESSAGE_ID = "messageId";
    private final CountDownLatch closeLatch;
    private Session session;
    private ArrayList<String> consumerBuffer = new ArrayList<String>();

    public SimpleConsumerSocket() {
        this.closeLatch = new CountDownLatch(1);
        consumerBuffer = new ArrayList<String>();
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
    public void onConnect(Session session) throws InterruptedException {
        log.info("Got connect: {}", session);
        this.session = session;
        log.debug("Got connected: {}", session);
    }

    @OnWebSocketMessage
    public void onMessage(String msg) throws JsonParseException, IOException {
        JsonObject message = new Gson().fromJson(msg, JsonObject.class);
        JsonObject ack = new JsonObject();
        String messageId = message.get(X_PULSAR_MESSAGE_ID).getAsString();
        consumerBuffer.add(messageId);
        ack.add("messageId", new JsonPrimitive(messageId));
        // Acking the proxy
        this.getRemote().sendString(ack.toString());
    }

    public RemoteEndpoint getRemote() {
        return this.session.getRemote();
    }

    public Session getSession() {
        return this.session;
    }

    public ArrayList<String> getBuffer() {
        return consumerBuffer;
    }

    private static final Logger log = LoggerFactory.getLogger(SimpleConsumerSocket.class);

}