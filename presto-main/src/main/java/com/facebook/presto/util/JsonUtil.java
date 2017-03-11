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
package com.facebook.presto.util;

import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.type.DecimalType;
import com.facebook.presto.spi.type.Decimals;
import com.facebook.presto.spi.type.StandardTypes;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.type.ArrayType;
import com.facebook.presto.type.MapType;
import com.facebook.presto.type.RowType;
import com.facebook.presto.type.UnknownType;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.airlift.slice.Slice;
import io.airlift.slice.SliceOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.facebook.presto.spi.StandardErrorCode.INVALID_FUNCTION_ARGUMENT;
import static com.facebook.presto.spi.type.Decimals.decodeUnscaledValue;
import static com.facebook.presto.spi.type.Decimals.isShortDecimal;
import static com.facebook.presto.util.JsonUtil.FieldNameProvider.createFieldNameProvider;
import static java.lang.Float.intBitsToFloat;
import static java.lang.String.format;

public final class JsonUtil
{
    private JsonUtil() {}

    public static JsonParser createJsonParser(JsonFactory factory, Slice json)
            throws IOException
    {
        return factory.createParser((InputStream) json.getInput());
    }

    public static JsonGenerator createJsonGenerator(JsonFactory factory, SliceOutput output)
            throws IOException
    {
        return factory.createGenerator((OutputStream) output);
    }

    public static boolean canCastToJson(Type type)
    {
        String baseType = type.getTypeSignature().getBase();
        if (type instanceof UnknownType ||
                baseType.equals(StandardTypes.BOOLEAN) ||
                baseType.equals(StandardTypes.TINYINT) ||
                baseType.equals(StandardTypes.SMALLINT) ||
                baseType.equals(StandardTypes.INTEGER) ||
                baseType.equals(StandardTypes.BIGINT) ||
                baseType.equals(StandardTypes.REAL) ||
                baseType.equals(StandardTypes.DOUBLE) ||
                baseType.equals(StandardTypes.DECIMAL) ||
                baseType.equals(StandardTypes.VARCHAR) ||
                baseType.equals(StandardTypes.JSON)) {
            return true;
        }
        if (type instanceof ArrayType) {
            return canCastToJson(((ArrayType) type).getElementType());
        }
        if (type instanceof MapType) {
            return isValidJsonObjectKeyType(((MapType) type).getKeyType()) && canCastToJson(((MapType) type).getValueType());
        }
        if (type instanceof RowType) {
            return type.getTypeParameters().stream().allMatch(JsonUtil::canCastToJson);
        }
        return false;
    }

    private static boolean isValidJsonObjectKeyType(Type type)
    {
        String baseType = type.getTypeSignature().getBase();
        return type instanceof UnknownType ||
                baseType.equals(StandardTypes.BOOLEAN) ||
                baseType.equals(StandardTypes.TINYINT) ||
                baseType.equals(StandardTypes.SMALLINT) ||
                baseType.equals(StandardTypes.INTEGER) ||
                baseType.equals(StandardTypes.BIGINT) ||
                baseType.equals(StandardTypes.REAL) ||
                baseType.equals(StandardTypes.DOUBLE) ||
                baseType.equals(StandardTypes.DECIMAL) ||
                baseType.equals(StandardTypes.VARCHAR);
    }

    // read the map key as the Json object key format (string)
    public interface FieldNameProvider
    {
        String getFieldName(Block block, int position);

        static FieldNameProvider createFieldNameProvider(Type type)
        {
            String baseType = type.getTypeSignature().getBase();
            if (type instanceof UnknownType) {
                return (block, position) -> null;
            }
            else if (baseType.equals(StandardTypes.BOOLEAN)) {
                return (block, position) -> type.getBoolean(block, position) ? "true" : "false";
            }
            else if (baseType.equals(StandardTypes.TINYINT) ||
                    baseType.equals(StandardTypes.SMALLINT) ||
                    baseType.equals(StandardTypes.INTEGER) ||
                    baseType.equals(StandardTypes.BIGINT)) {
                return (block, position) -> String.valueOf(type.getLong(block, position));
            }
            else if (baseType.equals(StandardTypes.REAL)) {
                return (block, position) -> String.valueOf(intBitsToFloat((int) type.getLong(block, position)));
            }
            else if (baseType.equals(StandardTypes.DOUBLE)) {
                return (block, position) -> String.valueOf(type.getDouble(block, position));
            }
            else if (baseType.equals(StandardTypes.DECIMAL)) {
                DecimalType decimalType = (DecimalType) type;
                if (isShortDecimal(decimalType)) {
                    return (block, position) -> Decimals.toString(decimalType.getLong(block, position), decimalType.getScale());
                }
                else {
                    return (block, position) -> Decimals.toString(
                            decodeUnscaledValue(type.getSlice(block, position)),
                            decimalType.getScale());
                }
            }
            else if (baseType.equals(StandardTypes.VARCHAR)) {
                return (block, position) -> type.getSlice(block, position).toStringUtf8();
            }
            else {
                throw new PrestoException(INVALID_FUNCTION_ARGUMENT, format("Unsupported type: %s", type));
            }
        }
    }

