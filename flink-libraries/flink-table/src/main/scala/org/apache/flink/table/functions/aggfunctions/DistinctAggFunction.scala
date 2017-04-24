/*
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
package org.apache.flink.table.functions.aggfunctions

import java.util.{List => JList, HashSet => JHashSet}

import org.apache.flink.api.java.tuple.{Tuple2 => JTuple2}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.table.functions.{Accumulator, AggregateFunction, FunctionContext}

import scala.collection.JavaConverters._

/**
  * A generic wrapper that deduplicates elements before passing the elements down to the
  * delegated aggregation function.
  *
  * NOTE: this function is inefficient as it makes no assumptions that on the merge functions.
  * It buffers all the values and replays them when doing merges.
  */
class DistinctAggFunction[T](delegate: AggregateFunction[T]) extends AggregateFunction[T] {

  override def createAccumulator(): Accumulator = {
    val acc = new DistinctAccumulator
    acc.f0 = new JHashSet[Any]()
    acc.f1 = delegate.createAccumulator()
    acc
  }

  override def getValue(accumulator: Accumulator): T = {
    val acc = accumulator.asInstanceOf[DistinctAccumulator]
    delegate.getValue(acc.f1)
  }

  override def accumulate(accumulator: Accumulator, input: Any): Unit = {
    val acc = accumulator.asInstanceOf[DistinctAccumulator]
    if (!acc.f0.contains(input)) {
      acc.f0.add(input)
      delegate.accumulate(acc.f1, input)
    }
  }

  override def merge(accumulators: JList[Accumulator]): Accumulator = {
    accumulators.asScala.reduceLeft((l, r) => {
      val leftAcc = l.asInstanceOf[DistinctAccumulator]
      val rightAcc = r.asInstanceOf[DistinctAccumulator]
      rightAcc.f0.asScala.filter(!leftAcc.f0.contains(_)).foreach(
        accumulate(leftAcc, _)
      )
      leftAcc
    })
    accumulators.get(0)
  }

  override def retract(accumulator: Accumulator, input: Any): Unit = {
    val acc = accumulator.asInstanceOf[DistinctAccumulator]
    acc.f0.remove(input)
    delegate.retract(acc.f1, input)
  }

  override def getAccumulatorType: TypeInformation[_] = delegate.getAccumulatorType

  override def open(context: FunctionContext): Unit = delegate.open(context)

  override def close(): Unit = delegate.close()

  override def resetAccumulator(accumulator: Accumulator): Unit = {
    val acc = accumulator.asInstanceOf[DistinctAccumulator]
    acc.f0.clear()
    delegate.resetAccumulator(acc.f1)
  }
}

private class DistinctAccumulator extends JTuple2[JHashSet[Any], Accumulator] with Accumulator
