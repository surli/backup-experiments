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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import io.druid.data.input.MapBasedRow;
import io.druid.data.input.Row;
import io.druid.granularity.AllGranularity;
import io.druid.java.util.common.IAE;
import io.druid.java.util.common.Pair;
import io.druid.java.util.common.guava.Accumulator;
import io.druid.query.QueryInterruptedException;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.dimension.DimensionSpec;
import io.druid.query.groupby.GroupByQuery;
import io.druid.query.groupby.GroupByQueryConfig;
import io.druid.query.groupby.GroupByQueryHelper;
import io.druid.query.groupby.RowBasedColumnSelectorFactory;
import io.druid.query.groupby.orderby.DefaultLimitSpec;
import io.druid.query.groupby.orderby.LimitSpec;
import io.druid.query.groupby.orderby.OrderByColumnSpec;
import io.druid.query.groupby.strategy.GroupByStrategyV2;
import io.druid.segment.ColumnSelectorFactory;
import io.druid.segment.DimensionSelector;
import io.druid.segment.data.IndexedInts;
import org.joda.time.DateTime;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// this class contains shared code between GroupByMergingQueryRunnerV2 and GroupByRowProcessor
public class RowBasedGrouperHelper
{

  public static Pair<Grouper<RowBasedKey>, Accumulator<Grouper<RowBasedKey>, Row>> createGrouperAccumulatorPair(
      final GroupByQuery query,
      final boolean isInputRaw,
      final GroupByQueryConfig config,
      final ByteBuffer buffer,
      final int concurrencyHint,
      final LimitedTemporaryStorage temporaryStorage,
      final ObjectMapper spillMapper,
      final AggregatorFactory[] aggregatorFactories
  )
  {
    // concurrencyHint >= 1 for concurrent groupers, -1 for single-threaded
    Preconditions.checkArgument(concurrencyHint >= 1 || concurrencyHint == -1, "invalid concurrencyHint");

    final GroupByQueryConfig querySpecificConfig = config.withOverrides(query);
    final boolean includeTimestamp = GroupByStrategyV2.getUniversalTimestamp(query) == null;
    final LimitSpec limitSpec = query.getLimitSpec();
    final int limit;
    if (limitSpec instanceof DefaultLimitSpec) {
      limit = ((DefaultLimitSpec) limitSpec).getLimit();
    } else {
      limit = -1;
    }
    boolean pushDownLimit = query.getContextBoolean(GroupByQueryConfig.CTX_KEY_PUSH_DOWN_LIMIT, false);
    if (pushDownLimit && limit < 0) {
      throw new IAE("If enabling limit push down, a limit spec must be provided.");
    }

    final ThreadLocal<Row> columnSelectorRow = new ThreadLocal<>();
    final ColumnSelectorFactory columnSelectorFactory = RowBasedColumnSelectorFactory.create(
        columnSelectorRow,
        GroupByQueryHelper.rowSignatureFor(query)
    );

    // If there's a limit spec, check if the sorting order contains any aggregators
    boolean sortHasAggs = false;
    if (limit > -1) {
      sortHasAggs = DefaultLimitSpec.sortingOrderHasAggs(limitSpec, Arrays.asList(aggregatorFactories));
      // If the sorting order only uses columns in the grouping key, we can always push the limit down
      // to the buffer grouper without affecting result accuracy
      if (!sortHasAggs) {
        pushDownLimit = true;
      }
    }

    // If only applying an orderby without a limit, don't try to push down
    if (limit == Integer.MAX_VALUE) {
      pushDownLimit = false;
    }

    final Grouper.KeySerdeFactory<RowBasedKey> keySerdeFactory = new RowBasedKeySerdeFactory(
        includeTimestamp,
        query.getContextSortByDimsFirst(),
        query.getDimensions(),
        querySpecificConfig.getMaxMergingDictionarySize() / (concurrencyHint == -1 ? 1 : concurrencyHint),
        aggregatorFactories,
        pushDownLimit ? limitSpec : null
    );

    final Grouper<RowBasedKey> grouper;
    if (concurrencyHint == -1) {
      grouper = new SpillingGrouper<>(
          buffer,
          keySerdeFactory,
          columnSelectorFactory,
          aggregatorFactories,
          querySpecificConfig.getBufferGrouperMaxSize(),
          querySpecificConfig.getBufferGrouperMaxLoadFactor(),
          querySpecificConfig.getBufferGrouperInitialBuckets(),
          temporaryStorage,
          spillMapper,
          true,
          pushDownLimit ? limit : -1,
          sortHasAggs
      );
    } else {
      grouper = new ConcurrentGrouper<>(
          buffer,
          keySerdeFactory,
          columnSelectorFactory,
          aggregatorFactories,
          querySpecificConfig.getBufferGrouperMaxSize(),
          querySpecificConfig.getBufferGrouperMaxLoadFactor(),
          querySpecificConfig.getBufferGrouperInitialBuckets(),
          temporaryStorage,
          spillMapper,
          concurrencyHint,
          pushDownLimit ? limit : -1,
          sortHasAggs
      );
    }

    final DimensionSelector[] dimensionSelectors;
    if (isInputRaw) {
      dimensionSelectors = new DimensionSelector[query.getDimensions().size()];
      for (int i = 0; i < dimensionSelectors.length; i++) {
        dimensionSelectors[i] = columnSelectorFactory.makeDimensionSelector(query.getDimensions().get(i));
      }
    } else {
      dimensionSelectors = null;
    }

    final Accumulator<Grouper<RowBasedKey>, Row> accumulator = new Accumulator<Grouper<RowBasedKey>, Row>()
    {
      @Override
      public Grouper<RowBasedKey> accumulate(
          final Grouper<RowBasedKey> theGrouper,
          final Row row
      )
      {
        if (Thread.interrupted()) {
          throw new QueryInterruptedException(new InterruptedException());
        }

        if (theGrouper == null) {
          // Pass-through null returns without doing more work.
          return null;
        }

        columnSelectorRow.set(row);

        final int dimStart;
        final Comparable[] key;

        if (includeTimestamp) {
          key = new Comparable[query.getDimensions().size() + 1];

          final long timestamp;
          if (isInputRaw) {
            if (query.getGranularity() instanceof AllGranularity) {
              timestamp = query.getIntervals().get(0).getStartMillis();
            } else {
              timestamp = query.getGranularity().truncate(row.getTimestampFromEpoch());
            }
          } else {
            timestamp = row.getTimestampFromEpoch();
          }

          key[0] = timestamp;
          dimStart = 1;
        } else {
          key = new Comparable[query.getDimensions().size()];
          dimStart = 0;
        }

        for (int i = dimStart; i < key.length; i++) {
          final String value;
          if (isInputRaw) {
            IndexedInts index = dimensionSelectors[i - dimStart].getRow();
            value = index.size() == 0 ? "" : dimensionSelectors[i - dimStart].lookupName(index.get(0));
          } else {
            value = (String) row.getRaw(query.getDimensions().get(i - dimStart).getOutputName());
          }
          key[i] = Strings.nullToEmpty(value);
        }

        final boolean didAggregate = theGrouper.aggregate(new RowBasedKey(key));
        if (!didAggregate) {
          // null return means grouping resources were exhausted.
          return null;
        }
        columnSelectorRow.set(null);

        return theGrouper;
      }
    };

    return new Pair<>(grouper, accumulator);
  }

