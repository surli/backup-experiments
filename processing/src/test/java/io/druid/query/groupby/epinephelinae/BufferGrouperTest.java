/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.druid.query.groupby.epinephelinae;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import io.druid.data.input.MapBasedRow;
import io.druid.java.util.common.IAE;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.aggregation.CountAggregatorFactory;
import io.druid.query.aggregation.LongSumAggregatorFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;

public class BufferGrouperTest
{
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testSimple()
  {
    final TestColumnSelectorFactory columnSelectorFactory = GrouperTestUtil.newColumnSelectorFactory();
    final Grouper<Integer> grouper = new BufferGrouper<>(
        ByteBuffer.allocate(1000),
        GrouperTestUtil.intKeySerde(),
        columnSelectorFactory,
        new AggregatorFactory[]{
            new LongSumAggregatorFactory("valueSum", "value"),
            new CountAggregatorFactory("count")
        },
        Integer.MAX_VALUE,
        0,
        0,
        -1,
        false
    );

    columnSelectorFactory.setRow(new MapBasedRow(0, ImmutableMap.<String, Object>of("value", 10L)));
    grouper.aggregate(12);
    grouper.aggregate(6);
    grouper.aggregate(10);
    grouper.aggregate(6);
    grouper.aggregate(12);
    grouper.aggregate(12);

    final List<Grouper.Entry<Integer>> expected = ImmutableList.of(
        new Grouper.Entry<>(6, new Object[]{20L, 2L}),
        new Grouper.Entry<>(10, new Object[]{10L, 1L}),
        new Grouper.Entry<>(12, new Object[]{30L, 3L})
    );
    final List<Grouper.Entry<Integer>> unsortedEntries = Lists.newArrayList(grouper.iterator(false));
    final List<Grouper.Entry<Integer>> sortedEntries = Lists.newArrayList(grouper.iterator(true));

    Assert.assertEquals(expected, sortedEntries);
    Assert.assertEquals(
        expected,
        Ordering.from(
            new Comparator<Grouper.Entry<Integer>>()
            {
              @Override
              public int compare(Grouper.Entry<Integer> o1, Grouper.Entry<Integer> o2)
              {
                return Ints.compare(o1.getKey(), o2.getKey());
              }
            }
        ).sortedCopy(unsortedEntries)
    );
  }

  @Test
  public void testGrowing()
  {
    final TestColumnSelectorFactory columnSelectorFactory = GrouperTestUtil.newColumnSelectorFactory();
    final Grouper<Integer> grouper = makeGrouper(columnSelectorFactory, 10000, 2, -1);
    final int expectedMaxSize = 219;

    columnSelectorFactory.setRow(new MapBasedRow(0, ImmutableMap.<String, Object>of("value", 10L)));
    for (int i = 0; i < expectedMaxSize; i++) {
      Assert.assertTrue(String.valueOf(i), grouper.aggregate(i));
    }
    Assert.assertFalse(grouper.aggregate(expectedMaxSize));

    // Aggregate slightly different row
    columnSelectorFactory.setRow(new MapBasedRow(0, ImmutableMap.<String, Object>of("value", 11L)));
    for (int i = 0; i < expectedMaxSize; i++) {
      Assert.assertTrue(String.valueOf(i), grouper.aggregate(i));
    }
    Assert.assertFalse(grouper.aggregate(expectedMaxSize));

    final List<Grouper.Entry<Integer>> expected = Lists.newArrayList();
    for (int i = 0; i < expectedMaxSize; i++) {
      expected.add(new Grouper.Entry<>(i, new Object[]{21L, 2L}));
    }

    Assert.assertEquals(expected, Lists.newArrayList(grouper.iterator(true)));
  }

  @Test
  public void testNoGrowing()
  {
    final TestColumnSelectorFactory columnSelectorFactory = GrouperTestUtil.newColumnSelectorFactory();
    final Grouper<Integer> grouper = makeGrouper(columnSelectorFactory, 10000, Integer.MAX_VALUE, -1);
    final int expectedMaxSize = 267;

    columnSelectorFactory.setRow(new MapBasedRow(0, ImmutableMap.<String, Object>of("value", 10L)));
    for (int i = 0; i < expectedMaxSize; i++) {
      Assert.assertTrue(String.valueOf(i), grouper.aggregate(i));
    }
    Assert.assertFalse(grouper.aggregate(expectedMaxSize));

    // Aggregate slightly different row
    columnSelectorFactory.setRow(new MapBasedRow(0, ImmutableMap.<String, Object>of("value", 11L)));
    for (int i = 0; i < expectedMaxSize; i++) {
      Assert.assertTrue(String.valueOf(i), grouper.aggregate(i));
    }
    Assert.assertFalse(grouper.aggregate(expectedMaxSize));

    final List<Grouper.Entry<Integer>> expected = Lists.newArrayList();
    for (int i = 0; i < expectedMaxSize; i++) {
      expected.add(new Grouper.Entry<>(i, new Object[]{21L, 2L}));
    }

    Assert.assertEquals(expected, Lists.newArrayList(grouper.iterator(true)));
  }

