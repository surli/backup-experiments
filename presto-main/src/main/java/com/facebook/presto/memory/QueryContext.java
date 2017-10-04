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
package com.facebook.presto.memory;

import com.facebook.presto.Session;
import com.facebook.presto.execution.TaskId;
import com.facebook.presto.execution.TaskStateMachine;
import com.facebook.presto.operator.TaskContext;
import com.facebook.presto.spi.QueryId;
import com.facebook.presto.spi.memory.AggregatedMemoryContext;
import com.facebook.presto.spiller.SpillSpaceTracker;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.airlift.units.DataSize;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

import static com.facebook.presto.ExceededMemoryLimitException.exceededLocalLimit;
import static com.facebook.presto.ExceededSpillLimitException.exceededPerQueryLocalLimit;
import static com.facebook.presto.operator.Operator.NOT_BLOCKED;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static io.airlift.units.DataSize.Unit.MEGABYTE;
import static io.airlift.units.DataSize.succinctBytes;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@ThreadSafe
public class QueryContext
{
    private static final long GUARANTEED_MEMORY = new DataSize(1, MEGABYTE).toBytes();

    private final QueryId queryId;
    private final Executor notificationExecutor;
    private final ScheduledExecutorService yieldExecutor;
    private final long maxSpill;
    private final SpillSpaceTracker spillSpaceTracker;
    private final Map<TaskId, TaskContext> taskContexts = new ConcurrentHashMap();
    private final MemoryPool systemMemoryPool;

    // TODO: This field should be final. However, due to the way QueryContext is constructed the memory limit is not known in advance
    @GuardedBy("this")
    private long maxMemory;

    @GuardedBy("this")
    private final MemoryTrackingContext queryMemoryContext;

    @GuardedBy("this")
    private MemoryPool memoryPool;

    @GuardedBy("this")
    private long spillUsed;

    public QueryContext(
            QueryId queryId,
            DataSize maxMemory,
            MemoryPool memoryPool,
            MemoryPool systemMemoryPool,
            Executor notificationExecutor,
            ScheduledExecutorService yieldExecutor,
            DataSize maxSpill,
            SpillSpaceTracker spillSpaceTracker)
    {
        this.queryId = requireNonNull(queryId, "queryId is null");
        this.maxMemory = requireNonNull(maxMemory, "maxMemory is null").toBytes();
        this.memoryPool = requireNonNull(memoryPool, "memoryPool is null");
        this.systemMemoryPool = requireNonNull(systemMemoryPool, "systemMemoryPool is null");
        this.notificationExecutor = requireNonNull(notificationExecutor, "notificationExecutor is null");
        this.yieldExecutor = requireNonNull(yieldExecutor, "yieldExecutor is null");
        this.maxSpill = requireNonNull(maxSpill, "maxSpill is null").toBytes();
        this.spillSpaceTracker = requireNonNull(spillSpaceTracker, "spillSpaceTracker is null");
        this.queryMemoryContext = new MemoryTrackingContext(new AggregatedMemoryContext(), new AggregatedMemoryContext(), new AggregatedMemoryContext());
    }

    // TODO: This method should be removed, and the correct limit set in the constructor. However, due to the way QueryContext is constructed the memory limit is not known in advance
    public synchronized void setResourceOvercommit()
    {
        // Allow the query to use the entire pool. This way the worker will kill the query, if it uses the entire local general pool.
        // The coordinator will kill the query if the cluster runs out of memory.
        maxMemory = memoryPool.getMaxBytes();
    }

    public synchronized ListenableFuture<?> reserveMemory(long bytes)
    {
        // "bytes" have already been reflected in the queryMemoryContext reserved memory
        if (queryMemoryContext.reservedUserMemory() > maxMemory) {
            throw exceededLocalLimit(succinctBytes(maxMemory));
        }
        ListenableFuture<?> future = memoryPool.reserve(queryId, bytes);
        // Never block queries using a trivial amount of memory
        if (queryMemoryContext.reservedUserMemory() < GUARANTEED_MEMORY) {
            return NOT_BLOCKED;
        }
        return future;
    }

