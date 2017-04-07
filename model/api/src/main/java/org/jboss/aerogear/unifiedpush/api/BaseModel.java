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
package org.jboss.aerogear.unifiedpush.api;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseModel implements Serializable {

    private static final long serialVersionUID = -4123402116687584512L;

    private String id = UUID.randomUUID().toString();

    /**
     * Key identifying the model object in the underlying database (primary key)
     *
     * @param id value of the primary key
     */
    public void setId(final String id) {
        this.id = id;
    }
    public String getId() {
        return this.id;
    }

}
