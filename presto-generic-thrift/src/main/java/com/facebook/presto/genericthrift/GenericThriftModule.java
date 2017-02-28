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

import com.facebook.presto.genericthrift.client.ThriftPrestoClient;
import com.facebook.presto.spi.type.TypeManager;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import static com.facebook.swift.service.guice.ThriftClientBinder.thriftClientBinder;
import static io.airlift.configuration.ConfigBinder.configBinder;
import static java.util.Objects.requireNonNull;

public class GenericThriftModule
        implements Module
{
    private final TypeManager typeManager;

    public GenericThriftModule(TypeManager typeManager)
    {
        this.typeManager = requireNonNull(typeManager, "typeManager is null");
    }

    @Override
    public void configure(Binder binder)
    {
        binder.bind(GenericThriftConnector.class).in(Scopes.SINGLETON);
        thriftClientBinder(binder).bindThriftClient(ThriftPrestoClient.class);
        binder.bind(GenericThriftMetadata.class).in(Scopes.SINGLETON);
        binder.bind(GenericThriftSplitManager.class).in(Scopes.SINGLETON);
        binder.bind(GenericThriftPageSourceProvider.class).in(Scopes.SINGLETON);
        binder.bind(TypeManager.class).toInstance(typeManager);
        binder.bind(PrestoClientProvider.class).to(PrestoThriftClientProvider.class).in(Scopes.SINGLETON);
        configBinder(binder).bindConfig(GenericThriftConfig.class);
        binder.bind(GenericThriftInternalSessionProperties.class).in(Scopes.SINGLETON);
        binder.bind(GenericThriftClientSessionProperties.class).in(Scopes.SINGLETON);
    }
}
