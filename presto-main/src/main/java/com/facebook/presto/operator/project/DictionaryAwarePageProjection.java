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
package com.facebook.presto.operator.project;

import com.facebook.presto.operator.DriverYieldSignal;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.Page;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.block.DictionaryBlock;
import com.facebook.presto.spi.block.DictionaryId;
import com.facebook.presto.spi.block.LazyBlock;
import com.facebook.presto.spi.block.RunLengthEncodedBlock;
import com.facebook.presto.spi.type.Type;

import javax.annotation.Nullable;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

public class DictionaryAwarePageProjection
        implements PageProjection
{
    private final PageProjection projection;
    private final Function<DictionaryBlock, DictionaryId> sourceIdFunction;

    private Block lastInputDictionary;
    private Optional<Block> lastOutputDictionary;
    private long lastDictionaryUsageCount;

    public DictionaryAwarePageProjection(PageProjection projection, Function<DictionaryBlock, DictionaryId> sourceIdFunction)
    {
        this.projection = requireNonNull(projection, "projection is null");
        this.sourceIdFunction = sourceIdFunction;
        verify(projection.isDeterministic(), "projection must be deterministic");
        verify(projection.getInputChannels().size() == 1, "projection must have only one input");
    }

    @Override
    public Type getType()
    {
        return projection.getType();
    }

    @Override
    public boolean isDeterministic()
    {
        return projection.isDeterministic();
    }

    @Override
    public InputChannels getInputChannels()
    {
        return projection.getInputChannels();
    }

    @Override
    public PageProjectionOutput project(ConnectorSession session, DriverYieldSignal yieldSignal, Page page, SelectedPositions selectedPositions)
    {
        return new DictionaryAwarePageProjectionOutput(session, yieldSignal, page, selectedPositions);
    }

    private class DictionaryAwarePageProjectionOutput
            implements PageProjectionOutput
    {
        private final ConnectorSession session;
        private final DriverYieldSignal yieldSignal;
        private final Block block;
        private final SelectedPositions selectedPositions;

        // if the block is RLE or dictionary block, we may handle the output in a special way
        private Optional<PageProjectionOutput> specialBlockProjectionOutput;
        // always prepare to fall back to a general block in case the special handling does not apply or fails
        private final PageProjectionOutput genericBlockProjectionOutput;

        public DictionaryAwarePageProjectionOutput(@Nullable ConnectorSession session, DriverYieldSignal yieldSignal, Page page, SelectedPositions selectedPositions)
        {
            this.session = session;
            this.yieldSignal = requireNonNull(yieldSignal, "yieldSignal is null");

            Block block = requireNonNull(page, "page is null").getBlock(0);
            if (block instanceof LazyBlock) {
                block = ((LazyBlock) block).getBlock();
            }
            this.block = block;
            this.selectedPositions = requireNonNull(selectedPositions, "selectedPositions is null");

            Optional<Block> dictionary = Optional.empty();
            if (block instanceof RunLengthEncodedBlock) {
                dictionary = Optional.of(((RunLengthEncodedBlock) block).getValue());
            }
            else if (block instanceof DictionaryBlock) {
                // Attempt to process the dictionary.  If dictionary is processing has not been considered effective, an empty response will be returned
                dictionary = Optional.of(((DictionaryBlock) block).getDictionary());
            }

            // prepare both specialized and general handling for projection
            // note that project() is rather cheap given it only initialize an iterator
            specialBlockProjectionOutput = createSpecialBlockProjection(dictionary);
            genericBlockProjectionOutput = projection.project(session, yieldSignal, new Page(block), selectedPositions);
        }

        @Override
        public Optional<Block> compute()
        {
            if (specialBlockProjectionOutput.isPresent()) {
                try {
                    Optional<Block> output = specialBlockProjectionOutput.get().compute();
                    if (output.isPresent()) {
                        lastOutputDictionary = output;
                    }
                    else {
                        // yield
                        return Optional.empty();
                    }
                }
                catch (Exception ignored) {
                    // Processing of dictionary failed, but we ignore the exception here
                    // and force reprocessing of the whole block using the normal code.
                    // The second pass may not fail due to filtering.
                    // todo dictionary processing should be able to tolerate failures of unused elements
                    specialBlockProjectionOutput = emptySpecialBlockProjection();
                }
            }

            if (block instanceof DictionaryBlock) {
                // record the usage count regardless of dictionary processing choice, so we have stats for next time
                lastDictionaryUsageCount += selectedPositions.size();
            }

            if (lastOutputDictionary.isPresent()) {
                if (block instanceof RunLengthEncodedBlock) {
                    // single value block is always considered effective, but the processing could have thrown
                    // in that case we fallback and process again so the correct error message sent
                    return Optional.of(new RunLengthEncodedBlock(lastOutputDictionary.get(), selectedPositions.size()));
                }

                if (block instanceof DictionaryBlock) {
                    DictionaryBlock dictionaryBlock = (DictionaryBlock) block;
                    // if dictionary was processed, produce a dictionary block; otherwise do normal processing
                    lastDictionaryUsageCount += selectedPositions.size();
                    int[] outputIds = filterDictionaryIds(dictionaryBlock, selectedPositions);
                    return Optional.of(new DictionaryBlock(selectedPositions.size(), lastOutputDictionary.get(), outputIds, false, sourceIdFunction.apply(dictionaryBlock)));
                }

                verify(false, "unexpected block type " + block.getClass());
            }

            // there is no special handling or special handling fails; fall back to general projection
            verify(!specialBlockProjectionOutput.isPresent());
            return genericBlockProjectionOutput.compute();
        }

        private Optional<PageProjectionOutput> createSpecialBlockProjection(Optional<Block> dictionary)
        {
            if (!dictionary.isPresent()) {
                return emptySpecialBlockProjection();
            }

            if (lastInputDictionary == dictionary.get()) {
                if (!lastOutputDictionary.isPresent()) {
                    // we must have fallen back last time
                    return emptySpecialBlockProjection();
                }
                return Optional.of(() -> lastOutputDictionary);
            }

            // Process dictionary if:
            //   this is the first block
            //   there is only entry in the dictionary
            //   the last dictionary was used for more positions than were in the dictionary
            boolean shouldProcessDictionary = lastInputDictionary == null || dictionary.get().getPositionCount() == 1 || lastDictionaryUsageCount >= lastInputDictionary.getPositionCount();

            lastDictionaryUsageCount = 0;
            lastInputDictionary = dictionary.get();

            if (shouldProcessDictionary) {
                return Optional.of(projection.project(session, yieldSignal, new Page(lastInputDictionary), SelectedPositions.positionsRange(0, lastInputDictionary.getPositionCount())));
            }
            return emptySpecialBlockProjection();
        }

        private Optional<PageProjectionOutput> emptySpecialBlockProjection()
        {
            lastOutputDictionary = Optional.empty();
            return Optional.empty();
        }
    }

    private static int[] filterDictionaryIds(DictionaryBlock dictionaryBlock, SelectedPositions selectedPositions)
    {
        int[] outputIds = new int[selectedPositions.size()];
        if (selectedPositions.isList()) {
            int[] positions = selectedPositions.getPositions();
            int endPosition = selectedPositions.getOffset() + selectedPositions.size();
            int outputIndex = 0;
            for (int position = selectedPositions.getOffset(); position < endPosition; position++) {
                outputIds[outputIndex++] = dictionaryBlock.getId(positions[position]);
            }
        }
        else {
            int endPosition = selectedPositions.getOffset() + selectedPositions.size();
            int outputIndex = 0;
            for (int position = selectedPositions.getOffset(); position < endPosition; position++) {
                outputIds[outputIndex++] = dictionaryBlock.getId(position);
            }
        }
        return outputIds;
    }
}
