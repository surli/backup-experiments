package com.psddev.dari.elasticsearch;

import com.psddev.dari.util.Settings;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractElasticTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractElasticTest.class);

    private static boolean initialize = true;

    /**
     *
     */
    public static void deleteIndex(String index, String nodeHost) {
        LOGGER.info("Deleting Index " + index);
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpDelete delete = new HttpDelete(nodeHost + index);
            delete.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(delete);
            String json = EntityUtils.toString(response.getEntity());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            assertTrue("ClientProtocolException", 1==0);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue("IOException", 1==0);
        }
    }

    /**
     *
     */
    public static void createIndexandMapping(String index, String nodeHost) {
        LOGGER.info("Mapping Index " + index);
        try {
            String json = ElasticsearchDatabase.getMapping("");
            json = "{\n"
                    + "  \"mappings\": {\n"
                    + "    \"_default_\":\n" +
                    json + "}}";

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPut put = new HttpPut(nodeHost + index);
            put.addHeader("accept", "application/json");
            StringEntity input = new StringEntity(json);
            put.setEntity(input);
            HttpResponse response = httpClient.execute(put);
            if (response.getStatusLine().getStatusCode() > 201) {
                LOGGER.info("ELK createIndexandMapping Response > 201");
                assertTrue("Response > 201", 1==0);
            }
            json = EntityUtils.toString(response.getEntity());
            LOGGER.info("ELK createIndexandMapping Response " + json);
        } catch (ClientProtocolException e) {
            LOGGER.info("ELK createIndexandMapping ClientProtocolException");
            e.printStackTrace();
            assertTrue("ClientProtocolException", 1==0);
        } catch (IOException e) {
            LOGGER.info("ELK createIndexandMapping IOException");
            e.printStackTrace();
            assertTrue("IOException", 1==0);
        }
    }

    /**
     *
     */
    private static String getNodeHost() {
        String host = (String) Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.HOSTNAME_SUB_SETTING);
        return "http://" + host + ":9200/";
    }

    /**
     *
     */
    public static Map<String, Object> getDatabaseSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put(ElasticsearchDatabase.CLUSTER_NAME_SUB_SETTING, Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.CLUSTER_NAME_SUB_SETTING));
        settings.put(ElasticsearchDatabase.INDEX_NAME_SUB_SETTING, Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.INDEX_NAME_SUB_SETTING));
        settings.put(ElasticsearchDatabase.CLUSTER_PORT_SUB_SETTING, Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.CLUSTER_PORT_SUB_SETTING));
        settings.put(ElasticsearchDatabase.HOSTNAME_SUB_SETTING, Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.HOSTNAME_SUB_SETTING));
        settings.put(ElasticsearchDatabase.SUBQUERY_RESOLVE_LIMIT_SETTING, Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.SUBQUERY_RESOLVE_LIMIT_SETTING));
        settings.put(ElasticsearchDatabase.SEARCH_TIMEOUT_SETTING, Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.SEARCH_TIMEOUT_SETTING));
        return settings;
    }

    /**
     *
     */
    public void before() {
        String nodeHost = getNodeHost();
        String clusterName = ElasticsearchDatabase.getClusterName(nodeHost);
        Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.CLUSTER_NAME_SUB_SETTING, clusterName);
        assertThat(clusterName, notNullValue());
        String version = ElasticsearchDatabase.getVersion(nodeHost);
        if (version != null && version.length() > 2) {
            version = version.substring(0, 2);
        }
        assertEquals(version, "5.");
    }

    /**
     *
     */
    @BeforeClass
    public static void createDatabase() {
        if (initialize) {
            initialize = false;

            Settings.setOverride(ElasticsearchDatabase.DEFAULT_DATABASE_NAME, ElasticsearchDatabase.DATABASE_NAME);
            Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + "class", ElasticsearchDatabase.class.getName());
            Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.CLUSTER_NAME_SUB_SETTING, "elasticsearch_a");
            Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.INDEX_NAME_SUB_SETTING, "index1");
            Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.CLUSTER_PORT_SUB_SETTING, "9300");
            Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.HOSTNAME_SUB_SETTING, "localhost");
            Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.SUBQUERY_RESOLVE_LIMIT_SETTING, "1000");

            String nodeHost = getNodeHost();
            if (!ElasticsearchDatabase.checkAlive(nodeHost)) {
                // ok create embedded since it is not already running for test
                EmbeddedElasticsearchServer.setup();
            }
            String clusterName = ElasticsearchDatabase.getClusterName(nodeHost);
            Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.CLUSTER_NAME_SUB_SETTING, clusterName);
            deleteIndex((String) Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.INDEX_NAME_SUB_SETTING), nodeHost);
            deleteIndex(Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.INDEX_NAME_SUB_SETTING) + "*", nodeHost);
            // create base index1
            createIndexandMapping((String) Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.INDEX_NAME_SUB_SETTING), nodeHost);
        }
    }
}
