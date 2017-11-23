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

import com.google.common.util.concurrent.UncheckedExecutionException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.spf4j.base.AbstractRunnable;
import org.spf4j.base.Callables;
import org.spf4j.base.ParameterizedSupplier;
import org.spf4j.base.Throwables;
import org.spf4j.base.TimeoutRunnable;

/**
 * Executor that will execute Callables with retry. This executor cannot be used inside a Completion service.
 *
 *
 * @author zoly
 */
public class RetryExecutor {

  public static final
          ParameterizedSupplier<Callables.RetryPredicate<Object, Object>, Callable<Object>> NO_RETRY_SUPPLIER
          = new ParameterizedSupplier<Callables.RetryPredicate<Object, Object>, Callable<Object>>() {

    @Override
    public Callables.RetryPredicate<Object, Object> get(final Callable<Object> parameter) {
      return Callables.RetryPredicate.NORETRY_DELAY_PREDICATE;
    }
  };

  private final ExecutorService executionService;
  /**
   * can contain: DelayedCallables for execution. (delayed retries) or FailedExecutionResults for results.
   *
   */
  private final DelayQueue<FailedExecutionResult> executionEvents = new DelayQueue<>();
  private final
          ParameterizedSupplier<Callables.RetryPredicate<Object, Object>, Callable<?>>
          resultRetryPredicateSupplier;
  private final
          ParameterizedSupplier<Callables.RetryPredicate<Exception, Object>, Callable<?>>
          exceptionRetryPredicateSupplier;
  private volatile RetryManager retryManager;
  private Future<?> retryManagerFuture;
  private final BlockingQueue<Future<?>> completionQueue;
  private final Object sync = new Object();

  private void startRetryManager() {
    if (this.retryManager == null) {
      synchronized (sync) {
        if (this.retryManager == null) {
          this.retryManager = new RetryManager();
          this.retryManagerFuture = DefaultExecutor.INSTANCE.submit(retryManager);
        }
      }
    }
  }

  private void shutdownRetryManager() {
    synchronized (sync) {
      if (this.retryManager != null) {
        retryManager.shutdown();
        retryManager = null;
      }
    }
  }

  /**
   * this class represents either a execution failure notification or a retry command.
   */
  private static class FailedExecutionResult implements Delayed {

    private final ExecutionException exception;
    private final RetryableCallable<Object> callable;
    private final long deadline;

    FailedExecutionResult(@Nullable final ExecutionException exception,
            final RetryableCallable callable, final long delay) {
      this.exception = exception;
      this.callable = callable;
      this.deadline = delay + System.currentTimeMillis();
    }

    @Override
    public long getDelay(final TimeUnit unit) {
      return unit.convert(deadline - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(final Delayed o) {
      long tDelay = getDelay(TimeUnit.MILLISECONDS);
      long oDelay = o.getDelay(TimeUnit.MILLISECONDS);
      if (tDelay > oDelay) {
        return 1;
      } else if (tDelay < oDelay) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == null) {
        return false;
      } else {
        if (obj instanceof FailedExecutionResult) {
          return this.compareTo((FailedExecutionResult) obj) == 0;
        } else {
          return false;
        }
      }
    }

    @Override
    public int hashCode() {
      int hash = 7;
      return 53 * hash + (this.callable != null ? this.callable.hashCode() : 0);
    }

    @Nullable
    public ExecutionException getException() {
      return exception;
    }

    public RetryableCallable<Object> getCallable() {
      return callable;
    }

  }

  private class RetryableCallable<T> implements Callable<T>, Runnable {

    private volatile Callable<T> callable;
    @Nullable
    private final FutureBean<T> future;
    private volatile FailedExecutionResult previousResult;
    private final Callables.RetryPredicate<Object, T> resultRetryPredicate;
    private final Callables.RetryPredicate<Exception, T> exceptionRetryPredicate;

