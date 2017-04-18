/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jackrabbit.oak.plugins.index.lucene;

import java.io.IOException;

import javax.management.openmbean.TabularData;

import org.apache.jackrabbit.oak.commons.jmx.Description;
import org.apache.jackrabbit.oak.commons.jmx.Name;

public interface LuceneIndexMBean {
    String TYPE = "LuceneIndex";

    TabularData getIndexStats() throws IOException;

    TabularData getBadIndexStats();

    TabularData getBadPersistedIndexStats();

    boolean isFailing();

    @Description("Determines the set of index paths upto given maxLevel. This can be used to determine the value for" +
            "[includedPaths]. For this to work you should have [evaluatePathRestrictions] set to true in your index " +
            "definition")
    String[] getIndexedPaths(
            @Description("Index path for which stats are to be determined")
            @Name("indexPath")
            String indexPath,
            @Name("maxLevel")
            @Description("Maximum depth to examine. E.g. 5. Stats calculation would " +
                    "break out after this limit")
            int maxLevel,
            @Description("Maximum number of unique paths to examine. E.g. 100. Stats " +
                    "calculation would break out after this limit")
            @Name("maxPathCount")
            int maxPathCount
            ) throws IOException;

    @Description("Retrieves the fields, and number of documents for each field, for an index. " +
            "This allows to investigate what is stored in the index.")
    String[] getFieldInfo(
            @Name("indexPath")
            @Description("The index path (empty for all indexes)")
                    String indexPath
    ) throws IOException;

    @Description("Returns the stored index definition for index at given path in string form")
    String getStoredIndexDefinition(@Name("indexPath") String indexPath);

    @Description("Returns the diff of index definition for index at given path from the stored index definition in " +
            "string form")
    String diffStoredIndexDefinition(@Name("indexPath") String indexPath);

}
