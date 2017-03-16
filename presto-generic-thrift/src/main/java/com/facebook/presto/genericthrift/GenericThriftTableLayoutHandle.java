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
package com.facebook.presto.genericthrift;

import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.ConnectorTableLayoutHandle;
import com.facebook.presto.spi.predicate.TupleDomain;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

public class GenericThriftTableLayoutHandle
        implements ConnectorTableLayoutHandle
{
    private final byte[] layoutId;
    private final TupleDomain<ColumnHandle> predicate;

    @JsonCreator
    public GenericThriftTableLayoutHandle(
            @JsonProperty("layoutId") byte[] layoutId,
            @JsonProperty("predicate") TupleDomain<ColumnHandle> predicate)
    {
        this.layoutId = requireNonNull(layoutId, "layoutId is null");
        this.predicate = requireNonNull(predicate, "predicate is null");
    }

    @JsonProperty
    public byte[] getLayoutId()
    {
        return layoutId;
    }

    @JsonProperty
    public TupleDomain<ColumnHandle> getPredicate()
    {
        return predicate;
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

        GenericThriftTableLayoutHandle other = (GenericThriftTableLayoutHandle) o;
        return Arrays.equals(layoutId, other.layoutId)
                && Objects.equals(predicate, other.predicate);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(layoutId, predicate);
    }

    @Override
    public String toString()
    {
        return toStringHelper(this)
                .add("layoutId", layoutId)
                .add("predicate", predicate)
                .toString();
    }
}
