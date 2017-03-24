package com.psddev.dari.elasticsearch;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;

class ElasticsearchDatabaseConnection {
    private static TransportClient client = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchDatabase.class);

    /**
     * Check nodes are not empty and isAlive
     *
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isAlive(TransportClient client) {
        List<DiscoveryNode> nodes = client.connectedNodes();
        return !nodes.isEmpty();
    }

    /**
     * Force a close
     */
    public static synchronized void closeClient() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    /**
     * getClient synchronized and calls PreBuiltTransportClient
     *
     * @return {code null} is error
     */
    public static synchronized TransportClient getClient(Settings nodeSettings, List<ElasticsearchDatabase.Node> nodes) {
        if (nodeSettings == null || nodes.size() == 0) {
            LOGGER.warn("Elasticsearch openConnection missing nodeSettings/nodes");
            nodeSettings = Settings.builder()
                    .put("client.transport.sniff", true).build();
        }
        if (client == null) {
            try {
                client = new PreBuiltTransportClient(nodeSettings);
                for (ElasticsearchDatabase.Node n : nodes) {
                    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(n.hostname), n.port));
                }
                if (!isAlive(client)) {
                    LOGGER.warn("Elasticsearch openConnection Not Alive!");
                    return null;
                }
                return client;
            } catch (Exception error) {
                LOGGER.warn(
                        String.format("Elasticsearch openConnection Cannot open ES Exception [%s: %s]",
                                error.getClass().getName(),
                                error.getMessage()),
                        error);
            }
            return null;
        } else {
            try {
                if (!isAlive(client)) {
                    LOGGER.warn("Elasticsearch openConnection Not Alive!");
                    client = new PreBuiltTransportClient(nodeSettings);
                    for (ElasticsearchDatabase.Node n : nodes) {
                        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(n.hostname), n.port));
                    }
                }
            } catch (Exception error) {
                LOGGER.warn(
                        String.format("Elasticsearch openConnection Cannot open ES Exception [%s: %s]",
                                error.getClass().getName(),
                                error.getMessage()),
                        error);
            }
            return client;
        }
    }
}
