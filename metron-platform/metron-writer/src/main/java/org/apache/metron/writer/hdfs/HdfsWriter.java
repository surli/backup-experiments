/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.writer.hdfs;

import org.apache.storm.tuple.Tuple;
import org.apache.metron.common.configuration.EnrichmentConfigurations;
import org.apache.metron.common.configuration.writer.WriterConfiguration;
import org.apache.metron.common.writer.BulkMessageWriter;
import org.apache.metron.common.writer.BulkWriterResponse;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.NoRotationPolicy;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.hdfs.common.rotation.RotationAction;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class HdfsWriter implements BulkMessageWriter<JSONObject>, Serializable {
  List<RotationAction> rotationActions = new ArrayList<>();
  FileRotationPolicy rotationPolicy = new NoRotationPolicy();
  SyncPolicy syncPolicy = new CountSyncPolicy(1); //sync every time, duh.
  FileNameFormat fileNameFormat;
  Map<String, SourceHandler> sourceHandlerMap = new HashMap<>();
  transient Map stormConfig;
  public HdfsWriter withFileNameFormat(FileNameFormat fileNameFormat){
    this.fileNameFormat = fileNameFormat;
    return this;
  }

  public HdfsWriter withSyncPolicy(SyncPolicy syncPolicy){
    this.syncPolicy = syncPolicy;
    return this;
  }
  public HdfsWriter withRotationPolicy(FileRotationPolicy rotationPolicy){
    this.rotationPolicy = rotationPolicy;
    return this;
  }

  public HdfsWriter addRotationAction(RotationAction action){
    this.rotationActions.add(action);
    return this;
  }

  @Override
  public void init(Map stormConfig, WriterConfiguration configurations) {
    this.stormConfig = stormConfig;
  }


  @Override
  public BulkWriterResponse write(String sourceType
                   , WriterConfiguration configurations
                   , Iterable<Tuple> tuples
                   , List<JSONObject> messages
                   ) throws Exception
  {
    BulkWriterResponse response = new BulkWriterResponse();
    SourceHandler handler = getSourceHandler(configurations.getIndex(sourceType));
    try {
      handler.handle(messages);
    } catch(Exception e) {
      response.addAllErrors(e, tuples);
    }

    response.addAllSuccesses(tuples);
    return response;
  }

  @Override
  public String getName() {
    return "hdfs";
  }

  @Override
  public void close() {
    for(SourceHandler handler : sourceHandlerMap.values()) {
      handler.close();
    }
  }
  private synchronized SourceHandler getSourceHandler(String sourceType) throws IOException {
    SourceHandler ret = sourceHandlerMap.get(sourceType);
    if(ret == null) {
      ret = new SourceHandler(rotationActions, rotationPolicy, syncPolicy, new SourceFileNameFormat(sourceType, fileNameFormat), stormConfig);
      sourceHandlerMap.put(sourceType, ret);
    }
    return ret;
  }
}
