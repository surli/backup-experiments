/*
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
package com.facebook.presto.genericthrift;

import com.facebook.presto.spi.connector.Connector;
import com.facebook.presto.spi.connector.ConnectorIndexProvider;
import com.facebook.presto.spi.connector.ConnectorMetadata;
import com.facebook.presto.spi.connector.ConnectorPageSourceProvider;
import com.facebook.presto.spi.connector.ConnectorSplitManager;
import com.facebook.presto.spi.connector.ConnectorTransactionHandle;
import com.facebook.presto.spi.session.PropertyMetadata;
import com.facebook.presto.spi.transaction.IsolationLevel;
import com.google.common.collect.ImmutableList;
import io.airlift.bootstrap.LifeCycleManager;
import io.airlift.log.Logger;

import javax.inject.Inject;

import java.util.List;

import static com.facebook.presto.genericthrift.GenericThriftTransactionHandle.INSTANCE;
import static java.util.Objects.requireNonNull;

public class GenericThriftConnector
        implements Connector
{
    private static final Logger log = Logger.get(GenericThriftConnector.class);
    private final LifeCycleManager lifeCycleManager;
    private final GenericThriftMetadata metadata;
    private final GenericThriftSplitManager splitManager;
    private final GenericThriftPageSourceProvider pageSourceProvider;
    private final GenericThriftInternalSessionProperties internalSessionProperties;
    private final GenericThriftClientSessionProperties clientSessionProperties;
    private final GenericThriftIndexProvider indexProvider;

    @Inject
    public GenericThriftConnector(LifeCycleManager lifeCycleManager,
            GenericThriftMetadata metadata,
            GenericThriftSplitManager splitManager,
            GenericThriftPageSourceProvider pageSourceProvider,
            GenericThriftInternalSessionProperties internalSessionProperties,
            GenericThriftClientSessionProperties clientSessionProperties,
            GenericThriftIndexProvider indexProvider)
    {
        this.lifeCycleManager = requireNonNull(lifeCycleManager, "lifeCycleManager is null");
        this.metadata = requireNonNull(metadata, "metadata is null");
        this.splitManager = requireNonNull(splitManager, "splitManager is null");
        this.pageSourceProvider = requireNonNull(pageSourceProvider, "pageSourceProvider is null");
        this.internalSessionProperties = requireNonNull(internalSessionProperties, "internalSessionProperties is null");
        this.clientSessionProperties = requireNonNull(clientSessionProperties, "clientSessionProperties is null");
        this.indexProvider = requireNonNull(indexProvider, "indexProvider is null");
    }

    @Override
    public ConnectorTransactionHandle beginTransaction(IsolationLevel isolationLevel, boolean readOnly)
    {
        return INSTANCE;
    }

    @Override
    public ConnectorMetadata getMetadata(ConnectorTransactionHandle transactionHandle)
    {
        return metadata;
    }

    @Override
    public ConnectorSplitManager getSplitManager()
    {
        return splitManager;
    }

    @Override
    public ConnectorPageSourceProvider getPageSourceProvider()
    {
        return pageSourceProvider;
    }

    @Override
    public List<PropertyMetadata<?>> getSessionProperties()
    {
        return ImmutableList.<PropertyMetadata<?>>builder()
                .addAll(internalSessionProperties.getSessionProperties())
                .addAll(clientSessionProperties.getSessionProperties())
                .build();
    }

    @Override
    public ConnectorIndexProvider getIndexProvider()
    {
        return indexProvider;
    }

    @Override
    public final void shutdown()
    {
        try {
            lifeCycleManager.stop();
        }
        catch (Exception e) {
            log.error(e, "Error shutting down connector");
        }
    }
}
