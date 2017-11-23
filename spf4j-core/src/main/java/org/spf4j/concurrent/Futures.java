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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.spf4j.base.Pair;
import org.spf4j.base.Throwables;

/**
 *
 * @author zoly
 */
public final class Futures {

  private Futures() { }

  @CheckReturnValue
  public static RuntimeException cancelAll(final boolean mayInterrupt, final Future... futures) {
    RuntimeException ex = null;
    for (Future future : futures) {
      try {
        future.cancel(mayInterrupt);
      } catch (RuntimeException e) {
        if (ex == null) {
          ex = e;
        } else {
          ex = Throwables.suppress(ex, e);
        }
      }
    }
    return ex;
  }

  @CheckReturnValue
  public static RuntimeException cancelAll(final boolean mayInterrupt, final Iterator<Future> iterator) {
    RuntimeException ex = null;
    while (iterator.hasNext()) {
      Future future = iterator.next();
      try {
        future.cancel(mayInterrupt);
      } catch (RuntimeException e) {
        if (ex == null) {
          ex = e;
        } else {
          ex = Throwables.suppress(ex, e);
        }
      }
    }
    return ex;
  }


  @CheckReturnValue
  @Nonnull
  public static Pair<Map<Future, Object>, Exception> getAll(final long timeoutMillis, final Future... futures)  {
    long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
    return getAllWithDeadlineNanos(deadlineNanos, futures);
  }

  /**
   * Gets all futures resuls.
   *
   * @param deadlineNanos
   * @param futures
   * @return
   */
  @CheckReturnValue
  @Nonnull
  public static Pair<Map<Future, Object>, Exception> getAllWithDeadlineNanos(final long deadlineNanos,
          final Future... futures) {
    Exception exception = null;
    Map<Future, Object> results = new HashMap<>(futures.length);
    for (int i = 0; i < futures.length; i++) {
      Future future = futures[i];
      try {
        final long toNanos = deadlineNanos - System.nanoTime();
        if (toNanos > 0) {
          results.put(future, future.get(toNanos, TimeUnit.NANOSECONDS));
        } else {
          throw new TimeoutException("Timed out when about to run " + future);
        }
      } catch (InterruptedException | TimeoutException ex) {
        if (exception == null) {
          exception = ex;
        } else {
          exception = Throwables.suppress(ex, exception);
        }
        int next = i + 1;
        if (next < futures.length) {
          RuntimeException cex = cancelAll(true, Arrays.copyOfRange(futures, next, futures.length));
          if (cex != null) {
            exception = Throwables.suppress(exception, cex);
          }
        }
        break;
      } catch (ExecutionException | RuntimeException ex) {
        if (exception == null) {
          exception = ex;
        } else {
          exception = Throwables.suppress(exception, ex);
        }
      }
    }
    return Pair.of(results, exception);
  }

  @CheckReturnValue
  @Nonnull
  public static Pair<Map<Future, Object>, Exception> getAll(final long timeoutMillis, final Iterable<Future> futures)  {
    long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
    return getAllWithDeadlineNanos(deadlineNanos, futures);
  }


  @CheckReturnValue
  @Nonnull
  public static Pair<Map<Future, Object>, Exception> getAllWithDeadlineNanos(final long deadlineNanos,
          final Iterable<Future> futures) {
    Exception exception = null;
    Map<Future, Object> results;
    if (futures instanceof Collection) {
      results = new HashMap<>(((Collection) futures).size());
    } else {
      results = new HashMap<>();
    }
    Iterator<Future> iterator = futures.iterator();
    while (iterator.hasNext()) {
      Future future = iterator.next();
      try {
        final long toNanos = deadlineNanos - System.nanoTime();
        if (toNanos > 0) {
          results.put(future, future.get(toNanos, TimeUnit.NANOSECONDS));
        } else {
          throw new TimeoutException("Timed out when about to run " + future);
        }
      } catch (InterruptedException | TimeoutException ex) {
        if (exception == null) {
          exception = ex;
        } else {
          exception = Throwables.suppress(ex, exception);
        }
        RuntimeException cex = cancelAll(true, iterator);
        if (cex != null) {
          exception = Throwables.suppress(exception, cex);
        }
        break;
      } catch (ExecutionException | RuntimeException ex) {
        if (exception == null) {
          exception = ex;
        } else {
          exception = Throwables.suppress(exception, ex);
        }
      }
    }
    return Pair.of(results, exception);
  }




}
