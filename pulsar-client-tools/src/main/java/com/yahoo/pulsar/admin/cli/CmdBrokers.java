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
package com.yahoo.pulsar.admin.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.yahoo.pulsar.client.admin.PulsarAdmin;

@Parameters(commandDescription = "Operations about brokers")
public class CmdBrokers extends CmdBase {

    @Parameters(commandDescription = "List active brokers of the cluster")
    private class List extends CliCommand {
        @Parameter(description = "cluster-name\n", required = true)
        private java.util.List<String> params;

        @Override
        void run() throws Exception {
            String cluster = getOneArgument(params);
            print(admin.brokers().getActiveBrokers(cluster));
        }
    }

    @Parameters(commandDescription = "List namespaces owned by the broker")
    private class Namespaces extends CliCommand {
        @Parameter(description = "cluster-name\n", required = true)
        private java.util.List<String> params;
        @Parameter(names = "--url", description = "broker-url\n", required = true)
        private String brokerUrl;

        @Override
        void run() throws Exception {
            String cluster = getOneArgument(params);
            print(admin.brokers().getOwnedNamespaces(cluster, brokerUrl));
        }
    }

    CmdBrokers(PulsarAdmin admin) {
        super("brokers", admin);
        jcommander.addCommand("list", new List());
        jcommander.addCommand("namespaces", new Namespaces());
    }
}