  public static CloseableGrouperIterator<RowBasedKey, Row> makeGrouperIterator(
      final Grouper<RowBasedKey> grouper,
      final GroupByQuery query,
      final Closeable closeable
  )
  {
    final boolean includeTimestamp = GroupByStrategyV2.getUniversalTimestamp(query) == null;

    return new CloseableGrouperIterator<>(
        grouper,
        true,
        new Function<Grouper.Entry<RowBasedKey>, Row>()
        {
          @Override
          public Row apply(Grouper.Entry<RowBasedKey> entry)
          {
            Map<String, Object> theMap = Maps.newLinkedHashMap();

            // Get timestamp, maybe.
            final DateTime timestamp;
            final int dimStart;

            if (includeTimestamp) {
              timestamp = query.getGranularity().toDateTime(((long) (entry.getKey().getKey()[0])));
              dimStart = 1;
            } else {
              timestamp = null;
              dimStart = 0;
            }

            // Add dimensions.
            for (int i = dimStart; i < entry.getKey().getKey().length; i++) {
              theMap.put(
                  query.getDimensions().get(i - dimStart).getOutputName(),
                  Strings.emptyToNull((String) entry.getKey().getKey()[i])
              );
            }

            // Add aggregations.
            for (int i = 0; i < entry.getValues().length; i++) {
              theMap.put(query.getAggregatorSpecs().get(i).getName(), entry.getValues()[i]);
            }

            return new MapBasedRow(timestamp, theMap);
          }
        },
        closeable
    );
  }

  static class RowBasedKey
  {
    private final Object[] key;

    RowBasedKey(final Object[] key)
    {
      this.key = key;
    }

