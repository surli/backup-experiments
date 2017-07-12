/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.util;

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * 结果集工具类.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResultSetUtil {
    
    /**
     * 根据返回值类型返回特定类型的结果.
     * 
     * @param value 原始结果
     * @param convertType 返回值类型
     * @return 特定类型的返回结果
     */
    public static Object convertValue(final Object value, final Class<?> convertType) {
        if (null == value) {
            return convertNullValue(convertType);
        } 
        if (value.getClass() == convertType) {
            return value;
        }
        if (value instanceof Number) {
            return convertNumberValue(value, convertType);
        }
        if (value instanceof Date) {
            return convertDateValue(value, convertType);
        }
        if (String.class.equals(convertType)) {
            return value.toString();
        } else {
            return value;
        }
    }
    
    private static Object convertNullValue(final Class<?> convertType) {
        switch (convertType.getName()) {
            case "byte":
                return (byte) 0;
            case "short":
                return (short) 0;
            case "int":
                return 0;
            case "long":
                return 0L;
            case "double":
                return 0D;
            case "float":
                return 0F;
            default:
                return null;
        }
    }
    
    private static Object convertNumberValue(final Object value, final Class<?> convertType) {
        Number number = (Number) value;
        switch (convertType.getName()) {
            case "byte":
                return number.byteValue();
            case "short":
                return number.shortValue();
            case "int":
                return number.intValue();
            case "long":
                return number.longValue();
            case "double":
                return number.doubleValue();
            case "float":
                return number.floatValue();
            case "java.math.BigDecimal":
                return new BigDecimal(number.toString());
            case "java.lang.Object":
                return value;
            case "java.lang.String":
                return value.toString();
            default:
                throw new ShardingJdbcException("Unsupported data type:%s", convertType);
        }
    }
    
    private static Object convertDateValue(final Object value, final Class<?> convertType) {
        Date date = (Date) value;
        switch (convertType.getName()) {
            case "java.sql.Date":
                return new java.sql.Date(date.getTime());
            case "java.sql.Time":
                return new Time(date.getTime());
            case "java.sql.Timestamp":
                return new Timestamp(date.getTime());
            default:
                throw new ShardingJdbcException("Unsupported Date type:%s", convertType);
        }
    }
    
    /**
     * 获取结果集的列标签与索引的映射.
     * 
     * @param resultSet 结果集对象
     * @return 列标签与索引的映射
     * @throws SQLException SQL异常
     */
    public static Map<String, Integer> getColumnLabelIndexMap(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            result.put(SQLUtil.getExactlyValue(resultSetMetaData.getColumnLabel(i)), i);
        }
        return result;
    }
}
