/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * <h1>Multiplexing support</h1>
 *
 * <p>This package contains support classes for implementing a multiplexed persistence at the {@linkplain org.apache.jackrabbit.oak.spi.state.NodeStore} level.</p>
 *
 * <h2>Design goals</h2>
 * <ol>
 *   <li>Transparency of the multiplexing setup. Neither the NodeStores nor the code using a multiplexed
 *       NodeStore should be aware of the specific implementation being used.</li>
 *   <li>Persistence-agnosticity. The multiplexing support should be applicable to any conformant
 *       NodeStore implementation.</li>
 *   <li>Negligible performance impact. Multiplexing should not add a significat performance overhead.</li>
 * </ol>
 *
 * <h2>Implementation</h2>
 *
 * <p>The main entry point is the {@link org.apache.jackrabbit.oak.plugins.multiplex.MultiplexingNodeStore},
 * which wraps one or more NodeStore instances. Also of interest are the {@link org.apache.jackrabbit.oak.plugins.multiplex.MultiplexingNodeState} and {@link org.apache.jackrabbit.oak.plugins.multiplex.MultiplexingNodeBuilder}.
 *
 * <p>These classes maintain internal mappings of the 'native' objects. For instance, if the
 * multiplexing NodeStore holds two MemoryNodeStore instances, then a call to {@linkplain org.apache.jackrabbit.oak.spi.state.NodeStore#getRoot()}
 * will return a multiplexing NodeState backed by two MemoryNodeState instances. Similarly, a call to {@linkplain org.apache.jackrabbit.oak.spi.state.NodeState#builder()} will return a multiplexing
 * NodeBuilder backed by two MemoryNodeState instances.
 *
 * <p>Using this approach allows us to always keep related NodeStore, NodeState and NodeBuilder
 * instances isolated from other instances.
 *
 * <h2>Open items</h2>
 *
 * <p>1. Brute-force support for oak:mount nodes.
 *
 *  <p>The {@link org.apache.jackrabbit.oak.spi.mount.Mount#getPathFragmentName()} method defines
 *  a name pattern that can be used by mounted stores to contribute to a patch which is not
 *  owned by them. For instance, a mount named <em>apps</em> which owns <tt>/libs,/apps</tt>
 *  can own another subtree anywhere in the repository given that a node named <tt>:oak-mount-apps</tt>
 *  is found.
 *
 *  <p>The current implementation naively queries all stores whenever the child node list is prepared.
 *  This is obviously correct but may be slow.
 *  {@link org.apache.jackrabbit.oak.plugins.multiplex.MultiplexingContext#getContributingStores(java.lang.String, com.google.common.base.Function)}
 */
package org.apache.jackrabbit.oak.plugins.multiplex;

