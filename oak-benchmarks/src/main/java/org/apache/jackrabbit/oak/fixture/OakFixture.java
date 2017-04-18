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
package org.apache.jackrabbit.oak.fixture;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;

import javax.sql.DataSource;

import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBBlobStore;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBDataSourceFactory;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBDocumentStore;
import org.apache.jackrabbit.oak.plugins.document.rdb.RDBOptions;
import org.apache.jackrabbit.oak.plugins.document.util.MongoConnection;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeStore;
import org.apache.jackrabbit.oak.spi.blob.BlobStore;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.apache.jackrabbit.oak.stats.StatisticsProvider;

public abstract class OakFixture {

    public static final String OAK_MEMORY = "Oak-Memory";
    public static final String OAK_MEMORY_NS = "Oak-MemoryNS";

    public static final String OAK_MONGO = "Oak-Mongo";
    public static final String OAK_MONGO_DS = "Oak-Mongo-DS";
    public static final String OAK_MONGO_NS = "Oak-MongoNS";

    public static final String OAK_RDB = "Oak-RDB";
    public static final String OAK_RDB_DS = "Oak-RDB-DS";

    public static final String OAK_SEGMENT_TAR = "Oak-Segment-Tar";
    public static final String OAK_SEGMENT_TAR_DS = "Oak-Segment-Tar-DS";

    public static final String OAK_MULTIPLEXING = "Oak-Multiplexing";
    public static final String OAK_MULTIPLEXING_MEMORY = "Oak-Multiplexing-Memory";


    private final String name;
    protected final String unique;

    protected OakFixture(String name) {
        this.name = name;
        this.unique = getUniqueDatabaseName(name);
    }

    public static String getUniqueDatabaseName(String name) {
        return String.format("%s-%d", name, System.currentTimeMillis());
    }

    public abstract Oak getOak(int clusterId) throws Exception;

    public abstract Oak[] setUpCluster(int n, StatisticsProvider statsProvider) throws Exception;

    public abstract void tearDownCluster();

    @Override
    public String toString() {
        return name;
    }

    public static OakFixture getMemory(long cacheSize) {
        return getMemory(OAK_MEMORY, cacheSize);
    }

    public static OakFixture getMemoryNS(long cacheSize) {
        return getMemory(OAK_MEMORY_NS, cacheSize);
    }

    public static OakFixture getMemory(String name, final long cacheSize) {
        return new OakFixture(name) {

            @Override
            public Oak getOak(int clusterId) throws Exception {
                Oak oak;
                oak = newOak(new MemoryNodeStore());
                return oak;
            }

            @Override
            public Oak[] setUpCluster(int n, StatisticsProvider statsProvider) throws Exception {
                Oak[] cluster = new Oak[n];
                for (int i = 0; i < cluster.length; i++) {
                    Oak oak;
                    oak = newOak(new MemoryNodeStore());
                    cluster[i] = oak;
                }
                return cluster;
            }

            @Override
            public void tearDownCluster() {
                // nothing to do
            }
        };
    }

    public static OakFixture getMongo(String uri,
                                      boolean dropDBAfterTest, long cacheSize) {
        return getMongo(OAK_MONGO, uri,
                dropDBAfterTest, cacheSize, false, null, 0);
    }

    public static OakFixture getMongo(String host, int port, String database,
                                      boolean dropDBAfterTest, long cacheSize) {
        return getMongo(OAK_MONGO, host, port, database,
                dropDBAfterTest, cacheSize, false, null, 0);
    }

    public static OakFixture getMongoNS(String uri,
                                      boolean dropDBAfterTest, long cacheSize) {
        return getMongo(OAK_MONGO_NS, uri,
                dropDBAfterTest, cacheSize, false, null, 0);
    }

    public static OakFixture getMongoNS(String host, int port, String database,
                                        boolean dropDBAfterTest, long cacheSize) {
        return getMongo(OAK_MONGO_NS, host, port, database,
                dropDBAfterTest, cacheSize, false, null, 0);
    }

    public static OakFixture getMongo(String name, final String host,
                                      final int port, String database,
                                      final boolean dropDBAfterTest, final long cacheSize,
                                      final boolean useFileDataStore,
                                      final File base,
                                      final int fdsCacheInMB) {
        if (database == null) {
            database = getUniqueDatabaseName(name);
        }
        String uri = "mongodb://" + host + ":" + port + "/" + database;
        return getMongo(name, uri, dropDBAfterTest, cacheSize, useFileDataStore, base, fdsCacheInMB);
    }

