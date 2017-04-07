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


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.serviceHolder.ApnsServiceHolder;
import org.jboss.aerogear.unifiedpush.message.serviceHolder.ServiceConstructor;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.notnoop.apns.ApnsService;

public class APNsPushNotificationSenderTest {

    @Test
    public void callbackOnError() throws Exception {
        final APNsPushNotificationSender sender = new APNsPushNotificationSender(createMockApnsServiceHolder());
        final NotificationSenderCallback callback = mock(NotificationSenderCallback.class);

        final iOSVariant iosVariant = mock(iOSVariant.class);
        when(iosVariant.getVariantID()).thenReturn(UUID.randomUUID().toString());
        when(iosVariant.getCertificate()).thenReturn(readCertificate());
        when(iosVariant.getPassphrase()).thenReturn("123456");

        sender.sendPushMessage(iosVariant, Arrays.asList("token"), new UnifiedPushMessage(), "123", callback);

        verify(callback).onError("Error sending payload to APNs server: Invalid hex character: t");
    }

    /**
     * The store read by this method was copied from
     * https://github.com/notnoop/java-apns/tree/master/src/test/resources
     */
    private static byte[] readCertificate() throws Exception {
        return asByteArray(APNsPushNotificationSenderTest.class.getResourceAsStream("/clientStore.p12"));
    }

    private static byte[] asByteArray(final InputStream is) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int reads = is.read();
        while (reads != -1) {
            baos.write(reads);
            reads = is.read();
        }
        return baos.toByteArray();
    }

    private ApnsServiceHolder createMockApnsServiceHolder() {
        ApnsServiceHolder apnsServiceHolder = mock(ApnsServiceHolder.class);
        when(apnsServiceHolder.dequeueOrCreateNewService(Mockito.anyString(), Mockito.anyString(), Mockito.any(ServiceConstructor.class))).thenAnswer(new Answer<ApnsService>() {
            @Override
            public ApnsService answer(InvocationOnMock invocation) throws Throwable {
                ServiceConstructor<ApnsService> constructor = (ServiceConstructor<ApnsService>) invocation.getArguments()[2];
                return constructor.construct();
            }
        });
        return apnsServiceHolder;
    }
}