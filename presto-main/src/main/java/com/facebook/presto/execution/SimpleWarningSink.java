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
package com.facebook.presto.execution;

import com.facebook.presto.client.QueryError;
import com.google.common.collect.ImmutableList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Single threaded, deduplicating warning sink.
public class SimpleWarningSink
        implements WarningSink
{
    private LinkedHashMap<QueryError, Integer> warnings = new LinkedHashMap<>();

    @Override
    public void addWarning(QueryError warning)
    {
        if (!warnings.containsKey(warning)) {
            warnings.put(warning, warnings.size());
        }
    }

    @Override
    public List<QueryError> getWarnings()
    {
        if (warnings.isEmpty()) {
            return ImmutableList.of();
        }
        QueryError[] index = new QueryError[warnings.size()];
        for (Map.Entry<QueryError, Integer> entry : warnings.entrySet()) {
            index[entry.getValue()] = entry.getKey();
        }
        return ImmutableList.copyOf(index);
    }
}