    // given block and position, write to JsonGenerator
    public interface JsonGeneratorWriter
    {
        // write a Json value into the JsonGenerator, provided by block and position
        void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                throws IOException;

        static JsonGeneratorWriter createJsonGeneratorWriter(Type type)
        {
            String baseType = type.getTypeSignature().getBase();
            if (type instanceof UnknownType) {
                return new JsonGeneratorUnknownWriter();
            }
            else if (baseType.equals(StandardTypes.BOOLEAN)) {
                return new JsonGeneratorBooleanWriter(type);
            }
            else if (baseType.equals(StandardTypes.TINYINT) ||
                    baseType.equals(StandardTypes.SMALLINT) ||
                    baseType.equals(StandardTypes.INTEGER) ||
                    baseType.equals(StandardTypes.BIGINT)) {
                return new JsonGeneratorLongWriter(type);
            }
            else if (baseType.equals(StandardTypes.REAL)) {
                return new JsonGeneratorRealWriter(type);
            }
            else if (baseType.equals(StandardTypes.DOUBLE)) {
                return new JsonGeneratorDoubleWriter(type);
            }
            else if (baseType.equals(StandardTypes.DECIMAL)) {
                if (isShortDecimal(type)) {
                    return new JsonGeneratorShortDecimalWriter((DecimalType) type);
                }
                else {
                    return new JsonGeneratorLongDeicmalWriter((DecimalType) type);
                }
            }
            else if (baseType.equals(StandardTypes.VARCHAR)) {
                return new JsonGeneratorVarcharWriter(type);
            }
            else if (baseType.equals(StandardTypes.JSON)) {
                return new JsonGeneratorJsonWriter(type);
            }

            if (type instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) type;
                return new JsonGeneratorArrayWriter(
                        arrayType,
                        createJsonGeneratorWriter(arrayType.getElementType()));
            }
            if (type instanceof MapType) {
                MapType mapType = (MapType) type;
                return new JsonGeneratorMapWriter(
                        mapType,
                        createFieldNameProvider(mapType.getKeyType()),
                        createJsonGeneratorWriter(mapType.getValueType()));
            }
            if (type instanceof RowType) {
                List<Type> fieldTypes = type.getTypeParameters();
                List<JsonGeneratorWriter> fieldWriters = new ArrayList<>(fieldTypes.size());
                for (int i = 0; i < fieldTypes.size(); i++) {
                    fieldWriters.add(createJsonGeneratorWriter(fieldTypes.get(i)));
                }
                return new JsonGeneratorRowWriter((RowType) type, fieldWriters);
            }

            throw new PrestoException(INVALID_FUNCTION_ARGUMENT, format("Unsupported type: %s", type));
        }

