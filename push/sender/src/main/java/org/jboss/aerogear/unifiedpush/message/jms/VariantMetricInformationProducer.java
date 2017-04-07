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
package org.jboss.aerogear.unifiedpush.message.jms;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;

/**
 * Receives CDI event with {@link VariantMetricInformation} payload and dispatches this payload to JMS queue.
 *
 * This bean serves as mediator for decoupling of JMS subsystem and services that triggers these messages.
 */
@Stateless
public class VariantMetricInformationProducer extends AbstractJMSMessageProducer {

    @Resource(mappedName = "java:/queue/MetricsQueue")
    private Queue metricsQueue;

    @Inject
    private JmsClient jmsClient;

    public void queueMessageVariantForProcessing(@Observes @DispatchToQueue VariantMetricInformation msg) {
        jmsClient.send(msg).withProperty("pushMessageInformationId", msg.getPushMessageInformation().getId()).to(metricsQueue);
    }

}
