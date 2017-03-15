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

import static com.facebook.presto.spi.block.BlockUtil.intSaturatedCast;
import static io.airlift.slice.SizeOf.sizeOf;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class MapBlock
        extends AbstractMapBlock
{
    private static final int INSTANCE_SIZE = ClassLayout.parseClass(MapBlock.class).instanceSize();

    private final int startOffset;
    private final int positionCount;

    private final boolean[] valueIsNull;
    private final int[] offsets;
    private final Block keyBlock;
    private final Block valueBlock;
    private final int[] hashTable; // hash to location in map;

    private int sizeInBytes;
    private final int retainedSizeInBytes;

    /**
     * @param keyNativeHashCode (T)long
     * @param keyBlockNativeEquals (T, Block,int)long
     */
    public MapBlock(int startOffset, int positionCount, boolean[] valueIsNull, int[] offsets, Block keyBlock, Block valueBlock, int[] hashTable, Type keyType, MethodHandle keyNativeHashCode, MethodHandle keyBlockNativeEquals)
    {
        super(keyType, keyNativeHashCode, keyBlockNativeEquals);

        this.startOffset = startOffset;
        this.positionCount = positionCount;
        this.valueIsNull = valueIsNull;
        this.offsets = requireNonNull(offsets, "offsets is null");
        this.keyBlock = requireNonNull(keyBlock, "keyBlock is null");
        this.valueBlock = requireNonNull(valueBlock, "valueBlock is null");
        if (keyBlock.getPositionCount() != valueBlock.getPositionCount()) {
            throw new IllegalArgumentException(format("keyBlock and valueBlock has different size: %s %s", keyBlock.getPositionCount(), valueBlock.getPositionCount()));
        }
        if (hashTable.length < keyBlock.getPositionCount() * HASH_MULTIPLIER) {
            throw new IllegalArgumentException(format("keyBlock/valueBlock size does not match hash table size: %s %s", keyBlock.getPositionCount(), hashTable.length));
        }
        this.hashTable = hashTable;

        this.sizeInBytes = -1;
        this.retainedSizeInBytes = intSaturatedCast(
                INSTANCE_SIZE + keyBlock.getRetainedSizeInBytes() + valueBlock.getRetainedSizeInBytes() + sizeOf(offsets) + sizeOf(valueIsNull) + sizeOf(hashTable));
    }

    @Override
    protected Block getKeys()
    {
        return keyBlock;
    }

    @Override
    protected Block getValues()
    {
        return valueBlock;
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
        return startOffset;
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
        // this is racy but is safe because sizeInBytes is an int and the calculation is stable
        if (sizeInBytes < 0) {
            calculateSize();
        }
        return sizeInBytes;
    }

    private void calculateSize()
    {
        int valueStart = offsets[startOffset];
        int valueEnd = offsets[startOffset + positionCount];
        sizeInBytes = keyBlock.getRegionSizeInBytes(valueStart, valueEnd - valueStart) +
                valueBlock.getRegionSizeInBytes(valueStart, valueEnd - valueStart) +
                (Integer.BYTES + Byte.BYTES) * this.positionCount +
                Integer.BYTES * HASH_MULTIPLIER * (valueEnd - valueStart);
    }

    @Override
    public int getRetainedSizeInBytes()
    {
        return retainedSizeInBytes;
    }

    static void buildHashTable(Block keyBlock, int keyOffset, int keyCount, MethodHandle keyBlockHashCode, int[] outputHashTable)
    {
        // This method assumes that keyBlock has no duplicated entries (in the specified range)
        buildHashTable(keyBlock, keyOffset, keyCount, keyBlockHashCode, outputHashTable, keyOffset * HASH_MULTIPLIER, keyCount * HASH_MULTIPLIER);
    }

    static void buildHashTable(Block keyBlock, int keyOffset, int keyCount, MethodHandle keyBlockHashCode, int[] outputHashTable, int hashTableOffset, int hashTableSize)
    {
        // This method assumes that keyBlock has no duplicated entries (in the specified range)
        for (int i = 0; i < keyCount; i++) {
            if (keyBlock.isNull(keyOffset + i)) {
                throw new IllegalArgumentException("map keys cannot be null");
            }

            long hashCode;
            try {
                hashCode = (long) keyBlockHashCode.invokeExact(keyBlock, keyOffset + i);
            }
            catch (Throwable throwable) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                throw new RuntimeException(throwable);
            }

            int hash = (int) Math.floorMod(hashCode, hashTableSize);
            while (true) {
                if (outputHashTable[hashTableOffset + hash] == -1) {
                    outputHashTable[hashTableOffset + hash] = i;
                    break;
                }
                hash++;
                if (hash == hashTableSize) {
                    hash = 0;
                }
            }
        }
    }

    private void checkReadablePosition(int position)
    {
        if (position < 0 || position >= getPositionCount()) {
            throw new IllegalArgumentException("position is not valid");
        }
    }

    private int getOffset(int position)
    {
        return offsets[startOffset + position];
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("MapBlock{");
        sb.append("positionCount=").append(getPositionCount());
        sb.append('}');
        return sb.toString();
    }
}