    public synchronized ListenableFuture<?> reserveRevocableMemory(long bytes)
    {
        return memoryPool.reserveRevocable(queryId, bytes);
    }

    public synchronized ListenableFuture<?> reserveSystemMemory(long bytes)
    {
        return systemMemoryPool.reserve(queryId, bytes);
    }

    public synchronized ListenableFuture<?> reserveSpill(long bytes)
    {
        checkArgument(bytes >= 0, "bytes is negative");
        if (spillUsed + bytes > maxSpill) {
            throw exceededPerQueryLocalLimit(succinctBytes(maxSpill));
        }
        ListenableFuture<?> future = spillSpaceTracker.reserve(bytes);
        spillUsed += bytes;
        return future;
    }

    public synchronized boolean tryReserveMemory(long bytes)
    {
        // "bytes" is not yet reflected in the queryMemoryContext
        if (queryMemoryContext.reservedUserMemory() + bytes > maxMemory) {
            return false;
        }
        if (memoryPool.tryReserve(queryId, bytes)) {
            return true;
        }
        return false;
    }

    public synchronized void freeMemory(long bytes)
    {
        memoryPool.free(queryId, bytes);
    }

    public synchronized void freeRevocableMemory(long bytes)
    {
        memoryPool.freeRevocable(queryId, bytes);
    }

    public synchronized void freeSystemMemory(long bytes)
    {
        systemMemoryPool.free(queryId, bytes);
    }

    public synchronized void freeSpill(long bytes)
    {
        checkArgument(spillUsed - bytes >= 0, "tried to free more memory than is reserved");
        spillUsed -= bytes;
        spillSpaceTracker.free(bytes);
    }

    public synchronized void setMemoryPool(MemoryPool pool)
    {
        requireNonNull(pool, "pool is null");
        if (pool.getId().equals(memoryPool.getId())) {
            // Don't unblock our tasks and thrash the pools, if this is a no-op
            return;
        }
        MemoryPool originalPool = memoryPool;
        long originalReserved = queryMemoryContext.reservedUserMemory() + queryMemoryContext.reservedRevocableMemory();
        memoryPool = pool;
        ListenableFuture<?> future = pool.reserve(queryId, originalReserved);
        Futures.addCallback(future, new FutureCallback<Object>()
        {
            @Override
            public void onSuccess(Object result)
            {
                originalPool.free(queryId, originalReserved);
                // Unblock all the tasks, if they were waiting for memory, since we're in a new pool.
                taskContexts.values().forEach(TaskContext::moreMemoryAvailable);
            }

            @Override
            public void onFailure(Throwable t)
            {
                originalPool.free(queryId, originalReserved);
                // Unblock all the tasks, if they were waiting for memory, since we're in a new pool.
                taskContexts.values().forEach(TaskContext::moreMemoryAvailable);
            }
        });
    }

    public synchronized MemoryPool getMemoryPool()
    {
        return memoryPool;
    }

    public TaskContext addTaskContext(TaskStateMachine taskStateMachine, Session session, boolean verboseStats, boolean cpuTimerEnabled)
    {
        TaskContext taskContext = new TaskContext(this, taskStateMachine, notificationExecutor, yieldExecutor, session, queryMemoryContext.newMemoryTrackingContext(), verboseStats, cpuTimerEnabled);
        taskContexts.put(taskStateMachine.getTaskId(), taskContext);
        return taskContext;
    }

    public <C, R> R accept(QueryContextVisitor<C, R> visitor, C context)
    {
        return visitor.visitQueryContext(this, context);
    }

    public <C, R> List<R> acceptChildren(QueryContextVisitor<C, R> visitor, C context)
    {
        return taskContexts.values()
                .stream()
                .map(taskContext -> taskContext.accept(visitor, context))
                .collect(toList());
    }

    public TaskContext getTaskContextByTaskId(TaskId taskId)
    {
        TaskContext taskContext = taskContexts.get(taskId);
        verify(taskContext != null, "task does not exist");
        return taskContext;
    }
}
