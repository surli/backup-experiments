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

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.List;

public class ByteBufferMinMaxOffsetHeapTest
{
  @Test
  public void testSimple()
  {
    int limit = 15;
    ByteBuffer mybuffer = ByteBuffer.allocate(1000000);
    ByteBufferMinMaxOffsetHeap heap = new ByteBufferMinMaxOffsetHeap(mybuffer, limit, Ordering.<Integer>natural(), null);

    int[] values = new int[]{
        30, 45, 81, 92, 68, 54, 66, 33, 89, 98,
        87, 62, 84, 39, 13, 32, 67, 50, 21, 53,
        93, 18, 86, 41, 14, 56, 51, 69, 91, 60,
        6, 2, 79, 4, 35, 17, 71, 22, 29, 76,
        57, 97, 73, 24, 94, 77, 80, 15, 52, 88,
        95, 96, 9, 3, 48, 58, 75, 82, 90, 65,
        36, 85, 20, 34, 37, 72, 11, 78, 28, 43,
        27, 12, 83, 38, 59, 19, 31, 46, 40, 63,
        23, 70, 26, 8, 64, 16, 10, 74, 7, 25,
        5, 42, 47, 44, 1, 49, 99
    };

    for (int i = 0; i < values.length; i++){
      heap.addOffset(values[i]);
    }

    /*
    [1, 15, 13, 2, 3, 5, 4, 7, 14, 10, 9, 8, 6, 12, 11, ]
     */

    int x = heap.deleteAt(8);
    heap.addOffset(x);

    x = heap.deleteAt(2);
    heap.addOffset(x);

    List<Integer> expected = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    List<Integer> actual = Lists.newArrayList();

    for (int i = 0; i < limit; i++) {
      int min = heap.removeMin();
      actual.add(min);
    }

    Assert.assertEquals(expected, actual);
  }
}
