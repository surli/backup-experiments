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
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spf4j.base.AbstractRunnable;
import org.spf4j.concurrent.DefaultExecutor;
import org.spf4j.ds.UpdateablePriorityQueue;
import org.spf4j.recyclable.ObjectBorrowException;
import org.spf4j.recyclable.ObjectCreationException;
import org.spf4j.recyclable.ObjectDisposeException;
import org.spf4j.recyclable.RecyclingSupplier;

/**
 * a object sharing pool. this pool allows for non exclusive object sharing. TODO: synchronization is too coarse, can be
 * improved.
 *
 * @author zoly
 */
public final class SharingObjectPool<T> implements RecyclingSupplier<T> {

  private static final Logger LOG = LoggerFactory.getLogger(SharingObjectPool.class);

  private static final Comparator<SharedObject<?>> SH_COMP = new Comparator<SharedObject<?>>() {

    @Override
    public int compare(final SharedObject<?> o1, final SharedObject<?> o2) {
      return o1.nrTimesShared - o2.nrTimesShared;
    }

  };

  private final Factory<T> factory;

  private final UpdateablePriorityQueue<SharedObject<T>> pooledObjects;
  private final Map<T, UpdateablePriorityQueue<SharedObject<T>>.ElementRef> o2QueueRefMap;

  private int nrObjects;
  private final int maxSize;
  private boolean closed;
  private final boolean asyncValidate;

  public static final class SharedObject<T> {

    private int nrTimesShared;

    private final T object;

    public SharedObject(final T object) {
      this(object, 0);
    }

    public SharedObject(final T object, final int nrTimeShared) {
      this.object = object;
      this.nrTimesShared = nrTimeShared;
    }

    public T getObject() {
      return object;
    }

    private void inc() {
      nrTimesShared++;
    }

    private void dec() {
      nrTimesShared--;
    }

    public int getNrTimesShared() {
      return nrTimesShared;
    }

    @Override
    public String toString() {
      return "SharedObject{" + "nrTimesShared=" + nrTimesShared + ", object=" + object + '}';
    }

  }

  public SharingObjectPool(final Factory<T> factory, final int coreSize, final int maxSize)
          throws ObjectCreationException {
    this(factory, coreSize, maxSize, false);
  }

  public SharingObjectPool(final Factory<T> factory, final int coreSize, final int maxSize,
          final boolean asyncValidate)
          throws ObjectCreationException {
    if (maxSize <= 0) {
      throw new IllegalArgumentException("max size must be greater than zero and not " + maxSize);
    }
    if (maxSize < coreSize) {
      throw new IllegalArgumentException("max size must be greater than core size  and not "
              + maxSize + " < " + coreSize);
    }
    this.factory = factory;
    this.pooledObjects = new UpdateablePriorityQueue<>(maxSize, SH_COMP);
    this.nrObjects = 0;
    this.closed = false;
    this.asyncValidate = asyncValidate;
    o2QueueRefMap = new IdentityHashMap<>(maxSize);
    for (int i = 0; i < coreSize; i++) {
      createObject(0);
    }
    this.maxSize = maxSize;
  }

  @Override
  public synchronized T get() throws ObjectBorrowException, ObjectCreationException {
    if (closed) {
      throw new ObjectBorrowException("Reclycler is closed " + this);
    }
    UpdateablePriorityQueue<SharedObject<T>>.ElementRef peekEntry = pooledObjects.peekEntry();
    if (peekEntry != null) {
      final SharedObject<T> elem = peekEntry.getElem();
      if (elem.getNrTimesShared() == 0) {
        elem.inc();
        peekEntry.elementMutated();
        return elem.getObject();
      } else if (nrObjects < maxSize) {
        return createObject(1);
      } else {
        return elem.getObject();
      }
    } else {
      return createObject(1);
    }
  }

  private synchronized T createObject(final int nrTimesShared) throws ObjectCreationException {
    T obj = factory.create();
    o2QueueRefMap.put(obj, pooledObjects.add(new SharedObject<>(obj, nrTimesShared)));
    nrObjects++;
    return obj;
  }

  @Override
  public void recycle(final T object, final Exception e) {
    if (e != null) {
      if (asyncValidate) {
        DefaultExecutor.INSTANCE.execute(new AbstractRunnable(true) {

          @Override
          public void doRun() {
            validate(object, e);
          }
        });
      } else {
        validate(object, e);
      }
    } else {
      returnToQueue(object);
    }
  }

  @SuppressFBWarnings("REC_CATCH_EXCEPTION")
  private synchronized void validate(final T object, final Exception e) {
    if (o2QueueRefMap.containsKey(object)) {
      // element still in queue
      boolean isValid;
      try {
        isValid = factory.validate(object, e); // validate
      } catch (Exception ex) {
        ex.addSuppressed(e);
        LOG.warn("Validation failed for {}", object, ex);
        isValid = false;
      }
      if (!isValid) { // remove from pool
        UpdateablePriorityQueue.ElementRef qref = o2QueueRefMap.remove(object);
        nrObjects--;
        qref.remove();
      } else {
        returnToQueue(object);
      }
    }
  }

  private synchronized void returnToQueue(final T object) {
    // object is valid
    UpdateablePriorityQueue<SharedObject<T>>.ElementRef ref = o2QueueRefMap.get(object);
    if (ref != null) { // elem can already be removed if it fails validation.
      final SharedObject<T> elem = ref.getElem();
      elem.dec();
      this.notifyAll();
      ref.elementMutated();
    }
  }

  @Override
  public void recycle(final T object) {
    recycle(object, null);
  }

  @Override
  public synchronized boolean tryDispose(final long timeoutMillis)
          throws ObjectDisposeException, InterruptedException {
    if (!closed) {
      long deadline = System.currentTimeMillis() + timeoutMillis;
      closed = true;
      ObjectDisposeException exres = null;
      Iterator<SharedObject<T>> iterator = pooledObjects.iterator();
      while (iterator.hasNext()) {
        SharedObject<T> so = iterator.next();
        try {
          while (so.getNrTimesShared() > 0) {
            long waitFor = deadline - System.currentTimeMillis();
            if (waitFor > 0) {
              this.wait(waitFor);
            } else {
              return false;
            }
          }
          T o = so.getObject();
          o2QueueRefMap.remove(o);
          iterator.remove();
          nrObjects--;
          factory.dispose(o);
        } catch (ObjectDisposeException ex) {
          if (exres == null) {
            exres = ex;
          } else {
            ex.addSuppressed(exres);
            exres = ex;
          }
        }
      }
      if (exres != null) {
        throw exres;
      }
    }
    return true;
  }

  @Override
  public synchronized String toString() {
    return "SharingObjectPool{" + "factory=" + factory + ", pooledObjects=" + pooledObjects + '}';
  }

}
