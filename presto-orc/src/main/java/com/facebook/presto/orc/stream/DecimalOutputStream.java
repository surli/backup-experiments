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
package com.facebook.presto.orc.stream;

import com.facebook.presto.orc.OrcOutputBuffer;
import com.facebook.presto.orc.checkpoint.DecimalStreamCheckpoint;
import com.facebook.presto.orc.metadata.CompressionKind;
import com.facebook.presto.orc.metadata.Stream;
import com.facebook.presto.spi.type.Decimals;
import com.google.common.collect.ImmutableList;
import io.airlift.slice.Slice;
import io.airlift.slice.SliceOutput;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.facebook.presto.orc.metadata.Stream.StreamKind.DATA;
import static com.google.common.base.Preconditions.checkState;

public class DecimalOutputStream
        implements ValueOutputStream<DecimalStreamCheckpoint>
{
    private final OrcOutputBuffer buffer;
    private final List<DecimalStreamCheckpoint> checkpoints = new ArrayList<>();

    private boolean closed;

    public DecimalOutputStream(CompressionKind compression, int bufferSize)
    {
        this.buffer = new OrcOutputBuffer(compression, bufferSize);
    }

    @Override
    public Class<DecimalStreamCheckpoint> getCheckpointType()
    {
        return DecimalStreamCheckpoint.class;
    }

    // todo rewrite without BigInteger
    // This comes from the Apache Hive ORC code
    public void writeUnscaledValue(Slice slice)
    {
        BigInteger value = Decimals.decodeUnscaledValue(slice);

        // encode the signed number as a positive integer
        value = value.shiftLeft(1);
        int sign = value.signum();
        if (sign < 0) {
            value = value.negate();
            value = value.subtract(BigInteger.ONE);
        }
        int length = value.bitLength();
        while (true) {
            long lowBits = value.longValue() & 0x7fffffffffffffffL;
            length -= 63;
            // write out the next 63 bits worth of data
            for (int i = 0; i < 9; ++i) {
                // if this is the last byte, leave the high bit off
                if (length <= 0 && (lowBits & ~0x7f) == 0) {
                    buffer.write((byte) lowBits);
                    return;
                }
                else {
                    buffer.write((byte) (0x80 | (lowBits & 0x7f)));
                    lowBits >>>= 7;
                }
            }
            value = value.shiftRight(63);
        }
    }

    public void writeUnscaledValue(long value)
    {
        checkState(!closed);
        value = (value << 1) ^ (value >> 63);
        writeVLong(buffer, value);
    }

    // todo see if this can be faster
    // todo share with long writer
    private static void writeVLong(SliceOutput output, long value)
    {
        while (true) {
            // if there are less than 7 bits left, we are done
            if ((value & ~0b111_1111) == 0) {
                output.write((byte) value);
                return;
            }
            else {
                output.write((byte) (0x80 | (value & 0x7f)));
                value >>>= 7;
            }
        }
    }

    @Override
    public void recordCheckpoint()
    {
        checkState(!closed);
        checkpoints.add(new DecimalStreamCheckpoint(buffer.getCheckpoint()));
    }

    @Override
    public void close()
    {
        closed = true;
    }

    @Override
    public List<DecimalStreamCheckpoint> getCheckpoints()
    {
        checkState(closed);
        return ImmutableList.copyOf(checkpoints);
    }

    @Override
    public Optional<Stream> writeDataStreams(int column, SliceOutput outputStream)
    {
        checkState(closed);
        int length = buffer.writeDataTo(outputStream);
        return Optional.of(new Stream(column, DATA, length, true));
    }

    @Override
    public long getBufferedBytes()
    {
        return buffer.size();
    }

    @Override
    public long getRetainedBytes()
    {
        return buffer.getRetainedSize();
    }

    @Override
    public void reset()
    {
        closed = false;
        buffer.reset();
        checkpoints.clear();
    }
}