    @JsonCreator
    public static RowBasedKey fromJsonArray(final Object[] key)
    {
      // Type info is lost during serde. We know we don't want ints as timestamps, so adjust.
      if (key.length > 0 && key[0] instanceof Integer) {
        key[0] = ((Integer) key[0]).longValue();
      }

      return new RowBasedKey(key);
    }

    @JsonValue
    public Object[] getKey()
    {
      return key;
    }

    @Override
    public boolean equals(Object o)
    {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      RowBasedKey that = (RowBasedKey) o;

      return Arrays.equals(key, that.key);
    }

    @Override
    public int hashCode()
    {
      return Arrays.hashCode(key);
    }

    @Override
    public String toString()
    {
      return Arrays.toString(key);
    }
  }

  private static class RowBasedKeySerdeFactory implements Grouper.KeySerdeFactory<RowBasedKey>
  {
    private final boolean includeTimestamp;
    private final boolean sortByDimsFirst;
    private final int dimCount;
    private final long maxDictionarySize;
    private final LimitSpec limitSpec;
    private final List<DimensionSpec> dimensions;
    final AggregatorFactory[] aggregatorFactories;

    RowBasedKeySerdeFactory(
        boolean includeTimestamp,
        boolean sortByDimsFirst,
        List<DimensionSpec> dimensions,
        long maxDictionarySize,
        final AggregatorFactory[] aggregatorFactories,
        LimitSpec limitSpec
    )
    {
      this.includeTimestamp = includeTimestamp;
      this.sortByDimsFirst = sortByDimsFirst;
      this.dimensions = dimensions;
      this.dimCount = dimensions.size();
      this.maxDictionarySize = maxDictionarySize;
      this.limitSpec = limitSpec;
      this.aggregatorFactories = aggregatorFactories;
    }

    @Override
    public Grouper.KeySerde<RowBasedKey> factorize()
    {
      return new RowBasedKeySerde(
          includeTimestamp,
          sortByDimsFirst,
          dimensions,
          maxDictionarySize,
          limitSpec
      );
    }

    @Override
    public Comparator<Grouper.Entry<RowBasedKey>> objectComparator(boolean forceDefaultOrder)
    {
      if (limitSpec != null && !forceDefaultOrder) {
        return objectComparatorWithAggs();
      }

      if (includeTimestamp) {
        if (sortByDimsFirst) {
          return new Comparator<Grouper.Entry<RowBasedKey>>()
          {
            @Override
            public int compare(Grouper.Entry<RowBasedKey> entry1, Grouper.Entry<RowBasedKey> entry2)
            {
              final int cmp = compareDimsInRows(entry1.getKey(), entry2.getKey(), 1);
              if (cmp != 0) {
                return cmp;
              }

              return Longs.compare((long) entry1.getKey().getKey()[0], (long) entry2.getKey().getKey()[0]);
            }
          };
        } else {
          return new Comparator<Grouper.Entry<RowBasedKey>>()
          {
            @Override
            public int compare(Grouper.Entry<RowBasedKey> entry1, Grouper.Entry<RowBasedKey> entry2)
            {
              final int timeCompare = Longs.compare(
                  (long) entry1.getKey().getKey()[0],
                  (long) entry2.getKey().getKey()[0]
              );

              if (timeCompare != 0) {
                return timeCompare;
              }

              return compareDimsInRows(entry1.getKey(), entry2.getKey(), 1);
            }
          };
        }
      } else {
        return new Comparator<Grouper.Entry<RowBasedKey>>()
        {
          public int compare(Grouper.Entry<RowBasedKey> entry1, Grouper.Entry<RowBasedKey> entry2)
          {
            return compareDimsInRows(entry1.getKey(), entry2.getKey(), 0);
          }
        };
      }
    }

