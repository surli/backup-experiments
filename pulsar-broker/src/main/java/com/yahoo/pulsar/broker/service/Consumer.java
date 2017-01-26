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

import static com.google.common.base.Preconditions.checkArgument;
import static com.yahoo.pulsar.broker.service.persistent.PersistentTopic.DATE_FORMAT;
import static com.yahoo.pulsar.common.api.Commands.readChecksum;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.bookkeeper.mledger.Entry;
import org.apache.bookkeeper.mledger.impl.PositionImpl;
import org.apache.bookkeeper.mledger.util.Rate;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.yahoo.pulsar.common.api.Commands;
import com.yahoo.pulsar.common.api.proto.PulsarApi;
import com.yahoo.pulsar.common.api.proto.PulsarApi.CommandAck;
import com.yahoo.pulsar.common.api.proto.PulsarApi.CommandAck.AckType;
import com.yahoo.pulsar.common.api.proto.PulsarApi.CommandSubscribe.SubType;
import com.yahoo.pulsar.common.api.proto.PulsarApi.MessageIdData;
import com.yahoo.pulsar.common.api.proto.PulsarApi.ProtocolVersion;
import com.yahoo.pulsar.common.naming.DestinationName;
import com.yahoo.pulsar.common.policies.data.ConsumerStats;
import com.yahoo.pulsar.common.util.collections.ConcurrentOpenHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * A Consumer is a consumer currently connected and associated with a Subscription
 */
public class Consumer {
    private final Subscription subscription;
    private final SubType subType;
    private final ServerCnx cnx;
    private final String appId;

    private final long consumerId;
    private final String consumerName;
    private final Rate msgOut;

    // Represents how many messages we can safely send to the consumer without
    // overflowing its receiving queue. The consumer will use Flow commands to
    // increase its availability
    private final AtomicInteger messagePermits = new AtomicInteger(0);
    // It starts keep tracking of messagePermits once consumer gets blocked, as consumer needs two separate counts:
    // messagePermits (1) before and (2) after being blocked: to dispatch only blockedPermit number of messages at the
    // time of redelivery
    private final AtomicInteger permitsReceivedWhileConsumerBlocked = new AtomicInteger(0);

    private final ConcurrentOpenHashMap<PositionImpl, Integer> pendingAcks;

    private final ConsumerStats stats;
    
    private final int maxUnackedMessages;
    private AtomicInteger unackedMessages = new AtomicInteger(0);
    private volatile boolean blockedConsumerOnUnackedMsgs = false;

    public Consumer(Subscription subscription, SubType subType, long consumerId, String consumerName,
            int maxUnackedMessages, ServerCnx cnx, String appId) throws BrokerServiceException {

        this.subscription = subscription;
        this.subType = subType;
        this.consumerId = consumerId;
        this.consumerName = consumerName;
        this.maxUnackedMessages = maxUnackedMessages;
        this.cnx = cnx;
        this.msgOut = new Rate();
        this.appId = appId;

        stats = new ConsumerStats();
        stats.address = cnx.clientAddress().toString();
        stats.consumerName = consumerName;
        stats.connectedSince = DATE_FORMAT.format(Instant.now());

        if (subType == SubType.Shared) {
            this.pendingAcks = new ConcurrentOpenHashMap<PositionImpl, Integer>(256, 2);
        } else {
            // We don't need to keep track of pending acks if the subscription is not shared
            this.pendingAcks = null;
        }
    }

    public SubType subType() {
        return subType;
    }

    public long consumerId() {
        return consumerId;
    }

    public String consumerName() {
        return consumerName;
    }

    /**
     * Dispatch a list of entries to the consumer.
     *
     * @return a promise that can be use to track when all the data has been written into the socket
     */
    public Pair<ChannelPromise, Integer> sendMessages(final List<Entry> entries) {
        final ChannelHandlerContext ctx = cnx.ctx();
        final MutablePair<ChannelPromise, Integer> sentMessages = new MutablePair<ChannelPromise, Integer>();
        final ChannelPromise writePromise = ctx.newPromise();
        sentMessages.setLeft(writePromise);
        if (entries.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("[{}] List of messages is empty, triggering write future immediately for consumerId {}",
                        subscription, consumerId);
            }
            writePromise.setSuccess();
            sentMessages.setRight(0);
            return sentMessages;
        }

