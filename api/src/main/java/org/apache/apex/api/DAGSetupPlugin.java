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
package org.apache.apex.api;

import java.io.Serializable;

import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.DAG;

/**
 * DAGSetupPlugin allows user provided code to run at various stages
 * during DAG preparation. Currently following stages are supported
 *
 * <ul>
 *   <li>Before dag is populated</li>
 *   <li>After dag is populated</li>
 *   <li>Before dag is configured</li>
 *   <li>After dag is configured</li>
 *   <li>Before dag is validated</li>
 *   <li>After dag is validated</li>
 * </ul>
 */
@InterfaceStability.Evolving
public interface DAGSetupPlugin extends ApexPlugin<DAGSetupPlugin.DAGSetupPluginContext>, Serializable
{
  interface DAGSetupPluginContext extends ApexPluginContext
  {
    /**
     * This event is sent before platform adds operators and streams in the DAG. i.e this method
     * will get called just before {@link com.datatorrent.api.StreamingApplication#populateDAG(DAG, Configuration)}
     *
     * For Application specified using property and json file format, this will be sent
     * before platform adds operators and streams in the DAG as per specification in the file.
     */
    EventType<Void> PRE_POPULATE_DAG = new EventType<>();

    /**
     * This event is sent after platform adds operators and streams in the DAG. i.e this method
     * will get called just after {@link com.datatorrent.api.StreamingApplication#populateDAG(DAG, Configuration)}
     * in case application is specified in java.
     *
     * For Application specified using property and json file format, this will be sent
     * after platform has added operators and streams in the DAG as per specification in the file.
     */
    EventType<Void> POST_POPULATE_DAG = new EventType<>();

    /**
     * This event is sent before DAG is configured, i.e operator and application
     * properties/attributes are injected from configuration files.
     */
    EventType<Void> PRE_CONFIGURE_DAG = new EventType<>();

    /**
     * This event is sent after DAG is configured, i.e operator and application
     * properties/attributes are injected from configuration files.
     */
    EventType<Void> POST_CONFIGURE_DAG = new EventType<>();

    /**
     * This event is sent just before dag is validated before final job submission.
     */
    EventType<Void> PRE_VALIDATE_DAG = new EventType<>();

    /**
     * This event is sent after dag is validated. If plugin makes in incompatible changes
     * to the DAG at this stage, then application may get launched incorrectly or application
     * launch may fail.
     */
    EventType<Void> POST_VALIDATE_DAG = new EventType<>();

    DAG getDAG();

    Configuration getConfiguration();
  }
}
