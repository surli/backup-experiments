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
package com.yahoo.pulsar.client.admin.internal;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import com.yahoo.pulsar.client.admin.Brokers;
import com.yahoo.pulsar.client.admin.PulsarAdminException;
import com.yahoo.pulsar.client.api.Authentication;
import com.yahoo.pulsar.common.policies.data.NamespaceOwnershipStatus;

public class BrokersImpl extends BaseResource implements Brokers {
    private final WebTarget brokers;

    public BrokersImpl(WebTarget web, Authentication auth) {
        super(auth);
        brokers = web.path("/brokers");
    }

    @Override
    public List<String> getActiveBrokers(String cluster) throws PulsarAdminException {
        try {
            return request(brokers.path(cluster)).get(new GenericType<List<String>>() {
            });
        } catch (Exception e) {
            throw getApiException(e);
        }
    }

    @Override
    public Map<String, NamespaceOwnershipStatus> getOwnedNamespaces(String cluster, String brokerUrl)
            throws PulsarAdminException {
        try {
            return request(brokers.path(cluster).path(brokerUrl).path("ownedNamespaces")).get(
                    new GenericType<Map<String, NamespaceOwnershipStatus>>() {
                    });
        } catch (Exception e) {
            throw getApiException(e);
        }
    }

}
