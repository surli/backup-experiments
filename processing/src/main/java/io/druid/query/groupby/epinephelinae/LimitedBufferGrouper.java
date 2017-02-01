/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.druid.query.groupby.epinephelinae;

import com.google.common.primitives.Ints;
import io.druid.java.util.common.IAE;
import io.druid.java.util.common.ISE;
import io.druid.java.util.common.logger.Logger;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.aggregation.BufferAggregator;
import io.druid.segment.ColumnSelectorFactory;

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class LimitedBufferGrouper<KeyType> implements Grouper<KeyType>
{
  public class BufferGrouperOffsetHeapIndexUpdater
  {
    private ByteBuffer hashTableBuffer;
    private final int indexPosition;

    public BufferGrouperOffsetHeapIndexUpdater(
        ByteBuffer hashTableBuffer,
        int indexPosition
    )
    {
      this.hashTableBuffer = hashTableBuffer;
      this.indexPosition = indexPosition;
    }

    public void setHashTableBuffer(ByteBuffer newTableBuffer) {
      hashTableBuffer = newTableBuffer;
    }

    public void updateHeapIndexForOffset(int bucketOffset, int newHeapIndex)
    {
      hashTableBuffer.putInt(bucketOffset + indexPosition, newHeapIndex);
    }

    public int getHeapIndexForOffset(int bucketOffset)
    {
      return hashTableBuffer.getInt(bucketOffset + indexPosition);
    }
  }

  private static final Logger log = new Logger(BufferGrouper.class);

  private static final int MIN_INITIAL_BUCKETS = 4;
  private static final int DEFAULT_INITIAL_BUCKETS = 1024;
  private static final float DEFAULT_MAX_LOAD_FACTOR = 0.7f;
  private static final int HASH_SIZE = Ints.BYTES;

  private final ByteBuffer buffer;
  private final Grouper.KeySerde<KeyType> keySerde;
  private final int keySize;
  private final AggregatorFactory[] aggregatorFactories;
  private final BufferAggregator[] aggregators;
  private final int[] aggregatorOffsets;
  private final int initialBuckets;
  private final int bucketSize;
  private final int tableArenaSize;
  private final int bufferGrouperMaxSize; // Integer.MAX_VALUE in production, only used for unit tests
  private final float maxLoadFactor;

  // Buffer pointing to the current table (it moves around as the table grows)
  private ByteBuffer tableBuffer;

  // Offset of tableBuffer within the larger buffer
  private int tableStart;

  // Current number of buckets in the table
  private int buckets;

  // Number of elements in the table right now
  private int size;

  // Maximum number of elements in the table before it must be resized
  private int maxSize;

  // Limit to apply to results.
  // If limit > 0, track hash table entries in a binary heap with size of limit.
  // If -1, no limit is applied, hash table entry offsets are tracked with an unordered list with no limit.
  private int limit;

  // Indicates if the sorting order has aggregators, used when pushing down limit/sorting.
  // When the sorting order has aggs, grouping key comparisons need to also compare on aggregators.
  // Additionally, results must be resorted by grouping key to allow results to merge correctly.
  private boolean sortHasAggs;

  // Min-max heap, used for storing offsets when applying limits/sorting in the BufferGrouper
  private ByteBufferMinMaxOffsetHeap offsetHeap;

  // ByteBuffer slice used by the min-max offset heap
  private ByteBuffer offsetHeapBuffer;

  // Updates the heap index field for buckets, created passed to the heap when
  // pushing down limit and the sort order includes aggregators
  private final BufferGrouperOffsetHeapIndexUpdater heapIndexUpdater;

  // how many times the table buffer has flipped (through swapPushDownBuffers())
  private int growthCount;

  public LimitedBufferGrouper(
      final ByteBuffer buffer,
      final Grouper.KeySerde<KeyType> keySerde,
      final ColumnSelectorFactory columnSelectorFactory,
      final AggregatorFactory[] aggregatorFactories,
      final int bufferGrouperMaxSize,
      final float maxLoadFactor,
      final int initialBuckets,
      final int limit,
      final boolean sortHasAggs
  )
  {
    this.buffer = buffer;
    this.keySerde = keySerde;
    this.keySize = keySerde.keySize();
    this.aggregators = new BufferAggregator[aggregatorFactories.length];
    this.aggregatorOffsets = new int[aggregatorFactories.length];
    this.bufferGrouperMaxSize = bufferGrouperMaxSize;
    this.maxLoadFactor = maxLoadFactor > 0 ? maxLoadFactor : DEFAULT_MAX_LOAD_FACTOR;
    this.initialBuckets = initialBuckets > 0 ? Math.max(MIN_INITIAL_BUCKETS, initialBuckets) : DEFAULT_INITIAL_BUCKETS;
    this.growthCount = 0;
    this.sortHasAggs = sortHasAggs;

    if (this.maxLoadFactor >= 1.0f) {
      throw new IAE("Invalid maxLoadFactor[%f], must be < 1.0", maxLoadFactor);
    }

    int offset = HASH_SIZE + keySize;
    this.aggregatorFactories = aggregatorFactories;
    for (int i = 0; i < aggregatorFactories.length; i++) {
      aggregators[i] = aggregatorFactories[i].factorizeBuffered(columnSelectorFactory);
      aggregatorOffsets[i] = offset;
      offset += aggregatorFactories[i].getMaxIntermediateSize();
    }

    // For each bucket, store an extra field indicating the bucket's current index within the heap when
    // pushing down limits
    this.heapIndexUpdater = new BufferGrouperOffsetHeapIndexUpdater(buffer, offset);
    offset += Ints.BYTES;
    this.limit = limit;

    this.bucketSize = offset;

    //only store offsets up to `limit` + 1 instead of up to # of buckets, we only keep the top results
    int heapByteSize = (limit + 1) * Ints.BYTES;
    this.tableArenaSize = ((buffer.capacity() - heapByteSize) / bucketSize) * bucketSize;

    reset();
  }

  @Override
  public boolean aggregate(KeyType key, int keyHash)
  {
    final ByteBuffer keyBuffer = keySerde.toByteBuffer(key);
    if (keyBuffer == null) {
      return false;
    }

    if (keyBuffer.remaining() != keySize) {
      throw new IAE(
          "keySerde.toByteBuffer(key).remaining[%s] != keySerde.keySize[%s], buffer was the wrong size?!",
          keyBuffer.remaining(),
          keySize
      );
    }

    int bucket = findBucket(
        tableBuffer,
        buckets,
        bucketSize,
        size < Math.min(maxSize, bufferGrouperMaxSize),
        keyBuffer,
        keySize,
        keyHash
    );

    if (bucket < 0) {
      if (size < bufferGrouperMaxSize) {
        swapPushDownBuffers();
        bucket = findBucket(tableBuffer, buckets, bucketSize, size < maxSize, keyBuffer, keySize, keyHash);
      }

      if (bucket < 0) {
        return false;
      }
    }

    final int offset = bucket * bucketSize;

    // Set up key if this is a new bucket.
    if (!isUsed(bucket)) {
      tableBuffer.position(offset);
      tableBuffer.putInt(keyHash | 0x80000000);
      tableBuffer.put(keyBuffer);

      for (int i = 0; i < aggregators.length; i++) {
        aggregators[i].init(tableBuffer, offset + aggregatorOffsets[i]);
      }

      heapIndexUpdater.updateHeapIndexForOffset(offset, -1);
      size++;
    }

    // Aggregate the current row.
    for (int i = 0; i < aggregators.length; i++) {
      aggregators[i].aggregate(tableBuffer, offset + aggregatorOffsets[i]);
    }

    int heapIndex = heapIndexUpdater.getHeapIndexForOffset(offset);
    if (heapIndex < 0) {
      // not in the heap, add it
      offsetHeap.addOffset(offset);
    } else if (sortHasAggs) {
      // Since the sorting columns contain at least one aggregator, we need to remove and reinsert
      // the entries after aggregating to maintain proper ordering
      offsetHeap.deleteAt(heapIndex);
      offsetHeap.addOffset(offset);
    }

    return true;
  }

  @Override
  public boolean aggregate(final KeyType key)
  {
    return aggregate(key, Groupers.hash(key));
  }

  // just split the table arena buffer into two halves, swapping to the other half when the current half is full
  @Override
  public void reset()
  {
    size = 0;
    buckets = (tableArenaSize / 2) / bucketSize;
    if (buckets < (limit + 1)) {
      throw new IAE(
          "Buffer capacity [%d] is too small, minimum bytes needed: [%d]",
          buffer.capacity(),
          (limit + 1) * (Ints.BYTES + bucketSize * 2)
      );
    }
    maxSize = maxSizeForBuckets(buckets);

    // start at the first half
    tableStart = 0;

    final ByteBuffer bufferDup = buffer.duplicate();
    bufferDup.position(tableStart);
    bufferDup.limit(tableStart + buckets * bucketSize);
    tableBuffer = bufferDup.slice();

    // Clear used bits of new table
    for (int i = 0; i < buckets; i++) {
      tableBuffer.put(i * bucketSize, (byte) 0);
    }

    if (heapIndexUpdater != null) {
      heapIndexUpdater.setHashTableBuffer(tableBuffer);
    }

    keySerde.reset();

    offsetHeapBuffer = initOffsetHeap();
  }

  @Override
  public Iterator<Entry<KeyType>> iterator(final boolean sorted)
  {
    if (sortHasAggs) {
      // re-sort the heap in place, it's also an array of offsets in the buffer
      return makeDefaultOrderingIterator(offsetHeap.getHeapSize());
    } else {
      return makeHeapIterator();
    }
  }

  @Override
  public void close()
  {
    for (BufferAggregator aggregator : aggregators) {
      try {
        aggregator.close();
      }
      catch (Exception e) {
        log.warn(e, "Could not close aggregator, skipping.", aggregator);
      }
    }
  }

  public int getGrowthCount()
  {
    return growthCount;
  }

  public int getSize()
  {
    return size;
  }

  public int getBuckets()
  {
    return buckets;
  }

  public int getLimit()
  {
    return limit;
  }

  public int getMaxSize()
  {
    return maxSize;
  }

  private Iterator<Grouper.Entry<KeyType>> makeDefaultOrderingIterator(final int size)
  {
    final List<Integer> wrappedOffsets = new AbstractList<Integer>()
    {
      @Override
      public Integer get(int index)
      {
        return buffer.getInt(tableArenaSize + index * Ints.BYTES);
      }

      @Override
      public Integer set(int index, Integer element)
      {
        final Integer oldValue = get(index);
        buffer.putInt(tableArenaSize + index * Ints.BYTES, element);
        return oldValue;
      }

      @Override
      public int size()
      {
        return size;
      }
    };

    final Grouper.KeyComparator comparator = keySerde.bufferComparator();

    // Sort offsets in-place.
    Collections.sort(
        wrappedOffsets,
        new Comparator<Integer>()
        {
          @Override
          public int compare(Integer lhs, Integer rhs)
          {
            return comparator.compare(
                tableBuffer,
                tableBuffer,
                lhs + HASH_SIZE,
                rhs + HASH_SIZE
            );
          }
        }
    );

    return new Iterator<Grouper.Entry<KeyType>>()
    {
      int curr = 0;

      @Override
      public boolean hasNext()
      {
        return curr < size;
      }

      @Override
      public Grouper.Entry<KeyType> next()
      {
        return bucketEntryForOffset(wrappedOffsets.get(curr++));
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }

  private Iterator<Grouper.Entry<KeyType>> makeHeapIterator()
  {
    final int initialHeapSize = offsetHeap.getHeapSize();
    return new Iterator<Grouper.Entry<KeyType>>()
    {
      int curr = 0;

      @Override
      public boolean hasNext()
      {
        return curr < initialHeapSize;
      }

      @Override
      public Grouper.Entry<KeyType> next()
      {
        if (curr >= initialHeapSize) {
          throw new NoSuchElementException();
        }
        final int offset = offsetHeap.removeMin();
        final Grouper.Entry<KeyType> entry = bucketEntryForOffset(offset);
        curr++;

        return entry;
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }

  private ByteBuffer initOffsetHeap()
  {
    ByteBuffer heapBuffer = buffer.duplicate();
    heapBuffer.position(tableArenaSize);
    heapBuffer = heapBuffer.slice();
    int heapSize = (limit + 1) * Ints.BYTES;
    heapBuffer.limit(heapSize);

    Comparator<Integer> comparator = new Comparator<Integer>()
    {
      final Grouper.KeyComparator keyComparator = keySerde.bufferComparatorWithAggregators(
          aggregatorFactories,
          aggregatorOffsets
      );
      @Override
      public int compare(Integer o1, Integer o2)
      {
        return keyComparator.compare(tableBuffer, tableBuffer, o1 + HASH_SIZE, o2 + HASH_SIZE);
      }
    };

    this.offsetHeap = new ByteBufferMinMaxOffsetHeap(heapBuffer, limit, comparator, heapIndexUpdater);
    return heapBuffer;
  }

  private boolean isUsed(final int bucket)
  {
    return (tableBuffer.get(bucket * bucketSize) & 0x80) == 0x80;
  }

  private boolean isOffsetUsed(final int bucketOffset)
  {
    return (tableBuffer.get(bucketOffset) & 0x80) == 0x80;
  }

  private Grouper.Entry<KeyType> bucketEntryForOffset(final int bucketOffset)
  {
    final KeyType key = keySerde.fromByteBuffer(tableBuffer, bucketOffset + HASH_SIZE);
    final Object[] values = new Object[aggregators.length];
    for (int i = 0; i < aggregators.length; i++) {
      values[i] = aggregators[i].get(tableBuffer, bucketOffset + aggregatorOffsets[i]);
    }

    return new Grouper.Entry<>(key, values);
  }

  // We don't delete keys in the buffers because it's a linear probing hash table, so when it fills,
  // swap to the other buffer and copy the valid keys (in the heap) over
  private void swapPushDownBuffers()
  {
    if (growthCount % 2 == 0) {
      tableStart = tableArenaSize / 2;
    } else {
      tableStart = 0;
    }

    ByteBuffer newTableBuffer = buffer.duplicate();
    newTableBuffer.position(tableStart);
    newTableBuffer.limit(tableStart + buckets * bucketSize);
    newTableBuffer = newTableBuffer.slice();

    // Clear used bits of new table
    for (int i = 0; i < buckets; i++) {
      newTableBuffer.put(i * bucketSize, (byte) 0);
    }

    // Loop over old buckets and copy to new table
    final ByteBuffer entryBuffer = tableBuffer.duplicate();
    final ByteBuffer keyBuffer = tableBuffer.duplicate();

    // copy old buckets
    int numCopied = copyBucketsAndOffsetHeap(buckets, entryBuffer, keyBuffer, newTableBuffer);

    // when using the heap, only copy buckets that are still in the heap, drop the rest
    // (they would be excluded by the limit)
    size = numCopied;
    tableBuffer = newTableBuffer;
    if (heapIndexUpdater != null) {
      heapIndexUpdater.setHashTableBuffer(tableBuffer);
    }
    growthCount++;
  }

  // Iterate through the heap, copy buckets to the new table buffer, and update the bucket offsets within the heap
  private int copyBucketsAndOffsetHeap(int numNewBuckets, ByteBuffer entryBuffer, ByteBuffer keyBuffer, ByteBuffer newTableBuffer)
  {
    int numCopied = 0;
    for (int i = 0; i < offsetHeap.getHeapSize(); i++) {
      final int oldBucketOffset = offsetHeapBuffer.getInt(i * Ints.BYTES);
      if (isOffsetUsed(oldBucketOffset)) {
        entryBuffer.limit(oldBucketOffset + bucketSize);
        entryBuffer.position(oldBucketOffset);
        keyBuffer.limit(entryBuffer.position() + HASH_SIZE + keySize);
        keyBuffer.position(entryBuffer.position() + HASH_SIZE);

        final int keyHash = entryBuffer.getInt(entryBuffer.position()) & 0x7fffffff;
        final int newBucket = findBucket(newTableBuffer, numNewBuckets, bucketSize, true, keyBuffer, keySize, keyHash);

        if (newBucket < 0) {
          throw new ISE("WTF?! Couldn't find a bucket while resizing?!");
        }

        final int newOffset = newBucket * bucketSize;
        newTableBuffer.position(newOffset);
        newTableBuffer.put(entryBuffer);

        offsetHeapBuffer.putInt(i * Ints.BYTES, newOffset);
        numCopied++;
      }
    }
    return numCopied;
  }

  private int maxSizeForBuckets(int buckets)
  {
    return Math.max(1, (int) (buckets * maxLoadFactor));
  }

  /**
   * Finds the bucket into which we should insert a key.
   *
   * @param keyBuffer key, must have exactly keySize bytes remaining. Will not be modified.
   *
   * @return bucket index for this key, or -1 if no bucket is available due to being full
   */
  private static int findBucket(
      final ByteBuffer tableBuffer,
      final int buckets,
      final int bucketSize,
      final boolean allowNewBucket,
      final ByteBuffer keyBuffer,
      final int keySize,
      final int keyHash
  )
  {
    // startBucket will never be negative since keyHash is always positive (see Groupers.hash)
    final int startBucket = keyHash % buckets;
    int bucket = startBucket;

outer:
    while (true) {
      final int bucketOffset = bucket * bucketSize;

      if ((tableBuffer.get(bucketOffset) & 0x80) == 0) {
        // Found unused bucket before finding our key
        return allowNewBucket ? bucket : -1;
      }

      for (int i = bucketOffset + HASH_SIZE, j = keyBuffer.position(); j < keyBuffer.position() + keySize; i++, j++) {
        if (tableBuffer.get(i) != keyBuffer.get(j)) {
          bucket += 1;
          if (bucket == buckets) {
            bucket = 0;
          }

          if (bucket == startBucket) {
            // Came back around to the start without finding a free slot, that was a long trip!
            // Should never happen unless buckets == maxSize.
            return -1;
          }

          continue outer;
        }
      }

      // Found our key in a used bucket
      return bucket;
    }
  }
}
