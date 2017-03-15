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

package com.facebook.presto.spi.block;

import com.facebook.presto.spi.type.Type;
import org.openjdk.jol.info.ClassLayout;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;

import static com.facebook.presto.spi.block.BlockUtil.calculateBlockResetSize;
import static com.facebook.presto.spi.block.BlockUtil.intSaturatedCast;
import static io.airlift.slice.SizeOf.sizeOf;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class MapBlockBuilder
        extends AbstractMapBlock
        implements BlockBuilder
{
    private static final int INSTANCE_SIZE = ClassLayout.parseClass(MapBlockBuilder.class).instanceSize() + BlockBuilderStatus.INSTANCE_SIZE;

    private final MethodHandle keyBlockHashCode;

    private BlockBuilderStatus blockBuilderStatus;

    private int positionCount;
    private int[] offsets;
    private boolean[] valueIsNull;
    private final BlockBuilder keyBlockBuilder;
    private final BlockBuilder valueBlockBuilder;
    private int[] hashTable;

    private int currentEntrySize;

    public MapBlockBuilder(
            Type keyType,
            Type valueType,
            MethodHandle keyNativeHashCode,
            MethodHandle keyBlockHashCode,
            MethodHandle keyBlockNativeEquals,
            BlockBuilderStatus blockBuilderStatus,
            int expectedEntries)
    {
        super(keyType, keyNativeHashCode, keyBlockNativeEquals);

        this.keyBlockHashCode = requireNonNull(keyBlockHashCode, "keyBlockHashCode is null");
        this.blockBuilderStatus = requireNonNull(blockBuilderStatus, "blockBuilderStatus is null");

        this.positionCount = 0;
        this.offsets = new int[expectedEntries + 1];
        this.valueIsNull = new boolean[expectedEntries];
        this.keyBlockBuilder = keyType.createBlockBuilder(blockBuilderStatus, expectedEntries);
        this.valueBlockBuilder = valueType.createBlockBuilder(blockBuilderStatus, expectedEntries);
        this.hashTable = new int[16];
        Arrays.fill(hashTable, -1);
    }

    @Override
    protected Block getKeys()
    {
        return keyBlockBuilder;
    }

    @Override
    protected Block getValues()
    {
        return valueBlockBuilder;
    }

    @Override
    protected int[] getHashTable()
    {
        return hashTable;
    }

    @Override
    protected int[] getOffsets()
    {
        return offsets;
    }

    @Override
    protected int getOffsetBase()
    {
        return 0;
    }

    @Override
    protected boolean[] getValueIsNull()
    {
        return valueIsNull;
    }

    @Override
    public int getPositionCount()
    {
        return positionCount;
    }

    @Override
    public int getSizeInBytes()
    {
        return keyBlockBuilder.getSizeInBytes() + valueBlockBuilder.getSizeInBytes() +
                (Integer.BYTES + Byte.BYTES) * positionCount +
                Integer.BYTES * HASH_MULTIPLIER * keyBlockBuilder.getPositionCount();
    }

    @Override
    public int getRetainedSizeInBytes()
    {
        return intSaturatedCast(
                INSTANCE_SIZE + keyBlockBuilder.getRetainedSizeInBytes() + valueBlockBuilder.getRetainedSizeInBytes() + sizeOf(offsets) + sizeOf(valueIsNull) + sizeOf(hashTable));
    }

    @Override
    public MapElementBlockWriter beginBlockEntry()
    {
        if (currentEntrySize != 0) {
            throw new IllegalStateException("Expected current entry size to be exactly 0 but was " + currentEntrySize);
        }
        currentEntrySize++;
        return new MapElementBlockWriter(keyBlockBuilder.getPositionCount() * 2, keyBlockBuilder, valueBlockBuilder);
    }

    @Override
    public BlockBuilder closeEntry()
    {
        if (currentEntrySize != 1) {
            throw new IllegalStateException("Expected entry size to be exactly 1 but was " + currentEntrySize);
        }

        entryAdded(false);
        currentEntrySize = 0;

        int previousAggregatedEntryCount = offsets[positionCount - 1];
        int aggregatedEntryCount = offsets[positionCount];
        int entryCount = aggregatedEntryCount - previousAggregatedEntryCount;
        if (hashTable.length < aggregatedEntryCount * HASH_MULTIPLIER) {
            int newSize = BlockUtil.calculateNewArraySize(aggregatedEntryCount * HASH_MULTIPLIER);
            int oldSize = hashTable.length;
            hashTable = Arrays.copyOf(hashTable, newSize);
            Arrays.fill(hashTable, oldSize, hashTable.length, -1);
        }
        MapBlock.buildHashTable(keyBlockBuilder, previousAggregatedEntryCount, entryCount, keyBlockHashCode, hashTable, previousAggregatedEntryCount * HASH_MULTIPLIER, entryCount * HASH_MULTIPLIER);

        return this;
    }

    @Override
    public BlockBuilder appendNull()
    {
        if (currentEntrySize > 0) {
            throw new IllegalStateException("Current entry must be closed before a null can be written");
        }

        entryAdded(true);
        return this;
    }

    private void entryAdded(boolean isNull)
    {
        if (keyBlockBuilder.getPositionCount() != valueBlockBuilder.getPositionCount()) {
            throw new IllegalStateException(format("keyBlock and valueBlock has different size: %s %s", keyBlockBuilder.getPositionCount(), valueBlockBuilder.getPositionCount()));
        }
        if (valueIsNull.length <= positionCount) {
            int newSize = BlockUtil.calculateNewArraySize(valueIsNull.length);
            valueIsNull = Arrays.copyOf(valueIsNull, newSize);
            offsets = Arrays.copyOf(offsets, newSize + 1);
        }
        offsets[positionCount + 1] = keyBlockBuilder.getPositionCount();
        valueIsNull[positionCount] = isNull;
        positionCount++;

        blockBuilderStatus.addBytes(Integer.BYTES + Byte.BYTES);
    }

    @Override
    public Block build()
    {
        return new MapBlock(
                0,
                positionCount,
                valueIsNull,
                offsets,
                keyBlockBuilder.build(),
                valueBlockBuilder.build(),
                Arrays.copyOf(hashTable, offsets[positionCount] * HASH_MULTIPLIER),
                keyType,
                keyNativeHashCode,
                keyBlockNativeEquals);
    }

    @Override
    public void reset(BlockBuilderStatus blockBuilderStatus)
    {
        this.blockBuilderStatus = requireNonNull(blockBuilderStatus, "blockBuilderStatus is null");

        int newSize = calculateBlockResetSize(getPositionCount());
        int newHashTableSize = calculateBlockResetSize(offsets[positionCount + 1]);
        valueIsNull = new boolean[newSize];
        offsets = new int[newSize + 1];
        keyBlockBuilder.reset(blockBuilderStatus);
        valueBlockBuilder.reset(blockBuilderStatus);
        hashTable = new int[newHashTableSize];

        currentEntrySize = 0;
        positionCount = 0;
    }

    @Override
    public String toString()
    {
        return "MapBlockBuilder{" +
                "positionCount=" + getPositionCount() +
                '}';
    }

    @Override
    public BlockBuilder writeObject(Object value)
    {
        if (currentEntrySize != 0) {
            throw new IllegalStateException("Expected current entry size to be exactly 0 but was " + currentEntrySize);
        }
        currentEntrySize++;

        Block block = (Block) value;
        int blockPositionCount = block.getPositionCount();
        if (blockPositionCount % 2 != 0) {
            throw new IllegalArgumentException(format("block position count is not even: %s", blockPositionCount));
        }
        for (int i = 0; i < blockPositionCount; i += 2) {
            if (block.isNull(i)) {
                keyBlockBuilder.appendNull();
            }
            else {
                block.writePositionTo(i, keyBlockBuilder);
                keyBlockBuilder.closeEntry();
            }
            if (block.isNull(i + 1)) {
                valueBlockBuilder.appendNull();
            }
            else {
                block.writePositionTo(i + 1, valueBlockBuilder);
                valueBlockBuilder.closeEntry();
            }
        }
        return this;
    }

    @Override
    public void assureLoaded()
    {
    }
}