        sentMessages.setRight(updatePermitsAndPendingAcks(entries));

        ctx.channel().eventLoop().execute(() -> {
            for (int i = 0; i < entries.size(); i++) {
                Entry entry = entries.get(i);
                PositionImpl pos = (PositionImpl) entry.getPosition();
                MessageIdData.Builder messageIdBuilder = MessageIdData.newBuilder();
                MessageIdData messageId = messageIdBuilder.setLedgerId(pos.getLedgerId()).setEntryId(pos.getEntryId())
                        .build();

                ByteBuf metadataAndPayload = entry.getDataBuffer();

                // skip checksum by incrementing reader-index if consumer-client doesn't support checksum verification
                if (cnx.getRemoteEndpointProtocolVersion() < ProtocolVersion.v6.getNumber()) {
                    readChecksum(metadataAndPayload);
                }
                
                // stats
                msgOut.recordEvent(metadataAndPayload.readableBytes());

                if (log.isDebugEnabled()) {
                    log.debug("[{}] Sending message to consumerId {}, entry id {}", subscription, consumerId,
                            pos.getEntryId());
                }

                // We only want to pass the "real" promise on the last entry written
                ChannelPromise promise = ctx.voidPromise();
                if (i == (entries.size() - 1)) {
                    promise = writePromise;
                }
                ctx.write(Commands.newMessage(consumerId, messageId, metadataAndPayload), promise);
                messageId.recycle();
                messageIdBuilder.recycle();
            }

            ctx.flush();
        });

