/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.metron.hbase.client;

import org.apache.storm.tuple.Tuple;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.metron.hbase.Widget;
import org.apache.metron.hbase.WidgetMapper;
import org.apache.metron.hbase.bolt.mapper.ColumnList;
import org.apache.metron.hbase.bolt.mapper.HBaseMapper;
import org.apache.metron.hbase.bolt.mapper.HBaseProjectionCriteria;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.metron.hbase.WidgetMapper.CF;
import static org.apache.metron.hbase.WidgetMapper.QCOST;
import static org.apache.metron.hbase.WidgetMapper.QNAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the HBaseClient
 */
public class HBaseClientTest {

  private static final String tableName = "widgets";

  private static HBaseTestingUtility util;
  private HBaseClient client;
  private HTableInterface table;
  private Tuple tuple1;
  private Tuple tuple2;
  byte[] rowKey1;
  byte[] rowKey2;
  ColumnList cols1;
  ColumnList cols2;
  private Widget widget1;
  private Widget widget2;
  private HBaseMapper mapper;

  @BeforeClass
  public static void startHBase() throws Exception {
    Configuration config = HBaseConfiguration.create();
    config.set("hbase.master.hostname", "localhost");
    config.set("hbase.regionserver.hostname", "localhost");
    util = new HBaseTestingUtility(config);
    util.startMiniCluster();
  }

  @AfterClass
  public static void stopHBase() throws Exception {
    util.shutdownMiniCluster();
    util.cleanupTestDir();
  }

  @Before
  public void setupTuples() throws Exception {

    // setup the first tuple
    widget1 = new Widget("widget1", 100);
    tuple1 = mock(Tuple.class);
    when(tuple1.getValueByField(eq("widget"))).thenReturn(widget1);

    rowKey1 = mapper.rowKey(tuple1);
    cols1 = mapper.columns(tuple1);

    // setup the second tuple
    widget2 = new Widget("widget2", 200);
    tuple2 = mock(Tuple.class);
    when(tuple2.getValueByField(eq("widget"))).thenReturn(widget2);

    rowKey2 = mapper.rowKey(tuple2);
    cols2 = mapper.columns(tuple2);
  }

  @Before
  public void setup() throws Exception {

    // create a mapper
    mapper = new WidgetMapper();

    // create the table
    table = util.createTable(Bytes.toBytes(tableName), WidgetMapper.CF);
    util.waitTableEnabled(table.getName());

    // setup the client
    client = new HBaseClient((c,t) -> table, table.getConfiguration(), tableName);
  }

  @After
  public void tearDown() throws Exception {
    util.deleteTable(tableName);
  }

  /**
   * Should be able to read/write a single Widget.
   */
  @Test
  public void testWrite() throws Exception {

    // add a tuple to the batch
    client.addMutation(rowKey1, cols1, Durability.SYNC_WAL);
    client.mutate();

    HBaseProjectionCriteria criteria = new HBaseProjectionCriteria();
    criteria.addColumnFamily(WidgetMapper.CF_STRING);

    // read back the tuple
    client.addGet(rowKey1, criteria);
    Result[] results = client.getAll();
    Assert.assertEquals(1, results.length);

    // validate
    assertEquals(1, results.length);
    assertEquals(widget1, toWidget(results[0]));
  }

  /**
   * Should be able to read/write multiple Widgets in a batch.
   */
  @Test
  public void testBatchWrite() throws Exception {

    // add two mutations to the queue
    client.addMutation(rowKey1, cols1, Durability.SYNC_WAL);
    client.addMutation(rowKey2, cols2, Durability.SYNC_WAL);
    client.mutate();

    HBaseProjectionCriteria criteria = new HBaseProjectionCriteria();
    criteria.addColumnFamily(WidgetMapper.CF_STRING);

    // read back both tuples
    client.addGet(rowKey1, criteria);
    client.addGet(rowKey2, criteria);
    Result[] results = client.getAll();

    // validate
    assertEquals(2, results.length);
    List<Widget> expected = Arrays.asList(widget1, widget2);
    for(Result result : results) {
      Widget widget = toWidget(result);
      Assert.assertTrue(expected.contains(widget));
    }
  }

  /**
   * Should be able to read back widgets that were written with a TTL 30 days out.
   */
  @Test
  public void testWriteWithTimeToLive() throws Exception {
    long timeToLive = TimeUnit.DAYS.toMillis(30);

    // add two mutations to the queue
    client.addMutation(rowKey1, cols1, Durability.SYNC_WAL, timeToLive);
    client.addMutation(rowKey2, cols2, Durability.SYNC_WAL, timeToLive);
    client.mutate();

    HBaseProjectionCriteria criteria = new HBaseProjectionCriteria();
    criteria.addColumnFamily(WidgetMapper.CF_STRING);

    // read back both tuples
    client.addGet(rowKey1, criteria);
    client.addGet(rowKey2, criteria);
    Result[] results = client.getAll();

    // validate
    assertEquals(2, results.length);
    List<Widget> expected = Arrays.asList(widget1, widget2);
    for(Result result : results) {
      Widget widget = toWidget(result);
      Assert.assertTrue(expected.contains(widget));
    }
  }

  /**
   * Should NOT be able to read widgets that are expired due to the TTL.
   */
  @Test
  public void testExpiredWidgets() throws Exception {
    long timeToLive = TimeUnit.MILLISECONDS.toMillis(1);

    // add two mutations to the queue
    client.addMutation(rowKey1, cols1, Durability.SYNC_WAL, timeToLive);
    client.addMutation(rowKey2, cols2, Durability.SYNC_WAL, timeToLive);
    client.mutate();

    HBaseProjectionCriteria criteria = new HBaseProjectionCriteria();
    criteria.addColumnFamily(WidgetMapper.CF_STRING);

    // wait for a second to ensure the TTL has expired
    Thread.sleep(TimeUnit.SECONDS.toMillis(2));

    // read back both tuples
    client.addGet(rowKey1, criteria);
    client.addGet(rowKey2, criteria);
    Result[] results = client.getAll();

    // validate - the TTL should have expired all widgets
    List<Widget> widgets = Arrays
            .stream(results)
            .map(r -> toWidget(r))
            .filter(w -> w != null)
            .collect(Collectors.toList());
    assertEquals(0, widgets.size());
  }

  /**
   * Transforms the HBase Result to a Widget.
   * @param result The HBase Result.
   * @return The Widget.
   */
  private Widget toWidget(Result result) {
    Widget widget = null;

    if(result.containsColumn(CF, QCOST) && result.containsColumn(CF, QNAME)) {
      String name = Bytes.toString(result.getValue(CF, QNAME));
      int cost = Bytes.toInt(result.getValue(CF, QCOST));
      widget = new Widget(name, cost);
    }

    return widget;
  }
}