    RetryableCallable(final Callable<T> callable, @Nullable final FutureBean<T> future,
            final FailedExecutionResult previousResult,
            final Callables.RetryPredicate<?, T> resultRetryPredicate,
            final Callables.RetryPredicate<Exception, T> exceptionRetryPredicate) {
      this.callable = callable;
      this.future = future;
      this.previousResult = previousResult;
      this.resultRetryPredicate = (Callables.RetryPredicate<Object, T>) resultRetryPredicate;
      this.exceptionRetryPredicate = exceptionRetryPredicate;
    }

    RetryableCallable(final Runnable task, final Object result, @Nullable final FutureBean<T> future,
            @Nullable final FailedExecutionResult previousResult,
            final Callables.RetryPredicate<?, T> resultRetryPredicate,
            final Callables.RetryPredicate<Exception, T> exceptionRetryPredicate) {
      this(new Callable() {

        @Override
        public Object call() {
          task.run();
          return result;
        }
      }, future, previousResult, resultRetryPredicate, exceptionRetryPredicate);
    }

    @Override
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    public T call() {
      try {
        T result = callable.call();
        Callables.RetryDecision<T> decision = this.resultRetryPredicate.getDecision(sync, callable);
        final Callables.RetryDecision.Type decisionType = decision.getDecisionType();
        switch (decisionType) {
          case Retry:
            final long delay = decision.getDelayMillis();
            startRetryManager();
            this.callable = decision.getNewCallable();
            executionEvents.add(new FailedExecutionResult(null, this, delay));
            break;
          case Abort:
            if (future != null) {
              future.setResult(result);
            }
            break;
          default:
            throw new IllegalStateException("Invalid decision type" + decisionType);
        }
        return null;
      } catch (Exception e) {
        Callables.RetryDecision<T> decision = this.exceptionRetryPredicate.getDecision(e, callable);
        final Callables.RetryDecision.Type decisionType = decision.getDecisionType();
        switch (decisionType) {
          case Retry:
            final long delay = decision.getDelayMillis();
            startRetryManager();
            this.callable = decision.getNewCallable();
            startRetryManager();
            if (previousResult != null) {
              final ExecutionException exception = previousResult.getException();
              if (exception != null) {
                e = Throwables.suppress(e, exception);
              }
            }
            executionEvents.add(new FailedExecutionResult(new ExecutionException(e), this, delay));
            break;
          case Abort:
            if (future != null) {
              future.setExceptionResult(new ExecutionException(e));
            }
            break;
          default:
            throw new IllegalStateException("Invalid decision type" + decisionType, e);
        }
        return null;
      }
    }

    @Override
    public void run() {
      call();
    }

    public FailedExecutionResult getPreviousResult() {
      return previousResult;
    }

    public void setPreviousResult(final FailedExecutionResult previousResult) {
      this.previousResult = previousResult;
    }

  }

  private class RetryManager extends AbstractRunnable {

    RetryManager() {
      super("RetryManager");
      isRunning = true;
    }

    public void shutdown() {
      isRunning = false;
    }

    private volatile boolean isRunning;

    @Override
    public void doRun() {
      while (isRunning) {
        try {
          FailedExecutionResult event = executionEvents.poll(1000, TimeUnit.SECONDS);
          if (event != null) {
            final RetryableCallable<Object> callable = event.getCallable();
            callable.setPreviousResult(event);
            executionService.execute(callable);
          }
        } catch (InterruptedException ex) {
          isRunning = false;
          break;
        }
      }
    }

  }


  public RetryExecutor(final ExecutorService exec,
          final ParameterizedSupplier<Callables.RetryPredicate<Exception, Object>,
                  Callable<Object>> exceptionRetryPredicateSupplier,
          @Nullable final BlockingQueue<Future<?>> completionQueue) {
    this(exec, (ParameterizedSupplier) NO_RETRY_SUPPLIER,
            (ParameterizedSupplier) exceptionRetryPredicateSupplier, completionQueue);
  }

