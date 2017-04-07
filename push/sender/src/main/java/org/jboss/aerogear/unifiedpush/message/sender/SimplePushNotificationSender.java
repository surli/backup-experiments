/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.message.sender;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@SenderType(VariantType.SIMPLE_PUSH)
public class SimplePushNotificationSender implements PushNotificationSender {

    private final Logger logger = LoggerFactory.getLogger(SimplePushNotificationSender.class);

    /*
     * Sends SimplePush notifications to all connected clients, that are represented by
     * the {@link Collection} of channelIDs, for the given SimplePush network.
     */
    @Override
    public void sendPushMessage(Variant variant, Collection<String> tokens, UnifiedPushMessage pushMessage, String pushMessageInformationId, NotificationSenderCallback callback) {

        // no need to send empty list
        if (tokens.isEmpty()) {
            return;
        }

        String payload = pushMessage.getMessage().getSimplePush();
        // if there was no payload provided, but we have clients, we send an empty string
        // the SimplePush Server accepts that and will use the timestamp
        if (payload == null) {
            payload = "";
        }

        // iterate over all the given channels, if there are channels:
        boolean hasWarning = false;
        for (String clientURL : tokens) {

            HttpURLConnection conn = null;
            try {
                // PUT the version payload to the SimplePushServer
                logger.trace("Sending out SimplePush payload: {}", payload);
                conn = put(clientURL, payload);
                int simplePushStatusCode = conn.getResponseCode();
                logger.trace("SimplePush Status: {}", simplePushStatusCode);

                if (Status.OK.getStatusCode() != simplePushStatusCode) {
                    hasWarning = true;
                }
            } catch (Exception e) {
                // any error while performing the PUT
                logger.error("Error delivering SimplePush payload", e);
                hasWarning = true;
            } finally {
                // tear down
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        logger.info(String.format("Sent push notification to SimplePush Server for %d  tokens",tokens.size()));

        if (hasWarning) {
            callback.onError("Error delivering SimplePush payload");
        }
        else {
            callback.onSuccess();
        }
    }

    /*
     * Returns HttpURLConnection that 'puts' the given body to the given URL.
     */
    protected HttpURLConnection put(String url, String body) throws IOException {

        if (url == null) {
            throw new IllegalArgumentException("SimplePush Update URL cannot be null");
        }

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conn = getConnection(url);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestProperty("Content-Length", Integer.toString(bytes.length));
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestMethod("PUT");
        try (OutputStream out = conn.getOutputStream()) {
            out.write(bytes);
            out.flush();
        }
        return conn;
    }

    /*
     * Convenience method to open/establish a HttpURLConnection.
     */
    protected HttpURLConnection getConnection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }
}
