/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.perf.impl;

import org.junit.Assert;
import org.junit.Test;
import org.spf4j.base.Arrays;

/**
 *
 * @author zoly
 */
public final class QuantizedRecorderTest {

  @Test
  public void testMagnitudes() {
    long[] createMagnitudeLimits = QuantizedAccumulator.createMagnitudeLimits(10, -2, 3);
    Assert.assertArrayEquals(new long[]{-100, -10, 0, 10, 100, 1000}, createMagnitudeLimits);

    createMagnitudeLimits = QuantizedAccumulator.createMagnitudeLimits(3, -2, 3);
    Assert.assertArrayEquals(new long[]{-9, -3, 0, 3, 9, 27}, createMagnitudeLimits);

    createMagnitudeLimits = QuantizedAccumulator.createMagnitudeLimits(10, 2, 3);
    Assert.assertArrayEquals(new long[]{100, 1000}, createMagnitudeLimits);

    createMagnitudeLimits = QuantizedAccumulator.createMagnitudeLimits(10, -3, -1);
    Assert.assertArrayEquals(new long[]{-1000, -100, -10}, createMagnitudeLimits);
  }

  @Test
  public void testMagnitudes2() {
    long[] createMagnitudeLimits = QuantizedAccumulator.createMagnitudeLimits2(10, -2, 3);
    Assert.assertArrayEquals(new long[]{-100, -10, 0, 10, 100, 1000}, createMagnitudeLimits);

    createMagnitudeLimits = QuantizedAccumulator.createMagnitudeLimits2(3, -2, 3);
    Assert.assertArrayEquals(new long[]{-9, -3, 0, 3, 9, 27}, createMagnitudeLimits);

    createMagnitudeLimits = QuantizedAccumulator.createMagnitudeLimits2(10, 2, 3);
    Assert.assertArrayEquals(new long[]{100, 1000}, createMagnitudeLimits);

    createMagnitudeLimits = QuantizedAccumulator.createMagnitudeLimits2(10, -3, -1);
    Assert.assertArrayEquals(new long[]{-1000, -100, -10}, createMagnitudeLimits);
  }


  @Test
  public void testFindBucket() {
    long [] bucketLimits = new long [] {-100, -80, -60, -40, -10, -8, -6, -4, -2, 0, 2, 4, 6, 8, 10, 40, 60, 80, 100};
    Assert.assertEquals(0, QuantizedAccumulator.findBucket(bucketLimits, -1000));
    Assert.assertEquals(1, QuantizedAccumulator.findBucket(bucketLimits, -100));
    Assert.assertEquals(1, QuantizedAccumulator.findBucket(bucketLimits, -90));
    Assert.assertEquals(4, QuantizedAccumulator.findBucket(bucketLimits, -40));
    Assert.assertEquals(4, QuantizedAccumulator.findBucket(bucketLimits, -35));
    Assert.assertEquals(10, QuantizedAccumulator.findBucket(bucketLimits, 0));
    Assert.assertEquals(12, QuantizedAccumulator.findBucket(bucketLimits, 5));
    Assert.assertEquals(19, QuantizedAccumulator.findBucket(bucketLimits, 100));
    Assert.assertEquals(19, QuantizedAccumulator.findBucket(bucketLimits, 101));
  }

  /**
   * Test of record method, of class QuantizedAccumulator.
   */
  @Test
  public void testRecord1() {
    System.out.println("record");
    QuantizedAccumulator instance = new QuantizedAccumulator("test", "", "ms",
            10, 0, 3, 10);
    instance.record(-1);
    instance.record(0);
    instance.record(0);
    instance.record(1);
    instance.record(1);
    instance.record(1);
    instance.record(2);
    instance.record(2);
    instance.record(2);
    instance.record(2);
    instance.record(10);
    instance.record(11);
    instance.record(250);
    instance.record(250);
    instance.record(15000);
    instance.record(15000);
    System.out.println(instance);
    Assert.assertEquals(15000, instance.getMaxMeasurement());
    Assert.assertEquals(-1, instance.getMinMeasurement());
    long[] vals = instance.get();
    Assert.assertEquals(instance.getInfo().getNumberOfMeasurements(), vals.length);
    String[] measurementNames = instance.getInfo().getMeasurementNames();
    int niIdx = Arrays.indexOf(measurementNames, "QNI_0");
    Assert.assertEquals(1, vals[niIdx]);
    int q23Idx = Arrays.indexOf(measurementNames, "Q2_3");
    Assert.assertEquals(4, vals[q23Idx]);
    int qPIIdx = Arrays.indexOf(measurementNames, "Q1000_PI");
    Assert.assertEquals(2, vals[qPIIdx]);
    int q2300Idx = Arrays.indexOf(measurementNames, "Q200_300");
    Assert.assertEquals(2, vals[q2300Idx]);
  }

