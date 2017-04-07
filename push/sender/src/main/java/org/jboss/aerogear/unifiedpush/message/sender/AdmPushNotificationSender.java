/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.sender;


import org.jboss.aerogear.adm.ADM;
import org.jboss.aerogear.adm.AdmService;
import org.jboss.aerogear.adm.PayloadBuilder;
import org.jboss.aerogear.unifiedpush.api.AdmVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@SenderType(VariantType.ADM)
public class AdmPushNotificationSender implements PushNotificationSender {
    private final Logger logger = LoggerFactory.getLogger(AdmPushNotificationSender.class);

    @Override
    public void sendPushMessage(Variant variant, Collection<String> clientIdentifiers, UnifiedPushMessage pushMessage, String pushMessageInformationId, NotificationSenderCallback senderCallback) {
        final AdmService admService = ADM.newService();

        final PayloadBuilder builder = ADM.newPayload();

        //flatten the "special keys"
        builder.dataField("alert", pushMessage.getMessage().getAlert());

        // if present, apply the time-to-live metadata:
        int ttl = pushMessage.getConfig().getTimeToLive();
        if (ttl != -1) {
            builder.expiresAfter(ttl);
        }

        //dirty hack for cordova,
        //TODO should be removed once we have our clients SDKs, tracked by AGPUSH-1269
        builder.dataField("message","useless payload");

        //Handle consolidation key
        builder.consolidationKey(pushMessage.getMessage().getConsolidationKey());

        pushMessage.getMessage().getUserData().keySet()
                .forEach(key -> builder.dataField(key, pushMessage.getMessage().getUserData().get(key)));

        //add the aerogear-push-id
        builder.dataField(InternalUnifiedPushMessage.PUSH_MESSAGE_ID, pushMessageInformationId);

        final AdmVariant admVariant = (AdmVariant) variant;

        clientIdentifiers.forEach(token -> {
            try {
                admService.sendMessageToDevice(token, admVariant.getClientId(), admVariant.getClientSecret(),  builder.build());
                senderCallback.onSuccess();
            } catch (Exception e) {
                logger.error("Error sending payload to ADM server", e);
                senderCallback.onError(e.getMessage());
            }
        });
        logger.info(String.format("Sent push notification to Amazon's ADM Server for %d tokens",clientIdentifiers.size()));
    }
}
