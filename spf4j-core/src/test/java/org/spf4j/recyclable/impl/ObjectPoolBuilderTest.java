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
package org.spf4j.recyclable.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.spf4j.base.Callables;
import org.spf4j.concurrent.RetryExecutor;
import org.spf4j.recyclable.ObjectBorrowException;
import org.spf4j.recyclable.ObjectCreationException;
import org.spf4j.recyclable.ObjectDisposeException;
import org.spf4j.recyclable.RecyclingSupplier;
import org.spf4j.recyclable.ObjectReturnException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;
import javax.annotation.concurrent.NotThreadSafe;
import org.junit.Assert;
import org.junit.Test;
import org.spf4j.base.Throwables;
import org.spf4j.concurrent.DefaultExecutor;
import org.spf4j.concurrent.LifoThreadPoolExecutorSQP;

/**
 *
 * @author zoly
 */
@SuppressFBWarnings({ "MDM_THREAD_YIELD", "SIC_INNER_SHOULD_BE_STATIC_ANON" })
@NotThreadSafe
public final class ObjectPoolBuilderTest {

    /**
     * Test of build method, of class RecyclingSupplierBuilder.
     */
    @Test
    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    public void testBuild() throws ObjectCreationException, InterruptedException,
            ObjectBorrowException, TimeoutException, ObjectDisposeException {
        System.out.println("test=build");
        RecyclingSupplier<ExpensiveTestObject> pool =
                new RecyclingSupplierBuilder(10, new ExpensiveTestObjectFactory()).build();
        System.out.println(pool);
        ExpensiveTestObject object = pool.get();
        System.out.println(pool);
        pool.recycle(object, null);
        System.out.println(pool);
        ExpensiveTestObject object2 = pool.get();
        Assert.assertSame(object2, object);

        pool.dispose();
    }

    @Test(expected = RuntimeException.class)
    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    public void testBuildSimple()
            throws ObjectCreationException, ObjectBorrowException,
            InterruptedException, TimeoutException, ObjectReturnException, ObjectDisposeException {
        System.out.println("test=buildSimple");
        RecyclingSupplier<ExpensiveTestObject> pool =
                new RecyclingSupplierBuilder(10, new ExpensiveTestObjectFactory()).build();
        System.out.println(pool);
        pool.get();
        pool.get();
        System.out.println(pool);
        org.spf4j.base.Runtime.setDeadline(System.currentTimeMillis() + 1000);
        pool.dispose();
        pool.get();
        System.out.println(pool);
    }



    @Test
    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    public void testBuild2() throws ObjectCreationException, InterruptedException,
            ObjectBorrowException, ExecutionException, TimeoutException {
        System.out.println("test=build2");
        final RecyclingSupplier<ExpensiveTestObject> pool =
                new RecyclingSupplierBuilder(10, new ExpensiveTestObjectFactory()).build();
        System.out.println(pool);
        final ExpensiveTestObject object = pool.get();
        System.out.println(pool);
        Future<Void> submit = DefaultExecutor.INSTANCE.submit(() -> {
          pool.recycle(object, null);
          return null;
        });
        submit.get();
        final ExpensiveTestObject object2 = pool.get();
        Assert.assertSame(object, object2);
        System.out.println(pool);
    }



    @Test(timeout = 20000)
    public void testPoolUseNoFailures()
                throws ObjectCreationException, ObjectBorrowException, InterruptedException,
                TimeoutException, ObjectReturnException, ObjectDisposeException, ExecutionException {
        System.out.println("test=poolUse");
        RecyclingSupplier<ExpensiveTestObject> pool
                = new RecyclingSupplierBuilder(10, new ExpensiveTestObjectFactory(1000000, 1000000, 1, 5)).build();
        runTest(pool, 0, 10000);
        pool.dispose();
    }

