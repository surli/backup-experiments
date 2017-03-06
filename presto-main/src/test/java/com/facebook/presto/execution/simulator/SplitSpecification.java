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
package com.facebook.presto.execution.simulator;

import com.facebook.presto.execution.simulator.SimulationSplit.IntermediateSplit;
import com.facebook.presto.execution.simulator.SimulationSplit.LeafSplit;

import java.util.concurrent.ScheduledExecutorService;

abstract class SplitSpecification
{
    private final long scheduledTimeNanos;
    private final long perQuantaNanos;

    SplitSpecification(long scheduledTimeNanos, long perQuantaNanos)
    {
        this.scheduledTimeNanos = scheduledTimeNanos;
        this.perQuantaNanos = perQuantaNanos;
    }

    long getScheduledTimeNanos()
    {
        return scheduledTimeNanos;
    }

    long getPerQuantaNanos()
    {
        return perQuantaNanos;
    }

    abstract SimulationSplit instantiate(SimulationTask task);

    static class LeafSplitSpecification
            extends SplitSpecification
    {
        LeafSplitSpecification(long scheduledTimeNanos, long perQuantaNanos)
        {
            super(scheduledTimeNanos, perQuantaNanos);
        }

        public LeafSplit instantiate(SimulationTask task)
        {
            return new LeafSplit(task, super.getScheduledTimeNanos(), super.getPerQuantaNanos());
        }
    }

    static class IntermediateSplitSpecification
            extends SplitSpecification
    {
        private final long wallTimeNanos;
        private final long numQuantas;
        private final long betweenQuantaNanos;
        private final ScheduledExecutorService wakeupExecutor;

        IntermediateSplitSpecification(
                long scheduledTimeNanos,
                long perQuantaNanos,
                long wallTimeNanos,
                long numQuantas,
                long betweenQuantaNanos,
                ScheduledExecutorService wakeupExecutor)
        {
            super(scheduledTimeNanos, perQuantaNanos);
            this.wallTimeNanos = wallTimeNanos;
            this.numQuantas = numQuantas;
            this.betweenQuantaNanos = betweenQuantaNanos;
            this.wakeupExecutor = wakeupExecutor;
        }

        public IntermediateSplit instantiate(SimulationTask task)
        {
            return new IntermediateSplit(task, wallTimeNanos, numQuantas, super.getPerQuantaNanos(), betweenQuantaNanos, super.getScheduledTimeNanos(), wakeupExecutor);
        }
    }
}
