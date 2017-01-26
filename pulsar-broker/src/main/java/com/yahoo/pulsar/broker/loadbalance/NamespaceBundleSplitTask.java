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
package com.yahoo.pulsar.broker.loadbalance;

import java.lang.Runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a task which detect hot namespace bundle to split
 */
public class NamespaceBundleSplitTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(NamespaceBundleSplitTask.class);
    private final LoadManager loadManager;

    public NamespaceBundleSplitTask(LoadManager manager) {
        loadManager = manager;
    }

    @Override
    public void run() {
        try {
            loadManager.doNamespaceBundleSplit();
        } catch (Exception e) {
            LOG.warn("Error while doing namespace bundle split.", e);
        }
    }
}
