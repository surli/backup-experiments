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
package org.apache.jackrabbit.oak.spi.security;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ConfigurationBaseTest {

    private final ConfigurationBase base = new ConfigurationBase() {};

    @Test(expected = IllegalStateException.class)
    public void testGetSecurityProvider() {
        base.getSecurityProvider();
    }

    @Test
    public void testSetSecurityProvider() {
        SecurityProvider securityProvider = Mockito.mock(SecurityProvider.class);
        base.setSecurityProvider(securityProvider);

        assertSame(securityProvider, base.getSecurityProvider());
    }

    @Test
    public void testGetParameters() {
        assertSame(ConfigurationParameters.EMPTY, base.getParameters());
    }

    @Test
    public void testSetParameters() {
        ConfigurationParameters params = ConfigurationParameters.of("key", "value");

        base.setParameters(params);
        assertEquals(params, base.getParameters());
    }
}