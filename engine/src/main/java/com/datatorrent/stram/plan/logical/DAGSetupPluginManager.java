/**
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
package com.datatorrent.stram.plan.logical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import org.apache.apex.api.ApexPluginContext;
import org.apache.apex.api.DAGSetupPlugin;
import org.apache.apex.engine.plugin.loaders.PropertyBasedPluginLocator;
import org.apache.hadoop.conf.Configuration;

import com.google.common.collect.Maps;

import com.datatorrent.api.Attribute;
import com.datatorrent.api.DAG;

import static org.slf4j.LoggerFactory.getLogger;

public class DAGSetupPluginManager
{
  private static final Logger LOG = getLogger(DAGSetupPluginManager.class);

  // Internal event types
  public static final ApexPluginContext.EventType<DAG> SETUP = new ApexPluginContext.EventType<>();
  public static final ApexPluginContext.EventType<Void> DESTROY = new ApexPluginContext.EventType<>();

  private final transient List<DAGSetupPlugin> plugins = new ArrayList<>();
  private Configuration conf;

  public static final String DAGSETUP_PLUGINS_CONF_KEY = "apex.plugin.dag.setup";
  private DAGSetupPlugin.DAGSetupPluginContext context;

  private Map<ApexPluginContext.EventType, List<ApexPluginContext.Handler>> eventHandlers = Maps.newHashMap();

  private void loadVisitors(Configuration conf)
  {
    this.conf = conf;
    if (!plugins.isEmpty()) {
      return;
    }

    PropertyBasedPluginLocator<DAGSetupPlugin> locator = new PropertyBasedPluginLocator<>(DAGSetupPlugin.class, DAGSETUP_PLUGINS_CONF_KEY);
    this.plugins.addAll(locator.discoverPlugins(conf));
  }

  private class DefaultDAGSetupPluginContext implements DAGSetupPlugin.DAGSetupPluginContext
  {
    private final DAG dag;
    private final Configuration conf;

    public DefaultDAGSetupPluginContext(DAG dag, Configuration conf)
    {
      this.dag = dag;
      this.conf = conf;
    }

    @Override
    public <T> void register(EventType<T> type, Handler<T> handler)
    {
      List<Handler> handlers = eventHandlers.get(type);
      if (handlers == null) {
        handlers = new ArrayList<>();
        eventHandlers.put(type, handlers);
      }
      handlers.add(handler);
    }

    public DAG getDAG()
    {
      return dag;
    }

    public Configuration getConfiguration()
    {
      return conf;
    }

    @Override
    public Attribute.AttributeMap getAttributes()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T getValue(Attribute<T> key)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setCounters(Object counters)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendMetrics(Collection<String> metricNames)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  public <T> void dispatch(ApexPluginContext.EventType<T> type, T data)
  {
    if (type == SETUP) {
      context = new DefaultDAGSetupPluginContext((DAG)data, conf);
    }
    if ((type == SETUP) || (type == DESTROY)) {
      for (DAGSetupPlugin plugin : plugins) {
        if (type == SETUP) {
          plugin.setup(context);
        } else if (type == DESTROY) {
          plugin.teardown();
        }
      }
    } else {
      List<ApexPluginContext.Handler> handlers = eventHandlers.get(type);
      for (ApexPluginContext.Handler handler : handlers) {
        handler.handle(type, data);
      }
    }
  }

  public static synchronized DAGSetupPluginManager getInstance(Configuration conf)
  {
    DAGSetupPluginManager manager = new DAGSetupPluginManager();
    manager.loadVisitors(conf);
    return manager;
  }
}
