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
package com.yahoo.pulsar.common.policies.data;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;

import com.google.common.base.Objects;
import com.yahoo.pulsar.common.policies.impl.AutoFailoverPolicyFactory;

public class AutoFailoverPolicyData {
    public AutoFailoverPolicyType policy_type;
    public Map<String, String> parameters;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AutoFailoverPolicyData) {
            AutoFailoverPolicyData other = (AutoFailoverPolicyData) obj;
            return Objects.equal(policy_type, other.policy_type) && Objects.equal(parameters, other.parameters);
        }

        return false;
    }

    public void validate() {
        checkArgument(policy_type != null && parameters != null);
        AutoFailoverPolicyFactory.create(this);
    }

    @Override
    public String toString() {
        return String.format("policy_type=%s parameters=%s", policy_type, parameters);
    }
}
