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
import com.facebook.presto.orc.checkpoint.FloatStreamCheckpoint;
import com.facebook.presto.orc.metadata.CompressionKind;
import com.facebook.presto.orc.metadata.Stream;
import com.google.common.collect.ImmutableList;
import io.airlift.slice.SliceOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.facebook.presto.orc.metadata.Stream.StreamKind.DATA;
import static com.google.common.base.Preconditions.checkState;

public class FloatOutputStream
        implements ValueOutputStream<FloatStreamCheckpoint>
{
    private final OrcOutputBuffer buffer;
    private final List<FloatStreamCheckpoint> checkpoints = new ArrayList<>();

    private boolean closed;

    public FloatOutputStream(CompressionKind compression, int bufferSize)
    {
        this.buffer = new OrcOutputBuffer(compression, bufferSize);
    }

    @Override
    public Class<FloatStreamCheckpoint> getCheckpointType()
    {
        return FloatStreamCheckpoint.class;
    }

    public void writeFloat(float value)
    {
        checkState(!closed);
        buffer.writeFloat(value);
    }

    @Override
    public void close()
    {
        closed = true;
    }

    @Override
    public void recordCheckpoint()
    {
        checkState(!closed);
        checkpoints.add(new FloatStreamCheckpoint(buffer.getCheckpoint()));
    }

    @Override
    public List<FloatStreamCheckpoint> getCheckpoints()
    {
        checkState(closed);
        return ImmutableList.copyOf(checkpoints);
    }

    @Override
    public Optional<Stream> writeDataStreams(int column, SliceOutput outputStream)
    {
        checkState(closed);
        int length = buffer.writeDataTo(outputStream);
        return Optional.of(new Stream(column, DATA, length, false));
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
