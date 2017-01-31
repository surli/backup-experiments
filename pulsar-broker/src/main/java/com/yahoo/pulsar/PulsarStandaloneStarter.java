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
package com.yahoo.pulsar;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.FileInputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yahoo.pulsar.broker.PulsarService;
import com.yahoo.pulsar.broker.ServiceConfiguration;
import com.yahoo.pulsar.client.admin.PulsarAdmin;
import com.yahoo.pulsar.client.admin.PulsarAdminException;
import com.yahoo.pulsar.common.configuration.PulsarConfigurationLoader;
import com.yahoo.pulsar.common.policies.data.ClusterData;
import com.yahoo.pulsar.common.policies.data.PropertyAdmin;
import com.yahoo.pulsar.zookeeper.LocalBookkeeperEnsemble;

public class PulsarStandaloneStarter {

    PulsarService broker;
    PulsarAdmin admin;
    LocalBookkeeperEnsemble bkEnsemble;
    ServiceConfiguration config;

    @Parameter(names = { "-c", "--config" }, description = "Configuration file path", required = true)
    private String configFile;

    @Parameter(names = { "--wipe-data" }, description = "Clean up previous ZK/BK data")
    private boolean wipeData = false;

    @Parameter(names = { "--num-bookies" }, description = "Number of local Bookies")
    private int numOfBk = 1;

    @Parameter(names = { "--zookeeper-port" }, description = "Local zookeeper's port")
    private int zkPort = 2181;

    @Parameter(names = { "--bookkeeper-port" }, description = "Local bookies base port")
    private int bkPort = 3181;

    @Parameter(names = { "--zookeeper-dir" }, description = "Local zooKeeper's data directory")
    private String zkDir = "data/standalone/zookeeper";

    @Parameter(names = { "--bookkeeper-dir" }, description = "Local bookies base data directory")
    private String bkDir = "data/standalone/bookkeeper";

    @Parameter(names = { "--no-broker" }, description = "Only start ZK and BK services, no broker")
    private boolean noBroker = false;

    @Parameter(names = { "--only-broker" }, description = "Only start Pulsar broker service (no ZK, BK)")
    private boolean onlyBroker = false;

    @Parameter(names = { "-h", "--help" }, description = "Show this help message")
    private boolean help = false;

    private static final Logger log = LoggerFactory.getLogger(PulsarStandaloneStarter.class);

    public PulsarStandaloneStarter(String[] args) throws Exception {

        JCommander jcommander = new JCommander();
        try {
            jcommander.addObject(this);
            jcommander.parse(args);
            if (help || isBlank(configFile)) {
                jcommander.usage();
                return;
            }

            if (noBroker && onlyBroker) {
                log.error("Only one option is allowed between '--no-broker' and '--only-broker'");
                jcommander.usage();
                return;
            }
        } catch (Exception e) {
            jcommander.usage();
            return;
        }

        this.config = PulsarConfigurationLoader.create((new FileInputStream(configFile)), ServiceConfiguration.class);
        PulsarConfigurationLoader.isComplete(config);
        // Set ZK server's host to localhost
        config.setZookeeperServers("127.0.0.1:" + zkPort);
        config.setGlobalZookeeperServers("127.0.0.1:" + zkPort);
        config.setWebSocketServiceEnabled(true);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    if (broker != null) {
                        broker.close();
                    }

                    if (bkEnsemble != null) {
                        bkEnsemble.stop();
                    }
                } catch (Exception e) {
                    log.error("Shutdown failed: {}", e.getMessage());
                }
            }
        });
    }

    void start() throws Exception {

        if (config == null) {
            System.exit(1);
        }

        log.debug("--- setup PulsarStandaloneStarter ---");

        if (!onlyBroker) {
            // Start LocalBookKeeper
            bkEnsemble = new LocalBookkeeperEnsemble(numOfBk, zkPort, bkPort, zkDir, bkDir, wipeData);
            bkEnsemble.start();
        }

        if (noBroker) {
            return;
        }

        // Start Broker
        broker = new PulsarService(config);
        broker.start();

        // Create a sample namespace
        URL url = new URL("http://127.0.0.1:" + config.getWebServicePort());
        admin = new PulsarAdmin(url, config.getBrokerClientAuthenticationPlugin(),
                config.getBrokerClientAuthenticationParameters());
        String property = "sample";
        String cluster = config.getClusterName();
        String namespace = property + "/" + cluster + "/ns1";
        try {
            if (!admin.clusters().getClusters().contains(cluster)) {
                admin.clusters().createCluster(cluster, new ClusterData(url.toString()));
            }

            if (!admin.properties().getProperties().contains(property)) {
                admin.properties().createProperty(property,
                        new PropertyAdmin(Lists.newArrayList(config.getSuperUserRoles()), Sets.newHashSet(cluster)));
            }

            if (!admin.namespaces().getNamespaces(property).contains(namespace)) {
                admin.namespaces().createNamespace(namespace);
            }
        } catch (PulsarAdminException e) {
            log.info(e.getMessage());
        }

        log.debug("--- setup completed ---");
    }

    public static void main(String args[]) throws Exception {
        // Start standalone
        PulsarStandaloneStarter standalone = new PulsarStandaloneStarter(args);
        standalone.start();
    }
}