    @Test(timeout = 16000)
    public void testPoolUseNoFailuresStarvation()
                throws ObjectCreationException, ObjectBorrowException, InterruptedException,
                TimeoutException, ObjectReturnException, ObjectDisposeException, ExecutionException {
        System.out.println("test=poolUse");
        RecyclingSupplier<ExpensiveTestObject> pool
                = new RecyclingSupplierBuilder(1, new ExpensiveTestObjectFactory(1000000, 1000000, 1, 5)).build();
        runTest(pool, 0, 15000);
        pool.dispose();
    }


    @Test(timeout = 20000)
    public void testPoolUse()
            throws ObjectCreationException, ObjectBorrowException, InterruptedException,
            TimeoutException, ObjectReturnException, ObjectDisposeException, ExecutionException {
                System.out.println("test=poolUse");
        final RecyclingSupplier<ExpensiveTestObject> pool
                = new RecyclingSupplierBuilder(10, new ExpensiveTestObjectFactory()).build();

        runTest(pool, 0, 10000);
        try {
            ExpensiveTestObject.setFAILALL(true);
            pool.dispose();
            Assert.fail();
        } catch (ObjectDisposeException ex) {
            Throwables.writeTo(ex, System.err, Throwables.PackageDetail.SHORT);
        } finally {
          ExpensiveTestObject.setFAILALL(false);
        }
    }

    @Test(timeout = 20000)
    public void testPoolUseWithMaintenance()
            throws ObjectCreationException, ObjectBorrowException, InterruptedException,
            TimeoutException, ObjectReturnException, ObjectDisposeException, ExecutionException {
        System.out.println("test=poolUseWithMainteinance");

        final RecyclingSupplier<ExpensiveTestObject> pool = new RecyclingSupplierBuilder(10, new ExpensiveTestObjectFactory())
                .withMaintenance(org.spf4j.concurrent.DefaultScheduler.INSTANCE, 10, true).build();
        runTest(pool, 5, 20000);
        try {
          pool.dispose();
        } catch (ObjectDisposeException ex) {
          Throwables.writeTo(ex, System.err, Throwables.PackageDetail.SHORT);
        }

    }

    private volatile boolean isDeadlock = false;

    private Thread startDeadlockMonitor(final RecyclingSupplier<ExpensiveTestObject> pool,
            final long deadlockTimeout) {
        isDeadlock = false;
        Thread monitor = new Thread(() -> {
          try {
            Thread.sleep(deadlockTimeout);
            ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
            System.err.println(Arrays.toString(threadMX.dumpAllThreads(true, true)));
            System.err.println(pool.toString());
            isDeadlock = true;
          } catch (InterruptedException ex) {
            // terminating monitor
            return;
          }
        });
        monitor.start();
        return monitor;
    }

   @SuppressFBWarnings("MDM_THREAD_YIELD")
   private void runTest(final RecyclingSupplier<ExpensiveTestObject> pool,
            final long sleepBetweenSubmit, final long deadlockTimeout) throws InterruptedException, ExecutionException {
        Thread monitor = startDeadlockMonitor(pool, deadlockTimeout);
        ExecutorService execService = new LifoThreadPoolExecutorSQP("test", 10, 10,
                5000, 1024, true);
        BlockingQueue<Future<?>> completionQueue = new LinkedBlockingDeque<>();
        RetryExecutor exec = new RetryExecutor(execService, (final Callable<Object> parameter)
                        -> new Callables.RetryPredicate<Exception, Object>() {
          @Override
          public Callables.RetryDecision<Object> getDecision(Exception value, Callable<Object> callable) {
            return Callables.RetryDecision.retry(0, callable);
          }
                }, completionQueue);
        int nrTests = 1000;
        for (int i = 0; i < nrTests; i++) {
            exec.submit(new TestCallable(pool, i));
            Thread.sleep(sleepBetweenSubmit);
        }
        for (int i = 0; i < nrTests; i++) {
            System.out.println("Done(" + completionQueue.take().get() + ')');
        }
        monitor.interrupt();
        monitor.join();
        Thread.sleep(100);
        Assert.assertEquals(0, completionQueue.size());
        if (isDeadlock) {
            Assert.fail("deadlock detected");
        }
    }


}
