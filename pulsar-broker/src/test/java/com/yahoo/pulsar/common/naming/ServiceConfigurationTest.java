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
package com.yahoo.pulsar.common.naming;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.testng.annotations.Test;

import com.yahoo.pulsar.broker.ServiceConfiguration;
import com.yahoo.pulsar.broker.ServiceConfigurationLoader;

/**
 * 
 *
 */
public class ServiceConfigurationTest {

    final String fileName = "configurations/pulsar_broker_test.conf"; // test-resource file

    /**
     * test {@link ServiceConfiguration} initialization
     * 
     * @throws Exception
     */
    @Test
    public void testInit() throws Exception {
        final String zookeeperServer = "localhost:2184";
        final int brokerServicePort = 1000;
        InputStream newStream = updateProp(zookeeperServer, String.valueOf(brokerServicePort), "ns1,ns2");
        final ServiceConfiguration config = ServiceConfigurationLoader.create(newStream);
        assertTrue(isNotBlank(config.getZookeeperServers()));
        assertTrue(config.getBrokerServicePort() == brokerServicePort);
        assertEquals(config.getBootstrapNamespaces().get(1), "ns2");
    }

    /**
     * test {@link ServiceConfiguration} with incorrect values.
     * 
     * @throws Exception
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInitFailure() throws Exception {
        final String zookeeperServer = "localhost:2184";
        InputStream newStream = updateProp(zookeeperServer, String.valueOf("invalid-string"), null);
        ServiceConfigurationLoader.create(newStream);
    }

    private InputStream updateProp(String zookeeperServer, String brokerServicePort, String namespace)
            throws IOException {
        checkNotNull(fileName);
        Properties properties = new Properties();
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        properties.load(stream);
        properties.setProperty("zookeeperServers", zookeeperServer);
        properties.setProperty("brokerServicePort", brokerServicePort);
        if (namespace != null)
            properties.setProperty("bootstrapNamespaces", namespace);
        StringWriter writer = new StringWriter();
        properties.list(new PrintWriter(writer));
        return new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
    }

}
