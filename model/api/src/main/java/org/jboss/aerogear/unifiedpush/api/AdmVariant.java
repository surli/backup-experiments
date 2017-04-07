/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.api;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AdmVariant extends Variant {

    private static final long serialVersionUID = -5343197341772916741L;

    @NotNull
    @Size(min = 1, max = 255, message = "ClientId must be max. 255 chars long")
    private String clientId;

    @Size(min = 1, max = 255, message = "Client Secret must be max. 255 chars long")
    private String clientSecret;


    /**
     * The client id to connect to the Amazon Device Messaging services
     * @return the client secret
     */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * The client secret (password) to connect to the Amazon Device Messaging services
     * @return the client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public VariantType getType() {
        return VariantType.ADM;
    }
}
