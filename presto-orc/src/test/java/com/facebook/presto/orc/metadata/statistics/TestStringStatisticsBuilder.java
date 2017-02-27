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
package com.facebook.presto.orc.metadata.statistics;

import io.airlift.slice.Slice;
import org.testng.annotations.Test;

import static com.facebook.presto.orc.metadata.statistics.AbstractStatisticsBuilderTest.StatisticsType.STRING;
import static io.airlift.slice.Slices.EMPTY_SLICE;
import static io.airlift.slice.Slices.utf8Slice;

public class TestStringStatisticsBuilder
        extends AbstractStatisticsBuilderTest<StringStatisticsBuilder, Slice>
{
    // U+0000 to U+D7FF
    private static final Slice LOW_BOTTOM_VALUE = utf8Slice("foo \u0000");
    private static final Slice LOW_TOP_VALUE = utf8Slice("foo \uD7FF");
    // U+10000 to U+10FFFF
    private static final Slice MEDIUM_BOTTOM_VALUE = utf8Slice("foo \uD800\uDC00");
    private static final Slice MEDIUM_TOP_VALUE = utf8Slice("foo \uDBFF\uDFFF");
    // U+E000 to U+FFFF
    private static final Slice HIGH_BOTTOM_VALUE = utf8Slice("foo \uE000");
    private static final Slice HIGH_TOP_VALUE = utf8Slice("foo \uFFFF");

    public TestStringStatisticsBuilder()
    {
        super(STRING, StringStatisticsBuilder::new, StringStatisticsBuilder::addValue);
    }

    @Test
    public void testMinMaxValues()
    {
        assertMinMaxValues(EMPTY_SLICE, EMPTY_SLICE);
        assertMinMaxValues(LOW_BOTTOM_VALUE, LOW_BOTTOM_VALUE);
        assertMinMaxValues(LOW_TOP_VALUE, LOW_TOP_VALUE);
        assertMinMaxValues(MEDIUM_BOTTOM_VALUE, MEDIUM_BOTTOM_VALUE);
        assertMinMaxValues(MEDIUM_TOP_VALUE, MEDIUM_TOP_VALUE);
        assertMinMaxValues(HIGH_BOTTOM_VALUE, HIGH_BOTTOM_VALUE);
        assertMinMaxValues(HIGH_TOP_VALUE, HIGH_TOP_VALUE);

        assertMinMaxValues(EMPTY_SLICE, LOW_BOTTOM_VALUE);
        assertMinMaxValues(EMPTY_SLICE, LOW_TOP_VALUE);
        assertMinMaxValues(EMPTY_SLICE, MEDIUM_BOTTOM_VALUE);
        assertMinMaxValues(EMPTY_SLICE, MEDIUM_TOP_VALUE);
        assertMinMaxValues(EMPTY_SLICE, HIGH_BOTTOM_VALUE);
        assertMinMaxValues(EMPTY_SLICE, HIGH_TOP_VALUE);

        assertMinMaxValues(LOW_BOTTOM_VALUE, LOW_TOP_VALUE);
        assertMinMaxValues(LOW_BOTTOM_VALUE, MEDIUM_BOTTOM_VALUE);
        assertMinMaxValues(LOW_BOTTOM_VALUE, MEDIUM_TOP_VALUE);
        assertMinMaxValues(LOW_BOTTOM_VALUE, HIGH_BOTTOM_VALUE);
        assertMinMaxValues(LOW_BOTTOM_VALUE, HIGH_TOP_VALUE);

        assertMinMaxValues(LOW_TOP_VALUE, MEDIUM_BOTTOM_VALUE);
        assertMinMaxValues(LOW_TOP_VALUE, MEDIUM_TOP_VALUE);
        assertMinMaxValues(LOW_TOP_VALUE, HIGH_BOTTOM_VALUE);
        assertMinMaxValues(LOW_TOP_VALUE, HIGH_TOP_VALUE);

        assertMinMaxValues(MEDIUM_BOTTOM_VALUE, MEDIUM_TOP_VALUE);
        assertMinMaxValues(MEDIUM_BOTTOM_VALUE, HIGH_BOTTOM_VALUE);
        assertMinMaxValues(MEDIUM_BOTTOM_VALUE, HIGH_TOP_VALUE);

        assertMinMaxValues(MEDIUM_TOP_VALUE, HIGH_BOTTOM_VALUE);
        assertMinMaxValues(MEDIUM_TOP_VALUE, HIGH_TOP_VALUE);

        assertMinMaxValues(HIGH_BOTTOM_VALUE, HIGH_TOP_VALUE);
    }
}