  public RetryExecutor(final ExecutorService exec,
          final ParameterizedSupplier<Callables.RetryPredicate<Object, Object>,
                  Callable<?>> resultRetryPredicateSupplier,
          final ParameterizedSupplier<Callables.RetryPredicate<Exception, Object>,
                  Callable<?>> exceptionRetryPredicateSupplier,
          @Nullable final BlockingQueue<Future<?>> completionQueue) {
    executionService = exec;
    this.resultRetryPredicateSupplier = resultRetryPredicateSupplier;
    this.exceptionRetryPredicateSupplier = exceptionRetryPredicateSupplier;
    this.completionQueue = completionQueue;
  }

  public final void shutdown() {
    shutdownRetryManager();
    executionService.shutdown();
  }

  public final List<Runnable> shutdownNow() {
    shutdownRetryManager();
    return executionService.shutdownNow();
  }

  public final boolean isShutdown() {
    return executionService.isShutdown();
  }

  public final boolean isTerminated() {
    return executionService.isTerminated();
  }

  public final boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
    try {
      this.retryManagerFuture.get();
    } catch (ExecutionException ex) {
      throw new UncheckedExecutionException(ex);
    }
    return executionService.awaitTermination(timeout, unit);
  }

  private FutureBean<?> createFutureBean() {
    if (completionQueue == null) {
      return new FutureBean<>();
    } else {
      return new FutureBean<Object>() {
        @Override
        public void done() {
          completionQueue.add(this);
        }
      };
    }
  }

  public final <A, E extends Exception> Future<A> submit(final Callables.TimeoutCallable<A, E> task) {
    FutureBean<?> result = createFutureBean();
    executionService.execute(new RetryableCallable(task, result, null,
            resultRetryPredicateSupplier.get((Callable<Object>) task),
            exceptionRetryPredicateSupplier.get((Callable<Object>) task)));
    return (Future<A>) result;
  }

  public final <A> Future<A> submit(final Callable<A> task) {
    FutureBean<?> result = createFutureBean();
    executionService.execute(new RetryableCallable(task, result, null,
            resultRetryPredicateSupplier.get((Callable<Object>) task),
            exceptionRetryPredicateSupplier.get((Callable<Object>) task)));
    return (Future<A>) result;
  }

  public final <A, E extends Exception> Future<A> submit(final TimeoutRunnable<E> task, final A result) {
    FutureBean<?> resultFuture = createFutureBean();
    executionService.execute(new RetryableCallable(task, result, resultFuture, null,
            resultRetryPredicateSupplier.get((Callable<?>) task),
            exceptionRetryPredicateSupplier.get((Callable<?>) task)));
    return (Future<A>) resultFuture;
  }

  public final <E extends Exception> Future<?> submit(final TimeoutRunnable<E> task) {
    FutureBean<?> resultFuture = createFutureBean();
    executionService.execute(new RetryableCallable(task, null, resultFuture, null,
            resultRetryPredicateSupplier.get((Callable<?>) task),
            exceptionRetryPredicateSupplier.get((Callable<?>) task)));
    return resultFuture;
  }

  public final <E extends Exception> void execute(final TimeoutRunnable<E> command) {
    executionService.execute(new RetryableCallable(command, null, null, null,
            resultRetryPredicateSupplier.get((Callable<?>) command),
            exceptionRetryPredicateSupplier.get((Callable<?>) command)));
  }

  @Override
  public final String toString() {
    return "RetryExecutor{" + "executionService=" + executionService + ", executionEvents="
            + executionEvents + ", resultRetryPredicateSupplier=" + resultRetryPredicateSupplier
            + ", exceptionRetryPredicateSupplier=" + exceptionRetryPredicateSupplier + ", retryManager="
            + retryManager + ", retryManagerFuture=" + retryManagerFuture + ", completionQueue="
            + completionQueue + ", sync=" + sync + '}';
  }

}
