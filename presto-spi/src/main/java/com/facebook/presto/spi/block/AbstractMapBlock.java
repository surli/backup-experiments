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

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public abstract class AbstractMapBlock
        implements Block
{
    // inverse of hash fill ratio, must be integer
    static final int HASH_MULTIPLIER = 2;

    protected final Type keyType;
    protected final MethodHandle keyNativeHashCode;
    protected final MethodHandle keyBlockNativeEquals;

    public AbstractMapBlock(Type keyType, MethodHandle keyNativeHashCode, MethodHandle keyBlockNativeEquals)
    {
        this.keyType = requireNonNull(keyType, "keyType is null");
        this.keyNativeHashCode = requireNonNull(keyNativeHashCode, "keyNativeHashCode is null");
        this.keyBlockNativeEquals = requireNonNull(keyBlockNativeEquals, "keyBlockNativeEquals is null");
    }

    protected abstract Block getKeys();

    protected abstract Block getValues();

    protected abstract int[] getHashTable();

    /**
     * offset is entry-based, not position-based. In other words,
     * if offset[1] is 3, it means the first map has 3 key-value pairs.
     */
    protected abstract int[] getOffsets();

    /**
     * offset is entry-based, not position-based.
     */
    protected abstract int getOffsetBase();

    protected abstract boolean[] getValueIsNull();

    private int getOffset(int position)
    {
        return getOffsets()[position + getOffsetBase()];
    }

    @Override
    public BlockEncoding getEncoding()
    {
        return new MapBlockEncoding(keyType, keyNativeHashCode, keyBlockNativeEquals, getKeys().getEncoding(), getValues().getEncoding());
    }

    @Override
    public Block copyPositions(List<Integer> positions)
    {
        int[] newOffsets = new int[positions.size() + 1];
        boolean[] newValueIsNull = new boolean[positions.size()];

        List<Integer> valuesPositions = new ArrayList<>();
        int newPosition = 0;
        for (int position : positions) {
            if (isNull(position)) {
                newValueIsNull[newPosition] = true;
                newOffsets[newPosition + 1] = newOffsets[newPosition];
            }
            else {
                int valuesStartOffset = getOffset(position);
                int valuesEndOffset = getOffset(position + 1);
                int valuesLength = valuesEndOffset - valuesStartOffset;

                newOffsets[newPosition + 1] = newOffsets[newPosition] + valuesLength;

                for (int elementIndex = valuesStartOffset; elementIndex < valuesEndOffset; elementIndex++) {
                    valuesPositions.add(elementIndex);
                }
            }
            newPosition++;
        }

        int[] hashTable = getHashTable();
        int[] newHashTable = new int[newOffsets[newOffsets.length - 1] * HASH_MULTIPLIER];
        int newHashIndex = 0;
        for (int position : positions) {
            int valuesStartOffset = getOffset(position);
            int valuesEndOffset = getOffset(position + 1);
            for (int hashIndex = valuesStartOffset * HASH_MULTIPLIER; hashIndex < valuesEndOffset * HASH_MULTIPLIER; hashIndex++) {
                newHashTable[newHashIndex] = hashTable[hashIndex];
                newHashIndex++;
            }
        }

        Block newKeys = getKeys().copyPositions(valuesPositions);
        Block newValues = getValues().copyPositions(valuesPositions);
        return new MapBlock(0, positions.size(), newValueIsNull, newOffsets, newKeys, newValues, newHashTable, keyType, keyNativeHashCode, keyBlockNativeEquals);
    }

    @Override
    public Block getRegion(int position, int length)
    {
        int positionCount = getPositionCount();
        if (position < 0 || length < 0 || position + length > positionCount) {
            throw new IndexOutOfBoundsException("Invalid position " + position + " in block with " + positionCount + " positions");
        }

        if (position == 0 && length == positionCount) {
            return this;
        }

        return new MapBlock(
                position + getOffsetBase(),
                length,
                getValueIsNull(),
                getOffsets(),
                getKeys(),
                getValues(),
                getHashTable(),
                keyType,
                keyNativeHashCode,
                keyBlockNativeEquals);
    }

    @Override
    public int getRegionSizeInBytes(int position, int length)
    {
        int positionCount = getPositionCount();
        if (position < 0 || length < 0 || position + length > positionCount) {
            throw new IndexOutOfBoundsException("Invalid position " + position + " in block with " + positionCount + " positions");
        }

        int valueStart = getOffsets()[getOffsetBase() + position];
        int valueEnd = getOffsets()[getOffsetBase() + position + length];

        return getKeys().getRegionSizeInBytes(valueStart, valueEnd - valueStart) +
                getValues().getRegionSizeInBytes(valueStart, valueEnd - valueStart) +
                (Integer.BYTES + Byte.BYTES) * length +
                Integer.BYTES * HASH_MULTIPLIER * (valueEnd - valueStart);
    }

    @Override
    public Block copyRegion(int position, int length)
    {
        int positionCount = getPositionCount();
        if (position < 0 || length < 0 || position + length > positionCount) {
            throw new IndexOutOfBoundsException("Invalid position " + position + " in block with " + positionCount + " positions");
        }

        int startValueOffset = getOffset(position);
        int endValueOffset = getOffset(position + length);
        Block newKeys = getKeys().copyRegion(startValueOffset, endValueOffset - startValueOffset);
        Block newValues = getValues().copyRegion(startValueOffset, endValueOffset - startValueOffset);

        int[] newOffsets = new int[length + 1];
        for (int i = 1; i < newOffsets.length; i++) {
            newOffsets[i] = getOffset(position + i) - startValueOffset;
        }
        boolean[] newValueIsNull = Arrays.copyOfRange(getValueIsNull(), position + getOffsetBase(), position + getOffsetBase() + length);
        int[] newHashTable = Arrays.copyOfRange(getHashTable(), startValueOffset * HASH_MULTIPLIER, endValueOffset * HASH_MULTIPLIER);

        return new MapBlock(
                0,
                length,
                newValueIsNull,
                newOffsets,
                newKeys,
                newValues,
                newHashTable,
                keyType,
                keyNativeHashCode,
                keyBlockNativeEquals);
    }

    @Override
    public <T> T getObject(int position, Class<T> clazz)
    {
        if (clazz != Block.class) {
            throw new IllegalArgumentException("clazz must be Block.class");
        }
        checkReadablePosition(position);

        int startEntryOffset = getOffset(position);
        int endEntryOffset = getOffset(position + 1);
        return clazz.cast(new MapElementBlock(
                startEntryOffset * 2,
                (endEntryOffset - startEntryOffset) * 2,
                getKeys(),
                getValues(),
                getHashTable(),
                keyType,
                keyNativeHashCode,
                keyBlockNativeEquals));
    }

    @Override
    public void writePositionTo(int position, BlockBuilder blockBuilder)
    {
        checkReadablePosition(position);
        BlockBuilder entryBuilder = blockBuilder.beginBlockEntry();
        int startValueOffset = getOffset(position);
        int endValueOffset = getOffset(position + 1);
        for (int i = startValueOffset; i < endValueOffset; i++) {
            if (getKeys().isNull(i)) {
                entryBuilder.appendNull();
            }
            else {
                getKeys().writePositionTo(i, entryBuilder);
                entryBuilder.closeEntry();
            }
            if (getValues().isNull(i)) {
                entryBuilder.appendNull();
            }
            else {
                getValues().writePositionTo(i, entryBuilder);
                entryBuilder.closeEntry();
            }
        }
    }

    @Override
    public Block getSingleValueBlock(int position)
    {
        checkReadablePosition(position);

        int startValueOffset = getOffset(position);
        int endValueOffset = getOffset(position + 1);
        int valueLength = endValueOffset - startValueOffset;
        Block newKeys = getKeys().copyRegion(startValueOffset, valueLength);
        Block newValues = getValues().copyRegion(startValueOffset, valueLength);
        int[] newHashTable = Arrays.copyOfRange(getHashTable(), startValueOffset * HASH_MULTIPLIER, endValueOffset * HASH_MULTIPLIER);

        return new MapBlock(
                0,
                1,
                new boolean[] {isNull(position)},
                new int[] {0, valueLength},
                newKeys,
                newValues,
                newHashTable,
                keyType,
                keyNativeHashCode,
                keyBlockNativeEquals);
    }

    @Override
    public boolean isNull(int position)
    {
        checkReadablePosition(position);
        return getValueIsNull()[position + getOffsetBase()];
    }

    private void checkReadablePosition(int position)
    {
        if (position < 0 || position >= getPositionCount()) {
            throw new IllegalArgumentException("position is not valid");
        }
    }
}
