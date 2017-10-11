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
package org.spf4j.base;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Test;
import org.spf4j.base.Callables.AdvancedAction;
import org.spf4j.base.Callables.AdvancedRetryPredicate;
import org.spf4j.base.Callables.TimeoutCallable;

/**
 *
 * @author zoly
 */
@SuppressFBWarnings("BED_BOGUS_EXCEPTION_DECLARATION") // fb-contrib issue,re ported.
public final class CallablesTest {

  /**
   * Test exception propagation.
   */
  @Test(expected = TestException.class)
  public void testExceptionPropagation() throws Exception {
    Callables.executeWithRetry(new TimeoutCallable<Integer, TestException>(60000) {
      @Override
      public Integer call(final long deadline) throws TestException {
        throw new TestException();
      }
    }, 3, 10, TestException.class);
  }

  @Test(expected = TimeoutException.class)
  public void testExceptionPropagation2() throws InterruptedException, TimeoutException {
    Callables.executeWithRetry(new TimeoutCallable<Integer, RuntimeException>(3000) {
      @Override
      public Integer call(final long deadline) throws TimeoutException {
        throw new TimeoutException();
      }
    }, 3, 10, RuntimeException.class);
  }

  /**
   * Test of executeWithRetry method, of class Callables.
   */
  @Test
  public void testExecuteWithRetry4args1() throws Exception {
    System.out.println("executeWithRetry");
    Integer result = Callables.executeWithRetry(new TimeoutCallable<Integer, RuntimeException>(60000) {
      @Override
      public Integer call(final long deadline) {
        return 1;
      }
    }, 3, 10, RuntimeException.class);
    Assert.assertEquals(1L, result.longValue());
  }

  /**
   * Test of executeWithRetry method, of class Callables.
   */
  @Test
  public void testExecuteWithRetry4args2() throws Exception {
    System.out.println("testExecuteWithRetry4args2");
    long startTime = System.currentTimeMillis();
    Integer result = Callables.executeWithRetry(new TimeoutCallable<Integer, IOException>(60000) {
      private int count;

      @Override
      public Integer call(final long deadline) throws IOException {
        count++;
        if (count < 20) {
          throw new SocketException("Aaaaaaaaaaa" + count);
        }

        return 1;
      }
    }, 1, 10, IOException.class);
    long elapsedTime = System.currentTimeMillis() - startTime;
    Assert.assertEquals(1L, result.longValue());
    Assert.assertTrue("Operation has to take at least 10 ms", elapsedTime > 10L);
  }

  @Test
  public void testExecuteWithRetryFailureTest() throws IOException, InterruptedException {
    try {
      Callables.executeWithRetry(new TimeoutCallable<Integer, IOException>(100) {

        @Override
        public Integer call(final long deadline) throws IOException {
          throw new SocketException("Aaaaaaaaaaa " + System.currentTimeMillis());
        }
      }, 4, 100, IOException.class);
      Assert.fail("Should not get here");
    } catch (IOException | TimeoutException e) {
      Assert.assertTrue("must have supressed exceptions: "
              + com.google.common.base.Throwables.getStackTraceAsString(e),
              Throwables.getSuppressed(e).length >= 1);
    }

  }

  @Test
  @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
  public void testSuppression() throws InterruptedException, IOException, TimeoutException  {
    System.out.println("executeWithRetry");
    long startTime = System.currentTimeMillis();
    Integer result = Callables.executeWithRetry(new TimeoutCallable<Integer, IOException>(60000) {
      private int count;

      @Override
      public Integer call(final long deadline) throws IOException {
        count++;
        if (count < 15) {
          throw new SocketException("Aaaaaaaaaaa" + count);
        }

        return 1;
      }
    }, 1, 20, new AdvancedRetryPredicate<Exception>() {

      @Override
      public AdvancedAction apply(final Exception input) {
        final Throwable[] suppressed = Throwables.getSuppressed(input);
        if (suppressed.length > 0) {
          throw new UnsupportedOperationException();
        }
        return AdvancedAction.RETRY;
      }
    }, IOException.class);
    long elapsedTime = System.currentTimeMillis() - startTime;
    Assert.assertEquals(1L, result.longValue());
    Assert.assertTrue("Operation has to take at least 10 ms and not " + elapsedTime, elapsedTime > 10L);
  }

