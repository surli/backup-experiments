/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.jms;

import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream.QueryBuilder;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Stateless
public class MocksForTokenLoaderTransactionFailForGCM  {

    @Resource(mappedName = "java:/queue/TestTokenLoaderTransactionFailForGCM")
    private Queue allTokens;

    @Inject
    private JmsClient jmsClient;

    /**
     * Returns mock {@link ClientInstallationService} that generates fake unique Android tokens
     *
     * @return mock {@link ClientInstallationService} that generates fake unique Android tokens
     */
    @Produces
    public ClientInstallationService getClientInstallationService() {

        ClientInstallationService mock = mock(ClientInstallationService.class);

        when(mock.findAllDeviceTokenForVariantIDByCriteria(Mockito.anyString(), Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyString())).thenAnswer(new Answer<QueryBuilder<String>>() {
            @Override
            public QueryBuilder<String> answer(InvocationOnMock invocation) throws Throwable {
                return new QueryBuilder<String>() {

                    @Override
                    public QueryBuilder<String> fetchSize(int fetchSize) {
                        return this;
                    }

                    @Override
                    public ResultsStream<String> executeQuery() {
                        return new ResultsStream<String>() {

                            private int counter = 0;

                            @Override
                            public String get() throws ResultStreamException {
                                if (counter >= 0) {
                                    return "eHlfnI0__dI:APA91bEhtHefML2lr_sBQ-bdXIyEn5owzkZg_p_y7SRyNKRMZ3XuzZhBpTOYIh46tqRYQIc-7RTADk4nM5H-ONgPDWHodQDS24O5GuKP8EZEKwNh4Zxdv1wkZJh7cU2PoLz9gn4Nxqz-" + counter;
                                }
                                return null;
                            }

                            @Override
                            public boolean next() throws ResultStreamException {
                                if (--counter >= 0) {
                                    return true;
                                }
                                if (null != jmsClient.receive().inTransaction().noWait().withSelector("id = '%s'", TestTokenLoaderTransactionFailForGCM.messageId).from(allTokens)) {
                                    counter = 1000;
                                    return next();
                                }
                                return false;
                            }
                        };
                    }
                };
            }
        });

        when(mock.findAllOldGoogleCloudMessagingDeviceTokenForVariantIDByCriteria(Mockito.anyString(), Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyString())).thenAnswer(new Answer<QueryBuilder<String>>() {
            @Override
            public QueryBuilder<String> answer(InvocationOnMock invocation) throws Throwable {
                return new QueryBuilder<String>() {

                    @Override
                    public QueryBuilder<String> fetchSize(int fetchSize) {
                        return this;
                    }

                    @Override
                    public ResultsStream<String> executeQuery() {
                        return new ResultsStream<String>() {

                            private int counter = 0;

                            @Override
                            public String get() throws ResultStreamException {
                                if (counter >= 0) {
                                    return "APA91bEhtHefML2lr_sBQ-bdXIyEn5owzkZg_p_y7SRyNKRMZ3XuzZhBpTOYIh46tqRYQIc-7RTADk4nM5H-ONgPDWHodQDS24O5GuKP8EZEKwNh4Zxdv1wkZJh7cU2PoLz9gn4Nxqz-" + counter;
                                }
                                return null;
                            }

                            @Override
                            public boolean next() throws ResultStreamException {
                                if (--counter >= 0) {
                                    return true;
                                }
                                if (null != jmsClient.receive().inTransaction().noWait().withSelector("id = '%s'", TestTokenLoaderTransactionFailForGCM.messageId).from(allTokens)) {
                                    counter = 1000;
                                    return next();
                                }
                                return false;
                            }
                        };
                    }
                };
            }
        });

        return mock;
    }
}