    public static OakFixture getMongo(final String name, final String uri,
                                      final boolean dropDBAfterTest, final long cacheSize,
                                      final boolean useDataStore,
                                      final File base, final int dsCacheInMB) {
        return new MongoFixture(name, uri, dropDBAfterTest, cacheSize, useDataStore, base, dsCacheInMB);
    }

    public static OakFixture getRDB(final String name, final String jdbcuri, final String jdbcuser, final String jdbcpasswd,
        final String tablePrefix, final boolean dropDBAfterTest, final long cacheSize) {
        return getRDB(name, jdbcuri, jdbcuser, jdbcpasswd, tablePrefix, dropDBAfterTest, cacheSize, false, null, 0);
    }

    public static OakFixture getRDB(final String name, final String jdbcuri, final String jdbcuser, final String jdbcpasswd,
                                    final String tablePrefix, final boolean dropDBAfterTest, final long cacheSize,
                                    final boolean useDataStore, final File base, final int dsCacheInMB) {
        return new OakFixture(name) {
            private DocumentMK[] kernels;
            private BlobStoreFixture blobStoreFixture;

            private RDBOptions getOptions(boolean dropDBAFterTest, String tablePrefix) {
                return new RDBOptions().dropTablesOnClose(dropDBAfterTest).tablePrefix(tablePrefix);
            }

            private BlobStore getBlobStore(StatisticsProvider statsProvider) {
                try {
                    if (useDataStore) {
                        initializeBlobStoreFixture(statsProvider);
                        return blobStoreFixture.setUp();
                    } else {
                        DataSource ds = RDBDataSourceFactory.forJdbcUrl(jdbcuri, jdbcuser, jdbcpasswd);
                        return new RDBBlobStore(ds, getOptions(dropDBAfterTest, tablePrefix));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Oak getOak(int clusterId) throws Exception {
                DataSource ds = RDBDataSourceFactory.forJdbcUrl(jdbcuri, jdbcuser, jdbcpasswd);
                DocumentMK.Builder mkBuilder = new DocumentMK.Builder()
                        .setRDBConnection(ds, getOptions(dropDBAfterTest, tablePrefix)).memoryCacheSize(cacheSize)
                        .setClusterId(clusterId).setLogging(false);
                BlobStore blobStore = getBlobStore(StatisticsProvider.NOOP);
                if (blobStore != null) {
                    mkBuilder.setBlobStore(blobStore);
                }
                DocumentMK dmk = mkBuilder.open();
                return newOak(dmk.getNodeStore());
            }

            @Override
            public Oak[] setUpCluster(int n, StatisticsProvider statsProvider) throws Exception {
                Oak[] cluster = new Oak[n];
                kernels = new DocumentMK[cluster.length];
                for (int i = 0; i < cluster.length; i++) {
                    BlobStore blobStore = getBlobStore(statsProvider);
                    DataSource ds = RDBDataSourceFactory.forJdbcUrl(jdbcuri, jdbcuser, jdbcpasswd);
                    DocumentMK.Builder mkBuilder = new DocumentMK.Builder()
                            .setStatisticsProvider(statsProvider)
                            .setRDBConnection(ds, getOptions(dropDBAfterTest, tablePrefix)).memoryCacheSize(cacheSize)
                            // FIXME: OAK-3389
                            .setLeaseCheck(false)
                            .setClusterId(i + 1).setLogging(false);
                    if (blobStore != null) {
                        mkBuilder.setBlobStore(blobStore);
                    }
                    kernels[i] = mkBuilder.open();
                    cluster[i] = newOak(kernels[i].getNodeStore());
                }
                return cluster;
            }

            @Override
            public void tearDownCluster() {
                String dropped = "";
                for (DocumentMK kernel : kernels) {
                    kernel.dispose();
                    if (kernel.getDocumentStore() instanceof RDBDocumentStore) {
                        dropped += ((RDBDocumentStore)kernel.getDocumentStore()).getDroppedTables();
                    }
                }
                if (dropDBAfterTest) {
                    if(blobStoreFixture != null){
                        blobStoreFixture.tearDown();
                    }

                    if (dropped.isEmpty()) {
                        throw new RuntimeException("dropdb was set, but tables have not been dropped");
                    }
                }
            }

            private void initializeBlobStoreFixture(StatisticsProvider statsProvider) {
                if (useDataStore && blobStoreFixture == null) {
                    blobStoreFixture = BlobStoreFixture.create(base, true, dsCacheInMB, statsProvider);
                }
            }
        };
    }

    public static OakFixture getSegmentTar(final String name, final File base,
        final int maxFileSizeMB, final int cacheSizeMB, final boolean memoryMapping,
        final boolean useBlobStore) {
        return getSegmentTar(name, base, maxFileSizeMB, cacheSizeMB, memoryMapping, useBlobStore, 0);
    }

    public static OakFixture getSegmentTar(final String name, final File base,
        final int maxFileSizeMB, final int cacheSizeMB, final boolean memoryMapping,
        final boolean useBlobStore, final int dsCacheInMB) {
        return new SegmentTarFixture(name, base, maxFileSizeMB, cacheSizeMB, memoryMapping, useBlobStore, dsCacheInMB);
    }

    public static OakFixture getMultiplexing(final String name, final File base,
                                             final int maxFileSizeMB, final int cacheSizeMB, final boolean memoryMapping,
                                             final int mounts, final int pathsPerMount) {
        return new MultiplexingFixture(name, base, maxFileSizeMB, cacheSizeMB, memoryMapping, mounts, pathsPerMount);
    }

    public static OakFixture getMultiplexingInMemory(final String name, final int mounts, final int pathsPerMount) {
        return new MultiplexingFixture(name, mounts, pathsPerMount);
    }

    public static class MongoFixture extends OakFixture {

        private final String uri;

        private final boolean dropDBAfterTest;

        private final long cacheSize;

        private final boolean useDataStore;

        private final File base;

        private final int dsCacheInMB;

        private DocumentMK[] kernels;
        private BlobStoreFixture blobStoreFixture;

        public MongoFixture(final String name, final String uri,
                            final boolean dropDBAfterTest, final long cacheSize,
                            final boolean useDataStore,
                            final File base, final int dsCacheInMB) {
            super(name);
            this.uri = uri;
            this.dropDBAfterTest = dropDBAfterTest;
            this.cacheSize = cacheSize;
            this.useDataStore = useDataStore;
            this.base = base;
            this.dsCacheInMB = dsCacheInMB;
        }

        public DocumentMK.Builder getBuilder(int clusterId) throws UnknownHostException {
            MongoConnection mongo = new MongoConnection(uri);
            DocumentMK.Builder mkBuilder = new DocumentMK.Builder().
                    setMongoDB(mongo.getDB()).
                    memoryCacheSize(cacheSize).
                    //TODO Persistent cache should be removed in teardown
                            setPersistentCache("target/persistentCache,time").
                            setClusterId(clusterId).
                            setLogging(false);
            setupBlobStore(mkBuilder, StatisticsProvider.NOOP);
            return mkBuilder;
        }

        @Override
        public Oak getOak(int clusterId) throws Exception {
            DocumentMK dmk = getBuilder(clusterId).open();
            return newOak(dmk.getNodeStore());
        }

        public Oak[] setUpCluster(DocumentMK.Builder[] builders, StatisticsProvider statsProvider) throws Exception {
            Oak[] cluster = new Oak[builders.length];
            kernels = new DocumentMK[cluster.length];
            for (int i = 0; i < cluster.length; i++) {
                kernels[i] = builders[i].open();
                cluster[i] = newOak(kernels[i].getNodeStore());
            }
            return cluster;
        }

        @Override
        public Oak[] setUpCluster(int n, StatisticsProvider statsProvider) throws Exception {
            DocumentMK.Builder[] builders = new DocumentMK.Builder[n];
            for (int i = 0; i < n; i++) {
                builders[i] = getBuilder(i + 1);
            }
            return setUpCluster(builders, statsProvider);
        }

        @Override
        public void tearDownCluster() {
            for (DocumentMK kernel : kernels) {
                kernel.dispose();
            }
            if (dropDBAfterTest) {
                try {
                    MongoConnection mongo =
                            new MongoConnection(uri);
                    mongo.getDB().dropDatabase();
                    mongo.close();
                    if(blobStoreFixture != null){
                        blobStoreFixture.tearDown();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void setupBlobStore(DocumentMK.Builder mkBuilder, StatisticsProvider statsProvider) {
            initializeBlobStoreFixture(statsProvider);
            if (blobStoreFixture != null) {
                mkBuilder.setBlobStore(blobStoreFixture.setUp());
            }
        }

        private void initializeBlobStoreFixture(StatisticsProvider statsProvider) {
            if (blobStoreFixture != null){
                return;
            }

            if (useDataStore) {
                blobStoreFixture =
                        BlobStoreFixture.create(base, true, dsCacheInMB, statsProvider);
            }
        }

    }

    static Oak newOak(NodeStore nodeStore) {
        return new Oak(nodeStore).with(ManagementFactory.getPlatformMBeanServer());
    }    

}