  @Test
  public void testExecuteWithRetryTimeout() {
    try {
      Callables.executeWithRetry(new TimeoutCallable<Integer, IOException>(1000) {
        private int count;

        @Override
        @SuppressFBWarnings("MDM_THREAD_YIELD")
        public Integer call(final long deadline) throws IOException, InterruptedException {
          Thread.sleep(2000);
          count++;
          if (count < 5) {
            throw new SocketException("Aaaaaaaaaaa" + count);
          }
          return 1;
        }
      }, 1, 10, IOException.class);
      Assert.fail();
    } catch (InterruptedException | IOException | TimeoutException ex) {
      Throwables.writeTo(ex, System.err, Throwables.PackageDetail.NONE);
    }
  }

  @Test(expected = TimeoutException.class)
  public void testExecuteWithRetryTimeout2() throws InterruptedException, IOException, TimeoutException {
    Callables.executeWithRetry(new TimeoutCallable<Integer, IOException>(1000) {
      private int count = 0;

      @Override
      public Integer call(final long deadline) throws IOException {
        System.out.println("Exec at " + System.currentTimeMillis());
        count++;
        if (count < 200) {
          throw new SocketException("Aaaaaaaaaaa" + count);
        }
        return 1;
      }
    }, 0, 100, IOException.class);
    Assert.fail();
  }

  /**
   * Test of executeWithRetry method, of class Callables.
   */
  public void testExecuteWithRetry4args3() throws Exception {
    System.out.println("executeWithRetry");
    final CallableImpl callableImpl = new CallableImpl(60000);
    try {
      Callables.executeWithRetry(callableImpl, 3, 10, Exception.class);
      Assert.fail("this should throw a exception");
    } catch (Exception e) {
      Assert.assertEquals(11, callableImpl.getCount());
      System.out.println("Exception as expected " + e);
    }
  }

  public void testExecuteWithRetry5args3() throws Exception {
    System.out.println("executeWithRetry");
    final CallableImpl2 callableImpl = new CallableImpl2(60000);
    Callables.executeWithRetry(callableImpl, 2, 10,
            (t, deadline, callable)
                    -> t > 0 ? Callables.RetryDecision.retry(0, callable) : Callables.RetryDecision.abort(),
            Callables.DEFAULT_EXCEPTION_RETRY, Exception.class);
    Assert.assertEquals(4, callableImpl.getCount());
  }

  private static class CallableImpl extends TimeoutCallable<Integer, Exception> {

    private int count;

    public CallableImpl(final int timeoutMillis) {
      super(timeoutMillis);
    }

    @Override
    public Integer call(final long deadline) throws Exception {
      count++;
      throw new Exception("Aaaaaaaaaaa" + count);
    }

    public int getCount() {
      return count;
    }

  }

  private static class CallableImpl2 extends TimeoutCallable<Integer, Exception> {

    private int count;

    public CallableImpl2(final int timeoutMillis) {
      super(timeoutMillis);
    }

    @Override
    public Integer call(final long deadline) throws Exception {
      count++;
      return count;
    }

    public int getCount() {
      return count;
    }

  }

  @Test
  public void testOverflow() {
    Assert.assertEquals(Long.MAX_VALUE, Callables.overflowSafeAdd(Long.MAX_VALUE, 1));
    Assert.assertEquals(Long.MAX_VALUE, Callables.overflowSafeAdd(Long.MAX_VALUE, Long.MAX_VALUE));
    Assert.assertEquals(3, Callables.overflowSafeAdd(1, 2));
  }

}