    private Comparator<Grouper.Entry<RowBasedKey>> objectComparatorWithAggs()
    {
      // use the actual sort order from the limitspec if pushing down to merge partial results correctly
      final List<Integer> directions = Lists.newArrayList();
      final List<Boolean> aggFlags = Lists.newArrayList();
      final List<Integer> fieldIndices = Lists.newArrayList();
      final Set<Integer> orderByIndices = new HashSet<>();

      for (OrderByColumnSpec orderSpec : ((DefaultLimitSpec) limitSpec).getColumns()) {
        int direction = orderSpec.getDirection() == OrderByColumnSpec.Direction.ASCENDING ? 1 : -1;
        int dimIndex = OrderByColumnSpec.getDimIndexForOrderBy(orderSpec, dimensions);
        if (dimIndex >= 0) {
          fieldIndices.add(dimIndex);
          orderByIndices.add(dimIndex);
          directions.add(direction);
          aggFlags.add(false);
        } else {
          int aggIndex = OrderByColumnSpec.getAggIndexForOrderBy(orderSpec, Arrays.asList(aggregatorFactories));
          if (aggIndex >= 0) {
            fieldIndices.add(aggIndex);
            directions.add(direction);
            aggFlags.add(true);
          }
        }
      }

      for (int i = 0; i < dimCount; i++) {
        if (!orderByIndices.contains(i)) {
          fieldIndices.add(i);
          aggFlags.add(false);
          directions.add(1);
        }
      }

      if (includeTimestamp) {
        if (sortByDimsFirst) {
          return new Comparator<Grouper.Entry<RowBasedKey>>()
          {
            @Override
            public int compare(Grouper.Entry<RowBasedKey> entry1, Grouper.Entry<RowBasedKey> entry2)
            {
              final int cmp = compareDimsInRowsWithAggs(entry1, entry2, 1, directions, aggFlags, fieldIndices);
              if (cmp != 0) {
                return cmp;
              }

              return Longs.compare((long) entry1.getKey().getKey()[0], (long) entry2.getKey().getKey()[0]);
            }
          };
        } else {
          return new Comparator<Grouper.Entry<RowBasedKey>>()
          {
            @Override
            public int compare(Grouper.Entry<RowBasedKey> entry1, Grouper.Entry<RowBasedKey> entry2)
            {
              final int timeCompare = Longs.compare((long) entry1.getKey().getKey()[0], (long) entry2.getKey().getKey()[0]);

              if (timeCompare != 0) {
                return timeCompare;
              }

              return compareDimsInRowsWithAggs(entry1, entry2, 1, directions, aggFlags, fieldIndices);
            }
          };
        }
      } else {
        return new Comparator<Grouper.Entry<RowBasedKey>>()
        {
          @Override
          public int compare(Grouper.Entry<RowBasedKey> entry1, Grouper.Entry<RowBasedKey> entry2)
          {
            return compareDimsInRowsWithAggs(entry1, entry2, 0, directions, aggFlags, fieldIndices);
          }
        };
      }
    }

    private static int compareDimsInRows(RowBasedKey key1, RowBasedKey key2, int dimStart)
    {
      for (int i = dimStart; i < key1.getKey().length; i++) {
        final int cmp = ((String) key1.getKey()[i]).compareTo((String) key2.getKey()[i]);
        if (cmp != 0) {
          return cmp;
        }
      }

      return 0;
    }

    private static int compareDimsInRowsWithAggs(
        Grouper.Entry<RowBasedKey> entry1,
        Grouper.Entry<RowBasedKey> entry2,
        int dimStart,
        final List<Integer> directions,
        final List<Boolean> aggFlags,
        final List<Integer> fieldIndices
    )
    {
      for (int i = 0; i < fieldIndices.size(); i++) {
        final int fieldIndex = fieldIndices.get(i);
        final int cmp;
        if (aggFlags.get(i)) {
          cmp = ((Comparable) entry1.getValues()[fieldIndex]).compareTo(entry2.getValues()[fieldIndex]);
        } else {
          cmp = ((Comparable) entry1.getKey().getKey()[fieldIndex + dimStart]).compareTo(
              entry2.getKey().getKey()[fieldIndex + dimStart]
          );
        }
        if (cmp != 0) {
          return cmp * directions.get(i);
        }
      }

      return 0;
    }
  }

  private static class RowBasedKeySerde implements Grouper.KeySerde<RowBasedGrouperHelper.RowBasedKey>
  {
    // Entry in dictionary, node pointer in reverseDictionary, hash + k/v/next pointer in reverseDictionary nodes
    private static final int ROUGH_OVERHEAD_PER_DICTIONARY_ENTRY = Longs.BYTES * 5 + Ints.BYTES;

    private final boolean includeTimestamp;
    private final boolean sortByDimsFirst;
    private final List<DimensionSpec> dimensions;
    private final int dimCount;
    private final int keySize;
    private final ByteBuffer keyBuffer;
    private final List<String> dictionary = Lists.newArrayList();
    private final Map<String, Integer> reverseDictionary = Maps.newHashMap();
    private final List<RowBasedKeySerdeHelper> serdeHelpers;
    private final LimitSpec limitSpec;

    // Size limiting for the dictionary, in (roughly estimated) bytes.
    private final long maxDictionarySize;
    private long currentEstimatedSize = 0;

    // dictionary id -> its position if it were sorted by dictionary value
    private int[] sortableIds = null;

