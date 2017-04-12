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
package org.apache.metron.enrichment.bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.metron.common.bolt.ConfiguredBolt;
import org.apache.metron.common.bolt.ConfiguredEnrichmentBolt;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class SplitBolt<T extends Cloneable> extends
        ConfiguredEnrichmentBolt {

  protected OutputCollector collector;

  public SplitBolt(String zookeeperUrl) {
    super(zookeeperUrl);
  }

  @Override
  public final void prepare(Map map, TopologyContext topologyContext,
                       OutputCollector outputCollector) {
    super.prepare(map, topologyContext, outputCollector);
    collector = outputCollector;
    prepare(map, topologyContext);
  }

  @Override
  public final void execute(Tuple tuple) {
    emit(tuple, generateMessage(tuple));
  }

  @Override
  public final void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declareStream("message", new Fields("key", "message", "subgroup"));
    for (String streamId : getStreamIds()) {
      declarer.declareStream(streamId, new Fields("key", "message"));
    }
    declarer.declareStream("error", new Fields("message"));
    declareOther(declarer);
  }

  public void emit(Tuple tuple, T message) {
    if (message == null) return;
    String key = getKey(tuple, message);
    Map<String, List<T>> streamMessageMap = splitMessage(message);
    for (String streamId : streamMessageMap.keySet()) {
      List<T> streamMessages = streamMessageMap.get(streamId);
      if(streamMessages != null) {
        for(T streamMessage : streamMessages) {
          if (streamMessage == null) {
            streamMessage = getDefaultMessage(streamId);
          }
          collector.emit(streamId, new Values(key, streamMessage));
        }
      }
      else {
        throw new IllegalArgumentException("Enrichment must send some list of messages, not null.");
      }
    }
    collector.emit("message", tuple, new Values(key, message, ""));
    collector.ack(tuple);
    emitOther(tuple, message);
  }

  protected T getDefaultMessage(String streamId) {
    throw new IllegalArgumentException("Could not find a message for" +
            " stream: " + streamId);
  }

  public abstract void prepare(Map map, TopologyContext topologyContext);

  public abstract Set<String> getStreamIds();

  public abstract String getKey(Tuple tuple, T message);

  public abstract T generateMessage(Tuple tuple);

  public abstract Map<String, List<T>> splitMessage(T message);

  public abstract void declareOther(OutputFieldsDeclarer declarer);

  public abstract void emitOther(Tuple tuple, T message);


}
