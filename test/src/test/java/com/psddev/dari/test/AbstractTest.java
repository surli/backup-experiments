package com.psddev.dari.test;

import com.psddev.dari.h2.H2Database;
import com.psddev.dari.elasticsearch.ElasticsearchDatabase;
import com.psddev.dari.elasticsearch.EmbeddedElasticsearchServer;
import com.psddev.dari.util.Settings;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.http.HttpResponse;
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
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public abstract class AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);
    private static boolean initialize = true;
    private static boolean isEmbedded = false;

    public static boolean getIsEmbedded() {
        return isEmbedded;
    }

    public static void setIsEmbedded(boolean isEmbedded) {
        AbstractTest.isEmbedded = isEmbedded;
    }

    /**
     *
     */
    public static void deleteIndex(String index, String nodeHost) throws IOException {
        LOGGER.info("Deleting Index " + index);
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpDelete delete = new HttpDelete(nodeHost + index);
            delete.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(delete);
            String res = EntityUtils.toString(response.getEntity());
            LOGGER.info("Deleted Index {} [{}]", index, res);
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("Warning: deleteIndex[%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
            throw error;
        }
    }

    /**
     * Create the index and map it
     */
    public static void createIndexandMapping(String index, String nodeHost) throws IOException {
        LOGGER.info("Mapping Index " + index);
        try {
            String jsonMap = ElasticsearchDatabase.getMapping("");
            String jsonSetting = ElasticsearchDatabase.getSetting("");
            String json = "{\n"
                    + "  \"settings\":\n"
                    + jsonSetting + ",\n"
                    + "  \"mappings\": {\n"
                    + "    \"_default_\":\n" +
                    jsonMap + "}}";

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPut put = new HttpPut(nodeHost + index);
            put.addHeader("accept", "application/json");
            StringEntity input = new StringEntity(json);
            put.setEntity(input);
            HttpResponse response = httpClient.execute(put);
            if (response.getStatusLine().getStatusCode() > 201) {
                LOGGER.info("ELK createIndexandMapping Response > 201");
                assertThat("Response > 201", response.getStatusLine().getStatusCode(), greaterThan(201));
            }
            json = EntityUtils.toString(response.getEntity());
            LOGGER.info("ELK createIndexandMapping Response " + json);
        } catch (IOException error) {
            LOGGER.warn(
                    String.format("Warning: createIndexandMapping[%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
            throw error;
        }
    }

    public static void H2SetupDatabase() {
        String DATABASE_NAME = "h2";
        String SETTING_KEY_PREFIX = "dari/database/" + DATABASE_NAME + "/";

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test" + UUID.randomUUID().toString().replaceAll("-", "") + ";DB_CLOSE_DELAY=-1");

        Settings.setOverride("dari/defaultDatabase", DATABASE_NAME);
        Settings.setOverride(SETTING_KEY_PREFIX + "class", H2Database.class.getName());
        Settings.setOverride(SETTING_KEY_PREFIX + H2Database.DATA_SOURCE_SUB_SETTING, dataSource);
        Settings.setOverride(SETTING_KEY_PREFIX + H2Database.INDEX_SPATIAL_SUB_SETTING, Boolean.TRUE);

    }

    public static void ElasticSetupDatabase() throws Exception {

        String clusterName = "elasticsearch_a";

        Settings.setOverride(ElasticsearchDatabase.DEFAULT_DATABASE_NAME, ElasticsearchDatabase.DATABASE_NAME);
        Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + "class", ElasticsearchDatabase.class.getName());
        Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.CLUSTER_NAME_SUB_SETTING, clusterName);
        Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.INDEX_NAME_SUB_SETTING, "index1");
        Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + "1/" + ElasticsearchDatabase.CLUSTER_PORT_SUB_SETTING, "9300");
        Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + "1/" + ElasticsearchDatabase.CLUSTER_REST_PORT_SUB_SETTING, "9200");
        Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + "1/" + ElasticsearchDatabase.HOSTNAME_SUB_SETTING, "localhost");
        Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.SUBQUERY_RESOLVE_LIMIT_SETTING, "1000");

        String nodeHost = ElasticsearchDatabase.getNodeHost();
        if (!ElasticsearchDatabase.checkAlive(nodeHost)) {
            // ok create embedded since it is not already running for test
            isEmbedded = true;
            EmbeddedElasticsearchServer.setup(clusterName);
        }
        String verifyClusterName = ElasticsearchDatabase.getClusterName(nodeHost);
        Settings.setOverride(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.CLUSTER_NAME_SUB_SETTING, verifyClusterName);
        deleteIndex((String) Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.INDEX_NAME_SUB_SETTING), nodeHost);
        deleteIndex(Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.INDEX_NAME_SUB_SETTING) + "*", nodeHost);
        // create base index1
        createIndexandMapping((String) Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + ElasticsearchDatabase.INDEX_NAME_SUB_SETTING), nodeHost);
    }

    /**
     * Create Elastic database if !initialize
     */
    @BeforeClass
    public static void createDatabase() throws Exception {
        if (initialize) {
            initialize = false;

            String value=System.getProperty("DBTYPE");

            if (value.equals("elastic") || value.equals("com.psddev.dari.test.ElasticTest")) {
                LOGGER.info("======Elastic");
                ElasticSetupDatabase();
            } else if (value.equals("h2") || value.equals("com.psddev.dari.test.H2Test")) {
                LOGGER.info("======H2");
                H2SetupDatabase();
            }

        }
    }
}
