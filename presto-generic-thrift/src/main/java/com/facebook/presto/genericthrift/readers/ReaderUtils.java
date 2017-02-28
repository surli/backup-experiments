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
package com.facebook.presto.genericthrift.readers;

import com.facebook.presto.genericthrift.client.ThriftColumnData;

import java.util.List;

public final class ReaderUtils
{
    private ReaderUtils()
    {
    }

    public static ThriftColumnData columnByName(List<ThriftColumnData> columnsData, String columnName)
    {
        for (ThriftColumnData columnData : columnsData) {
            if (columnName.equals(columnData.getColumnName())) {
                return columnData;
            }
        }
        throw new IllegalArgumentException("Column not present in data: " + columnName);
    }
}
