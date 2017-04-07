/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.authorization.admin;

import java.util.List;
import java.util.Map;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.model.Policy;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.services.resources.admin.RealmAuth;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PermissionService extends PolicyService {
    public PermissionService(ResourceServer resourceServer, AuthorizationProvider authorization, RealmAuth auth) {
        super(resourceServer, authorization, auth);
    }

    @Override
    protected Object doCreatePolicyTypeResource(String type) {
        return new PermissionTypeService(type, resourceServer, authorization, auth);
    }

    @Override
    protected PolicyResourceService doCreatePolicyResource(Policy policy) {
        return new PermissionResourceService(policy, resourceServer, authorization, auth);
    }

    @Override
    protected List<Object> doSearch(Integer firstResult, Integer maxResult, Map<String, String[]> filters) {
        filters.put("permission", new String[] {Boolean.TRUE.toString()});
        return super.doSearch(firstResult, maxResult, filters);
    }
}
