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
package com.facebook.presto.genericthrift.client;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

import java.util.List;

import static com.facebook.swift.codec.ThriftField.Requiredness.OPTIONAL;
import static java.util.Objects.requireNonNull;

@ThriftStruct
public final class ThriftSplitBatch
{
    private final List<ThriftSplit> splits;
    private final String nextToken;

    @ThriftConstructor
    public ThriftSplitBatch(List<ThriftSplit> splits, @Nullable String nextToken)
    {
        this.splits = ImmutableList.copyOf(requireNonNull(splits, "splits"));
        this.nextToken = nextToken;
    }

    @ThriftField(1)
    public List<ThriftSplit> getSplits()
    {
        return splits;
    }

    @Nullable
    @ThriftField(value = 2, requiredness = OPTIONAL)
    public String getNextToken()
    {
        return nextToken;
    }
}
