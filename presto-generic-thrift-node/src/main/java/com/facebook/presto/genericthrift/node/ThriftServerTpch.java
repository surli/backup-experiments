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
package com.facebook.presto.genericthrift.node;

import com.facebook.presto.genericthrift.client.ThriftColumnData;
import com.facebook.presto.genericthrift.client.ThriftColumnMetadata;
import com.facebook.presto.genericthrift.client.ThriftConnectorSession;
import com.facebook.presto.genericthrift.client.ThriftIndexLayoutResult;
import com.facebook.presto.genericthrift.client.ThriftNullableIndexLayoutResult;
import com.facebook.presto.genericthrift.client.ThriftNullableTableMetadata;
import com.facebook.presto.genericthrift.client.ThriftPrestoClient;
import com.facebook.presto.genericthrift.client.ThriftPropertyMetadata;
import com.facebook.presto.genericthrift.client.ThriftRowsBatch;
import com.facebook.presto.genericthrift.client.ThriftSchemaTableName;
import com.facebook.presto.genericthrift.client.ThriftSessionValue;
import com.facebook.presto.genericthrift.client.ThriftSplit;
import com.facebook.presto.genericthrift.client.ThriftSplitBatch;
import com.facebook.presto.genericthrift.client.ThriftSplitsOrRows;
import com.facebook.presto.genericthrift.client.ThriftTableLayout;
import com.facebook.presto.genericthrift.client.ThriftTableLayoutResult;
import com.facebook.presto.genericthrift.client.ThriftTableMetadata;
import com.facebook.presto.genericthrift.client.ThriftTupleDomain;
import com.facebook.presto.genericthrift.node.states.IndexInfo;
import com.facebook.presto.genericthrift.node.states.LayoutInfo;
import com.facebook.presto.genericthrift.node.states.SplitInfo;
import com.facebook.presto.genericthrift.node.utils.WrappingRecordSet;
import com.facebook.presto.genericthrift.writers.ColumnWriter;
import com.facebook.presto.genericthrift.writers.ColumnWriters;
import com.facebook.presto.spi.RecordCursor;
import com.facebook.presto.spi.RecordSet;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.split.MappedRecordSet;
import com.facebook.presto.tests.tpch.TpchIndexedData;
import com.facebook.presto.tpch.TpchMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import io.airlift.tpch.TpchColumn;
import io.airlift.tpch.TpchColumnType;
import io.airlift.tpch.TpchEntity;
import io.airlift.tpch.TpchTable;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.facebook.presto.tests.AbstractTestIndexedQueries.INDEX_SPEC;
import static com.facebook.presto.tpch.TpchMetadata.ROW_NUMBER_COLUMN_NAME;
import static com.facebook.presto.tpch.TpchMetadata.getPrestoType;
import static com.facebook.presto.tpch.TpchRecordSet.createTpchRecordSet;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static io.airlift.concurrent.Threads.threadsNamed;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;

