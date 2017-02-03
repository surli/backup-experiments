/*
 * Copyright 2015 Rackspace
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rackspacecloud.blueflood.types;

import com.google.common.annotations.VisibleForTesting;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Arrays;

public class BluefloodSet {

    public BluefloodSet() {
    }

    @VisibleForTesting
    public BluefloodSet(String name, String[] values) {
        this.name = name;
        this.values = values;
    }

    @NotEmpty
    private String name;

    private String[] values;

    public String getName() {
        return name;
    }

    public String[] getValues() {
        return Arrays.copyOf(values, values.length, String[].class);
    }
}