        class JsonGeneratorUnknownWriter
                implements JsonGeneratorWriter
        {
            private JsonGeneratorUnknownWriter() {}

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                jsonGenerator.writeNull();
            }
        }

        class JsonGeneratorBooleanWriter
                implements JsonGeneratorWriter
        {
            private Type type;

            private JsonGeneratorBooleanWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    boolean value = type.getBoolean(block, position);
                    jsonGenerator.writeBoolean(value);
                }
            }
        }

        class JsonGeneratorLongWriter
                implements JsonGeneratorWriter
        {
            private Type type;

            private JsonGeneratorLongWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    long value = type.getLong(block, position);
                    jsonGenerator.writeNumber(value);
                }
            }
        }

        class JsonGeneratorRealWriter
                implements JsonGeneratorWriter
        {
            private Type type;

            private JsonGeneratorRealWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    float value = intBitsToFloat((int) type.getLong(block, position));
                    jsonGenerator.writeNumber(value);
                }
            }
        }

        class JsonGeneratorDoubleWriter
                implements JsonGeneratorWriter
        {
            private Type type;

            private JsonGeneratorDoubleWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    double value = type.getDouble(block, position);
                    jsonGenerator.writeNumber(value);
                }
            }
        }

        class JsonGeneratorShortDecimalWriter
                implements JsonGeneratorWriter
        {
            private DecimalType type;

            private JsonGeneratorShortDecimalWriter(DecimalType type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    BigDecimal value = BigDecimal.valueOf(type.getLong(block, position), type.getScale());
                    jsonGenerator.writeNumber(value);
                }
            }
        }

        class JsonGeneratorLongDeicmalWriter
                implements JsonGeneratorWriter
        {
            private DecimalType type;

            private JsonGeneratorLongDeicmalWriter(DecimalType type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    BigDecimal value = new BigDecimal(
                            decodeUnscaledValue(type.getSlice(block, position)),
                            type.getScale());
                    jsonGenerator.writeNumber(value);
                }
            }
        }

        class JsonGeneratorVarcharWriter
                implements JsonGeneratorWriter
        {
            private Type type;

            private JsonGeneratorVarcharWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    Slice value = type.getSlice(block, position);
                    jsonGenerator.writeString(value.toStringUtf8());
                }
            }
        }

        class JsonGeneratorJsonWriter
                implements JsonGeneratorWriter
        {
            private Type type;

            private JsonGeneratorJsonWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    Slice value = type.getSlice(block, position);
                    jsonGenerator.writeRawValue(value.toStringUtf8());
                }
            }
        }

        class JsonGeneratorArrayWriter
                implements JsonGeneratorWriter
        {
            private ArrayType type;
            private JsonGeneratorWriter elementWriter;

            private JsonGeneratorArrayWriter(ArrayType type, JsonGeneratorWriter elementWriter)
            {
                this.type = type;
                this.elementWriter = elementWriter;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    Block arrayBlock = type.getObject(block, position);
                    jsonGenerator.writeStartArray();
                    for (int i = 0; i < arrayBlock.getPositionCount(); i++) {
                        elementWriter.writeJsonValue(jsonGenerator, arrayBlock, i, session);
                    }
                    jsonGenerator.writeEndArray();
                }
            }
        }

        class JsonGeneratorMapWriter
                implements JsonGeneratorWriter
        {
            private MapType type;
            private FieldNameProvider keyProvider;
            private JsonGeneratorWriter valueWriter;

            private JsonGeneratorMapWriter(MapType type, FieldNameProvider keyProvider, JsonGeneratorWriter valueWriter)
            {
                this.type = type;
                this.keyProvider = keyProvider;
                this.valueWriter = valueWriter;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    Block mapBlock = type.getObject(block, position);
                    Map<String, Integer> orderedKeyToValuePosition = new TreeMap<>();
                    for (int i = 0; i < mapBlock.getPositionCount(); i += 2) {
                        String objectKey = keyProvider.getFieldName(mapBlock, i);
                        orderedKeyToValuePosition.put(objectKey, i + 1);
                    }

                    jsonGenerator.writeStartObject();
                    for (Map.Entry<String, Integer> entry : orderedKeyToValuePosition.entrySet()) {
                        jsonGenerator.writeFieldName(entry.getKey());
                        valueWriter.writeJsonValue(jsonGenerator, mapBlock, entry.getValue(), session);
                    }
                    jsonGenerator.writeEndObject();
                }
            }
        }

        class JsonGeneratorRowWriter
                implements JsonGeneratorWriter
        {
            private RowType type;
            private List<JsonGeneratorWriter> fieldWriters;

            private JsonGeneratorRowWriter(RowType type, List<JsonGeneratorWriter> fieldWriters)
            {
                this.type = type;
                this.fieldWriters = fieldWriters;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position, ConnectorSession session)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    Block rowBlock = type.getObject(block, position);
                    jsonGenerator.writeStartArray();
                    for (int i = 0; i < rowBlock.getPositionCount(); i++) {
                        fieldWriters.get(i).writeJsonValue(jsonGenerator, rowBlock, i, session);
                    }
                    jsonGenerator.writeEndArray();
                }
            }
        }
    }
}
