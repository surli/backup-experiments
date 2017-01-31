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
package com.yahoo.pulsar.client.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import com.yahoo.pulsar.common.naming.DestinationName;
import com.yahoo.pulsar.common.partition.PartitionedTopicMetadata;

/**
 * Provides lookup service to find broker which serves given topic. It helps to
 * lookup
 * <ul>
 * <li><b>topic-lookup:</b> lookup to find broker-address which serves given
 * topic</li>
 * <li><b>Partitioned-topic-Metadata-lookup:</b> lookup to find
 * PartitionedMetadata for a given topic</li>
 * </ul>
 * 
 */
interface LookupService {

	/**
	 * Calls broker lookup-api to get broker {@link InetSocketAddress} which serves namespacebundle that
	 * contains given topic.
	 * 
	 * @param destination:
	 *            topic-name
	 * @return broker-socket-address that serves given topic
	 */
	public CompletableFuture<InetSocketAddress> getBroker(DestinationName topic);
    
	/**
	 * Returns {@link PartitionedTopicMetadata} for a given topic.
	 * 
	 * @param destination : topic-name
	 * @return
	 */
	public CompletableFuture<PartitionedTopicMetadata> getPartitionedTopicMetadata(DestinationName destination);
	
	/**
	 * Returns broker-service lookup api url.
	 * 
	 * @return
	 */
	public String getServiceUrl();
}