    RowBasedKeySerde(
        final boolean includeTimestamp,
        final boolean sortByDimsFirst,
        final List<DimensionSpec> dimensions,
        final long maxDictionarySize,
        LimitSpec limitSpec
    )
    {
      this.includeTimestamp = includeTimestamp;
      this.sortByDimsFirst = sortByDimsFirst;
      this.dimensions = dimensions;
      this.dimCount = dimensions.size();
      this.maxDictionarySize = maxDictionarySize;
      this.serdeHelpers = getSerdeHelpers();
      this.keySize = (includeTimestamp ? Longs.BYTES : 0) + getTotalKeySize();
      this.keyBuffer = ByteBuffer.allocate(keySize);
      this.limitSpec = limitSpec;
    }

    @Override
    public int keySize()
    {
      return keySize;
    }

    @Override
    public Class<RowBasedKey> keyClazz()
    {
      return RowBasedKey.class;
    }

    @Override
    public ByteBuffer toByteBuffer(RowBasedKey key)
    {
      keyBuffer.rewind();

      final int dimStart;
      if (includeTimestamp) {
        keyBuffer.putLong((long) key.getKey()[0]);
        dimStart = 1;
      } else {
        dimStart = 0;
      }

      for (int i = dimStart; i < key.getKey().length; i++) {
        final int id = addToDictionary((String) key.getKey()[i]);
        if (id < 0) {
          return null;
        }
        keyBuffer.putInt(id);
      }

      keyBuffer.flip();
      return keyBuffer;
    }

    @Override
    public RowBasedKey fromByteBuffer(ByteBuffer buffer, int position)
    {
      final int dimStart;
      final Comparable[] key;
      final int dimsPosition;

      if (includeTimestamp) {
        key = new Comparable[dimCount + 1];
        key[0] = buffer.getLong(position);
        dimsPosition = position + Longs.BYTES;
        dimStart = 1;
      } else {
        key = new Comparable[dimCount];
        dimsPosition = position;
        dimStart = 0;
      }

      for (int i = dimStart; i < key.length; i++) {
        key[i] = dictionary.get(buffer.getInt(dimsPosition + (Ints.BYTES * (i - dimStart))));
      }

      return new RowBasedKey(key);
    }

    @Override
    public Grouper.KeyComparator bufferComparator()
    {
      if (sortableIds == null) {
        Map<String, Integer> sortedMap = Maps.newTreeMap();
        for (int id = 0; id < dictionary.size(); id++) {
          sortedMap.put(dictionary.get(id), id);
        }
        sortableIds = new int[dictionary.size()];
        int index = 0;
        for (final Integer id : sortedMap.values()) {
          sortableIds[id] = index++;
        }
      }

      if (includeTimestamp) {
        if (sortByDimsFirst) {
          return new Grouper.KeyComparator()
          {
            @Override
            public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
            {
              final int cmp = compareDimsInBuffersForNullFudgeTimestamp(
                  sortableIds,
                  dimCount,
                  lhsBuffer,
                  rhsBuffer,
                  lhsPosition,
                  rhsPosition
              );
              if (cmp != 0) {
                return cmp;
              }

              return Longs.compare(lhsBuffer.getLong(lhsPosition), rhsBuffer.getLong(rhsPosition));
            }
          };
        } else {
          return new Grouper.KeyComparator()
          {
            @Override
            public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
            {
              final int timeCompare = Longs.compare(lhsBuffer.getLong(lhsPosition), rhsBuffer.getLong(rhsPosition));

              if (timeCompare != 0) {
                return timeCompare;
              }

              return compareDimsInBuffersForNullFudgeTimestamp(
                  sortableIds,
                  dimCount,
                  lhsBuffer,
                  rhsBuffer,
                  lhsPosition,
                  rhsPosition
              );
            }
          };
        }
      } else {
        return new Grouper.KeyComparator()
        {
          @Override
          public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
          {
            for (int i = 0; i < dimCount; i++) {
              final int cmp = Ints.compare(
                  sortableIds[lhsBuffer.getInt(lhsPosition + (Ints.BYTES * i))],
                  sortableIds[rhsBuffer.getInt(rhsPosition + (Ints.BYTES * i))]
              );

              if (cmp != 0) {
                return cmp;
              }
            }

            return 0;
          }
        };
      }
    }

