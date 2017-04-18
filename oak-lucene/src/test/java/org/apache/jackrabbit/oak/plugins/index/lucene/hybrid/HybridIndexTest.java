/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jackrabbit.oak.plugins.index.lucene.hybrid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.api.ContentRepository;
import org.apache.jackrabbit.oak.api.Tree;
import org.apache.jackrabbit.oak.api.Type;
import org.apache.jackrabbit.oak.commons.PathUtils;
import org.apache.jackrabbit.oak.commons.concurrent.ExecutorCloser;
import org.apache.jackrabbit.oak.plugins.index.AsyncIndexUpdate;
import org.apache.jackrabbit.oak.plugins.index.IndexConstants;
import org.apache.jackrabbit.oak.plugins.index.counter.NodeCounterEditorProvider;
import org.apache.jackrabbit.oak.plugins.index.lucene.IndexCopier;
import org.apache.jackrabbit.oak.plugins.index.lucene.IndexTracker;
import org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexConstants;
import org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexConstants.IndexingMode;
import org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexEditorProvider;
import org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexProvider;
import org.apache.jackrabbit.oak.plugins.index.lucene.TestUtil;
import org.apache.jackrabbit.oak.plugins.index.lucene.TestUtil.OptionalEditorProvider;
import org.apache.jackrabbit.oak.plugins.index.lucene.reader.DefaultIndexReaderFactory;
import org.apache.jackrabbit.oak.plugins.index.lucene.reader.LuceneIndexReaderFactory;
import org.apache.jackrabbit.oak.plugins.index.lucene.util.IndexDefinitionBuilder;
import org.apache.jackrabbit.oak.plugins.index.lucene.util.IndexDefinitionBuilder.IndexRule;
import org.apache.jackrabbit.oak.plugins.index.nodetype.NodeTypeIndexProvider;
import org.apache.jackrabbit.oak.plugins.index.property.PropertyIndexEditorProvider;
import org.apache.jackrabbit.oak.plugins.memory.ArrayBasedBlob;
import org.apache.jackrabbit.oak.plugins.memory.MemoryNodeStore;
import org.apache.jackrabbit.oak.plugins.nodetype.TypeEditorProvider;
import org.apache.jackrabbit.oak.plugins.nodetype.write.InitialContent;
import org.apache.jackrabbit.oak.plugins.nodetype.write.NodeTypeRegistry;
import org.apache.jackrabbit.oak.query.AbstractQueryTest;
import org.apache.jackrabbit.oak.spi.commit.Observer;
import org.apache.jackrabbit.oak.spi.mount.MountInfoProvider;
import org.apache.jackrabbit.oak.spi.query.QueryIndexProvider;
import org.apache.jackrabbit.oak.spi.security.OpenSecurityProvider;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.apache.jackrabbit.oak.spi.whiteboard.Whiteboard;
import org.apache.jackrabbit.oak.spi.whiteboard.WhiteboardUtils;
import org.apache.jackrabbit.oak.stats.Clock;
import org.apache.jackrabbit.oak.stats.StatisticsProvider;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static org.apache.jackrabbit.oak.spi.mount.Mounts.defaultMountInfoProvider;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class HybridIndexTest extends AbstractQueryTest {
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder(new File("target"));
    private OptionalEditorProvider optionalEditorProvider = new OptionalEditorProvider();
    private NodeStore nodeStore;
    private DocumentQueue queue;
    private Clock clock = new Clock.Virtual();
    private Whiteboard wb;

    private long refreshDelta = TimeUnit.SECONDS.toMillis(1);

    @After
    public void tearDown(){
        new ExecutorCloser(executorService).close();
    }

    @Override
    protected ContentRepository createRepository() {
        IndexCopier copier;
        try {
            copier = new IndexCopier(executorService, temporaryFolder.getRoot());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MountInfoProvider mip = defaultMountInfoProvider();

        NRTIndexFactory nrtIndexFactory = new NRTIndexFactory(copier, clock, TimeUnit.MILLISECONDS.toSeconds(refreshDelta), StatisticsProvider.NOOP);
        LuceneIndexReaderFactory indexReaderFactory = new DefaultIndexReaderFactory(mip, copier);
        IndexTracker tracker = new IndexTracker(indexReaderFactory,nrtIndexFactory);
        LuceneIndexProvider provider = new LuceneIndexProvider(tracker);
        queue = new DocumentQueue(100, tracker, sameThreadExecutor());
        LuceneIndexEditorProvider editorProvider = new LuceneIndexEditorProvider(copier,
                tracker,
                null,
                null,
                mip);
        editorProvider.setIndexingQueue(queue);

        LocalIndexObserver localIndexObserver = new LocalIndexObserver(queue, StatisticsProvider.NOOP);

        nodeStore = new MemoryNodeStore();
        Oak oak = new Oak(nodeStore)
                .with(new InitialContent())
                .with(new OpenSecurityProvider())
                .with((QueryIndexProvider) provider)
                .with((Observer) provider)
                .with(localIndexObserver)
                .with(editorProvider)
                .with(new PropertyIndexEditorProvider())
                .with(new NodeTypeIndexProvider())
                .with(optionalEditorProvider)
                .with(new NodeCounterEditorProvider())
                //Effectively disable async indexing auto run
                //such that we can control run timing as per test requirement
                .withAsyncIndexing("async", TimeUnit.DAYS.toSeconds(1));

        wb = oak.getWhiteboard();
        return oak.createContentRepository();
    }

    @Test
    public void hybridIndex() throws Exception{
        String idxName = "hybridtest";
        Tree idx = createIndex(root.getTree("/"), idxName, Collections.singleton("foo"));
        TestUtil.enableIndexingMode(idx, IndexingMode.NRT);
        root.commit();

        //Get initial indexing done as local indexing only work
        //for incremental indexing
        createPath("/a").setProperty("foo", "bar");
        root.commit();

        runAsyncIndex();

        setTraversalEnabled(false);
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a"));

        //Add new node. This would not be reflected in result as local index would not be updated
        createPath("/b").setProperty("foo", "bar");
        root.commit();
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a"));

        //Now let some time elapse such that readers can be refreshed
        clock.waitUntil(clock.getTime() + refreshDelta + 1);

        //Now recently added stuff should be visible without async indexing run
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a", "/b"));

        createPath("/c").setProperty("foo", "bar");
        root.commit();

        //Post async index it should still be upto date
        runAsyncIndex();
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a", "/b", "/c"));
    }

    @Test
    public void noTextExtractionForSyncCommit() throws Exception{
        String idxName = "hybridtest";
        Tree idx = createFulltextIndex(root.getTree("/"), idxName);
        TestUtil.enableIndexingMode(idx, IndexingMode.NRT);
        root.commit();

        runAsyncIndex();

        AccessRecordingBlob testBlob =
                new AccessRecordingBlob("<?xml version=\"1.0\" encoding=\"UTF-8\"?><msg>sky is blue</msg>".getBytes());

        Tree test = root.getTree("/").addChild("test");
        TestUtil.createFileNode(test, "msg", testBlob, "application/xml");
        root.commit();

        assertEquals(0, testBlob.accessCount);
        assertQuery("select * from [nt:base] where CONTAINS(*, 'sky')", Collections.<String>emptyList());

        runAsyncIndex();
        assertEquals(1, testBlob.accessCount);
        assertQuery("select * from [nt:base] where CONTAINS(*, 'sky')", of("/test/msg/jcr:content"));

    }

    @Test
    public void hybridIndexSync() throws Exception{
        String idxName = "hybridtest";
        Tree idx = createIndex(root.getTree("/"), idxName, Collections.singleton("foo"));
        TestUtil.enableIndexingMode(idx, IndexingMode.SYNC);
        root.commit();

        //Get initial indexing done as local indexing only work
        //for incremental indexing
        createPath("/a").setProperty("foo", "bar");
        root.commit();

        runAsyncIndex();

        setTraversalEnabled(false);
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a"));

        //Add new node. This should get immediately reelected as its a sync index
        createPath("/b").setProperty("foo", "bar");
        root.commit();
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a", "/b"));
    }

    @Test
    public void usageBeforeFirstIndex() throws Exception{
        String idxName = "hybridtest";
        Tree idx = createIndex(root.getTree("/"), idxName, Collections.singleton("foo"));
        TestUtil.enableIndexingMode(idx, IndexingMode.SYNC);
        root.commit();

        createPath("/a").setProperty("foo", "bar");
        root.commit();
        setTraversalEnabled(false);
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a"));

        //Add new node. This should get immediately reelected as its a sync index
        createPath("/b").setProperty("foo", "bar");
        root.commit();
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a", "/b"));

        runAsyncIndex();
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a", "/b"));

        createPath("/c").setProperty("foo", "bar");
        root.commit();
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a", "/b", "/c"));
    }

    @Test
    public void newNodeTypesFoundLater() throws Exception{
        String idxName = "hybridtest";
        Tree idx = createIndex(root.getTree("/"), idxName, ImmutableSet.of("foo", "bar"));
        TestUtil.enableIndexingMode(idx, IndexingMode.SYNC);
        root.commit();

        setTraversalEnabled(false);

        createPath("/a").setProperty("foo", "bar");
        root.commit();
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a"));

        optionalEditorProvider.delegate = new TypeEditorProvider(false);
        NodeTypeRegistry.register(root, IOUtils.toInputStream(TestUtil.TEST_NODE_TYPE), "test nodeType");
        root.refresh();

        Tree b = createPath("/b");
        b.setProperty(JcrConstants.JCR_PRIMARYTYPE, "oak:TestNode", Type.NAME);
        b.setProperty("bar", "foo");
        root.commit();
        assertQuery("select [jcr:path] from [nt:base] where [bar] = 'foo'", of("/b"));
    }

    @Test
    public void newNodeTypesFoundLater2() throws Exception{
        String idxName = "hybridtest";
        IndexDefinitionBuilder idx = new IndexDefinitionBuilder();
        idx.indexRule("oak:TestNode")
                .property(JcrConstants.JCR_PRIMARYTYPE).propertyIndex();
        idx.indexRule("nt:base")
                .property("foo").propertyIndex()
                .property("bar").propertyIndex();
        idx.async("async","sync");
        idx.build(root.getTree("/").getChild("oak:index").addChild(idxName));

        //By default nodetype index indexes every nodetype. Declare a specific list
        //such that it does not indexes test nodetype
        Tree nodeType = root.getTree("/oak:index/nodetype");
        if (!nodeType.hasProperty(IndexConstants.DECLARING_NODE_TYPES)){
            nodeType.setProperty(IndexConstants.DECLARING_NODE_TYPES, ImmutableList.of("nt:file"), Type.NAMES);
            nodeType.setProperty(IndexConstants.REINDEX_PROPERTY_NAME, true);
        }

        root.commit();

        setTraversalEnabled(false);

        createPath("/a").setProperty("foo", "bar");
        root.commit();
        assertQuery("select [jcr:path] from [nt:base] where [foo] = 'bar'", of("/a"));

        optionalEditorProvider.delegate = new TypeEditorProvider(false);
        NodeTypeRegistry.register(root, IOUtils.toInputStream(TestUtil.TEST_NODE_TYPE), "test nodeType");
        root.refresh();

        Tree b = createPath("/b");
        b.setProperty(JcrConstants.JCR_PRIMARYTYPE, "oak:TestNode", Type.NAME);
        b.setProperty("bar", "foo");

        Tree c = createPath("/c");
        c.setProperty(JcrConstants.JCR_PRIMARYTYPE, "oak:TestNode", Type.NAME);
        root.commit();

        String query = "select [jcr:path] from [oak:TestNode] ";
        assertThat(explain(query), containsString("/oak:index/hybridtest"));
        assertQuery(query, of("/b", "/c"));
    }

    private String explain(String query){
        String explain = "explain " + query;
        return executeQuery(explain, "JCR-SQL2").get(0);
    }

    private void runAsyncIndex() {
        Runnable async = WhiteboardUtils.getService(wb, Runnable.class, new Predicate<Runnable>() {
            @Override
            public boolean apply(@Nullable Runnable input) {
                return input instanceof AsyncIndexUpdate;
            }
        });
        assertNotNull(async);
        async.run();
        root.refresh();
    }

    private Tree createPath(String path){
        Tree base = root.getTree("/");
        for (String e : PathUtils.elements(path)){
            base = base.addChild(e);
        }
        return base;
    }

    private static Tree createIndex(Tree index, String name, Set<String> propNames){
        IndexDefinitionBuilder idx = new IndexDefinitionBuilder();
        IndexRule rule = idx.indexRule("nt:base");
        for (String propName : propNames){
            rule.property(propName).propertyIndex();
        }
        Tree idxTree = index.getChild("oak:index").addChild(name);
        idx.build(idxTree);
        return idxTree;
    }

    private static Tree createFulltextIndex(Tree index, String name){
        IndexDefinitionBuilder idx = new IndexDefinitionBuilder();
        idx.evaluatePathRestrictions();
        idx.indexRule("nt:base")
                .property(LuceneIndexConstants.REGEX_ALL_PROPS, true)
                .analyzed()
                .nodeScopeIndex()
                .useInExcerpt();
        Tree idxTree = index.getChild("oak:index").addChild(name);
        idx.build(idxTree);
        return idxTree;
    }

    private static class AccessRecordingBlob extends ArrayBasedBlob {
        int accessCount = 0;
        public AccessRecordingBlob(byte[] value) {
            super(value);
        }

        @Nonnull
        @Override
        public InputStream getNewStream() {
            accessCount++;
            return super.getNewStream();
        }
    }
}
