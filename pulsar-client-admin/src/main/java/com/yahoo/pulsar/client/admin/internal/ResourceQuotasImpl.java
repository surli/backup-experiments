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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.yahoo.pulsar.client.admin.PulsarAdminException;
import com.yahoo.pulsar.client.admin.ResourceQuotas;
import com.yahoo.pulsar.client.api.Authentication;
import com.yahoo.pulsar.common.naming.NamespaceName;
import com.yahoo.pulsar.common.policies.data.ErrorData;
import com.yahoo.pulsar.common.policies.data.ResourceQuota;

public class ResourceQuotasImpl extends BaseResource implements ResourceQuotas {

    private final WebTarget quotas;

    public ResourceQuotasImpl(WebTarget web, Authentication auth) {
        super(auth);
        this.quotas = web.path("/resource-quotas");
    }

    public ResourceQuota getDefaultResourceQuota() throws PulsarAdminException {
        try {
            return request(quotas).get(ResourceQuota.class);
        } catch (Exception e) {
            throw getApiException(e);
        }
    }

    public void setDefaultResourceQuota(ResourceQuota quota) throws PulsarAdminException {
        try {
            request(quotas).post(Entity.entity(quota, MediaType.APPLICATION_JSON), ErrorData.class);
        } catch (Exception e) {
            throw getApiException(e);
        }
    }

    public ResourceQuota getNamespaceBundleResourceQuota(String namespace, String bundle) throws PulsarAdminException {
        try {
            NamespaceName ns = new NamespaceName(namespace);
            return request(
                    quotas.path(ns.getProperty()).path(ns.getCluster()).path(ns.getLocalName()).path(bundle))
                    .get(ResourceQuota.class);
        } catch (Exception e) {
            throw getApiException(e);
        }
    }

    public void setNamespaceBundleResourceQuota(String namespace, String bundle, ResourceQuota quota)
            throws PulsarAdminException {
        try {
            NamespaceName ns = new NamespaceName(namespace);
            request(
                quotas.path(ns.getProperty()).path(ns.getCluster()).path(ns.getLocalName()).path(bundle))
                    .post(Entity.entity(quota, MediaType.APPLICATION_JSON), ErrorData.class);
        } catch (Exception e) {
            throw getApiException(e);
        }
    }

    public void resetNamespaceBundleResourceQuota(String namespace, String bundle) throws PulsarAdminException {
        try {
            NamespaceName ns = new NamespaceName(namespace);
            request(
                quotas.path(ns.getProperty()).path(ns.getCluster()).path(ns.getLocalName()).path(bundle))
                    .delete();
        } catch (Exception e) {
            throw getApiException(e);
        }
    }
}