    @Override
    public Grouper.KeyComparator bufferComparatorWithAggregators(
        AggregatorFactory[] aggregatorFactories,
        int[] aggregatorOffsets
    )
    {
      final List<RowBasedKeySerdeHelper> adjustedSerdeHelpers;
      final List<Integer> directions = Lists.newArrayList();
      List<RowBasedKeySerdeHelper> orderByHelpers = new ArrayList<>();
      List<RowBasedKeySerdeHelper> otherDimHelpers = new ArrayList<>();
      Set<Integer> orderByIndices = new HashSet<>();

      int aggCount = 0;
      int direction;
      for (OrderByColumnSpec orderSpec : ((DefaultLimitSpec) limitSpec).getColumns()) {
        direction = orderSpec.getDirection() == OrderByColumnSpec.Direction.ASCENDING ? 1 : -1;
        int dimIndex = OrderByColumnSpec.getDimIndexForOrderBy(orderSpec, dimensions);
        if (dimIndex >= 0) {
          RowBasedKeySerdeHelper serdeHelper = serdeHelpers.get(dimIndex);
          orderByHelpers.add(serdeHelper);
          orderByIndices.add(dimIndex);
          directions.add(direction);
        } else {
          int aggIndex = OrderByColumnSpec.getAggIndexForOrderBy(orderSpec, Arrays.asList(aggregatorFactories));
          if (aggIndex >= 0) {
            aggCount++;
            String typeName = aggregatorFactories[aggIndex].getTypeName();
            int aggOffset = aggregatorOffsets[aggIndex] - Ints.BYTES;
            if (typeName.equals("long")) {
              RowBasedKeySerdeHelper serdeHelper = new LongRowBasedKeySerdeHelper(aggOffset);
              orderByHelpers.add(serdeHelper);
              directions.add(direction);
            } else if (typeName.equals("float")) {
              // called "float", but the aggs really return doubles
              RowBasedKeySerdeHelper serdeHelper = new DoubleRowBasedKeySerdeHelper(aggOffset);
              orderByHelpers.add(serdeHelper);
              directions.add(direction);
            } else {
              throw new IAE("Cannot order by a non-numeric aggregator[%s]", orderSpec);
            }
          }
        }
      }

      for (int i = 0; i < dimCount; i++) {
        if (!orderByIndices.contains(i)) {
          otherDimHelpers.add(serdeHelpers.get(i));
          directions.add(1); // default to Ascending order if dim is not in an orderby spec
        }
      }

      adjustedSerdeHelpers = orderByHelpers;
      adjustedSerdeHelpers.addAll(otherDimHelpers);

      final int fieldCount = dimCount + aggCount;

      if (includeTimestamp) {
        if (sortByDimsFirst) {
          return new Grouper.KeyComparator()
          {
            @Override
            public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
            {
              final int cmp = compareDimsInBuffersForNullFudgeTimestampForPushDown(
                  adjustedSerdeHelpers,
                  directions,
                  sortableIds,
                  fieldCount,
                  lhsBuffer,
                  rhsBuffer,
                  lhsPosition,
                  rhsPosition
              );
              if (cmp != 0) {
                return cmp;
              }

              return Longs.compare(lhsBuffer.getLong(lhsPosition), rhsBuffer.getLong(rhsPosition));
            }
          };
        } else {
          return new Grouper.KeyComparator()
          {
            @Override
            public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
            {
              final int timeCompare = Longs.compare(lhsBuffer.getLong(lhsPosition), rhsBuffer.getLong(rhsPosition));

              if (timeCompare != 0) {
                return timeCompare;
              }

              int cmp =  compareDimsInBuffersForNullFudgeTimestampForPushDown(
                  adjustedSerdeHelpers,
                  directions,
                  sortableIds,
                  fieldCount,
                  lhsBuffer,
                  rhsBuffer,
                  lhsPosition,
                  rhsPosition
              );

              return cmp;
            }
          };
        }
      } else {
        return new Grouper.KeyComparator()
        {
          @Override
          public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
          {
            for (int i = 0; i < fieldCount; i++) {
              final int cmp = adjustedSerdeHelpers.get(i).compare(
                  lhsBuffer,
                  rhsBuffer,
                  lhsPosition,
                  rhsPosition
              );

              if (cmp != 0) {
                return cmp * directions.get(i);
              }
            }

            return 0;
          }
        };
      }
    }

    private static int compareDimsInBuffersForNullFudgeTimestamp(
        int[] sortableIds,
        int dimCount,
        ByteBuffer lhsBuffer,
        ByteBuffer rhsBuffer,
        int lhsPosition,
        int rhsPosition
    )
    {
      for (int i = 0; i < dimCount; i++) {
        final int cmp = Ints.compare(
            sortableIds[lhsBuffer.getInt(lhsPosition + Longs.BYTES + (Ints.BYTES * i))],
            sortableIds[rhsBuffer.getInt(rhsPosition + Longs.BYTES + (Ints.BYTES * i))]
        );

        if (cmp != 0) {
          return cmp;
        }
      }

      return 0;
    }