public class ThriftServerTpch
        implements ThriftPrestoClient
{
    private static final int DEFAULT_NUMBER_OF_SPLITS = 3;
    private static final String NUMBER_OF_SPLITS_PARAMETER = "splits";
    private static final List<String> SCHEMAS = ImmutableList.of("tiny", "sf1");
    private final ObjectMapper mapper = new ObjectMapper();
    private final ListeningExecutorService splitsExecutor =
            listeningDecorator(newCachedThreadPool(threadsNamed("splits-generator-%s")));
    private final ListeningExecutorService dataExecutor =
            listeningDecorator(newCachedThreadPool(threadsNamed("data-generator-%s")));
    private final ListeningExecutorService indexDataExecutor =
            listeningDecorator(newCachedThreadPool(threadsNamed("index-data-generator-%s")));
    private final TpchIndexedData indexedData = new TpchIndexedData("tpchindexed", INDEX_SPEC);

    @Override
    public List<ThriftPropertyMetadata> listSessionProperties()
    {
        return ImmutableList.of(new ThriftPropertyMetadata(NUMBER_OF_SPLITS_PARAMETER, "integer", "Number of splits",
                new ThriftSessionValue(false, null, 3, null, null, null),
                false));
    }

    @Override
    public List<String> listSchemaNames()
    {
        return SCHEMAS;
    }

    @Override
    public List<ThriftSchemaTableName> listTables(@Nullable String schemaNameOrNull)
    {
        List<ThriftSchemaTableName> result = new ArrayList<>();
        for (String schemaName : getSchemaNames(schemaNameOrNull)) {
            for (TpchTable<?> tpchTable : TpchTable.getTables()) {
                result.add(new ThriftSchemaTableName(schemaName, tpchTable.getTableName()));
            }
        }
        return result;
    }

    private static List<String> getSchemaNames(String schemaNameOrNull)
    {
        if (schemaNameOrNull == null) {
            return SCHEMAS;
        }
        else if (SCHEMAS.contains(schemaNameOrNull)) {
            return ImmutableList.of(schemaNameOrNull);
        }
        else {
            return ImmutableList.of();
        }
    }

    @Override
    public ThriftNullableTableMetadata getTableMetadata(ThriftSchemaTableName schemaTableName)
    {
        String schemaName = schemaTableName.getSchemaName();
        String tableName = schemaTableName.getTableName();
        if (!SCHEMAS.contains(schemaName) || TpchTable.getTables().stream().noneMatch(table -> table.getTableName().equals(tableName))) {
            return new ThriftNullableTableMetadata(null);
        }
        TpchTable<?> tpchTable = TpchTable.getTable(schemaTableName.getTableName());
        List<ThriftColumnMetadata> columns = new ArrayList<>();
        for (TpchColumn<? extends TpchEntity> column : tpchTable.getColumns()) {
            columns.add(new ThriftColumnMetadata(column.getColumnName(), getThriftType(column.getType()), null, false));
        }
        columns.add(new ThriftColumnMetadata(ROW_NUMBER_COLUMN_NAME, "bigint", null, true));
        return new ThriftNullableTableMetadata(new ThriftTableMetadata(schemaTableName, columns));
    }

    private static String getThriftType(TpchColumnType tpchType)
    {
        return TpchMetadata.getPrestoType(tpchType).getTypeSignature().toString();
    }

    @Override
    public List<ThriftTableLayoutResult> getTableLayouts(
            ThriftConnectorSession session,
            ThriftSchemaTableName schemaTableName,
            ThriftTupleDomain outputConstraint,
            @Nullable Set<String> desiredColumns)
    {
        List<String> columns = desiredColumns != null ? ImmutableList.copyOf(desiredColumns) : allColumns(schemaTableName.getTableName());
        byte[] layoutId = serialize(new LayoutInfo(schemaTableName.getSchemaName(), schemaTableName.getTableName(), columns));
        return ImmutableList.of(new ThriftTableLayoutResult(new ThriftTableLayout(layoutId, outputConstraint), outputConstraint));
    }

    private static int getOrElse(Map<String, ThriftSessionValue> values, String name, int defaultValue)
    {
        ThriftSessionValue parameterValue = values.get(name);
        if (parameterValue != null && !parameterValue.isNullValue()) {
            return parameterValue.getIntValue();
        }
        else {
            return defaultValue;
        }
    }

    @Override
    public ListenableFuture<ThriftSplitBatch> getSplits(
            ThriftConnectorSession session,
            ThriftTableLayout layout,
            int maxSplitCount,
            @Nullable byte[] continuationToken)
    {
        return splitsExecutor.submit(() -> getSplitsInternal(session, layout, maxSplitCount, continuationToken));
    }

    private ThriftSplitBatch getSplitsInternal(
            ThriftConnectorSession session,
            ThriftTableLayout layout,
            int maxSplitCount,
            @Nullable byte[] continuationToken)
    {
        LayoutInfo layoutInfo = deserialize(layout.getLayoutId(), LayoutInfo.class);
        int totalParts = getOrElse(session.getProperties(), NUMBER_OF_SPLITS_PARAMETER, DEFAULT_NUMBER_OF_SPLITS);
        // last sent part
        int partNumber = continuationToken == null ? 0 : Ints.fromByteArray(continuationToken);
        int numberOfSplits = Math.min(maxSplitCount, totalParts - partNumber);

        List<ThriftSplit> splits = new ArrayList<>(numberOfSplits);
        for (int i = 0; i < numberOfSplits; i++) {
            SplitInfo splitInfo = new SplitInfo(
                    layoutInfo.getSchemaName(),
                    layoutInfo.getTableName(),
                    partNumber + 1,
                    totalParts,
                    layoutInfo.getColumnNames());
            byte[] splitId = serialize(splitInfo);
            splits.add(new ThriftSplit(splitId, ImmutableList.of()));
            partNumber++;
        }
        byte[] nextToken = partNumber < totalParts ? Ints.toByteArray(partNumber) : null;
        return new ThriftSplitBatch(splits, nextToken);
    }

    @Override
    public ListenableFuture<ThriftRowsBatch> getRows(byte[] splitId, int maxRowCount, @Nullable byte[] continuationToken)
    {
        return dataExecutor.submit(() -> getRowsInternal(splitId, maxRowCount, continuationToken));
    }

    @Override
    public ThriftNullableIndexLayoutResult resolveIndex(
            ThriftConnectorSession session,
            ThriftSchemaTableName schemaTableName,
            Set<String> indexableColumnNames,
            Set<String> outputColumnNames,
            ThriftTupleDomain outputConstraint)
    {
        Optional<TpchIndexedData.IndexedTable> indexedTable =
                indexedData.getIndexedTable(schemaTableName.getTableName(), schemaNameToScaleFactor(schemaTableName.getSchemaName()), indexableColumnNames);
        if (!indexedTable.isPresent()) {
            return new ThriftNullableIndexLayoutResult(null);
        }
        IndexInfo indexInfo = new IndexInfo(schemaTableName.getSchemaName(), schemaTableName.getTableName(), indexableColumnNames, ImmutableList.copyOf(outputColumnNames));
        return new ThriftNullableIndexLayoutResult(new ThriftIndexLayoutResult(serialize(indexInfo), outputConstraint));
    }

    @Override
    public ThriftSplitsOrRows getRowsOrSplitsForIndex(byte[] indexId, ThriftRowsBatch keys, int maxSplitCount, int maxRowCount)
    {
        return new ThriftSplitsOrRows(null, getRowsForIndexInternal(indexId, keys, maxRowCount, null));
    }

    @Override
    public ListenableFuture<ThriftSplitBatch> getSplitsForIndexContinued(byte[] indexId, ThriftRowsBatch keys, int maxSplitCount, @Nullable byte[] continuationToken)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListenableFuture<ThriftRowsBatch> getRowsForIndexContinued(byte[] indexId, ThriftRowsBatch keys, int maxRowCount, @Nullable byte[] continuationToken)
    {
        return indexDataExecutor.submit(() -> getRowsForIndexInternal(indexId, keys, maxRowCount, continuationToken));
    }

    @PreDestroy
    @Override
    public void close()
    {
        splitsExecutor.shutdownNow();
        dataExecutor.shutdownNow();
        indexDataExecutor.shutdownNow();
    }

    private ThriftRowsBatch getRowsForIndexInternal(byte[] indexId, ThriftRowsBatch keys, int maxRowCount, @Nullable byte[] continuationToken)
    {
        IndexInfo indexInfo = deserialize(indexId, IndexInfo.class);
        Optional<TpchIndexedData.IndexedTable> indexedTableOptional =
                indexedData.getIndexedTable(indexInfo.getTableName(), schemaNameToScaleFactor(indexInfo.getSchemaName()), indexInfo.getIndexableColumnNames());
        checkState(indexedTableOptional.isPresent(), "index is not present");
        TpchIndexedData.IndexedTable table = indexedTableOptional.get();
        RecordSet fullRecordSet = table.lookupKeys(new WrappingRecordSet(keys, table.getKeyColumns(), types(indexInfo.getTableName(), table.getKeyColumns())));
        List<Integer> outputRemap = computeRemap(table.getOutputColumns(), indexInfo.getOutputColumnNames());
        RecordSet outputRecordSet = new MappedRecordSet(fullRecordSet, outputRemap);
        return cursorToRowsBatch(outputRecordSet.cursor(), indexInfo.getOutputColumnNames(), maxRowCount, continuationToken);
    }

    private ThriftRowsBatch getRowsInternal(byte[] splitId, int maxRowCount, @Nullable byte[] continuationToken)
    {
        SplitInfo splitInfo = deserialize(splitId, SplitInfo.class);
        List<String> columnNames = splitInfo.getColumnNames();
        RecordCursor cursor = createCursor(splitInfo, splitInfo.getColumnNames());
        return cursorToRowsBatch(cursor, columnNames, maxRowCount, continuationToken);
    }

    private static ThriftRowsBatch cursorToRowsBatch(RecordCursor cursor, List<String> columnNames, int maxRowCount, @Nullable byte[] continuationToken)
    {
        long skip = continuationToken != null ? Longs.fromByteArray(continuationToken) : 0;
        // very inefficient implementation as it needs to re-generate all previous results to get the next batch
        skipRows(cursor, skip);
        int numColumns = columnNames.size();
        List<ColumnWriter> writers = new ArrayList<>(numColumns);
        for (int i = 0; i < numColumns; i++) {
            writers.add(ColumnWriters.create(columnNames.get(i), cursor.getType(i), maxRowCount));
        }
        boolean hasNext = cursor.advanceNextPosition();
        int rowIdx;
        for (rowIdx = 0; rowIdx < maxRowCount && hasNext; rowIdx++) {
            for (int columnIdx = 0; columnIdx < numColumns; columnIdx++) {
                writers.get(columnIdx).append(cursor, columnIdx);
            }
            hasNext = cursor.advanceNextPosition();
        }
        List<ThriftColumnData> result = new ArrayList<>(numColumns);
        for (ColumnWriter writer : writers) {
            result.addAll(writer.getResult());
        }

        return new ThriftRowsBatch(result, rowIdx, hasNext ? Longs.toByteArray(skip + rowIdx) : null);
    }

    private static void skipRows(RecordCursor cursor, long numberOfRows)
    {
        boolean hasPreviousData = true;
        for (long i = 0; i < numberOfRows; i++) {
            hasPreviousData = cursor.advanceNextPosition();
        }
        checkState(hasPreviousData, "Cursor is expected to have previously generated data");
    }

    private static List<Integer> computeRemap(List<String> startSchema, List<String> endSchema)
    {
        ImmutableList.Builder<Integer> builder = ImmutableList.builder();
        for (String columnName : endSchema) {
            int index = startSchema.indexOf(columnName);
            Preconditions.checkArgument(index != -1, "Column name in end that is not in the start: %s", columnName);
            builder.add(index);
        }
        return builder.build();
    }

    private static List<String> allColumns(String tableName)
    {
        return TpchTable.getTable(tableName).getColumns().stream().map(TpchColumn::getColumnName).collect(toList());
    }

    private static List<Type> types(String tableName, List<String> columnNames)
    {
        TpchTable<?> table = TpchTable.getTable(tableName);
        return columnNames.stream().map(name -> getPrestoType(table.getColumn(name).getType())).collect(toList());
    }

    private static RecordCursor createCursor(SplitInfo splitInfo, List<String> columnNames)
    {
        switch (splitInfo.getTableName()) {
            case "orders":
                return createCursor(TpchTable.ORDERS, columnNames, splitInfo);
            case "customer":
                return createCursor(TpchTable.CUSTOMER, columnNames, splitInfo);
            case "lineitem":
                return createCursor(TpchTable.LINE_ITEM, columnNames, splitInfo);
            case "nation":
                return createCursor(TpchTable.NATION, columnNames, splitInfo);
            case "region":
                return createCursor(TpchTable.REGION, columnNames, splitInfo);
        }
        throw new IllegalArgumentException("Table not setup: " + splitInfo.getTableName());
    }

    private static <T extends TpchEntity> RecordCursor createCursor(TpchTable<T> table, List<String> columnNames, SplitInfo splitInfo)
    {
        List<TpchColumn<T>> columns = columnNames.stream().map(table::getColumn).collect(toList());
        return createTpchRecordSet(
                table,
                columns,
                schemaNameToScaleFactor(splitInfo.getSchemaName()),
                splitInfo.getPartNumber(),
                splitInfo.getTotalParts()).cursor();
    }

    private static double schemaNameToScaleFactor(String schemaName)
    {
        switch (schemaName) {
            case "tiny":
                return 0.01;
            case "sf1":
                return 1.0;
        }
        throw new IllegalArgumentException("Invalid schema name: " + schemaName);
    }

    private byte[] serialize(Object value)
    {
        try {
            return mapper.writeValueAsBytes(value);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T deserialize(byte[] value, Class<T> tClass)
    {
        try {
            return mapper.readValue(value, tClass);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
