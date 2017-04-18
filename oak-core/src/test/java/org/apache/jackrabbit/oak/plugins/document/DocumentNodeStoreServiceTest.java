/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.document;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.commons.io.FilenameUtils;
import org.apache.jackrabbit.oak.plugins.document.spi.JournalPropertyService;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.apache.jackrabbit.oak.stats.StatisticsProvider;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;

public class DocumentNodeStoreServiceTest {

    @Rule
    public final OsgiContext context = new OsgiContext();

    @Rule
    public final TemporaryFolder target = new TemporaryFolder(new File("target"));

    private final DocumentNodeStoreService service = new DocumentNodeStoreService();

    private String repoHome;

    @Before
    public void setUp() throws  Exception {
        assumeTrue(MongoUtils.isAvailable());
        context.registerService(StatisticsProvider.class, StatisticsProvider.NOOP);
        MockOsgi.injectServices(service, context.bundleContext());
        repoHome = target.newFolder().getAbsolutePath();
    }

    @After
    public void tearDown() throws Exception {
        MockOsgi.deactivate(service);
        MongoUtils.dropCollections(MongoUtils.DB);
    }

    @Test
    public void persistentCache() {
        String persistentCache = FilenameUtils.concat(repoHome, "cache");
        assertPersistentCachePath(persistentCache, persistentCache, "");
    }

    @Test
    public void journalCache() {
        String journalCache = FilenameUtils.concat(repoHome, "diff-cache");
        assertJournalCachePath(journalCache, journalCache, "");
    }

    @Test
    public void persistentCacheWithRepositoryHome() {
        assertPersistentCachePath(FilenameUtils.concat(repoHome, "cache"),
                "cache", repoHome);
    }

    @Test
    public void journalCacheWithRepositoryHome() {
        assertJournalCachePath(FilenameUtils.concat(repoHome, "diff-cache"),
                "diff-cache", repoHome);
    }

    @Test
    public void defaultPersistentCacheWithRepositoryHome() {
        String persistentCache = FilenameUtils.concat(repoHome, "cache");
        assertPersistentCachePath(persistentCache, "", repoHome);
    }

    @Test
    public void defaultJournalCacheWithRepositoryHome() {
        String journalCache = FilenameUtils.concat(repoHome, "diff-cache");
        assertJournalCachePath(journalCache, "", repoHome);
    }

    @Test
    public void disablePersistentCacheWithRepositoryHome() {
        String persistentCache = FilenameUtils.concat(repoHome, "cache");
        assertNoPersistentCachePath(persistentCache, "-", repoHome);

    }

    @Test
    public void disableJournalCacheWithRepositoryHome() {
        String journalCache = FilenameUtils.concat(repoHome, "diff-cache");
        assertNoJournalCachePath(journalCache, "-", repoHome);
    }

    @Test
    public void journalPropertyTracker() throws Exception {
        MockOsgi.activate(service, context.bundleContext(), newConfig(repoHome));
        DocumentNodeStore store = context.getService(DocumentNodeStore.class);
        assertEquals(0, store.getJournalPropertyHandlerFactory().getServiceCount());

        context.registerService(JournalPropertyService.class, mock(JournalPropertyService.class));
        assertEquals(1, store.getJournalPropertyHandlerFactory().getServiceCount());
    }

    @Test
    public void setUpdateLimit() throws Exception {
        Map<String, Object> config = newConfig(repoHome);
        config.put(DocumentNodeStoreService.PROP_UPDATE_LIMIT, 17);
        MockOsgi.activate(service, context.bundleContext(), config);
        DocumentNodeStore store = context.getService(DocumentNodeStore.class);
        assertEquals(17, store.getUpdateLimit());
    }

    private void assertPersistentCachePath(String expectedPath,
                                           String persistentCache,
                                           String repoHome) {
        Map<String, Object> config = newConfig(repoHome);
        config.put("persistentCache", persistentCache);
        config.put("journalCache", "-");
        assertCachePath(expectedPath, config);
    }

    private void assertJournalCachePath(String expectedPath,
                                        String journalCache,
                                        String repoHome) {
        Map<String, Object> config = newConfig(repoHome);
        config.put("journalCache", journalCache);
        config.put("persistentCache", "-");
        assertCachePath(expectedPath, config);
    }

    private void assertCachePath(String expectedPath,
                                 Map<String, Object> config) {
        assertFalse(new File(expectedPath).exists());

        MockOsgi.activate(service, context.bundleContext(), config);

        assertNotNull(context.getService(NodeStore.class));
        // must exist after service was activated
        assertTrue(new File(expectedPath).exists());
    }

    private void assertNoPersistentCachePath(String unexpectedPath,
                                             String persistentCache,
                                             String repoHome) {
        Map<String, Object> config = newConfig(repoHome);
        config.put("persistentCache", persistentCache);
        assertNoCachePath(unexpectedPath, config);
    }

    private void assertNoJournalCachePath(String unexpectedPath,
                                          String journalCache,
                                          String repoHome) {
        Map<String, Object> config = newConfig(repoHome);
        config.put("journalCache", journalCache);
        assertNoCachePath(unexpectedPath, config);
    }

    private void assertNoCachePath(String unexpectedPath,
                                   Map<String, Object> config) {
        assertFalse(new File(unexpectedPath).exists());

        MockOsgi.activate(service, context.bundleContext(), config);

        assertNotNull(context.getService(NodeStore.class));
        // must not exist after service was activated
        assertFalse(new File(unexpectedPath).exists());
        // also assert there is no dash directory
        // the dash character is used to disable a persistent cache
        assertFalse(new File(repoHome, "-").exists());
    }

    private Map<String, Object> newConfig(String repoHome) {
        Map<String, Object> config = Maps.newHashMap();
        config.put("repository.home", repoHome);
        config.put("db", MongoUtils.DB);
        return config;
    }
}