    private static int compareDimsInBuffersForNullFudgeTimestampForPushDown(
        List<RowBasedKeySerdeHelper> serdeHelpers,
        List<Integer> directions,
        int[] sortableIds,
        int dimCount,
        ByteBuffer lhsBuffer,
        ByteBuffer rhsBuffer,
        int lhsPosition,
        int rhsPosition
    )
    {
      for (int i = 0; i < dimCount; i++) {
        final int cmp = serdeHelpers.get(i).compare(
            lhsBuffer,
            rhsBuffer,
            lhsPosition + Longs.BYTES,
            rhsPosition + Longs.BYTES
        );
        if (cmp != 0) {
          return cmp * directions.get(i);
        }
      }

      return 0;
    }

    @Override
    public void reset()
    {
      dictionary.clear();
      reverseDictionary.clear();
      sortableIds = null;
      currentEstimatedSize = 0;
    }

    /**
     * Adds s to the dictionary. If the dictionary's size limit would be exceeded by adding this key, then
     * this returns -1.
     *
     * @param s a string
     *
     * @return id for this string, or -1
     */
    private int addToDictionary(final String s)
    {
      Integer idx = reverseDictionary.get(s);
      if (idx == null) {
        final long additionalEstimatedSize = (long) s.length() * Chars.BYTES + ROUGH_OVERHEAD_PER_DICTIONARY_ENTRY;
        if (currentEstimatedSize + additionalEstimatedSize > maxDictionarySize) {
          return -1;
        }

        idx = dictionary.size();
        reverseDictionary.put(s, idx);
        dictionary.add(s);
        currentEstimatedSize += additionalEstimatedSize;
      }
      return idx;
    }

    private int getTotalKeySize()
    {
      int size = 0;
      for (RowBasedKeySerdeHelper helper : serdeHelpers) {
        size += helper.getKeyBufferValueSize();
      }
      return size;
    }

    private List<RowBasedKeySerdeHelper> getSerdeHelpers()
    {
      List<RowBasedKeySerdeHelper> helpers = new ArrayList<>();
      int keyBufferPosition = 0;
      for (int i = 0; i < dimCount; i++) {
        RowBasedKeySerdeHelper helper = new StringRowBasedKeySerdeHelper(keyBufferPosition);
        keyBufferPosition += helper.getKeyBufferValueSize();
        helpers.add(helper);
      }
      return helpers;
    }

    private interface RowBasedKeySerdeHelper
    {
      /**
       * @return The size in bytes for a value of the column handled by this SerdeHelper.
       */
      int getKeyBufferValueSize();

      /**
       * Read a value from RowBasedKey at `idx` and put the value at the current position of RowBasedKeySerde's keyBuffer.
       * advancing the position by the size returned by getKeyBufferValueSize().
       *
       * If an internal resource limit has been reached and the value could not be added to the keyBuffer,
       * (e.g., maximum dictionary size exceeded for Strings), this method returns false.
       *
       * @param key RowBasedKey containing the grouping key values for a row.
       * @param idx Index of the grouping key column within that this SerdeHelper handles
       * @return true if the value was added to the key, false otherwise
       */
      boolean putToKeyBuffer(RowBasedKey key, int idx);

      /**
       * Read a value from a ByteBuffer containing a grouping key in the same format as RowBasedKeySerde's keyBuffer and
       * put the value in `dimValues` at `dimValIdx`.
       *
       * The value to be read resides in the buffer at position (`initialOffset` + the SerdeHelper's keyBufferPosition).
       *
       * @param buffer ByteBuffer containing an array of grouping keys for a row
       * @param initialOffset Offset where non-timestamp grouping key columns start, needed because timestamp is not
       *                      always included in the buffer.
       * @param dimValIdx Index within dimValues to store the value read from the buffer
       * @param dimValues Output array containing grouping key values for a row
       */
      void getFromByteBuffer(ByteBuffer buffer, int initialOffset, int dimValIdx, Comparable[] dimValues);

      /**
       * Compare the values at lhsBuffer[lhsPosition] and rhsBuffer[rhsPosition] using the natural ordering
       * for this SerdeHelper's value type.
       *
       * @param lhsBuffer ByteBuffer containing an array of grouping keys for a row
       * @param rhsBuffer ByteBuffer containing an array of grouping keys for a row
       * @param lhsPosition Position of value within lhsBuffer
       * @param rhsPosition Position of value within rhsBuffer
       * @return Negative number if lhs < rhs, positive if lhs > rhs, 0 if lhs == rhs
       */
      int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition);
    }

    private class StringRowBasedKeySerdeHelper implements RowBasedKeySerdeHelper
    {
      final int keyBufferPosition;