        return sentMessages;
    }

    private void incrementUnackedMessages(int ackedMessages) {
        if (shouldBlockConsumerOnUnackMsgs() && unackedMessages.addAndGet(ackedMessages) >= maxUnackedMessages) {
            blockedConsumerOnUnackedMsgs = true;
        }
    }

    int getBatchSizeforEntry(ByteBuf metadataAndPayload) {
        try {
            // save the reader index and restore after parsing
            metadataAndPayload.markReaderIndex();
            PulsarApi.MessageMetadata metadata = Commands.parseMessageMetadata(metadataAndPayload);
            metadataAndPayload.resetReaderIndex();
            int batchSize = metadata.getNumMessagesInBatch();
            metadata.recycle();
            if (log.isDebugEnabled()) {
                log.debug("[{}] [{}] num messages in batch are {} ", subscription, consumerId, batchSize);
            }
            return batchSize;
        } catch (Throwable t) {
            log.error("[{}] [{}] Failed to parse message metadata", subscription, consumerId, t);
        }
        return -1;
    }

    int updatePermitsAndPendingAcks(final List<Entry> entries) {
        int permitsToReduce = 0;
        Iterator<Entry> iter = entries.iterator();
        while (iter.hasNext()) {
            Entry entry = iter.next();
            ByteBuf metadataAndPayload = entry.getDataBuffer();
            int batchSize = getBatchSizeforEntry(metadataAndPayload);
            if (batchSize == -1) {
                // this would suggest that the message might have been corrupted
                iter.remove();
                entry.release();
                PositionImpl pos = PositionImpl.get((PositionImpl) entry.getPosition());
                subscription.acknowledgeMessage(pos, AckType.Individual);
                continue;
            }
            if (pendingAcks != null) {
                PositionImpl pos = PositionImpl.get((PositionImpl) entry.getPosition());
                pendingAcks.put(pos, batchSize);
            }
            permitsToReduce += batchSize;
        }
        // reduce permit and increment unackedMsg count with total number of messages in batch-msgs
        int permits = messagePermits.addAndGet(-permitsToReduce);
        incrementUnackedMessages(permitsToReduce);
        if (permits < 0) {
            if (log.isDebugEnabled()) {
                log.debug("[{}] [{}] message permits dropped below 0 - {}", subscription, consumerId, permits);
            }
        }
        return permitsToReduce;
    }

    public boolean isWritable() {
        return cnx.isWritable();
    }

    public void sendError(ByteBuf error) {
        cnx.ctx().writeAndFlush(error).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Close the consumer if: a. the connection is dropped b. connection is open (graceful close) and there are no
     * pending message acks
     */
    public void close() throws BrokerServiceException {
        subscription.removeConsumer(this);
        cnx.removedConsumer(this);
    }

    public void disconnect() {
        log.info("Disconnecting consumer: {}", this);
        cnx.closeConsumer(this);
        try {
            close();
        } catch (BrokerServiceException e) {
            log.warn("Consumer {} was already closed: {}", this, e.getMessage(), e);
        }
    }

    void doUnsubscribe(final long requestId) {
        final ChannelHandlerContext ctx = cnx.ctx();

        subscription.doUnsubscribe(this).thenAccept(v -> {
            log.info("Unsubscribed successfully from {}", subscription);
            cnx.removedConsumer(this);
            ctx.writeAndFlush(Commands.newSuccess(requestId));
        }).exceptionally(exception -> {
            log.warn("Unsubscribe failed for {}", subscription, exception);
            ctx.writeAndFlush(
                    Commands.newError(requestId, BrokerServiceException.getClientErrorCode(exception.getCause()),
                            exception.getCause().getMessage()));
            return null;
        });
    }

    void messageAcked(CommandAck ack) {
        MessageIdData msgId = ack.getMessageId();
        PositionImpl position = PositionImpl.get(msgId.getLedgerId(), msgId.getEntryId());

        if (ack.hasValidationError()) {
            log.error("[{}] [{}] Received ack for corrupted message at {} - Reason: {}", subscription, consumerId,
                    position, ack.getValidationError());
        }

        if (subType == SubType.Shared) {
            // On shared subscriptions, cumulative ack is not supported
            checkArgument(ack.getAckType() == AckType.Individual);

            // Only ack a single message
            removePendingAcks(position);
            subscription.acknowledgeMessage(position, AckType.Individual);
        } else {
            subscription.acknowledgeMessage(position, ack.getAckType());
        }
       
    }

    void flowPermits(int additionalNumberOfMessages) {
        checkArgument(additionalNumberOfMessages > 0);

        // block shared consumer when unacked-messages reaches limit
        if (shouldBlockConsumerOnUnackMsgs() && unackedMessages.get() >= maxUnackedMessages) {
            blockedConsumerOnUnackedMsgs = true;
        }
        int oldPermits;
        if (!blockedConsumerOnUnackedMsgs) {
            oldPermits = messagePermits.getAndAdd(additionalNumberOfMessages);
            subscription.consumerFlow(this, additionalNumberOfMessages);
        } else {
            oldPermits = permitsReceivedWhileConsumerBlocked.getAndAdd(additionalNumberOfMessages);
        }

        if (log.isDebugEnabled()) {
            log.debug("[{}] Added more flow control message permits {} (old was: {})", this, additionalNumberOfMessages,
                    oldPermits);
        }

    }
    
    /**
     * Triggers dispatcher to dispatch {@code blockedPermits} number of messages and adds same number of permits to
     * {@code messagePermits} as it maintains count of actual dispatched message-permits.
     * 
     * @param consumer:
     *            Consumer whose blockedPermits needs to be dispatched
     */
    void flowConsumerBlockedPermits(Consumer consumer) {
        int additionalNumberOfPermits = consumer.permitsReceivedWhileConsumerBlocked.getAndSet(0);
        // add newly flow permits to actual consumer.messagePermits
        consumer.messagePermits.getAndAdd(additionalNumberOfPermits);
        // dispatch pending permits to flow more messages: it will add more permits to dispatcher and consumer
        subscription.consumerFlow(consumer, additionalNumberOfPermits);
    }

    public int getAvailablePermits() {
        return messagePermits.get();
    }

    public boolean isBlocked() {
        return blockedConsumerOnUnackedMsgs;
    }
    
    /**
     * Checks if consumer-blocking on unAckedMessages is allowed for below conditions:<br/>
     * a. consumer must have Shared-subscription<br/>
     * b. {@link maxUnackedMessages} value > 0
     * 
     * @return
     */
    private boolean shouldBlockConsumerOnUnackMsgs() {
        return SubType.Shared.equals(subType) && maxUnackedMessages > 0;
    }
    
    public void updateRates() {
        msgOut.calculateRate();
        stats.msgRateOut = msgOut.getRate();
        stats.msgThroughputOut = msgOut.getValueRate();
    }

    public ConsumerStats getStats() {
        stats.availablePermits = getAvailablePermits();
        stats.unackedMessages = unackedMessages.get();
        return stats;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("subscription", subscription).add("consumerId", consumerId)
                .add("consumerName", consumerName).add("address", this.cnx.clientAddress()).toString();
    }

    public ChannelHandlerContext ctx() {
        return cnx.ctx();
    }

    public void checkPermissions() {
        DestinationName destination = DestinationName.get(subscription.getDestination());
        if (cnx.getBrokerService().getAuthorizationManager() != null
                && !cnx.getBrokerService().getAuthorizationManager().canConsume(destination, appId)) {
            log.info("[{}] is not allowed to consume from Destination" + " [{}] anymore", appId,
                    subscription.getDestination());
            disconnect();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Consumer) {
            Consumer other = (Consumer) obj;
            return Objects.equal(cnx.clientAddress(), other.cnx.clientAddress()) && consumerId == other.consumerId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return consumerName.hashCode() + 31 * cnx.hashCode();
    }

    /**
     * first try to remove ack-position from the current_consumer's pendingAcks.
     * if ack-message doesn't present into current_consumer's pendingAcks
     *  a. try to remove from other connected subscribed consumers (It happens when client
     * tries to acknowledge message through different consumer under the same subscription)
     * 
     * 
     * @param position
     */
    private void removePendingAcks(PositionImpl position) {
        Consumer ackOwnedConsumer = null;
        if (pendingAcks.get(position) == null) {
            for (Consumer consumer : subscription.getConsumers()) {
                if (!consumer.equals(this) && consumer.getPendingAcks().get(position) != null) {
                    ackOwnedConsumer = consumer;
                    break;
                }
            }
        } else {
            ackOwnedConsumer = this;
        }
        
        // remove pending message from appropriate consumer and unblock unAckMsg-flow if requires
        if (ackOwnedConsumer != null) {
            int totalAckedMsgs = ackOwnedConsumer.getPendingAcks().remove(position);
            // unblock consumer-throttling when receives half of maxUnackedMessages => consumer can start again
            // consuming messages
            if (ackOwnedConsumer.shouldBlockConsumerOnUnackMsgs()
                    && ((ackOwnedConsumer.unackedMessages.addAndGet(-totalAckedMsgs) <= (maxUnackedMessages / 2))
                            && ackOwnedConsumer.blockedConsumerOnUnackedMsgs)) {
                ackOwnedConsumer.blockedConsumerOnUnackedMsgs = false;
                flowConsumerBlockedPermits(ackOwnedConsumer);
            }
        }
    }
    
    public ConcurrentOpenHashMap<PositionImpl, Integer> getPendingAcks() {
        return pendingAcks;
    }

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    public void redeliverUnacknowledgedMessages() {

        // cleanup unackedMessage bucket and redeliver those unack-msgs again
        unackedMessages.set(0);
        blockedConsumerOnUnackedMsgs = false;
        // redeliver unacked-msgs
        subscription.redeliverUnacknowledgedMessages(this);
        flowConsumerBlockedPermits(this);
        if (pendingAcks != null) {
            pendingAcks.clear();
        }

    }

    public void redeliverUnacknowledgedMessages(List<MessageIdData> messageIds) {

        int totalRedeliveryMessages = 0;
        List<PositionImpl> pendingPositions = Lists.newArrayList();
        for (MessageIdData msg : messageIds) {
            PositionImpl position = PositionImpl.get(msg.getLedgerId(), msg.getEntryId());
            Integer batchSize = pendingAcks.remove(position);
            if (batchSize != null) {
                totalRedeliveryMessages += batchSize;
                pendingPositions.add(position);
            }
        }

        unackedMessages.addAndGet(-totalRedeliveryMessages);
        blockedConsumerOnUnackedMsgs = false;

        subscription.redeliverUnacknowledgedMessages(this, pendingPositions);

        int numberOfBlockedPermits = Math.min(totalRedeliveryMessages,
                permitsReceivedWhileConsumerBlocked.get());

        // if permitsReceivedWhileConsumerBlocked has been accumulated then pass it to Dispatcher to flow messages
        if (numberOfBlockedPermits > 0) {
            permitsReceivedWhileConsumerBlocked.getAndAdd(-numberOfBlockedPermits);
            messagePermits.getAndAdd(numberOfBlockedPermits);
            subscription.consumerFlow(this, numberOfBlockedPermits);

        }
    }
}
