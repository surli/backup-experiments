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
package com.yahoo.pulsar.common.lookup.data;

public class LookupData {
    private String brokerUrl;
    private String brokerUrlTls;
    private String httpUrl; // Web service HTTP address
    private String nativeUrl;

    public LookupData() {
    }

    public LookupData(String brokerUrl, String brokerUrlTls, String httpUrl) {
        this.brokerUrl = brokerUrl;
        this.brokerUrlTls = brokerUrlTls;
        this.httpUrl = httpUrl;
        this.nativeUrl = brokerUrl;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public String getBrokerUrlTls() {
        return brokerUrlTls;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    /**
     * Legacy name, but client libraries are still using it so it needs to be included in Json
     */
    @Deprecated
    public String getNativeUrl() {
        return nativeUrl;
    }

    /**
     * "brokerUrlSsl" is needed in the serialized Json for compatibility reasons.
     *
     * Older C++ pulsar client library version will fail the lookup if this field is not included, even though it's not
     * used
     */
    @Deprecated
    public String getBrokerUrlSsl() {
        return "";
    }
}
