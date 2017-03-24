package com.psddev.dari.elasticsearch;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.psddev.dari.db.AbstractDatabase;
import com.psddev.dari.db.AbstractGrouping;
import com.psddev.dari.db.AtomicOperation;
import com.psddev.dari.db.ComparisonPredicate;
import com.psddev.dari.db.CompoundPredicate;
import com.psddev.dari.db.Database;
import com.psddev.dari.db.Grouping;
import com.psddev.dari.db.Location;
import com.psddev.dari.db.Modification;
import com.psddev.dari.db.ObjectField;
import com.psddev.dari.db.ObjectIndex;
import com.psddev.dari.db.ObjectMethod;
import com.psddev.dari.db.ObjectType;
import com.psddev.dari.db.Predicate;
import com.psddev.dari.db.PredicateParser;
import com.psddev.dari.db.Query;
import com.psddev.dari.db.Record;
import com.psddev.dari.db.Recordable;
import com.psddev.dari.db.Region;
import com.psddev.dari.db.Sorter;
import com.psddev.dari.db.State;
import com.psddev.dari.util.Settings;
import com.psddev.dari.db.StateSerializer;
import com.psddev.dari.db.UnsupportedIndexException;
import com.psddev.dari.db.UnsupportedPredicateException;
import com.psddev.dari.db.UpdateNotifier;
import com.psddev.dari.util.ObjectUtils;
import com.psddev.dari.util.PaginatedResult;
import com.psddev.dari.util.UuidUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import com.vividsolutions.jts.geom.Coordinate;
import org.elasticsearch.common.geo.builders.ShapeBuilders;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoShapeQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.weightFactorFunction;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.elasticsearch.search.sort.SortOrder.DESC;

/**
 * ElasticsearchDatabase for Elasticsearch 5.2
 *
 */