  @Test
  public void testRecord2() {
    System.out.println("record");
    QuantizedAccumulator instance = new QuantizedAccumulator("test", "", "ms",
            10, -3, 3, 10);
    instance.record(-15000);
    instance.record(-15000);
    instance.record(-15000);
    instance.record(-300);
    instance.record(-300);
    instance.record(-1);
    instance.record(0);
    instance.record(0);
    instance.record(1);
    instance.record(1);
    instance.record(1);
    instance.record(2);
    instance.record(2);
    instance.record(2);
    instance.record(2);
    instance.record(10);
    instance.record(11);
    instance.record(250);
    instance.record(250);
    instance.record(15000);
    System.out.println(instance);
    Assert.assertEquals(15000, instance.getMaxMeasurement());
    Assert.assertEquals(-15000, instance.getMinMeasurement());
    long[] vals = instance.get();
    Assert.assertEquals(instance.getInfo().getNumberOfMeasurements(), vals.length);
    String[] measurementNames = instance.getInfo().getMeasurementNames();
    int niIdx = Arrays.indexOf(measurementNames, "QNI_-1000");
    Assert.assertEquals(3, vals[niIdx]);
    int q23Idx = Arrays.indexOf(measurementNames, "Q2_3");
    Assert.assertEquals(4, vals[q23Idx]);
    int n10Idx = Arrays.indexOf(measurementNames, "Q-1_0");
    Assert.assertEquals(1, vals[n10Idx]);
    int qPIIdx = Arrays.indexOf(measurementNames, "Q1000_PI");
    long[] get = vals;
    Assert.assertEquals(1, get[qPIIdx]);
    int q2300Idx = Arrays.indexOf(measurementNames, "Q200_300");
    Assert.assertEquals(2, vals[q2300Idx]);
    int nq2300Idx = Arrays.indexOf(measurementNames, "Q-300_-200");
    Assert.assertEquals(2, vals[nq2300Idx]);
  }

  @Test
  public void testRecord2P() {
    System.out.println("record");
    QuantizedAccumulator instance = new QuantizedAccumulator("test", "", "ms",
            10, -3, 3, 5);
    instance.record(-15000);
    instance.record(-15000);
    instance.record(-15000);
    instance.record(-300);
    instance.record(-300);
    instance.record(-400);
    instance.record(-350);
    instance.record(-101);
    instance.record(-100);
    instance.record(-1);
    instance.record(0);
    instance.record(0);
    instance.record(1);
    instance.record(1);
    instance.record(1);
    instance.record(2);
    instance.record(2);
    instance.record(2);
    instance.record(2);
    instance.record(10);
    instance.record(11);
    instance.record(250);
    instance.record(250);
    instance.record(100);
    instance.record(399);
    instance.record(200);
    instance.record(400);
    instance.record(15000);
    System.out.println(instance);
    Assert.assertEquals(15000, instance.getMaxMeasurement());
    Assert.assertEquals(-15000, instance.getMinMeasurement());
    long[] vals = instance.get();
    Assert.assertEquals(instance.getInfo().getNumberOfMeasurements(), vals.length);
    String[] measurementNames = instance.getInfo().getMeasurementNames();
    int niIdx = Arrays.indexOf(measurementNames, "QNI_-1000");
    Assert.assertEquals(3, vals[niIdx]);
    int niIdx2 = Arrays.indexOf(measurementNames, "Q100_400");
    Assert.assertEquals(5, vals[niIdx2]);
    int niIdx3 = Arrays.indexOf(measurementNames, "Q400_600");
    Assert.assertEquals(1, vals[niIdx3]);
    int niIdx4 = Arrays.indexOf(measurementNames, "Q-400_-100");
    Assert.assertEquals(5, vals[niIdx4]);


  }

  @Test
  public void testRecord3() {
    System.out.println("record");
    QuantizedAccumulator instance = new QuantizedAccumulator("test", "", "ms",
            10, 0, 1, 10);
    instance.record(-1);
    instance.record(0);
    instance.record(1);
    instance.record(2);
    instance.record(2);
    instance.record(10);
    instance.record(11);
    instance.record(250);
    instance.record(15000);
    System.out.println(instance);
    long[] vals = instance.get();
    long[] result = vals;
    System.out.println(java.util.Arrays.toString(result));
    String[] measurementNames = instance.getInfo().getMeasurementNames();
    int niIdx = Arrays.indexOf(measurementNames, "QNI_0");
    Assert.assertEquals(1, vals[niIdx]);
    int niIdx2 = Arrays.indexOf(measurementNames, "Q10_PI");
    Assert.assertEquals(4, vals[niIdx2]);
    int niIdx3 = Arrays.indexOf(measurementNames, "Q2_3");
    Assert.assertEquals(2, vals[niIdx3]);

  }

  @Test
  public void testRecord4() {
    System.out.println("record");
    QuantizedAccumulator instance = new QuantizedAccumulator("test", "", "ms",
            10, -1, 1, 10);
    instance.record(0);
    instance.record(1);
    instance.record(2);
    instance.record(10);
    instance.record(11);
    instance.record(250);
    instance.record(15000);
    instance.record(-15000);
    instance.record(-10);
    instance.record(-10);
    System.out.println(instance);
    long[] vals = instance.get();
    long[] result = vals;
    System.out.println(java.util.Arrays.toString(result));
    String[] measurementNames = instance.getInfo().getMeasurementNames();
    int niIdx = Arrays.indexOf(measurementNames, "QNI_-10");
    Assert.assertEquals(1, vals[niIdx]);
    int niIdx2 = Arrays.indexOf(measurementNames, "Q10_PI");
    Assert.assertEquals(4, vals[niIdx2]);
    int niIdx3 = Arrays.indexOf(measurementNames, "Q2_3");
    Assert.assertEquals(1, vals[niIdx3]);
  }

}