  @Test
  public void testBufferGrouperWithLimitAndBufferSwapping()
  {
    final int limit = 100;
    final int keyBase = 100000;
    final TestColumnSelectorFactory columnSelectorFactory = GrouperTestUtil.newColumnSelectorFactory();
    final BufferGrouper<Integer> grouper = makeGrouper(columnSelectorFactory, 10000, 2, limit);
    final int numRows = 1000;

    columnSelectorFactory.setRow(new MapBasedRow(0, ImmutableMap.<String, Object>of("value", 10L)));
    for (int i = 0; i < numRows; i++) {
      Assert.assertTrue(String.valueOf(i + keyBase), grouper.aggregate(i + keyBase));
    }

    // bucket size is hash(int) + key(int) + aggs(2 longs) + heap offset(int) = 28 bytes
    // limit is 100 so heap occupies 101 * 4 bytes = 404 bytes
    // buffer is 10000 bytes, so table arena size is 10000 - 404 = 9596 bytes
    // table arena is split in halves when doing push down, so each half is 4798 bytes
    // each table arena half can hold 4798 / 28 = 171 buckets
    // First buffer swap occurs when we hit 171 buckets
    // Subsequent buffer swaps occur after every 71 buckets, since we keep 100 buckets due to the limit
    // With 1000 keys inserted, this results in one swap at the first 171 buckets, then 11 swaps afterwards.
    // After the last swap, we have 100 keys + 48 new keys inserted.
    Assert.assertEquals(12, grouper.getGrowthCount());
    Assert.assertEquals(148, grouper.getSize());
    Assert.assertEquals(171, grouper.getBuckets());
    Assert.assertEquals(171, grouper.getMaxSize());
    Assert.assertEquals(100, grouper.getLimit());

    // Aggregate slightly different row
    // Since these keys are smaller, they will evict the previous 100 top entries
    // First 100 of these new rows will be the expected results.
    columnSelectorFactory.setRow(new MapBasedRow(0, ImmutableMap.<String, Object>of("value", 11L)));
    for (int i = 0; i < numRows; i++) {
      Assert.assertTrue(String.valueOf(i), grouper.aggregate(i));
    }

    // we added another 1000 unique keys
    // previous size is 148, so next swap occurs after 23 rows
    // after that, there are 1000 - 23 = 977 rows, 977 / 71 = 13 additional swaps,
    // with 54 keys being added after the final swap.
    Assert.assertEquals(26, grouper.getGrowthCount());
    Assert.assertEquals(154, grouper.getSize());
    Assert.assertEquals(171, grouper.getBuckets());
    Assert.assertEquals(171, grouper.getMaxSize());
    Assert.assertEquals(100, grouper.getLimit());

    final List<Grouper.Entry<Integer>> expected = Lists.newArrayList();
    for (int i = 0; i < limit; i++) {
      expected.add(new Grouper.Entry<>(i, new Object[]{11L, 1L}));
    }

    Assert.assertEquals(expected, Lists.newArrayList(grouper.iterator(true)));
  }

  @Test
  public void testBufferGrouperWithLimitBufferTooSmall()
  {
    expectedException.expect(IAE.class);
    final TestColumnSelectorFactory columnSelectorFactory = GrouperTestUtil.newColumnSelectorFactory();
    final BufferGrouper<Integer> grouper = makeGrouper(columnSelectorFactory, 10, 2, 100);
  }

  @Test
  public void testBufferGrouperWithLimitMinBufferSize()
  {
    final int limit = 100;
    final int keyBase = 100000;
    final TestColumnSelectorFactory columnSelectorFactory = GrouperTestUtil.newColumnSelectorFactory();
    final BufferGrouper<Integer> grouper = makeGrouper(columnSelectorFactory, 6060, 2, limit);
    final int numRows = 1000;

    columnSelectorFactory.setRow(new MapBasedRow(0, ImmutableMap.<String, Object>of("value", 10L)));
    for (int i = 0; i < numRows; i++) {
      Assert.assertTrue(String.valueOf(i + keyBase), grouper.aggregate(i + keyBase));
    }

    // With minimum buffer size, after the first swap, every new key added will result in a swap
    Assert.assertEquals(899, grouper.getGrowthCount());
    Assert.assertEquals(101, grouper.getSize());
    Assert.assertEquals(101, grouper.getBuckets());
    Assert.assertEquals(101, grouper.getMaxSize());
    Assert.assertEquals(100, grouper.getLimit());

    // Aggregate slightly different row
    // Since these keys are smaller, they will evict the previous 100 top entries
    // First 100 of these new rows will be the expected results.
    columnSelectorFactory.setRow(new MapBasedRow(0, ImmutableMap.<String, Object>of("value", 11L)));
    for (int i = 0; i < numRows; i++) {
      Assert.assertTrue(String.valueOf(i), grouper.aggregate(i));
    }

    Assert.assertEquals(1899, grouper.getGrowthCount());
    Assert.assertEquals(101, grouper.getSize());
    Assert.assertEquals(101, grouper.getBuckets());
    Assert.assertEquals(101, grouper.getMaxSize());
    Assert.assertEquals(100, grouper.getLimit());

    final List<Grouper.Entry<Integer>> expected = Lists.newArrayList();
    for (int i = 0; i < limit; i++) {
      expected.add(new Grouper.Entry<>(i, new Object[]{11L, 1L}));
    }

    Assert.assertEquals(expected, Lists.newArrayList(grouper.iterator(true)));
  }

  private static BufferGrouper<Integer> makeGrouper(
      TestColumnSelectorFactory columnSelectorFactory,
      int bufferSize,
      int initialBuckets,
      int limit
  )
  {
    return new BufferGrouper<>(
        ByteBuffer.allocate(bufferSize),
        GrouperTestUtil.intKeySerde(),
        columnSelectorFactory,
        new AggregatorFactory[]{
            new LongSumAggregatorFactory("valueSum", "value"),
            new CountAggregatorFactory("count")
        },
        Integer.MAX_VALUE,
        0.75f,
        initialBuckets,
        limit,
        false
    );
  }
}