public class ElasticsearchDatabase extends AbstractDatabase<TransportClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchDatabase.class);

    public class Node {
        public String hostname;
        public int port;
        public int restPort;
    }

    public static final String ELASTIC_VERSION = "5.2.2";
    public static final String DEFAULT_DATABASE_NAME = "dari/defaultDatabase";
    public static final String DATABASE_NAME = "elasticsearch";
    public static final String SETTING_KEY_PREFIX = "dari/database/" + DATABASE_NAME + "/";
    public static final String CLUSTER_NAME_SUB_SETTING = "clusterName";
    public static final String CLUSTER_PORT_SUB_SETTING = "clusterPort";
    public static final String CLUSTER_REST_PORT_SUB_SETTING = "clusterRestPort";
    public static final String HOSTNAME_SUB_SETTING = "clusterHostname";
    public static final String INDEX_NAME_SUB_SETTING = "indexName";
    public static final String SEARCH_TIMEOUT_SETTING = "searchTimeout";
    public static final String SUBQUERY_RESOLVE_LIMIT_SETTING = "subQueryResolveLimit";

    public static final String ID_FIELD = "_id";
    public static final String UID_FIELD = "_uid";      // special for aggregations/sort
    public static final String TYPE_ID_FIELD = "_type";
    public static final String ALL_FIELD = "_all";
    public static final int INITIAL_FETCH_SIZE = 1000;
    public static final int SUBQUERY_MAX_ROWS = 5000;   // dari/subQueryResolveLimit
    public static final int TIMEOUT = 30000;            // 30 seconds
    public static final int FACET_MAX_ROWS = 100;
    public static final int CACHE_TIMEOUT_MIN = 30;
    public static final int CACHE_MAX_INDEX_SIZE = 2500;
    private static final long MILLISECONDS_IN_5YEAR = 1000L * 60L * 60L * 24L * 365L * 5L;
    private static final Pattern UUID_PATTERN = Pattern.compile("([A-Fa-f0-9]{8})-([A-Fa-f0-9]{4})-([A-Fa-f0-9]{4})-([A-Fa-f0-9]{4})-([A-Fa-f0-9]{12})");

    public class IndexKey {
        private String indexId;
        private List<Node> clusterNodes;
        private org.elasticsearch.common.settings.Settings nodeSettings;

        public String getIndexId() {
            return indexId;
        }

        public void setIndexId(String indexId) {
            this.indexId = indexId;
        }

        public List<Node> getClusterNodes() {
            return clusterNodes;
        }

        public void setClusterNodes(List<Node> clusterNodes) {
            this.clusterNodes = clusterNodes;
        }

        public org.elasticsearch.common.settings.Settings getNodeSettings() {
            return nodeSettings;
        }

        public void setNodeSettings(org.elasticsearch.common.settings.Settings nodeSettings) {
            this.nodeSettings = nodeSettings;
        }

        @Override
        public String toString() {

            return MoreObjects.toStringHelper(this)
                    .add("indexId", indexId)
                    .toString();

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            IndexKey indexKey = (IndexKey) o;

            return indexId.equals(indexKey.indexId);
        }

        @Override
        public int hashCode() {
            return indexId.hashCode();
        }
    }

    private static final LoadingCache<IndexKey, String> CREATE_INDEX_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(CACHE_MAX_INDEX_SIZE)
                    .expireAfterAccess(CACHE_TIMEOUT_MIN, TimeUnit.MINUTES)
                    .build(new CacheLoader<IndexKey, String>() {

                        @Override
                        public String load(IndexKey index) throws Exception {
                            TransportClient client = ElasticsearchDatabaseConnection.getClient(index.getNodeSettings(), index.getClusterNodes());
                            defaultMap(client, index.getIndexId());
                            LOGGER.debug("Elasticsearch creating index [{}]", index.getIndexId());
                            return "setIndex";
                        }
                    });

    private final List<UpdateNotifier<?>> updateNotifiers = new ArrayList<>();

    public static final String LOCATION_FIELD = "_location";
    public static final String REGION_FIELD = "_polygon";
    public static final String RAW_FIELD = "raw";
    public static final String MATCH_FIELD = "match";

    private final List<Node> clusterNodes = new ArrayList<>();
    private org.elasticsearch.common.settings.Settings nodeSettings;

    private String clusterName;
    private String indexName;
    private int searchTimeout = TIMEOUT;
    private int subQueryResolveLimit = SUBQUERY_MAX_ROWS;
    private transient TransportClient client;
    private boolean painlessModule = false;
    /**
     * The amount of rows per subquery(join) Elastic Search wrapped
     *
     * @see #readAll(Query)
     */
    public void setSubQueryResolveLimit(int subQueryResolveLimit) {

        this.subQueryResolveLimit = subQueryResolveLimit;
    }

    /**
     * The amount of rows per subquery(join) to Elastic Search
     *
     * @see #readAll(Query)
     */
    public int getSubQueryResolveLimit() {

        return subQueryResolveLimit;
    }

    /**
     * Set the timeout for calls into Elastic - might want to set lower/higher based on systems
     * Used in .setTimeout(new TimeValue(this.searchTimeout))
     *
     * @see #doInitialize
     */
    public void setSearchTimeout(int searchTimeout) {

        this.searchTimeout = searchTimeout;
    }

    /**
     * The timeout for calls to Elastic
     *
     * @see #readPartial(Query, long, int)
     */
    public int getSearchTimeout() {

        return searchTimeout;
    }

    /**
     * Set the ClusterName for Elastic
     *
     * @see #doInitialize
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * Get the ClusterName for Elastic
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * Get the indexName for Elastic
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Set the index for Elastic
     *
     * @see #doInitialize
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /**
     * The Elastic TransportClient using ElasticsearchDatabaseConnection.getClient()
     *
     * @return {@code null} indicates could not get client connection
     */
    @Override
    public TransportClient openConnection() {

        if (this.client != null && isAlive(this.client)) {
            return this.client;
        }
        try {
            this.client = ElasticsearchDatabaseConnection.getClient(nodeSettings, clusterNodes);
            return this.client;
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("Elasticsearch openConnection Cannot open ES Exception [%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
        }
        LOGGER.info("Elasticsearch openConnection return null");
        return null;
    }

    /**
     * Close the connection. Actually does not close() since the connection is persistent
     */
    @Override
    public void closeConnection(TransportClient client) {
        //client.close();
    }

    /**
     * Initialize all the settings for Elastic
     */
    @Override
    protected void doInitialize(String settingsKey, Map<String, Object> settings) {

        String clusterName = ObjectUtils.to(String.class, settings.get(CLUSTER_NAME_SUB_SETTING));

        Preconditions.checkNotNull(clusterName);

        this.clusterName = clusterName;

        String indexName = ObjectUtils.to(String.class, settings.get(INDEX_NAME_SUB_SETTING));

        Preconditions.checkNotNull(indexName);

        String subQueryResolveLimit = ObjectUtils.to(String.class, settings.get(SUBQUERY_RESOLVE_LIMIT_SETTING));

        if (subQueryResolveLimit == null) {
            this.subQueryResolveLimit = SUBQUERY_MAX_ROWS;
        } else {
            this.subQueryResolveLimit = Integer.parseInt(subQueryResolveLimit);

        }

        String clusterTimeout = ObjectUtils.to(String.class, settings.get(SEARCH_TIMEOUT_SETTING));

        if (clusterTimeout == null) {
            this.searchTimeout = TIMEOUT;
        } else {
            this.searchTimeout = Integer.parseInt(clusterTimeout);

        }

        boolean done = false;
        int nodeCount = 1;
        while (!done) {

            if (settings.get(String.valueOf(nodeCount)) == null) {
                done = true;
            } else {

                @SuppressWarnings("unchecked")
                Map<String, Object> subSettings = (Map<String, Object>) settings.get(String.valueOf(nodeCount));

                String clusterPort = ObjectUtils.to(String.class, subSettings.get(CLUSTER_PORT_SUB_SETTING));

                Preconditions.checkNotNull(clusterPort);

                String clusterRestPort = ObjectUtils.to(String.class, subSettings.get(CLUSTER_REST_PORT_SUB_SETTING));

                Preconditions.checkNotNull(clusterRestPort);

                String clusterHostname = ObjectUtils.to(String.class, subSettings.get(HOSTNAME_SUB_SETTING));

                Preconditions.checkNotNull(clusterHostname);

                Node n = new Node();
                n.hostname = clusterHostname;
                n.port = Integer.parseInt(clusterPort);
                n.restPort = Integer.parseInt(clusterRestPort);

                this.clusterNodes.add(n);

                nodeCount++;
            }
        }

        this.indexName = indexName;

        this.nodeSettings = org.elasticsearch.common.settings.Settings.builder()
                .put("cluster.name", this.clusterName)
                .put("client.transport.sniff", true).build();

        this.painlessModule = this.isModuleInstalled("lang-painless", "org.elasticsearch.painless.PainlessPlugin");

    }

    /**
     * Override to support in Elastic
     */
    @Override
    public Date readLastUpdate(Query<?> query) {
        return null;
    }

    public boolean isAlive(TransportClient client) {
        if (client != null) {
            List<DiscoveryNode> nodes = client.connectedNodes();
            if (!nodes.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get The running version of Elastic
     *
     * @return the version running can return null on exception
     */
    public static String getVersion(String nodeHost) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpGet getRequest = new HttpGet(nodeHost);
            getRequest.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(getRequest);
            String json = EntityUtils.toString(response.getEntity());
            JSONObject j = new JSONObject(json);
            if (j != null) {
                if (j.get("version") != null) {
                    if (j.getJSONObject("version") != null) {
                        JSONObject jo = j.getJSONObject("version");
                        String version = jo.getString("number");
                        if (!ELASTIC_VERSION.equals(version)) {
                            LOGGER.warn("Warning: Elasticsearch {} version is not {}", version, ELASTIC_VERSION);
                        }
                        return version;
                    }
                }
            }
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("Warning: Elasticsearch cannot get version [%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
        }
        return null;
    }

    /**
     * Get the clusterName from the running version
     *
     * @return the clusterName
     */
    public static String getClusterName(String nodeHost) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpGet getRequest = new HttpGet(nodeHost);
            getRequest.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(getRequest);
            String json;
            json = EntityUtils.toString(response.getEntity());
            JSONObject j = new JSONObject(json);
            if (j != null) {
                if (j.get("cluster_name") != null) {
                    return j.getString("cluster_name");
                }
            }
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("Warning: Elasticsearch cannot get cluster_name [%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
        }
        return null;
    }

    /**
     * Check to see if the running version is alive.
     *
     * @return true indicates node is alive
     */
    public static boolean checkAlive(String nodeHost) {
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();

            HttpGet getRequest = new HttpGet(nodeHost);
            getRequest.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(getRequest);
            String json;
            json = EntityUtils.toString(response.getEntity());
            JSONObject j = new JSONObject(json);
            if (j != null) {
                if (j.get("cluster_name") != null) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Warning: Elasticsearch is not already running");
        }
        return false;
    }

    /**
     * Get one node and return it for REST call
     */
    public static String getNodeHost() {
        String host = (String) Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + "1/" + ElasticsearchDatabase.HOSTNAME_SUB_SETTING);
        String port = (String) Settings.get(ElasticsearchDatabase.SETTING_KEY_PREFIX + "1/" + ElasticsearchDatabase.CLUSTER_REST_PORT_SUB_SETTING);

        return "http://" + host + ":" + port + "/";
    }

    /**
     * See if painless Elastic module is installed. This API does not work well here.
     */
    public boolean isModuleInstalled(String moduleName, String pluginName) {

        try {
            String nodes = getNodeHost() + "_nodes";

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(nodes);
            get.addHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(get);
            String json = EntityUtils.toString(response.getEntity());

            JSONObject j = new JSONObject(json);
            if (j != null) {
                if (j.get("nodes") != null) {
                    if (j.getJSONObject("nodes") != null) {
                        JSONObject jo = j.getJSONObject("nodes");
                        Iterator<?> keys = jo.keys();

                        while( keys.hasNext() ) {
                            String key = (String)keys.next();
                            if ( jo.get(key) instanceof JSONObject ) {
                                JSONObject node = (JSONObject) jo.get(key);
                                JSONArray modules = node.getJSONArray("modules");
                                for (int i = 0; i < modules.length(); i++) {
                                    JSONObject module = modules.getJSONObject(i);
                                    String name = (String) module.get("name");
                                    if (name.equals(moduleName)) {
                                        return true;
                                    }
                                }
                                JSONArray plugins = node.getJSONArray("plugins");
                                for (int i = 0; i < plugins.length(); i++) {
                                    JSONObject plugin = plugins.getJSONObject(i);
                                    String name = (String) plugin.get("name");
                                    if (name.equals(pluginName)) {
                                        return true;
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("Warning: Elasticsearch cannot check Painless [%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);

        }

        return false;
    }

    /**
     * Grab a connection and check it
     */
    public boolean isAlive() {
        TransportClient client = openConnection();
        if (client != null) {
            List<DiscoveryNode> nodes = client.connectedNodes();
            closeConnection(client);
            if (!nodes.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return results for Grouping in Elastic
     */
    @Override
    public <T> PaginatedResult<Grouping<T>> readPartialGrouped(Query<T> query, long offset, int limit, String... fields) {
        if (fields == null || fields.length != 1) {
            return super.readPartialGrouped(query, offset, limit, fields);
        }

        List<Grouping<T>> groupings = new ArrayList<>();

        TransportClient client = openConnection();
        if (client == null || !isAlive(client)) {
            return null;
        }

        Set<UUID> typeIds = query.getConcreteTypeIds(this);

        if (query.getGroup() != null && typeIds.size() == 0) {
            // should limit by the type
            LOGGER.debug("Elasticsearch PaginatedResult readPartialGrouped the call is to limit by from() but did not load typeIds! [{}]", query.getGroup());
        }
        String[] typeIdStrings = typeIds.size() == 0
                ? new String[]{}
                : typeIds.stream().map(UUID::toString).toArray(String[]::new);

        List<String> indexNames = new ArrayList<>();
        for (UUID u : typeIds) {
            indexNames.add(getIndexName() + u.toString().replaceAll("-", ""));
        }
        String[] indexIdStrings = indexNames.toArray(new String[0]);
        checkIndexes(indexIdStrings);

        SearchResponse response;
        QueryBuilder qb = predicateToQueryBuilder(query.getPredicate(), query);
        SearchRequestBuilder srb;

        Matcher groupingMatcher = Query.RANGE_PATTERN.matcher(fields[0]);
        if (groupingMatcher.find()) {
            String field = groupingMatcher.group(1);
            Double start = ObjectUtils.to(Double.class, groupingMatcher.group(2).trim());
            Double end = ObjectUtils.to(Double.class, groupingMatcher.group(3).trim());
            Double gap = ObjectUtils.to(Double.class, groupingMatcher.group(4).trim());

            if (typeIds.size() > 0) {
                srb = client.prepareSearch(indexIdStrings)
                        .setFetchSource(!query.isReferenceOnly())
                        .setTimeout(query.getTimeout() != null && query.getTimeout() > 0 ? TimeValue.timeValueMillis(query.getTimeout().longValue()) : TimeValue.timeValueMillis(this.searchTimeout));
                srb.setTypes(typeIdStrings);
            } else {
                srb = client.prepareSearch(getIndexName() + "*")
                        .setFetchSource(!query.isReferenceOnly())
                        .setTimeout(query.getTimeout() != null && query.getTimeout() > 0 ? TimeValue.timeValueMillis(query.getTimeout().longValue()) : TimeValue.timeValueMillis(this.searchTimeout));
            }
            srb.setQuery(qb)
                    .setFrom(0)
                    .setSize(0);

            RangeAggregationBuilder ab = AggregationBuilders.range("agg").field(field);
            for (double i = start; i < end; i = i + gap) {
                ab.addRange(i, i + gap);
            }
            srb.addAggregation(ab);
            LOGGER.debug("Elasticsearch readPartialGrouped typeIds [{}] - [{}]", (typeIdStrings.length == 0 ? "" : typeIdStrings), srb.toString());
            response = srb.execute().actionGet();
            SearchHits hits = response.getHits();

            Range agg = response.getAggregations().get("agg");

            for (Range.Bucket entry : agg.getBuckets()) {
                String key = entry.getKeyAsString();             // Range as key
                Number from = (Number) entry.getFrom();          // Bucket from
                Number to = (Number) entry.getTo();              // Bucket to
                long docCount = entry.getDocCount();             // Doc count

                LOGGER.debug("key [{}], from [{}], to [{}], doc_count [{}]", key, from, to, docCount);
                groupings.add(new ElasticGrouping<>(Collections.singletonList(key), query, fields, docCount));
            }
        } else {
            if (typeIds.size() > 0) {
                srb = client.prepareSearch(indexIdStrings)
                        .setFetchSource(!query.isReferenceOnly())
                        .setTimeout(query.getTimeout() != null && query.getTimeout() > 0 ? TimeValue.timeValueMillis(query.getTimeout().longValue()) : TimeValue.timeValueMillis(this.searchTimeout));
                srb.setTypes(typeIdStrings);
            } else {
                srb = client.prepareSearch(getIndexName() + "*")
                        .setFetchSource(!query.isReferenceOnly())
                        .setTimeout(query.getTimeout() != null && query.getTimeout() > 0 ? TimeValue.timeValueMillis(query.getTimeout().longValue()) : TimeValue.timeValueMillis(this.searchTimeout));
            }

            srb.setQuery(qb)
                    .setFrom(0)
                    .setSize(0);

            Query.MappedKey mappedKey = mapFullyDenormalizedKey(query, fields[0]);
            String elasticField = specialSortFields.get(mappedKey);
            if (elasticField == null) {
                if (mappedKey != null) {
                    String internalType = mappedKey.getInternalType();
                    if (internalType != null) {
                        if ("text".equals(internalType)) {
                            elasticField = addRaw(mappedKey.getIndexKey(null));
                        }
                    }
                }
                if (elasticField == null) {
                    elasticField = mappedKey.getIndexKey(null);
                }
            }

            if (query.getGroup() != null) {
                TermsAggregationBuilder ab = AggregationBuilders.terms("agg").field(elasticField).size(FACET_MAX_ROWS).order(Terms.Order.count(false));
                srb.addAggregation(ab);
            }
            LOGGER.debug("Elasticsearch readPartialGrouped typeIds [{}] - [{}]", (typeIdStrings.length == 0 ? "" : typeIdStrings), srb.toString());
            response = srb.execute().actionGet();
            SearchHits hits = response.getHits();

            Terms agg = response.getAggregations().get("agg");

            for (Terms.Bucket entry : agg.getBuckets()) {
                String key = entry.getKeyAsString();    // Term
                long docCount = entry.getDocCount();    // Doc count
                LOGGER.debug("key [{}], doc_count [{}]", key, docCount);
                groupings.add(new ElasticGrouping<>(Collections.singletonList(key), query, fields, docCount));
            }
        }

        return new PaginatedResult<>(offset, limit, groupings);
    }

    /**
     * Define the ElasticGrouping
     */
    private static class ElasticGrouping<T> extends AbstractGrouping<T> {

        private final long count;

        public ElasticGrouping(List<Object> keys, Query<T> query, String[] fields, long count) {
            super(keys, query, fields);
            this.count = count;
        }

        // --- AbstractGrouping support ---

        @Override
        protected Aggregate createAggregate(String field) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getCount() {
            return count;
        }
    }

    private void checkIndexes(String[] indexNames) {

        try {
            for (String newIndexname : indexNames) {
                IndexKey index = new IndexKey();
                index.setNodeSettings(this.nodeSettings);
                index.setClusterNodes(this.clusterNodes);
                index.setIndexId(newIndexname);
                CREATE_INDEX_CACHE.get(index);
            }
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("Elasticsearch checkIndexes Exception [%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
        }
    }

    /**
     * Read partial results from Elastic - convert Query to SearchRequestBuilder
     *
     * @see #getSubQueryResolveLimit()
     * @see #predicateToSortBuilder(List, QueryBuilder, Query, SearchRequestBuilder, String[])
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> PaginatedResult<T> readPartial(Query<T> query, long offset, int limit) {
        LOGGER.debug("Elasticsearch PaginatedResult readPartial query.getPredicate() [{}]", query.getPredicate());

        List<T> items = new ArrayList<>();
        TransportClient client = openConnection();
        if (client == null || !isAlive(client)) {
            LOGGER.info("readPartial could not openConnection / not Alive");
            return new PaginatedResult<>(offset, limit, 0, items);
        }

        Set<UUID> typeIds = new HashSet<>();

        if (!query.isFromAll()) {
            typeIds = query.getConcreteTypeIds(this);
            if (typeIds.size() == 0) {
                return new PaginatedResult<>(offset, limit, 0, items);
            }
        }

        if (query.getGroup() != null && typeIds.size() == 0) {
            // should limit by the type
            LOGGER.debug("Elasticsearch PaginatedResult readPartial the call is to limit by from() but did not load typeIds! [{}]", query.getGroup());
        }
        String[] typeIdStrings = typeIds.size() == 0
                ? new String[]{}
                : typeIds.stream().map(UUID::toString).toArray(String[]::new);

        List<String> indexNames = new ArrayList<>();
        for (UUID u : typeIds) {
            indexNames.add(getIndexName() + u.toString().replaceAll("-", ""));
        }
        String[] indexIdStrings = indexNames.toArray(new String[0]);
        checkIndexes(indexIdStrings);

        SearchResponse response;
        QueryBuilder qb = predicateToQueryBuilder(query.getPredicate(), query);
        SearchRequestBuilder srb;

        if (typeIds.size() > 0) {
            srb = client.prepareSearch(indexIdStrings)
                    .setFetchSource(!query.isReferenceOnly())
                    .setTimeout(query.getTimeout() != null && query.getTimeout() > 0 ? TimeValue.timeValueMillis(query.getTimeout().longValue()) : TimeValue.timeValueMillis(this.searchTimeout));
            srb.setTypes(typeIdStrings);
        } else {
            srb = client.prepareSearch(getIndexName() + "*")
                    .setFetchSource(!query.isReferenceOnly())
                    .setTimeout(query.getTimeout() != null && query.getTimeout() > 0 ? TimeValue.timeValueMillis(query.getTimeout().longValue()) : TimeValue.timeValueMillis(this.searchTimeout));
        }

        srb.setQuery(qb)
                .setFrom((int) offset)
                .setSize(limit);
        for (SortBuilder sb : predicateToSortBuilder(query.getSorters(), qb, query, srb, typeIdStrings)) {
            srb.addSort(sb);
        }

        LOGGER.debug("Elasticsearch srb index [{}] typeIds [{}] - [{}]", (indexIdStrings.length == 0 ? getIndexName() + "*" : indexIdStrings), (typeIdStrings.length == 0 ? "" : typeIdStrings), srb.toString());
        response = srb.execute().actionGet();
        SearchHits hits = response.getHits();

        for (SearchHit hit : hits.getHits()) {
            items.add(createSavedObjectWithHit(hit, query));
        }

        LOGGER.debug("Elasticsearch PaginatedResult readPartial hits [{} of {} totalHits]", items.size(), hits.getTotalHits());

        return new PaginatedResult<>(offset, limit, hits.getTotalHits(), items);
    }

    /**
     * Take the saved object and convert to objectState and swap it
     */
    private <T> T createSavedObjectWithHit(SearchHit hit, Query<T> query) {
        T object = createSavedObject(hit.getType(), hit.getId(), query);

        State objectState = State.getInstance(object);

        if (!objectState.isReferenceOnly()) {
            objectState.setValues(hit.getSource());
        }

        return swapObjectType(query, object);
    }

    /**
     * Check special fields for Elastic - sorting
     */
    private final Map<Query.MappedKey, String> specialSortFields; {
        Map<Query.MappedKey, String> m = new HashMap<>();
        m.put(Query.MappedKey.ID, "_ids");
        m.put(Query.MappedKey.TYPE, TYPE_ID_FIELD);
        m.put(Query.MappedKey.ANY, ALL_FIELD);
        specialSortFields = m;
    }

    private final Map<Query.MappedKey, String> specialRangeFields; {
        Map<Query.MappedKey, String> m = new HashMap<>();
        m.put(Query.MappedKey.ID, "_ids");
        m.put(Query.MappedKey.TYPE, TYPE_ID_FIELD);
        m.put(Query.MappedKey.ANY, ALL_FIELD);
        specialRangeFields = m;
    }

    /**
     * Check special fields for Elastic - non sorting
     */
    private final Map<Query.MappedKey, String> specialFields; {
        Map<Query.MappedKey, String> m = new HashMap<>();
        m.put(Query.MappedKey.ID, ID_FIELD);
        m.put(Query.MappedKey.TYPE, TYPE_ID_FIELD);
        m.put(Query.MappedKey.ANY, ALL_FIELD);
        specialFields = m;
    }

    /**
     * Denormalize the key or return Query.NoFieldException
     *
     * @return Query.MappedKey {@code null} can be returned
     * @throws Query.NoFieldException No field matches this key
     */
    private Query.MappedKey mapFullyDenormalizedKey(Query<?> query, String key) {
        if (key.equals("_ids")) {
            key = "_id";
        }
        Query.MappedKey mappedKey = query.mapDenormalizedKey(getEnvironment(), key);
        if (mappedKey == null) {
            return null;
        }
        if (isReference(key, query)) {
            return mappedKey;
        } else if (mappedKey.hasSubQuery()) {
            throw new Query.NoFieldException(query.getGroup(), key);
        }
        return mappedKey;
    }

    /**
     * Look for field name recursively in Elastic
     * Return true if found it, else false
     *
     * @see #checkElasticMappingField
     */
    private boolean findElasticMap(Map<String, Object> properties, List<String> key, int length) {
        if (properties != null) {
            if (length < key.size()) {
                /* check fields separate */
                if (properties.get("fields") != null) {
                    if (properties.get("fields") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fields = (Map<String, Object>) properties.get("fields");
                        if (fields.get(key.get(length)) != null) {
                            if (length == key.size() - 1) {
                                return true;
                            }
                        }
                    }
                }
                if (properties.get("properties") != null) {
                    if (properties.get("properties") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> p = (Map<String, Object>) properties.get("properties");
                        return findElasticMap(p, key, length);
                    }
                } else if (properties.get(key.get(length)) != null) {
                    if (length == key.size() - 1) {
                        return true;
                    }
                    if (properties.get(key.get(length)) instanceof Map) {
                        //noinspection unchecked
                        return findElasticMap((Map<String, Object>) properties.get(key.get(length)), key, length + 1);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check types for field in Elastic mapping. Note that the field must be inserted not null to show up.
     *
     * @return true if the field exists
     * @throws IOException the Elastic could fail on getting _mapping on the type
     * @see #findElasticMap
     */
    private boolean checkElasticMappingField(String[] typeIds, String field) throws IOException {

        if (typeIds != null && typeIds.length > 0) {
            GetMappingsResponse response = client.admin().indices()
                    .prepareGetMappings(indexName + "*")
                    .setTypes(typeIds)
                    .execute().actionGet();

            if (response != null && response.getMappings() != null) {
                for (String typeId : typeIds) {
                    LOGGER.debug("Checking mapping for {}, {}", indexName + typeId.replaceAll("-", ""), typeId);
                    if (response.getMappings().get(indexName + typeId.replaceAll("-", "")) != null
                            && response.getMappings().get(indexName + typeId.replaceAll("-", "")).get(typeId) != null) {
                        Map<String, Object> source = response.getMappings().get(indexName + typeId.replaceAll("-", "")).get(typeId).sourceAsMap();
                        if (source.get("properties") instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> properties = (Map<String, Object>) source.get("properties");
                            List<String> items = Arrays.asList(field.split("\\."));
                            if (!findElasticMap(properties, items, 0)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Interrogate the field and convert to Elasticsearch - Number is problematic
     */
    private String getFieldType(String key, Query<?> query) {
        if (key.equals(UID_FIELD) || key.equals(ID_FIELD) || key.equals(TYPE_ID_FIELD)) {
            return "keyword";
        }
        if (key.endsWith("." + RAW_FIELD)) {
            key = key.replaceAll("\\." + RAW_FIELD, "");
        }
        if (key.endsWith("." + LOCATION_FIELD)) {
            key = key.replaceAll("\\." + LOCATION_FIELD, "");
        }
        if (key.endsWith("." + REGION_FIELD)) {
            key = key.replaceAll("\\." + REGION_FIELD, "");
        }
        Query.MappedKey mappedKey = mapFullyDenormalizedKey(query, key);
        String elasticField = specialSortFields.get(mappedKey);

        if (elasticField == null) {
            if (mappedKey != null) {
                String internalType = mappedKey.getInternalType();
                if (internalType != null) {
                    if (ObjectField.DATE_TYPE.equals(internalType)) {
                        return "long";
                    } else if (ObjectField.TEXT_TYPE.equals(internalType)) {
                        return "keyword";
                    } else if (ObjectField.BOOLEAN_TYPE.equals(internalType)) {
                        return "boolean";
                    } else if (ObjectField.NUMBER_TYPE.equals(internalType)) {
                        //NUMBER_TYPE could be several Elasticsearch types - long, integer, short, byte, double, float
                        // return double since it is a superset
                        return "double";
                    } else if (ObjectField.REGION_TYPE.equals(internalType)) {
                        return "geo_shape";
                    } else if (ObjectField.LOCATION_TYPE.equals(internalType)) {
                        return "geo_point";
                    } else if (ObjectField.UUID_TYPE.equals(internalType)) {
                        return "keyword";
                    }
                }
            }
        }
        return null;
    }

    /**
     * Build a list of SortBuilder sorters based on Elastic
     */
    private List<SortBuilder> predicateToSortBuilder(List<Sorter> sorters, QueryBuilder orig, Query<?> query, SearchRequestBuilder srb, String[] typeIds) {
        List<SortBuilder> list = new ArrayList<>();
        if (sorters == null || sorters.size() == 0) {
            list.add(new ScoreSortBuilder());
            list.add(new FieldSortBuilder(TYPE_ID_FIELD).order(ASC).unmappedType("keyword"));
        } else {
            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();

            for (Sorter sorter : sorters) {
                String operator = sorter.getOperator();
                if (Sorter.ASCENDING_OPERATOR.equals(operator) || Sorter.DESCENDING_OPERATOR.equals(operator)) {
                    boolean isAscending = Sorter.ASCENDING_OPERATOR.equals(operator);
                    String queryKey = (String) sorter.getOptions().get(0);

                    String elasticField = convertAscendingElasticField(queryKey, query, typeIds);

                    if (elasticField == null) {
                        throw new UnsupportedIndexException(this, queryKey);
                    }
                    String unmappedTypeString = getFieldType(queryKey, query);
                    FieldSortBuilder fs = new FieldSortBuilder(elasticField).order(isAscending ? ASC : DESC);
                    if (unmappedTypeString != null) {
                        fs.unmappedType(unmappedTypeString);
                    }
                    list.add(fs);
                } else if (Sorter.OLDEST_OPERATOR.equals(operator) || Sorter.NEWEST_OPERATOR.equals(operator)) {
                    if (sorter.getOptions().size() < 2) {
                        throw new IllegalArgumentException(operator + " requires Date field");
                    }
                    boolean isOldest = Sorter.OLDEST_OPERATOR.equals(operator);
                    String queryKey = (String) sorter.getOptions().get(1);
                    Query.MappedKey mappedKey = mapFullyDenormalizedKey(query, queryKey);
                    String elasticField = specialSortFields.get(mappedKey);

                    if (elasticField == null) {
                        if (mappedKey != null) {
                            String internalType = mappedKey.getInternalType();
                            if (internalType != null) {
                                // only date can boost this way
                                if ("date".equals(internalType)) {
                                    elasticField = queryKey;
                                } else {
                                    throw new IllegalArgumentException();
                                }
                            }
                        }
                    }

                    if (elasticField == null) {
                        throw new UnsupportedIndexException(this, queryKey);
                    }

                    float boost = ObjectUtils.to(float.class, sorter.getOptions().get(0));
                    if (boost == 0) {
                        boost = 1.0f;
                    }
                    boost = .1f * boost;

                    long scale = MILLISECONDS_IN_5YEAR; // 5 years scaling
                    if (!isOldest) {
                        filterFunctionBuilders.add(
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.exponentialDecayFunction(elasticField, new Date().getTime(), scale, 0, .1).setWeight(boost))
                        );
                        // Solr: recip(x,m,a,b) implementing a/(m*x+b)
                        // boostFunctionBuilder.append(String.format("{!boost b=recip(ms(NOW/HOUR,%s),3.16e-11,%s,%s)}", elasticField, boost, boost));
                    } else {
                        filterFunctionBuilders.add(
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.exponentialDecayFunction(elasticField, DateUtils.addYears(new java.util.Date(), -5).getTime(), scale, 0, .1).setWeight(boost))
                        );
                        // Solr: linear(x,2,4) returns 2*x+4
                        // boostFunctionBuilder.append(String.format("{!boost b=linear(ms(NOW/HOUR,%s),3.16e-11,%s)}", elasticField, boost));
                    }

                } else if (Sorter.FARTHEST_OPERATOR.equals(operator) || Sorter.CLOSEST_OPERATOR.equals(operator)) {
                    if (sorter.getOptions().size() < 2) {
                        throw new IllegalArgumentException(operator + " requires Location");
                    }
                    boolean isClosest = Sorter.CLOSEST_OPERATOR.equals(operator);
                    String queryKey = (String) sorter.getOptions().get(0);

                    String elasticField = convertFarthestElasticField(queryKey, query, typeIds);

                    if (!(sorter.getOptions().get(1) instanceof Location)) {
                        throw new IllegalArgumentException(operator + " requires Location");
                    }
                    Location sort = (Location) sorter.getOptions().get(1);
                    list.add(new GeoDistanceSortBuilder(elasticField, new GeoPoint(sort.getX(), sort.getY()))
                            .order(isClosest ? SortOrder.ASC : SortOrder.DESC));
                } else if (Sorter.RELEVANT_OPERATOR.equals(operator)) {
                    Predicate sortPredicate;
                    Object predicateObject = sorter.getOptions().get(1);
                    Object boostObject = sorter.getOptions().get(0);
                    String boostStr = boostObject.toString();
                    Float boost = Float.valueOf(boostStr);
                    if (predicateObject instanceof Predicate) {
                        sortPredicate = (Predicate) predicateObject;
                        filterFunctionBuilders.add(
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        predicateToQueryBuilder(sortPredicate, query),
                                        weightFactorFunction(boost)));
                    } else {
                        list.add(new ScoreSortBuilder());
                    }
                } else {
                    throw new UnsupportedOperationException(operator + " not supported");
                }
            }
            if (filterFunctionBuilders.size() > 0) {
                list.add(new ScoreSortBuilder());
                FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
                for (int i = 0; i < filterFunctionBuilders.size(); i++) {
                    functions[i] = filterFunctionBuilders.get(i);
                }
                orig = QueryBuilders.functionScoreQuery(orig, functions)
                        .boostMode(CombineFunction.MULTIPLY)
                        .boost(1.0f)
                        .maxBoost(1000.0f);
                srb.setQuery(orig);
            }
        }
        return list;

    }

    /**
     * Handy to see if the object is a reference and not Embedded
     * Note: {@code ((Query.StandardMappedKey) mappedKey).hasSubQuery()} does not check field independently for _ref and not embedded
     *
     * @return if the queryKey is a reference and not an normal Record Object or Embedded object
     */
    private boolean isReference(String queryKey, Query<?> query) {
        try {
            Query.MappedKey mappedKey = query.mapDenormalizedKey(getEnvironment(), queryKey);
            if (mappedKey != null) {
                if (mappedKey.hasSubQuery()) {
                    return true;
                }

                if (mappedKey.getField() != null) {
                    if (mappedKey.getField().getState() != null && mappedKey.getField().getState() instanceof Map) {
                        Map<String, Object> itemMap = mappedKey.getField().getState().getSimpleValues();
                        if (itemMap != null) {
                            if (itemMap.get("valueTypes") != null && itemMap.get("valueTypes") instanceof List) {
                                if (itemMap.get("isEmbedded") != null && !((Boolean) itemMap.get("isEmbedded"))) {
                                    List l = (List) itemMap.get("valueTypes");
                                    if (l.size() == 1 && l.get(0) != null && l.get(0) instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> o = (Map<String, Object>) l.get(0);
                                        if (o.get(StateSerializer.REFERENCE_KEY) != null) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * For ascending sort take queryKey and return the Elastic equivalent for text, location and region
     *
     * @throws IllegalArgumentException  the argument is illegal
     * @throws UnsupportedIndexException the mapping not setup properly
     */
    private <T> String convertAscendingElasticField(String queryKey, Query<T> query, String[] typeIds) {
        String elasticField;

        Query.MappedKey mappedKey = mapFullyDenormalizedKey(query, queryKey);
        if (mappedKey != null && mappedKey.hasSubQuery()) {
            throw new IllegalArgumentException(queryKey + " cannot sort subQuery (create denormalized fields) on Ascending/Descending");
        }

        elasticField = specialSortFields.get(mappedKey);

        /* skip for special */
        if (elasticField == null) {
            queryKey = mappedKey.getIndexKey(null);
            String internalType = mappedKey.getInternalType();
            if (internalType != null) {
                if ("text".equals(internalType) || "uuid".equals(internalType)) {
                    elasticField = addRaw(queryKey);
                } else if ("location".equals(internalType)) {
                    elasticField = queryKey + "." + LOCATION_FIELD;
                    // not sure what to do with lat,long and sort?
                    throw new IllegalArgumentException(elasticField + " cannot sort Location on Ascending/Descending");
                } else if ("region".equals(internalType)) {
                    elasticField = queryKey + "." + REGION_FIELD;
                    throw new IllegalArgumentException(elasticField + " cannot sort GeoJSON in Elastic Search");
                }
            }
            if (elasticField == null) {
                elasticField = queryKey;
            }
        }
        return elasticField;
    }

    /**
     * For Farthest / Nearest sort get the elasticField
     */
    private <T> String convertFarthestElasticField(String queryKey, Query<T> query, String[] typeIds) {
        String elasticField;

        Query.MappedKey mappedKey = mapFullyDenormalizedKey(query, queryKey);

        if (mappedKey != null && mappedKey.hasSubQuery()) {
            throw new IllegalArgumentException(queryKey + " cannot sort subQuery (create denormalized fields) on Nearest/Farthest");
        }

        elasticField = specialSortFields.get(mappedKey);
        if (elasticField == null) {
            if (mappedKey != null) {
                elasticField = mappedKey.getIndexKey(null);
                String internalType = mappedKey.getInternalType();
                if (internalType != null) {
                    if ("number".equals(internalType)) {
                        throw new IllegalArgumentException(elasticField + " cannot sort Number Closest/Farthest");
                    }
                    if ("location".equals(internalType)) {
                        elasticField = elasticField + "." + LOCATION_FIELD;
                    }
                    if ("region".equals(internalType)) {
                        throw new IllegalArgumentException(elasticField + " cannot sort GeoJSON Closest/Farthest");
                    }
                    if ("uuid".equals(internalType)) {
                        throw new IllegalArgumentException(elasticField + " cannot sort UUID Closest/Farthest");
                    }
                    if ("text".equals(internalType)) {
                        throw new IllegalArgumentException(elasticField + " cannot sort Location on text Closest/Farthest");
                    }
                }
            }
            if (elasticField == null) {
                elasticField = queryKey;
            }
        }

        return elasticField;
    }

    /**
     * A reference is difficult to join in Elastic. This returns a list of Ids so you can join on next level of reference
     * In reality it is a join when you have a subQuery.
     *
     * @throws IllegalArgumentException the argument is illegal
     */
    private <T> List<String> referenceSwitcher(String key, Query<T> query) {
        String[] keyArr = key.split("/");
        List<String> allids = new ArrayList<>();
        List<?> list = new ArrayList<T>();
        // Go until last - since the user might want something besides != missing...
        if (keyArr.length > 0) {
            for (int i = 0; i < keyArr.length - 1; i++) {
                if (isReference(keyArr[i], query)) {
                    PaginatedResult<?> result;
                    if (allids.size() == 0) {
                        result = Query.from(query.getObjectClass()).where(keyArr[i] + " != missing").select(0, SUBQUERY_MAX_ROWS);
                    } else {
                        result = Query.from(query.getObjectClass()).where(keyArr[i] + " != missing").and("_id contains ?", allids).select(0, SUBQUERY_MAX_ROWS);
                    }

                    int count = (int) result.getCount();
                    list = result.getItems();
                    if (list.size() > (SUBQUERY_MAX_ROWS - 1)) {
                        LOGGER.warn("reference join in Elasticsearch is > {} which will limit results", (SUBQUERY_MAX_ROWS - 1));
                        throw new IllegalArgumentException(key + " / joins > " + (SUBQUERY_MAX_ROWS - 1) + " not allowed");
                    }
                    allids = new ArrayList<>();
                    for (int j = 0; j < list.size(); j++) {
                        if (list.get(j) instanceof Record) {
                            Map<String, Object> itemMap = ((Record) list.get(j)).getState().getSimpleValues(false);
                            if (itemMap.get(keyArr[i]) instanceof Map && itemMap.get(keyArr[i]) != null) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> o = (Map<String, Object>) itemMap.get(keyArr[i]);
                                if (o.get(StateSerializer.REFERENCE_KEY) != null) {
                                    allids.add((String) o.get(StateSerializer.REFERENCE_KEY));
                                }
                            } else if (itemMap.get(keyArr[i]) instanceof List && itemMap.get(keyArr[i]) != null) {
                                @SuppressWarnings("unchecked")
                                List<Object> subList = (List<Object>) itemMap.get(keyArr[i]);
                                for (Object sub : subList) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> s = (Map<String, Object>) sub;
                                    if (s.get(StateSerializer.REFERENCE_KEY) != null) {
                                        allids.add((String) s.get(StateSerializer.REFERENCE_KEY));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (list.size() > 0) {
            return allids;
        } else {
            return null;
        }
    }

    /**
     * For count we don't need to return any rows
     */
    @Override
    public long readCount(Query<?> query) {
        return readPartial(query, 0L, 0).getCount();
    }

    /**
     * Override to loop through at INITIAL_FETCH_SIZE at a time
     *
     * @param query Can't be {@code null}.
     * @see #readPartial(Query, long, int)
     * @see #getSubQueryResolveLimit()
     */
    @Override
    public <T> List<T> readAll(Query<T> query) {
        List<T> listFinal = new ArrayList<>();
        long row = 0L;
        boolean done = false;
        while (!done) {
            List<T> partial = readPartial(query, row, INITIAL_FETCH_SIZE).getItems();
            // most queries will be handled immediately
            if (partial.size() < INITIAL_FETCH_SIZE && row == 0) {
                return partial;
            }
            if (partial != null && partial.size() > 0) {
                listFinal.addAll(partial);
                row = row + partial.size();
            } else {
                done = true;
            }
        }
        return listFinal;
    }

    /**
     * Take circles and polygons and build a new GeoJson that works with Elastic
     */
    public String getGeoJson(List<Region.Circle> circles, Region.MultiPolygon polygons) {
        List<Map<String, Object>> features = new ArrayList<>();

        Map<String, Object> featureCollection = new HashMap<>();
        featureCollection.put("type", "geometrycollection");
        featureCollection.put("geometries", features);

        if (circles != null && circles.size() > 0) {

            for (Region.Circle circle : circles) {
                Map<String, Object> geometry = new HashMap<>();
                geometry.put("type", "circle");
                geometry.put("coordinates", circle.getGeoJsonArray().get(0)); // required for Elasticsearch
                geometry.put("radius", Math.ceil(circle.getRadius()) + "m");

                features.add(geometry);
            }
        }

        if (polygons != null && polygons.size() > 0) {
            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "multipolygon");
            geometry.put("coordinates", polygons);

            features.add(geometry);
        }

        return ObjectUtils.toJson(featureCollection);
    }

    /**
     * add Raw for fields that are not _ids, _id, _type
     */
    private static String addRaw(String query) {
        if ("_ids".equals(query) || "_id".equals(query) || "_type".equals(query)) {
            return query;
        } else {
            if (query.endsWith("." + RAW_FIELD)) {
                return query;
            } else {
                return query + "." + RAW_FIELD;
            }
        }

    }

    /**
     * Get the QueryBuilder equalsany for each v. Splits out "Location" and "Region"
     */
    private QueryBuilder geoLocationQuery(String simpleKey, String dotKey, String key, Query<?> query, Object v, ShapeRelation sr) {

        String geoType = null;
        if (v == null) {
            return QueryBuilders.boolQuery().mustNot(QueryBuilders.matchAllQuery());
        } else if (v instanceof String || isUUID(v)) {
            Query.MappedKey mappedKey = mapFullyDenormalizedKey(query, key);
            String checkField = specialFields.get(mappedKey);
            if (checkField == null) {
                if (simpleKey != null) {
                    key = addRaw(key);
                } else {
                    if (isUUID(v) && isReference(dotKey, query)) {
                        return QueryBuilders.termQuery(mappedKey.getField().getInternalName() + "._ref", v);
                    } else {
                        String internalType = mappedKey.getInternalType();
                        if (!"number".equals(internalType)) {
                            key = addRaw(key);
                        }
                    }
                }
            }
        } else {
            Query.MappedKey mappedKey = mapFullyDenormalizedKey(query, key);
            String checkField = specialFields.get(mappedKey);
            if (checkField == null) {
                String internalType = mappedKey.getInternalType();
                if (v instanceof Boolean) {
                    if (internalType != null && "location".equals(internalType)) {
                        throw new IllegalArgumentException(key + " boolean cannot be location");
                    }
                    if (internalType != null && "region".equals(internalType)) {
                        throw new IllegalArgumentException(key + " boolean cannot be region");
                    }
                } else if (internalType != null && "region".equals(internalType)) {
                    geoType = "region";
                } else if (internalType != null && "location".equals(internalType)) {
                    geoType = "location";
                }
            }
        }

        if (geoType != null && "location".equals(geoType)) {
            if (v instanceof Location) {
                return QueryBuilders.boolQuery().must(QueryBuilders.termQuery(key + ".x", ((Location) v).getX()))
                        .must(QueryBuilders.termQuery(key + ".y", ((Location) v).getY()));
            } else if (v instanceof Region) {
                return QueryBuilders.geoDistanceQuery(key + "." + LOCATION_FIELD).point(((Region) v).getX(), ((Region) v).getY())
                        .distance(Region.degreesToKilometers(((Region) v).getRadius()), DistanceUnit.KILOMETERS);
            }
        } else if (geoType != null && "region".equals(geoType)) {
            if (v instanceof Location) {
                return QueryBuilders.boolQuery().must(geoShape(key + "." + REGION_FIELD, ((Location) v).getX(), ((Location) v).getY()));

            } else if (v instanceof Region) {
                // required to fix array issue on Circles and capitals
                Region region = (Region) v;
                String geoJson = getGeoJson(region.getCircles(), region.getPolygons());

                String shapeJson = "{" + "\"shape\":" + geoJson + ", \"relation\": \"" + sr + "\"}";
                String nameJson = "{" + "\"" + key + "." + REGION_FIELD + "\":" + shapeJson + "}";
                String json = "{" + "\"geo_shape\":" + nameJson + "}";
                return QueryBuilders.boolQuery().must(QueryBuilders.wrapperQuery(json));
            }
        }

        return QueryBuilders.termQuery(key, v);
    }

    /**
     * Get QueryBuilder for geoShape for intersects
     *
     * @see #predicateToQueryBuilder
     */
    private GeoShapeQueryBuilder geoShapeIntersects(String key, double x, double y) {
        try {
            return QueryBuilders
                    .geoShapeQuery(key, ShapeBuilders.newPoint(new Coordinate(x, y))).relation(ShapeRelation.INTERSECTS);
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("geoShapeIntersects threw Exception [%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
        }
        return null;
    }

    /**
     * Get QueryBuilder for geoShape for contains
     *
     * @see #predicateToQueryBuilder
     */
    private GeoShapeQueryBuilder geoShape(String key, double x, double y) {
        try {
            return QueryBuilders
                    .geoShapeQuery(key, ShapeBuilders.newPoint(new Coordinate(x, y))).relation(ShapeRelation.CONTAINS);
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("geoShape threw Exception [%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
        }
        return null;
    }

    /**
     * Get QueryBuilder for Circle for contains
     * Currently use geoShape higher level
     */
    private GeoShapeQueryBuilder geoCircle(String key, double x, double y, double r) {
        try {
            return QueryBuilders
                    .geoShapeQuery(key, ShapeBuilders.newCircleBuilder().center(x, y).radius(r, DistanceUnit.KILOMETERS)).relation(ShapeRelation.CONTAINS);
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("geoCircle threw Exception [%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
        }
        return null;
    }

    /**
     * For contains, add * for within strings
     */
    private static Object containsWildcard(String operator, Object value) {
        if (operator.equals(PredicateParser.CONTAINS_OPERATOR) && (value instanceof String)) {
            return "*" + value + "*";
        }
        return value;
    }

    /**
     * For Matches, add ".match" to the query
     */
    private static String matchesAnalyzer(String operator, String key) {
        if (key.endsWith("." + RAW_FIELD) || key.equals("_any") || key.equals(ALL_FIELD)) {
            return key;
        }
        if (operator.equals(PredicateParser.MATCHES_ALL_OPERATOR) || operator.equals(PredicateParser.MATCHES_ANY_OPERATOR)) {
            return key + "." + MATCH_FIELD;
        }
        return key;
    }

    /**
     * This is the main method for querying Elastic. Converts predicate and query into QueryBuilder
     */
    private QueryBuilder predicateToQueryBuilder(Predicate predicate, Query<?> query) {
        if (predicate == null) {
            return QueryBuilders.matchAllQuery();
        }
        LOGGER.debug("predicate: [{}]", predicate.toString());
        if (predicate instanceof CompoundPredicate) {
            CompoundPredicate compound = (CompoundPredicate) predicate;
            List<Predicate> children = compound.getChildren();

            switch (compound.getOperator()) {
                case PredicateParser.AND_OPERATOR:
                    return combine(compound.getOperator(), children, BoolQueryBuilder::must, (predicate1) -> predicateToQueryBuilder(predicate1, query));

                case PredicateParser.OR_OPERATOR:
                    return combine(compound.getOperator(), children, BoolQueryBuilder::should, (predicate1) -> predicateToQueryBuilder(predicate1, query));

                case PredicateParser.NOT_OPERATOR:
                    return combine(compound.getOperator(), children, BoolQueryBuilder::mustNot, (predicate1) -> predicateToQueryBuilder(predicate1, query));

                default:
                    break;
            }

        } else if (predicate instanceof ComparisonPredicate) {
            ComparisonPredicate comparison = (ComparisonPredicate) predicate;
            //String pKey = "_any".equals(comparison.getKey()) ? ALL_FIELD : comparison.getKey();
            String operator = comparison.getOperator();

            String queryKey = comparison.getKey();
            Query.MappedKey mappedKey = query.mapDenormalizedKey(getEnvironment(), queryKey);
            String elasticField;

            if (operator.equals(PredicateParser.EQUALS_ANY_OPERATOR)
                || operator.equals(PredicateParser.NOT_EQUALS_ALL_OPERATOR)) {
                elasticField = specialFields.get(mappedKey);
            } else {
                elasticField = specialRangeFields.get(mappedKey);
            }

            if (elasticField == null) {
                String internalType = mappedKey.getInternalType();
                if (internalType != null) {
                    elasticField = mappedKey.getIndexKey(null); // whole string
                }
            }

            if (elasticField == null) {
                throw new UnsupportedIndexException(this, queryKey);
            }

            String key = elasticField;

            List<Object> values = comparison.getValues();

            String simpleKey = null;

            int slash = queryKey.lastIndexOf('/');
            String pKey = queryKey;

            // this specific one needs to be reduced
            if (queryKey.indexOf('/') != -1) {
                //Query.MappedKey mappedKey = mapFullyDenormalizedKey(query, queryKey);
                if (mappedKey != null && mappedKey.hasSubQuery()) {
                    // Elasticsearch like Solr does not support joins in 5.2. Might be memory issue and slow!
                    // to do this requires query, take results and send to other query. Sample tests do this.

                    List<String> ids = referenceSwitcher(queryKey, query);
                    if (ids != null && ids.size() > 0) {
                        pKey = pKey.substring(slash + 1);
                        ComparisonPredicate nComparison = new ComparisonPredicate(comparison.getOperator(),
                                comparison.isIgnoreCase(), pKey, comparison.getValues());
                        Query n = Query.fromAll().where(nComparison).and("_id contains ?", ids);
                        LOGGER.debug("returning subQuery ids [{}]", ids.size());
                        return predicateToQueryBuilder(n.getPredicate(), query);
                    }
                } else {
                    // fields().size not the same as array in keys "/"
                    List<String> keyArr = Arrays.asList(queryKey.split("/"));
                    if (mappedKey.getFields().size() != keyArr.size()) {
                        simpleKey = mappedKey.getField().getInternalName();
                    }
                }
            }

            String internalType = null;

            switch (operator) {
                case PredicateParser.EQUALS_ANY_OPERATOR:
                case PredicateParser.NOT_EQUALS_ALL_OPERATOR:

                    String finalSimpleKey = simpleKey;
                    if (operator.equals(PredicateParser.EQUALS_ANY_OPERATOR)) {
                        return combine(operator, values, BoolQueryBuilder::should, v ->
                                v == null ? QueryBuilders.matchAllQuery()
                                : Query.MISSING_VALUE.equals(v) ? QueryBuilders.existsQuery(key)
                                    : geoLocationQuery(finalSimpleKey, key, key, query, v, ShapeRelation.WITHIN));
                    } else {
                        return combine(operator, values, BoolQueryBuilder::mustNot, v ->
                                v == null ? QueryBuilders.matchAllQuery()
                                : Query.MISSING_VALUE.equals(v) ? QueryBuilders.existsQuery(key)
                                    : geoLocationQuery(finalSimpleKey, key, key, query, v, ShapeRelation.WITHIN));
                    }

                case PredicateParser.LESS_THAN_OPERATOR :
                    mappedKey = mapFullyDenormalizedKey(query, key);
                    String checkField = specialRangeFields.get(mappedKey);
                    if (checkField == null) {
                        internalType = mappedKey.getInternalType();
                    }

                    for (Object v : values) {
                        if (v instanceof Boolean) {
                            throw new IllegalArgumentException(operator + " cannot be boolean");
                        }
                        if (v != null && Query.MISSING_VALUE.equals(v)) {
                            throw new IllegalArgumentException(operator + " missing not allowed");
                        }
                        if (v != null && (internalType != null && "location".equals(internalType))) {
                            throw new IllegalArgumentException(operator + " cannot be location");
                        }
                        if (v != null && (internalType != null && "region".equals(internalType))) {
                            throw new IllegalArgumentException(operator + " cannot be region");
                        }
                    }

                    return combine(operator, values, BoolQueryBuilder::must, v ->
                                    v == null ? QueryBuilders.matchAllQuery()
                                    : (isUUID(v) ? QueryBuilders.rangeQuery(addRaw(key)).lt(v)
                                    : (v instanceof Location ? QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(key + ".x").lt(((Location) v).getX()))
                                        .must(QueryBuilders.rangeQuery(key + ".y").lt(((Location) v).getY()))
                                    : QueryBuilders.rangeQuery(key).lt(v))));

                case PredicateParser.LESS_THAN_OR_EQUALS_OPERATOR :
                    mappedKey = mapFullyDenormalizedKey(query, key);
                    checkField = specialRangeFields.get(mappedKey);
                    if (checkField == null) {
                        internalType = mappedKey.getInternalType();
                    }

                    for (Object v : values) {
                        if (v instanceof Boolean) {
                            throw new IllegalArgumentException(operator + " cannot be boolean");
                        }
                        if (v != null && Query.MISSING_VALUE.equals(v)) {
                            throw new IllegalArgumentException(operator + " missing not allowed");
                        }
                        if (v != null && internalType != null && "location".equals(internalType)) {
                            throw new IllegalArgumentException(operator + " cannot be location");
                        }
                        if (v != null && internalType != null && "region".equals(internalType)) {
                            throw new IllegalArgumentException(operator + " cannot be region");
                        }
                    }

                    return combine(operator, values, BoolQueryBuilder::must, v ->
                            v == null ? QueryBuilders.matchAllQuery()
                            : (isUUID(v) ? QueryBuilders.rangeQuery(addRaw(key)).lte(v)
                            : (v instanceof Location
                                    ? QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(key + ".x").lte(((Location) v).getX()))
                                    .must(QueryBuilders.rangeQuery(key + ".y").lte(((Location) v).getY()))
                                    : QueryBuilders.rangeQuery(key).lte(v))));

                case PredicateParser.GREATER_THAN_OPERATOR :
                    mappedKey = mapFullyDenormalizedKey(query, key);
                    checkField = specialRangeFields.get(mappedKey);
                    if (checkField == null) {
                        internalType = mappedKey.getInternalType();
                    }
                    for (Object v : values) {
                        if (v instanceof Boolean) {
                            throw new IllegalArgumentException(operator + " cannot be boolean");
                        }
                        if (v != null && Query.MISSING_VALUE.equals(v)) {
                            throw new IllegalArgumentException(operator + " missing not allowed");
                        }
                        if (v != null && (internalType != null && "location".equals(internalType))) {
                            throw new IllegalArgumentException(operator + " cannot be location");
                        }
                        if (v != null && (internalType != null && "region".equals(internalType))) {
                            throw new IllegalArgumentException(operator + " cannot be region");
                        }
                    }

                    return combine(operator, values, BoolQueryBuilder::must, v ->
                            v == null ? QueryBuilders.matchAllQuery()
                            : (isUUID(v) ? QueryBuilders.rangeQuery(addRaw(key)).gt(v)
                            : (v instanceof Location
                                    ? QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(key + ".x").gt(((Location) v).getX()))
                                    .must(QueryBuilders.rangeQuery(key + ".y").gt(((Location) v).getY()))
                                    : QueryBuilders.rangeQuery(key).gt(v))));

                case PredicateParser.GREATER_THAN_OR_EQUALS_OPERATOR :

                    mappedKey = mapFullyDenormalizedKey(query, key);
                    checkField = specialRangeFields.get(mappedKey);
                    if (checkField == null) {
                        internalType = mappedKey.getInternalType();
                    }
                    for (Object v : values) {
                        if (v != null && v instanceof Boolean) {
                            throw new IllegalArgumentException(operator + " cannot be boolean");
                        }
                        if (v != null && Query.MISSING_VALUE.equals(v)) {
                            throw new IllegalArgumentException(operator + " missing not allowed");
                        }
                        if (v != null && internalType != null && "location".equals(internalType)) {
                            throw new IllegalArgumentException(operator + " cannot be location");
                        }
                        if (v != null && internalType != null && "region".equals(internalType)) {
                            throw new IllegalArgumentException(key + " cannot be region");
                        }
                    }

                    return combine(operator, values, BoolQueryBuilder::must, v ->
                            v == null ? QueryBuilders.matchAllQuery()
                            : (isUUID(v) ? QueryBuilders.rangeQuery(addRaw(key)).gte(v)
                            : (v instanceof Location
                                    ? QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(key + ".x").gte(((Location) v).getX()))
                                    .must(QueryBuilders.rangeQuery(key + ".y").gte(((Location) v).getY()))
                                    : QueryBuilders.rangeQuery(key).gte(v))));

                case PredicateParser.STARTS_WITH_OPERATOR :
                    mappedKey = mapFullyDenormalizedKey(query, key);
                    checkField = specialRangeFields.get(mappedKey);
                    if (checkField == null) {
                        internalType = mappedKey.getInternalType();
                    }

                    for (Object v : values) {
                        if (internalType != null && "number".equals(internalType)) {
                            throw new IllegalArgumentException(operator + " number not allowed");
                        }
                        if (v != null && Query.MISSING_VALUE.equals(v)) {
                            throw new IllegalArgumentException(operator + " missing not allowed");
                        }
                        if (internalType != null && internalType.equals("uuid")) {
                            if (v == null || (v != null && v.equals(new UUID(0, 0)))) {
                                throw new IllegalArgumentException(operator + " UUID of null/0 not allowed");
                            }
                        }
                        if (v != null && v instanceof Location) {
                            throw new IllegalArgumentException(operator + " location not allowed");
                        }
                        if (v != null && v instanceof Region) {
                            throw new IllegalArgumentException(operator + " region not allowed");
                        }
                    }
                    return combine(operator, values, BoolQueryBuilder::should, v ->
                            v == null ? QueryBuilders.matchAllQuery()
                            : QueryBuilders.prefixQuery(key, String.valueOf(v)));

                case PredicateParser.CONTAINS_OPERATOR :
                case PredicateParser.MATCHES_ANY_OPERATOR :
                    // should = MATCHES_ANY
                    if (!"_any".equals(key) && !ALL_FIELD.equals(key)) {
                        mappedKey = mapFullyDenormalizedKey(query, key);
                        checkField = specialFields.get(mappedKey);
                        if (checkField == null) {
                            internalType = mappedKey.getInternalType();
                        }
                    }

                    for (Object v : values) {
                        if (internalType != null && "number".equals(internalType)) {
                            throw new IllegalArgumentException(operator + " number not allowed");
                        }
                        if (v != null && Query.MISSING_VALUE.equals(v)) {
                            throw new IllegalArgumentException(operator + " missing not allowed");
                        }
                        if (v != null && v instanceof Boolean) {
                            if (internalType != null && "region".equals(internalType)) {
                                throw new IllegalArgumentException(operator + " region with boolean not allowed");
                            }
                        }
                        if (internalType != null && internalType.equals("uuid")) {
                            if (v == null || (v != null && v.equals(new UUID(0, 0)))) {
                                throw new IllegalArgumentException(operator + " UUID of null/0 not allowed");
                            } else {
                                throw new IllegalArgumentException(operator + " UUID does not allow");
                            }
                        }
                        if (v != null && v instanceof Location) {
                            if ((internalType == null) || (internalType != null && "location".equals(internalType))) {
                                throw new IllegalArgumentException(operator + " location not allowed");
                            } else if (!"region".equals(internalType) && !"location".equals(internalType)) {
                                throw new IllegalArgumentException(operator + " location not allowed except for region/location");
                            }
                        }
                    }

                    String finalSimpleKey1 = simpleKey;
                    if (internalType != null && "region".equals(internalType)) {
                        return combine(operator, values, BoolQueryBuilder::should, v ->
                                v == null ? QueryBuilders.matchAllQuery()
                                : "*".equals(v)
                                ? QueryBuilders.matchAllQuery()
                                : (v instanceof Location
                                    ? QueryBuilders.boolQuery().must(geoShapeIntersects(key + "." + REGION_FIELD, ((Location) v).getX(), ((Location) v).getY()))
                                    : (v instanceof Region
                                        ? QueryBuilders.boolQuery().must(
                                                geoLocationQuery(finalSimpleKey1, key, key, query, v, ShapeRelation.CONTAINS))
                                        : QueryBuilders.queryStringQuery(String.valueOf(containsWildcard(operator, v))).field(matchesAnalyzer(operator, key)).field(key + ".*")))); //QueryBuilders.matchPhrasePrefixQuery(finalKey1, v))));
                    } else {
                        return combine(operator, values, BoolQueryBuilder::should, v ->
                                v == null ? QueryBuilders.matchAllQuery()
                                : "*".equals(v)
                                ? QueryBuilders.matchAllQuery()
                                : QueryBuilders.queryStringQuery(String.valueOf(containsWildcard(operator, v))).field(matchesAnalyzer(operator, key)).field(key + ".*")); //QueryBuilders.matchPhrasePrefixQuery(finalKey1, v));
                    }

                case PredicateParser.MATCHES_ALL_OPERATOR :

                    if (!"_any".equals(key) && !ALL_FIELD.equals(key)) {
                        mappedKey = mapFullyDenormalizedKey(query, key);
                        checkField = specialFields.get(mappedKey);
                        if (checkField == null) {
                            internalType = mappedKey.getInternalType();
                        }
                    }

                    for (Object v : values) {
                        if (v != null && Query.MISSING_VALUE.equals(v)) {
                            throw new IllegalArgumentException(operator + " missing not allowed");
                        }
                        if (v != null && v instanceof Boolean) {
                            if (internalType != null && "region".equals(internalType)) {
                                throw new IllegalArgumentException(operator + " region with boolean not allowed");
                            }
                        }
                        if (internalType != null && internalType.equals("uuid")) {
                            if (v == null || (v != null && v.equals(new UUID(0, 0)))) {
                                throw new IllegalArgumentException(operator + " UUID of null/0 not allowed");
                            } else {
                                throw new IllegalArgumentException(operator + " UUID does not allow");
                            }
                        }
                        if (v != null && v instanceof Location) {
                            if ((internalType == null) || (internalType != null && "location".equals(internalType))) {
                                throw new IllegalArgumentException(operator + " location not allowed");
                            } else if (!"region".equals(internalType) && !"location".equals(internalType)) {
                                throw new IllegalArgumentException(operator + " location not allowed except for region/location");
                            }
                        }
                    }

                    String finalSimpleKey2 = simpleKey;
                    if (internalType != null && "region".equals(internalType)) {
                        return combine(operator, values, BoolQueryBuilder::must, v ->
                                v == null ? QueryBuilders.matchAllQuery()
                                        : "*".equals(v)
                                        ? QueryBuilders.matchAllQuery()
                                        : (v instanceof Location
                                        ? QueryBuilders.boolQuery().must(geoShapeIntersects(key + "." + REGION_FIELD, ((Location) v).getX(), ((Location) v).getY()))
                                        : (v instanceof Region
                                        ? QueryBuilders.boolQuery().must(
                                        geoLocationQuery(finalSimpleKey2, key, key, query, v, ShapeRelation.CONTAINS))
                                        : QueryBuilders.queryStringQuery(String.valueOf(v)).field(matchesAnalyzer(operator, key)).field(key + ".*")))); //QueryBuilders.matchPhrasePrefixQuery(finalKey1, v))));
                    } else {
                        return combine(operator, values, BoolQueryBuilder::must, v ->
                                v == null ? QueryBuilders.matchAllQuery()
                                        : "*".equals(v)
                                        ? QueryBuilders.matchAllQuery()
                                        : QueryBuilders.queryStringQuery(String.valueOf(v)).field(matchesAnalyzer(operator, key)).field(key + ".*")); //QueryBuilders.matchPhrasePrefixQuery(finalKey1, v));
                    }

                case PredicateParser.MATCHES_EXACT_ANY_OPERATOR :
                case PredicateParser.MATCHES_EXACT_ALL_OPERATOR :
                default :
                    break;
            }
        }

        throw new UnsupportedPredicateException(this, predicate);
    }

    /**
     * Check String for UUID
     */
    private boolean isUUID(Object obj) {
        try {
            if (obj instanceof UUID) {
                return true;
            } else if (obj instanceof String) {
                //noinspection ResultOfMethodCallIgnored
                UUID.fromString((String) obj);
                return true;
            }
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return false;
    }

    /**
     * Combines the items and operators
     *
     * @see #predicateToQueryBuilder(Predicate, Query)
     */
    @SuppressWarnings("unchecked")
    private <T> QueryBuilder combine(String operatorType,
                                     Collection<T> items,
                                     BiFunction<BoolQueryBuilder, QueryBuilder, BoolQueryBuilder> operator,
                                     Function<T, QueryBuilder> itemFunction) {

        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (items.size() == 0) {
            LOGGER.debug("builder: {} mustNot matchAllQuery", builder.toString());
            return QueryBuilders.boolQuery().mustNot(QueryBuilders.matchAllQuery());
        }
        for (T item : items) {
            if (item == null) {
                operator = BoolQueryBuilder::mustNot;
            }
            if (item instanceof java.util.UUID) {
                item = (T) item.toString();
            }

            if (Query.MISSING_VALUE.equals(item)) {
                if (operatorType.equals(PredicateParser.EQUALS_ANY_OPERATOR)) {
                    operator = BoolQueryBuilder::mustNot;
                }
                if (operatorType.equals(PredicateParser.NOT_EQUALS_ALL_OPERATOR)) {
                    operator = BoolQueryBuilder::must;
                }
                builder = operator.apply(builder, itemFunction.apply(item));
            } else {
                builder = operator.apply(builder, itemFunction.apply(item));
            }
        }

        if (builder.hasClauses()) {
            return builder;
        } else {
            return QueryBuilders.matchAllQuery();
        }
    }

    /**
     * Force a flush if isImmediate since it needs to be committed now, otherwise just do a refresh which
     * is lightweight.
     */
    private static void refresh(TransportClient client, boolean isImmediate) {
        if (client != null) {
            if (isImmediate) {
                client.admin().indices().prepareFlush().get();
            }
            client.admin().indices().prepareRefresh().get();
        }
    }

    /**
     * Force a flush if isImmediate since it needs to be committed now, otherwise just do a refresh which
     * is lightweight.
     */
    @Override
    protected void commitTransaction(TransportClient client, boolean isImmediate) throws Exception {
        refresh(client, isImmediate);
    }

    /**
     * The settings for indexes analysis
     */
    public static String getSetting(String path) {
        try {
            LOGGER.debug("trying resource mapping.json mapping");
            return new String(Files.readAllBytes(Paths.get(ElasticsearchDatabase.class.getClassLoader().getResource(path + "setting.json").toURI())));
        } catch (Exception error) {
            LOGGER.info("using default setting");
            return "{\n"
                    + "    \"analysis\": { \n"
                    + "      \"analyzer\": {\n"
                    + "        \"text_analyzer\": {\n"
                    + "          \"char_filter\":  [ \"html_strip\" ],\n"
                    + "          \"tokenizer\": \"whitespace\",\n"
                    + "          \"filter\" : [\"text_delimiter\", \"lowercase\", \"text_stemmer\"]\n"
                    + "        }\n"
                    + "      },\n"
                    + "      \"filter\" : {\n"
                    + "        \"text_delimiter\" : {\n"
                    + "          \"type\" : \"word_delimiter\",\n"
                    + "          \"catenate_all\": false,\n"
                    + "          \"catenate_numbers\": true,\n"
                    + "          \"catenate_words\": true,\n"
                    + "          \"generate_number_parts\": true,\n"
                    + "          \"generate_word_parts\": true,\n"
                    + "          \"split_on_case_change\": true\n"
                    + "        },\n"
                    + "        \"text_stemmer\" : {\n"
                    + "          \"type\" : \"stemmer\",\n"
                    + "          \"name\" : \"porter2\"\n"
                    + "        }\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }";
        }
    }

    /**
     * The actual map for Elasticsearch
     */
    public static String getMapping(String path) {
        try {
            LOGGER.debug("trying resource mapping.json mapping");
            return new String(Files.readAllBytes(Paths.get(ElasticsearchDatabase.class.getClassLoader().getResource(path + "mapping.json").toURI())));
        } catch (Exception error) {
            LOGGER.info("using default mapping");
            return "{\n"
                    + "\"properties\" : {\n"
                    + "          \"_ids\": {\n"
                    + "                  \"type\":\"string\",\n"
                    + "                  \"index\": \"not_analyzed\",\n"
                    + "                  \"store\": \"false\"\n"
                    + "           }"
                    + "       },"
                    + "      \"dynamic_templates\": [\n"
                    + "        {\n"
                    + "          \"locationgeo\": {\n"
                    + "            \"match\": \""
                    + LOCATION_FIELD
                    + "\",\n"
                    + "            \"match_mapping_type\": \"string\",\n"
                    + "            \"mapping\": {\n"
                    + "              \"type\": \"geo_point\"\n"
                    + "            }\n"
                    + "          }\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"shapegeo\": {\n"
                    + "            \"match\": \""
                    + REGION_FIELD
                    + "\",\n"
                    + "            \"match_mapping_type\": \"object\",\n"
                    + "            \"mapping\": {\n"
                    + "              \"type\": \"geo_shape\"\n"
                    + "            }\n"
                    + "          }\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"int_template\": {\n"
                    + "            \"match\": \"_*\",\n"
                    + "            \"match_mapping_type\": \"string\",\n"
                    + "            \"mapping\": {\n"
                    + "              \"type\": \"keyword\"\n"
                    + "            }\n"
                    + "          }\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"notanalyzed\": {\n"
                    + "            \"match\": \"*\",\n"
                    + "            \"match_mapping_type\": \"string\",\n"
                    + "            \"mapping\": {\n"
                    + "              \"type\": \"text\",\n"
                    + "              \"analyzer\": \"text_analyzer\",\n"
                    + "              \"fields\": {\n"
                    + "                \"raw\": {\n"
                    + "                  \"type\": \"keyword\"\n"
                    + "                },\n"
                    + "                \"match\": {\n"
                    + "                  \"type\": \"text\",\n"
                    + "                  \"analyzer\": \"text_analyzer\"\n"
                    + "                }\n"
                    + "              }\n"
                    + "            }\n"
                    + "          }\n"
                    + "        }\n"
                    + "      ]\n"
                    + "    }\n";
        } // com.psddev.dari.db.ObjectType/fields text or boolean?
    }

    /**
     * Elastic mapping which will set the types used for Elastic on index creation
     *
     */
    public static synchronized void defaultMap(TransportClient client, String indexName) {

        String jsonMapping = getMapping("");
        String jsonSettings = getSetting("");

        if (client != null) {
            boolean indexExists = client.admin().indices()
                    .prepareExists(indexName)
                    .execute().actionGet().isExists();
            if (!indexExists) {
                CreateIndexRequestBuilder cirb = client.admin().indices().prepareCreate(indexName).addMapping("_default_", jsonMapping)
                        .setSettings(jsonSettings);
                CreateIndexResponse createIndexResponse = cirb.execute().actionGet();
                if (!createIndexResponse.isAcknowledged()) {
                    LOGGER.warn("Could not create index {}", indexName);
                }

                ClusterHealthResponse yellow = client.admin().cluster().prepareHealth(indexName)
                        .setWaitForYellowStatus()
                        .setTimeout(TimeValue.timeValueSeconds(10))
                        .get();

                try {
                    refresh(client, false);
                } catch (Exception e) {
                    LOGGER.warn("Refresh failed");
                }
            }
        }
    }

    /**
     * Delete the index, can be used to reset the mapping for the index
     *
     */
    public void deleteIndex(String indexName) {
        if (client != null) {
            IndicesExistsRequest existsRequest = client.admin().indices().prepareExists(indexName).request();
            if (client.admin().indices().exists(existsRequest).actionGet().isExists()) {
                LOGGER.info("index {} exists... deleting!", indexName);
                DeleteIndexResponse response = client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
                if (!response.isAcknowledged()) {
                    LOGGER.error("Failed to delete elastic search index named {}", indexName);
                }
            }
            client.close();
            client = null;
            this.client = openConnection();
        }
    }

    /**
     * getElasticGeometryMap Create new Map that can be consumed for geoShape
     */
    private static Map<String, Object> getElasticGeometryMap(Map<String, Object> valueMap, String name) {
        Map<String, Object> newValueMap = new HashMap<>();

        if (valueMap.size() > 2) {
            if (valueMap.containsKey("polygons") && valueMap.containsKey("circles") && valueMap.containsKey("radius")) {
                newValueMap.put("type", "geometrycollection");

                List<Map<String, Object>> newGeometries = new ArrayList<>();
                if (valueMap.get("polygons") != null && valueMap.get("polygons") instanceof List) {
                    List polygons = (List) valueMap.get("polygons");
                    if (polygons.size() > 0) {
                        Map<String, Object> newObject = new HashMap<>();
                        newObject.put("type", "multipolygon");
                        List<List<List<List<Double>>>> newPolygons = new ArrayList<>();
                        for (Object p : polygons) {
                            List<List<List<Double>>> newPolygon = new ArrayList<>();
                            if (p instanceof List) {
                                for (Object ring : (List) p) {
                                    List<List<Double>> newRing = new ArrayList<>();
                                    for (Object latlon : (List) ring) {
                                        List<Double> newLatLon = new ArrayList<>();

                                        Double lat = (Double) ((List) latlon).get(0);
                                        Double lon = (Double) ((List) latlon).get(1);
                                        newLatLon.add(lat);
                                        newLatLon.add(lon);
                                        newRing.add(newLatLon);
                                    }
                                    newPolygon.add(newRing);
                                }
                                newPolygons.add(newPolygon);
                            }
                        }
                        newObject.put("coordinates", newPolygons);
                        newGeometries.add(newObject);
                    }
                }

                if (valueMap.get("circles") != null && valueMap.get("circles") instanceof List) {
                    List circles = (List) valueMap.get("circles");
                    if (circles.size() > 0) {
                        for (Object c : circles) {
                            if (c instanceof List) {
                                Map<String, Object> newGeometry = new HashMap<>();
                                List<Double> newCircle = new ArrayList<>();

                                newGeometry.put("type", "circle");
                                Double lat = (Double) ((List) c).get(0);
                                Double lon = (Double) ((List) c).get(1);
                                Double r = (Double) ((List) c).get(2);

                                newCircle.add(lat);
                                newCircle.add(lon);
                                newGeometry.put("coordinates", newCircle);
                                newGeometry.put("radius", Math.ceil(Region.degreesToMeters(r)) + "m");
                                newGeometries.add(newGeometry);
                            }
                        }
                    }
                }
                newValueMap.put("geometries", newGeometries);
            }
        }
        return newValueMap;
    }

    /**
     * Take the polygons and circles used for Region, and convert to Elastic format
     * This removes valueMap items and converts to new geoJson
     *
     */
    private static void convertToGeometryCollection(Map<String, Object> valueMap, String name) {

        if (valueMap.size() > 2) {
            if (valueMap.containsKey("polygons") && valueMap.containsKey("circles") && valueMap.containsKey("radius")) {

                List<Map<String, Object>> newGeometries = new ArrayList<>();
                if (valueMap.get("polygons") != null && valueMap.get("polygons") instanceof List) {
                    List polygons = (List) valueMap.get("polygons");
                    if (polygons.size() > 0) {
                        Map<String, Object> newObject = new HashMap<>();
                        newObject.put("type", "multipolygon");
                        List<List<List<List<Double>>>> newPolygons = new ArrayList<>();
                        for (Object p : polygons) {
                            List<List<List<Double>>> newPolygon = new ArrayList<>();
                            if (p instanceof List) {
                                for (Object ring : (List) p) {
                                    List<List<Double>> newRing = new ArrayList<>();
                                    for (Object latlon : (List) ring) {
                                        List<Double> newLatLon = new ArrayList<>();

                                        Double lat = (Double) ((List) latlon).get(0);
                                        Double lon = (Double) ((List) latlon).get(1);
                                        newLatLon.add(lat);
                                        newLatLon.add(lon);
                                        newRing.add(newLatLon);
                                    }
                                    newPolygon.add(newRing);
                                }
                                newPolygons.add(newPolygon);
                            }
                        }
                        newObject.put("coordinates", newPolygons);
                        newGeometries.add(newObject);
                    }
                }

                if (valueMap.get("circles") != null && valueMap.get("circles") instanceof List) {
                    List circles = (List) valueMap.get("circles");
                    if (circles.size() > 0) {
                        for (Object c : circles) {
                            if (c instanceof List) {
                                Map<String, Object> newGeometry = new HashMap<>();
                                List<Double> newCircle = new ArrayList<>();

                                newGeometry.put("type", "circle");
                                Double lat = (Double) ((List) c).get(0);
                                Double lon = (Double) ((List) c).get(1);
                                Double r = (Double) ((List) c).get(2);

                                newCircle.add(lat);
                                newCircle.add(lon);
                                newGeometry.put("coordinates", newCircle);
                                newGeometry.put("radius", Math.ceil(Region.degreesToMeters(r)) + "m");
                                newGeometries.add(newGeometry);
                            }
                        }
                    }
                }
                if (valueMap.containsKey("x")) {
                    valueMap.remove("x");
                }
                if (valueMap.containsKey("y")) {
                    valueMap.remove("y");
                }
                if (valueMap.containsKey("radius")) {
                    valueMap.remove("radius");
                }
                if (valueMap.containsKey("circles")) {
                    valueMap.remove("circles");
                }
                if (valueMap.containsKey("polygons")) {
                    valueMap.remove("polygons");
                }
            }
        }
    }

    /**
     * Main function to convert a Geometry to Elastic before writing. This is destructive.
     *
     * @see #doWrites
     * @see #convertLocationToName(Map, String)
     *
     */
    @SuppressWarnings("unchecked")
    private static void convertRegionToName(Map<String, Object> map, String name) {

        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pair = it.next();
            String key = pair.getKey();
            Object value = pair.getValue();

            if (value instanceof Map) {
                Map<String, Object> valueMap = (Map<String, Object>) value;

                convertToGeometryCollection(valueMap, name);
                convertRegionToName((Map<String, Object>) value, name);
            } else if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    if (item instanceof Map) {
                        Map<String, Object> valueMap = (Map<String, Object>) item;

                        convertToGeometryCollection(valueMap, name);
                        convertRegionToName((Map<String, Object>) item, name);
                    }
                }
            }
        }
    }

    /**
     * Main function to convert a Geometry to Elastic before writing. This is destructive.
     *
     * @see #doWrites
     * @see #convertRegionToName(Map, String)
     *
     */
    private static void convertLocationToName(Map<String, Object> map, String name) {

        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> pair = it.next();
            String key = pair.getKey();
            Object value = pair.getValue();

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> valueMap = (Map<String, Object>) value;
                if (valueMap.size() == 2) {
                    if (valueMap.get("x") != null && valueMap.get("y") != null) {
                        valueMap.put(name, valueMap.get("x") + "," + valueMap.get("y"));
                    }
                }
                //noinspection unchecked
                convertLocationToName((Map<String, Object>) value, name);

            } else if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> valueMap = (Map<String, Object>) item;
                        if (valueMap.size() == 2) {
                            if (valueMap.get("x") != null && valueMap.get("y") != null) {
                                valueMap.put(name, valueMap.get("x") + "," + valueMap.get("y"));
                            }
                        }
                        //noinspection unchecked
                        convertLocationToName((Map<String, Object>) item, name);
                    }
                }
            }
        }
    }

    @Override
    public void addUpdateNotifier(UpdateNotifier<?> notifier) {
        updateNotifiers.add(notifier);
    }

    @Override
    public void removeUpdateNotifier(UpdateNotifier<?> notifier) {
        updateNotifiers.remove(notifier);
    }

    @Modification.FieldInternalNamePrefix("elastic.")
    public static class FieldData extends Modification<ObjectField> {

        private boolean excludeFromAny;

        public boolean isExcludeFromAny() {
            return excludeFromAny;
        }

        public void setExcludeFromAny(boolean excludeFromAny) {
            this.excludeFromAny = excludeFromAny;
        }
    }

    private static final char[] UUID_WORD_CHARS = new char[] {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z' };

    /**
     * Split up UUID
     */
    private static String uuidToWord(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        byte[] bytes = UuidUtils.toBytes(uuid);
        int bytesLength = bytes.length;
        int wordLast = bytesLength * 2;
        byte currentByte;
        char[] word = new char[wordLast + 1];

        for (int byteIndex = 0, hexIndex = 0;
             byteIndex < bytesLength;
             ++ byteIndex, ++ hexIndex) {

            currentByte = bytes[byteIndex];
            word[hexIndex] = UUID_WORD_CHARS[(currentByte & 0xf0) >> 4];
            ++ hexIndex;
            word[hexIndex] = UUID_WORD_CHARS[(currentByte & 0x0f)];
        }

        word[wordLast] = 'z';
        return new String(word);
    }

    /**
     * Used to get the Value for a Method
     */
    private static Object getStateMethodValue(State state, ObjectMethod method) {
        Object methodResult = state.getByPath(method.getInternalName());
        return State.toSimpleValue(methodResult, method.isEmbedded(), false);
    }

    /**
     * Add Indexed Values and Indexed Methods to Elastic
     */
    public void addDocumentValues(Map<String, Object> extras, StringBuilder allBuilder, boolean includeInAny, ObjectField field, String name, Object value) {

        if (value == null) {
            return;
        }

        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                addDocumentValues(extras, allBuilder, includeInAny, field, name, item);
            }
            return;
        }

        if (includeInAny) {
            includeInAny = field == null
                    || !field.as(FieldData.class).isExcludeFromAny();
        }

        if (value instanceof Recordable) {
            value = ((Recordable) value).getState().getSimpleValues();
        }

        if (value instanceof Map) {
            Map<?, ?> valueMap = (Map<?, ?>) value;

            if (field.getInternalItemType().equals(ObjectField.LOCATION_TYPE)) {
                if (valueMap.containsKey("x") && valueMap.containsKey("y")) {
                    addDocumentValues(extras, allBuilder, includeInAny, field, name + ".x", valueMap.get("x"));
                    addDocumentValues(extras, allBuilder, includeInAny, field, name + ".y", valueMap.get("y"));
                    name = name + "." + LOCATION_FIELD;
                    value = valueMap.get("x") + "," + valueMap.get("y");
                } else {
                    return;
                }

            } else if (field.getInternalItemType().equals(ObjectField.REGION_TYPE)) {

                Map<String, Object> m = getElasticGeometryMap((Map<String, Object>) valueMap, name);
                if (m.size() > 0) {
                    name = name + "." + REGION_FIELD;
                    value = m;
                } else {
                    return;
                }

            } else {
                UUID valueTypeId = ObjectUtils.to(UUID.class, valueMap.get(StateSerializer.TYPE_KEY));

                if (valueTypeId == null) {
                    if (field != null && ObjectField.RECORD_TYPE.equals(field.getInternalItemType())) {
                        for (Object item : valueMap.values()) {
                            if (item instanceof Map) {
                                addDocumentValues(extras, allBuilder, includeInAny, field, name, item);
                            }
                        }

                    } else {
                        for (Object item : valueMap.values()) {
                            // force to String
                            addDocumentValues(extras, allBuilder, includeInAny, field, name, String.valueOf(item));
                        }
                    }
                    return;

                } else {
                    UUID valueId = ObjectUtils.to(UUID.class, valueMap.get(StateSerializer.REFERENCE_KEY));

                    if (valueId == null) {
                        if (includeInAny) {
                            allBuilder.append(valueTypeId).append(' ');
                        }

                        ObjectType valueType = getEnvironment().getTypeById(valueTypeId);

                        if (valueType != null) {
                            State valueState = null;
                            if (!valueType.getMethods().isEmpty()) {
                                valueState = new State();
                                valueState.setType(valueType);
                                valueState.setId(ObjectUtils.to(UUID.class, valueMap.get(StateSerializer.ID_KEY)));
                            }
                            for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
                                String subName = entry.getKey().toString();
                                ObjectField subField = valueType.getField(subName);

                                if (subField != null) {
                                    addDocumentValues(
                                            extras,
                                            allBuilder,
                                            includeInAny,
                                            subField,
                                            name + "/" + subName,
                                            entry.getValue());
                                }
                                if (valueState != null) {
                                    valueState.putByPath(subName, entry.getValue());
                                }
                            }
                            if (valueState != null) {
                                for (ObjectMethod method : valueType.getMethods()) {
                                    addDocumentValues(
                                            extras,
                                            allBuilder,
                                            includeInAny,
                                            method,
                                            name + "/" + method.getInternalName(),
                                            getStateMethodValue(valueState, method)
                                    );
                                }
                            }
                        }
                        return;

                    } else {
                        value = valueId;
                        Set<ObjectField> denormFields = field.getEffectiveDenormalizedFields(getEnvironment().getTypeById(valueTypeId));

                        if (denormFields != null) {
                            State valueState = State.getInstance(Query.from(Object.class).where("_id = ?", valueId).first());
                            if (valueState != null) {
                                Map<String, Object> valueValues = valueState.getSimpleValues();
                                for (ObjectField denormField : denormFields) {
                                    String denormFieldName = denormField.getInternalName();
                                    addDocumentValues(
                                            extras,
                                            allBuilder,
                                            includeInAny,
                                            denormField,
                                            name + "/" + denormFieldName,
                                            valueValues.get(denormFieldName));
                                }
                            }
                        }
                    }
                }
            }
        }

        String trimmed = value.toString().trim();
        Matcher uuidMatcher = UUID_PATTERN.matcher(trimmed);
        int uuidLast = 0;

        while (uuidMatcher.find()) {
            if (includeInAny) {
                allBuilder.append(trimmed.substring(uuidLast, uuidMatcher.start()));
            }

            uuidLast = uuidMatcher.end();
            String word = uuidToWord(ObjectUtils.to(UUID.class, uuidMatcher.group(0)));

            if (includeInAny && word != null) {
                allBuilder.append(word);
            }
        }

        if (includeInAny) {
            allBuilder.append(trimmed.substring(uuidLast));
            allBuilder.append(' ');
        }

        if (value instanceof String) {
            value = ((String) value).trim();
            // don't save empty strings to be compatible with Solr
            if (((String) value).length() == 0) {
                return;
            }
        }
        if (extras.get(name) == null) {
            setValue(extras, name, value);
        } else {
            addValue(extras, name, value);
        }
    }

    /**
     * Set the value into extras
     */
    private static void setValue(Map<String, Object> extras, String name, Object value) {
        extras.put(name, value);
    }

    /**
     * Add value into extras
     */
    private static void addValue(Map<String, Object> extras, String name, Object value) {
        if (extras.get(name) instanceof List) {
            if (value instanceof List) {
                List vList = (List) extras.get(name);
                vList.addAll((List) value);
                extras.put(name, vList);
            } else {
                List vList = (List) extras.get(name);
                vList.add(value);
                extras.put(name, vList);
            }
        } else {
            List vList = new ArrayList<>();
            vList.add(extras.get(name));
            vList.add(value);
            extras.put(name, vList);
        }
    }

    /**
     * addIndexedFields to Elastic
     */
    public Map<String, Object> addIndexedFields(State state, StringBuilder allBuilder) {
        Map<String, Object> m = new HashMap<>();

        if (state.getType() != null) {
            Map<String, Object> stateValues = state.getSimpleValues();

            for (Map.Entry<String, Object> entry : stateValues.entrySet()) {
                String fieldName = entry.getKey();

                ObjectField field = state.getField(fieldName);
                if (field == null) {
                    continue;
                }

                String uniqueName = field.getUniqueName();
                addDocumentValues(
                        m,
                        allBuilder,
                        true,
                        field,
                        uniqueName,
                        entry.getValue());
            }
        }
        return m;
    }

    public Map<String, Object> addLabel(State state) {
        Map<String, Object> m = new HashMap<>();

        m.put(Query.LABEL_KEY, state.getLabel().trim().toLowerCase(Locale.ENGLISH));
        return m;
    }

    /**
     * Used to add Indexed Methods for Elastic
     */
    public Map<String, Object> addIndexedMethods(State state, StringBuilder allBuilder) {
        Map<String, Object> m = new HashMap<>();

        if (state.getType() != null) {
            List<ObjectMethod> methods = new ArrayList<>(state.getType().getMethods());
            methods.addAll(Database.Static.getDefault().getEnvironment().getMethods());

            for (ObjectMethod method : methods) {
                addDocumentValues(
                        m,
                        allBuilder,
                        true,
                        method,
                        method.getUniqueName(),
                        getStateMethodValue(state, method)
                );
            }
        }
        return m;
    }

    // Pass through distinct set of States to doSaves.
    @Override
    protected void doWriteRecalculations(TransportClient client, boolean isImmediate, Map<ObjectIndex, List<State>> recalculations) throws Exception {
        if (recalculations != null) {
            int count = 0;
            Set<State> states = new HashSet<>();
            for (Map.Entry<ObjectIndex, List<State>> entry : recalculations.entrySet()) {
                count++;
                states.addAll(entry.getValue());
            }
            if (count > 0) {
                doWrites(client, isImmediate, new ArrayList<>(states), new ArrayList<>(), new ArrayList<>());
            }
        }
    }

    // Pass through to doWriteRecalculations.
    @Override
    protected void doRecalculations(TransportClient client, boolean isImmediate, ObjectIndex index, List<State> states) throws Exception {
        Map<ObjectIndex, List<State>> recalculations = new HashMap<ObjectIndex, List<State>>();
        recalculations.put(index, states);
        doWriteRecalculations(client, isImmediate, recalculations);
    }

    /**
     * Write saves, indexes, deletes as a bulk Elastic operation
     *
     * @param indexes Not used
     */
    @Override
    protected void doWrites(TransportClient client, boolean isImmediate, List<State> saves, List<State> indexes, List<State> deletes) throws Exception {
        try {
            /* verify all indexes exist */
            Set<String> indexSet = new HashSet<>();

            if (saves != null) {
                for (State state : saves) {

                    String documentType = state.getTypeId().toString();
                    String newIndexname = indexName + documentType.replaceAll("-", "");
                    indexSet.add(newIndexname);
                }
            }

            if (deletes != null) {
                for (State state : deletes) {

                    String documentType = state.getTypeId().toString();
                    String newIndexname = indexName + documentType.replaceAll("-", "");
                    indexSet.add(newIndexname);
                }
            }

            checkIndexes(indexSet.toArray(new String[indexSet.size()]));

            BulkRequestBuilder bulk = client.prepareBulk();

            String indexName = getIndexName();

            if (saves != null) {
                    for (State state : saves) {
                        try {
                            boolean isNew = state.isNew();
                            UUID documentTypeUUID = state.getTypeId();
                            String documentType = documentTypeUUID.toString();
                            UUID documentUUID = state.getId();
                            String documentId = documentUUID.toString();
                            String newIndexname = indexName + documentType.replaceAll("-", "");
                            List<AtomicOperation> atomicOperations = state.getAtomicOperations();
                            StringBuilder allBuilder = new StringBuilder();

                            if (isNew || atomicOperations.isEmpty()) {
                                Map<String, Object> t = state.getSimpleValues();

                                // these 2 are disruptive to t
                                //convertLocationToName(t, LOCATION_FIELD);
                                //convertRegionToName(t, REGION_FIELD);

                                Map<String, Object> extraFields = addIndexedFields(state, allBuilder);
                                Map<String, Object> extraLabel = addLabel(state);
                                Map<String, Object> extraIndex = addIndexedMethods(state, allBuilder);

                                if (extraFields.size() > 0) {
                                    extraFields.forEach((s, obj) -> t.put(s, obj));
                                }
                                if (extraLabel.size() > 0) {
                                    extraLabel.forEach((s, obj) -> t.put(s, obj));
                                }
                                if (extraIndex.size() > 0) {
                                    extraIndex.forEach((s, obj) -> t.put(s, obj));
                                }
                                t.remove("_id");
                                t.remove("_type");
                                t.put("_ids", documentId); // Elastic range for iterator default _id will not work

                                LOGGER.debug("All field [{}]", allBuilder.toString());
                                LOGGER.debug("Elasticsearch doWrites saving index [{}] and _type [{}] and _id [{}] = [{}]",
                                        newIndexname, documentType, documentId, t.toString());
                                bulk.add(client.prepareIndex(newIndexname, documentType, documentId).setSource(t));

                            } else {
                                // there is no getting around it, must grab query to get old value and set it
                                boolean sendFullUpdate = false;
                                Object oldObject = Query
                                        .from(Object.class)
                                        .where("_id = ?", documentId)
                                        .using(this)
                                        .master()
                                        .noCache()
                                        .first();

                                if (oldObject == null) {
                                    retryWrites();
                                    break;
                                }

                                // Restore the data from the old object.
                                State oldState = State.getInstance(oldObject);
                                UUID oldTypeId = oldState.getVisibilityAwareTypeId();
                                UUID oldId = oldState.getId();

                                state.setValues(oldState.getValues());

                                if (!this.painlessModule) {
                                    LOGGER.info("Painless module is not installed");
                                    sendFullUpdate = true;
                                }

                                // Reset to old First
                                for (AtomicOperation operation : atomicOperations) {
                                    String field = operation.getField();
                                    state.putByPath(field, oldState.getByPath(field));
                                    if (field.indexOf('/') != -1) {
                                        sendFullUpdate = true;
                                    }
                                }

                                for (AtomicOperation operation : atomicOperations) {
                                    operation.execute(state); // sets state
                                    if (!sendFullUpdate) {
                                        String field = operation.getField().trim();
                                        if (operation instanceof AtomicOperation.Increment) {
                                            double newVal = ((AtomicOperation.Increment) operation).getValue();
                                            Map<String, Object> params = new HashMap<>();
                                            params.put("val", newVal);
                                            bulk.add(client.prepareUpdate(newIndexname, documentType, documentId)
                                                    .setScript(new Script(ScriptType.INLINE, "painless", "ctx._source." + field + " += params.val;", params))
                                                    .setDetectNoop(false));
                                        } else if (operation instanceof AtomicOperation.Add) {
                                            Object newVal = ((AtomicOperation.Add) operation).getValue();
                                            //String valJson = ObjectUtils.toJson(newVal);
                                            Map<String, Object> params = new HashMap<>();
                                            params.put("val", newVal);
                                            bulk.add(client.prepareUpdate(newIndexname, documentType, documentId)
                                                    .setScript(new Script(ScriptType.INLINE, "painless", "ctx._source." + field + ".add(params.val);", params))
                                                    .setDetectNoop(false));
                                        } else if (operation instanceof AtomicOperation.Remove) {
                                            Object newVal = ((AtomicOperation.Remove) operation).getValue();
                                            Map<String, Object> params = new HashMap<>();
                                            params.put("val", newVal);
                                            bulk.add(client.prepareUpdate(newIndexname, documentType, documentId)
                                                    .setScript(new Script(ScriptType.INLINE, "painless", "ctx._source." + field + ".remove(ctx._source." + field + ".indexOf(params.val));", params))
                                                    .setDetectNoop(false));
                                        } else if (operation instanceof AtomicOperation.Put) {
                                            Object newVal = ((AtomicOperation.Put) operation).getValue();
                                            Map<String, Object> params = new HashMap<>();
                                            params.put("val", newVal);
                                            bulk.add(client.prepareUpdate(newIndexname, documentType, documentId)
                                                    .setScript(new Script(ScriptType.INLINE, "painless", "ctx._source." + field + "= params.val;", params))
                                                    .setDetectNoop(false));
                                        } else if (operation instanceof AtomicOperation.Replace) {
                                            Object oldVal = ((AtomicOperation.Replace) operation).getOldValue();
                                            Object newVal = ((AtomicOperation.Replace) operation).getNewValue();
                                            Map<String, Object> params = new HashMap<>();
                                            params.put("oval", oldVal);
                                            params.put("nval", newVal);
                                            bulk.add(client.prepareUpdate(newIndexname, documentType, documentId)
                                                    .setScript(new Script(ScriptType.INLINE, "painless", "if (ctx._source." + field + " == params.oval) ctx._source." + field + "= params.nval;", params))
                                                    .setDetectNoop(false));
                                        } else {
                                            sendFullUpdate = true;
                                        }
                                    }
                                    // other ones can be done with normal update() since merge should work.
                                }

                                boolean sendExtraUpdate = false;
                                Map<String, Object> t = state.getSimpleValues();
                                Map<String, Object> extra = new HashMap<>();
                                Map<String, Object> extraFields = addIndexedFields(state, allBuilder);
                                Map<String, Object> extraLabel = addLabel(state);
                                Map<String, Object> extraIndex = addIndexedMethods(state, allBuilder);

                                if (extraFields.size() > 0) {
                                    sendExtraUpdate = true;
                                    extraFields.forEach((s, obj) -> t.put(s, obj));
                                    extraFields.forEach((s, obj) -> extra.put(s, obj));
                                }
                                if (extraLabel.size() > 0) {
                                    sendExtraUpdate = true;
                                    extraLabel.forEach((s, obj) -> t.put(s, obj));
                                    extraLabel.forEach((s, obj) -> extra.put(s, obj));
                                }
                                if (extraIndex.size() > 0) {
                                    sendExtraUpdate = true;
                                    extraIndex.forEach((s, obj) -> t.put(s, obj));
                                    extraIndex.forEach((s, obj) -> extra.put(s, obj));
                                }
                                t.remove("_id");
                                t.remove("_type");
                                t.put("_ids", documentId);

                                // if you move TypeId you need to add the whole document and remove old
                                if (!oldTypeId.equals(documentTypeUUID) || !oldId.equals(documentUUID)) {
                                    String oldDocumentType = oldTypeId.toString();
                                    String oldDocumentId = oldId.toString();
                                    String oldIndexname = indexName + oldDocumentType.replaceAll("-", "");
                                    bulk.add(client
                                            .prepareDelete(oldIndexname, oldDocumentType, oldDocumentId));
                                    LOGGER.debug("Elasticsearch doWrites moved typeId/Id atomic add index [{}] and _type [{}] and _id [{}] = [{}]",
                                            newIndexname, documentType, documentId, t.toString());
                                    bulk.add(client.prepareIndex(newIndexname, documentType, documentId).setSource(t));
                                } else if (sendFullUpdate) {
                                    LOGGER.debug("Elasticsearch doWrites sendFullUpdate atomic updating index [{}] and _type [{}] and _id [{}] = [{}]",
                                            newIndexname, documentType, documentId, t.toString());
                                    bulk.add(client.prepareUpdate(newIndexname, documentType, documentId).setDoc(t));
                                } else if (sendExtraUpdate) {
                                    LOGGER.debug("Elasticsearch doWrites sendExtraUpdate atomic updating index [{}] and _type [{}] and _id [{}] = [{}]",
                                            newIndexname, documentType, documentId, extra.toString());
                                    bulk.add(client.prepareUpdate(newIndexname, documentType, documentId).setDoc(extra));
                                }
                            }
                        } catch (Exception error) {
                            LOGGER.warn(
                                    String.format("Elasticsearch doWrites saves Exception [%s: %s]",
                                            error.getClass().getName(),
                                            error.getMessage()),
                                    error);
                            throw error;
                        }
                    }
            }

            if (deletes != null) {
                for (State state : deletes) {
                    String documentType = state.getTypeId().toString();
                    String documentId = state.getId().toString();
                    String newIndexname = indexName + documentType.replaceAll("-", "");

                    LOGGER.debug("Elasticsearch doWrites deleting index [{}] and _type [{}] and _id [{}]",
                            newIndexname, documentType, documentId);
                    try {
                        bulk.add(client
                                .prepareDelete(newIndexname, state.getTypeId().toString(), state.getId().toString()));
                    } catch (Exception error) {
                        LOGGER.warn(
                                String.format("Elasticsearch doWrites deletes Exception [%s: %s]",
                                        error.getClass().getName(),
                                        error.getMessage()),
                                error);
                        throw error;
                    }
                }
            }
            LOGGER.debug("Elasticsearch Writing [{}]", bulk.request().requests().toString());
            bulk.execute().actionGet();
            if (isImmediate) {
                commitTransaction(client, true);
            }
        } catch (Exception error) {
            LOGGER.warn(
                    String.format("Elasticsearch doWrites Exception [%s: %s]",
                            error.getClass().getName(),
                            error.getMessage()),
                    error);
            throw error;
        }
    }
}