      public StringRowBasedKeySerdeHelper(int keyBufferPosition)
      {
        this.keyBufferPosition = keyBufferPosition;
      }

      @Override
      public int getKeyBufferValueSize()
      {
        return Ints.BYTES;
      }

      @Override
      public boolean putToKeyBuffer(RowBasedKey key, int idx)
      {
        final int id = addToDictionary((String) key.getKey()[idx]);
        if (id < 0) {
          return false;
        }
        keyBuffer.putInt(id);
        return true;
      }

      @Override
      public void getFromByteBuffer(ByteBuffer buffer, int initialOffset, int dimValIdx, Comparable[] dimValues)
      {
        dimValues[dimValIdx] = dictionary.get(buffer.getInt(initialOffset + keyBufferPosition));
      }

      @Override
      public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
      {
        if (limitSpec != null) {
          String lhsStr = dictionary.get(lhsBuffer.getInt(lhsPosition + keyBufferPosition));
          String rhsStr = dictionary.get(rhsBuffer.getInt(rhsPosition + keyBufferPosition));
          return Ordering.<String>natural().compare(lhsStr, rhsStr);
        } else {
          return Ints.compare(
              sortableIds[lhsBuffer.getInt(lhsPosition + keyBufferPosition)],
              sortableIds[rhsBuffer.getInt(rhsPosition + keyBufferPosition)]
          );
        }
      }
    }

    private class LongRowBasedKeySerdeHelper implements RowBasedKeySerdeHelper
    {
      final int keyBufferPosition;

      public LongRowBasedKeySerdeHelper(int keyBufferPosition)
      {
        this.keyBufferPosition = keyBufferPosition;
      }

      @Override
      public int getKeyBufferValueSize()
      {
        return Longs.BYTES;
      }

      @Override
      public boolean putToKeyBuffer(RowBasedKey key, int idx)
      {
        keyBuffer.putLong((Long) key.getKey()[idx]);
        return true;
      }

      @Override
      public void getFromByteBuffer(ByteBuffer buffer, int initialOffset, int dimValIdx, Comparable[] dimValues)
      {
        dimValues[dimValIdx] = buffer.getLong(initialOffset + keyBufferPosition);
      }

      @Override
      public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
      {
        return Longs.compare(
            lhsBuffer.getLong(lhsPosition + keyBufferPosition),
            rhsBuffer.getLong(rhsPosition + keyBufferPosition)
        );
      }
    }

    private class FloatRowBasedKeySerdeHelper implements RowBasedKeySerdeHelper
    {
      final int keyBufferPosition;

      public FloatRowBasedKeySerdeHelper(int keyBufferPosition)
      {
        this.keyBufferPosition = keyBufferPosition;
      }

      @Override
      public int getKeyBufferValueSize()
      {
        return Floats.BYTES;
      }

      @Override
      public boolean putToKeyBuffer(RowBasedKey key, int idx)
      {
        keyBuffer.putFloat((Float) key.getKey()[idx]);
        return true;
      }

      @Override
      public void getFromByteBuffer(ByteBuffer buffer, int initialOffset, int dimValIdx, Comparable[] dimValues)
      {
        dimValues[dimValIdx] = buffer.getFloat(initialOffset + keyBufferPosition);
      }

      @Override
      public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
      {
        return Float.compare(
            lhsBuffer.getFloat(lhsPosition + keyBufferPosition),
            rhsBuffer.getFloat(rhsPosition + keyBufferPosition)
        );
      }
    }

    private class DoubleRowBasedKeySerdeHelper implements RowBasedKeySerdeHelper
    {
      final int keyBufferPosition;

      public DoubleRowBasedKeySerdeHelper(int keyBufferPosition)
      {
        this.keyBufferPosition = keyBufferPosition;
      }

      @Override
      public int getKeyBufferValueSize()
      {
        return Doubles.BYTES;
      }

      @Override
      public boolean putToKeyBuffer(RowBasedKey key, int idx)
      {
        keyBuffer.putDouble((Double) key.getKey()[idx]);
        return true;
      }

      @Override
      public void getFromByteBuffer(ByteBuffer buffer, int initialOffset, int dimValIdx, Comparable[] dimValues)
      {
        dimValues[dimValIdx] = buffer.getDouble(initialOffset + keyBufferPosition);
      }

      @Override
      public int compare(ByteBuffer lhsBuffer, ByteBuffer rhsBuffer, int lhsPosition, int rhsPosition)
      {
        return Double.compare(
            lhsBuffer.getDouble(lhsPosition + keyBufferPosition),
            rhsBuffer.getDouble(rhsPosition + keyBufferPosition)
        );
      }
    }
  }
}
