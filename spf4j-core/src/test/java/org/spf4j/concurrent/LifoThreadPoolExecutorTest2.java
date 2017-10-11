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
package org.spf4j.concurrent;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.spf4j.stackmonitor.FastStackCollector;

/**
 *
 * @author zoly
 */
@SuppressFBWarnings({ "HES_LOCAL_EXECUTOR_SERVICE", "PREDICTABLE_RANDOM" })
public class LifoThreadPoolExecutorTest2 {


    @Test
    public void testLifoExecSQ() throws InterruptedException, IOException, ExecutionException {
        LifoThreadPoolExecutorSQP executor =
                new LifoThreadPoolExecutorSQP("test", 2, 8, 1000, 1024, 1024);
        testPoolThreadDynamics(executor);
    }


    /**
     * this is to confirm JDK behavior.
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecutionException
     */
    @Test
    @Ignore
    public void testJdkExec() throws InterruptedException, IOException, ExecutionException {
        final LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue(1024);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 8, 1000, TimeUnit.MILLISECONDS,
                linkedBlockingQueue);
        testPoolThreadDynamics(executor);
    }


    private static final Runnable NOP = new Runnable() {

        @Override
        public void run() {
        }
    };

    @SuppressFBWarnings({ "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS", "ITC_INHERITANCE_TYPE_CHECKING" })
    public static void testPoolThreadDynamics(final ExecutorService executor)
            throws InterruptedException, IOException, ExecutionException {
        testMaxParallel(executor, 4);
        if (executor instanceof LifoThreadPoolExecutorSQP) {
            LifoThreadPoolExecutorSQP le = (LifoThreadPoolExecutorSQP) executor;
            Assert.assertEquals(4, le.getThreadCount());
            testMaxParallel(executor, 2);
            Assert.assertEquals(2, le.getThreadCount());
        } else if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executor;
            Assert.assertEquals(4, tpe.getPoolSize());
            try {
                while (true) {
                    executor.execute(NOP);
                }
            } catch (RejectedExecutionException ex) {
               //do nothing
            }
            Assert.assertEquals(8, tpe.getPoolSize()); // JDK thread pool is at max
            testMaxParallel(executor, 2);
            Assert.assertEquals(8, tpe.getPoolSize()); // JDK thread pool sucks.
        } else {
          throw new IllegalStateException("Unsupported " + executor);
        }
        executor.shutdown();
        boolean awaitTermination = executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(awaitTermination);
    }

    public static void testMaxParallel(final ExecutorService executor, final int maxParallel)
            throws  InterruptedException {
        final LongAdder adder = new LongAdder();
        final LongAdder exNr = new LongAdder();
        int nrExCaught = 0;
        final int testCount = 1000;
        long rejected = 0;
        final Runnable runnable = new Runnable() {
            @Override
            @SuppressFBWarnings("MDM_THREAD_YIELD")
            public void run() {
                adder.increment();
                long sleep = (long) (50 * Math.random());
                if (sleep < 10) {
                    exNr.increment();
                    throw new IllegalStateException();
                }
              try {
                Thread.sleep(sleep);
              } catch (InterruptedException ex) {
                // do nothing
              }
            }
        };
        long start = System.currentTimeMillis();
        List<Future<?>> futures = new ArrayList<>(maxParallel);
        for (int i = 0; i < testCount; i++) {
            futures.add(executor.submit(runnable));
            if (i % maxParallel == 0) {
                nrExCaught += consume(futures);
            }
        }
        nrExCaught += consume(futures);
        Assert.assertEquals(testCount, adder.sum());
        Assert.assertEquals(nrExCaught, exNr.sum());
        System.out.println("Stats for " + executor.getClass()
                + ", rejected = " + rejected + ", Exec time = " + (System.currentTimeMillis() - start));
        System.out.println("Threads: " + Arrays.toString(FastStackCollector.getThreads()));
    }

    public static int consume(List<Future<?>> futures) throws InterruptedException {
        int nrEx = 0;
        for (Future fut : futures) {
            try {
                fut.get();
            } catch (ExecutionException ex) {
                nrEx++;
            }
        }
        futures.clear();
        return nrEx;
    }


}
