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
package com.yahoo.pulsar.broker.service;

import java.util.concurrent.CompletableFuture;

import com.yahoo.pulsar.broker.service.persistent.PersistentSubscription;
import com.yahoo.pulsar.common.api.proto.PulsarApi.CommandSubscribe.SubType;
import com.yahoo.pulsar.common.policies.data.BacklogQuota;
import com.yahoo.pulsar.common.policies.data.Policies;
import com.yahoo.pulsar.common.util.collections.ConcurrentOpenHashMap;
import com.yahoo.pulsar.common.util.collections.ConcurrentOpenHashSet;

import io.netty.buffer.ByteBuf;

public interface Topic {

    public interface PublishCallback {
        void completed(Exception e, long ledgerId, long entryId);
    }

    void publishMessage(ByteBuf headersAndPayload, PublishCallback callback);

    void addProducer(Producer producer) throws BrokerServiceException;

    void removeProducer(Producer producer);

    CompletableFuture<Consumer> subscribe(ServerCnx cnx, String subscriptionName, long consumerId, SubType subType,
            String consumerName);

    CompletableFuture<Void> unsubscribe(String subName);

    ConcurrentOpenHashMap<String, PersistentSubscription> getSubscriptions();

    CompletableFuture<Void> delete();

    ConcurrentOpenHashSet<Producer> getProducers();

    String getName();

    CompletableFuture<Void> checkReplication();

    CompletableFuture<Void> close();

    void checkGC(int gcInterval);

    void checkMessageExpiry();

    CompletableFuture<Void> onPoliciesUpdate(Policies data);

    boolean isBacklogQuotaExceeded(String producerName);

    BacklogQuota getBacklogQuota();
}
