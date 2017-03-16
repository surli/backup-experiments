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
package com.facebook.presto.genericthrift.node.states;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class IndexInfo
{
    private final String schemaName;
    private final String tableName;
    private final Set<String> indexableColumnNames;
    private final List<String> outputColumnNames;

    @JsonCreator
    public IndexInfo(
            @JsonProperty("schemaName") String schemaName,
            @JsonProperty("tableName") String tableName,
            @JsonProperty("indexableColumnNames") Set<String> indexableColumnNames,
            @JsonProperty("outputColumnNames") List<String> outputColumnNames)
    {
        this.schemaName = requireNonNull(schemaName, "schemaName is null");
        this.tableName = requireNonNull(tableName, "tableName is null");
        this.indexableColumnNames = requireNonNull(indexableColumnNames, "indexableColumnNames is null");
        this.outputColumnNames = requireNonNull(outputColumnNames, "outputColumnNames is null");
    }

    @JsonProperty
    public String getSchemaName()
    {
        return schemaName;
    }

    @JsonProperty
    public String getTableName()
    {
        return tableName;
    }

    @JsonProperty
    public Set<String> getIndexableColumnNames()
    {
        return indexableColumnNames;
    }

    @JsonProperty
    public List<String> getOutputColumnNames()
    {
        return outputColumnNames;
    }
}
