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
import static java.lang.Float.intBitsToFloat;
import static java.lang.String.format;
import static java.lang.String.valueOf;

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
            return type.getTypeParameters().stream().allMatch(fieldType -> canCastToJson(fieldType));
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

    // Read the map key as the Json object key format (string)
    public abstract static class MapObjectKeyReader
    {
        public abstract String read(Block block, int position);

        public static MapObjectKeyReader createMapObjectKeyReader(Type type)
        {
            String baseType = type.getTypeSignature().getBase();
            if (type instanceof UnknownType) {
                return new MapObjectKeyReader() {
                    @Override
                    public String read(Block block, int position)
                    {
                        return null;
                    }
                };
            }
            else if (baseType.equals(StandardTypes.BOOLEAN)) {
                return new MapObjectKeyReader() {
                    @Override
                    public String read(Block block, int position)
                    {
                        return type.getBoolean(block, position) ? "true" : "false";
                    }
                };
            }
            else if (baseType.equals(StandardTypes.TINYINT) ||
                    baseType.equals(StandardTypes.SMALLINT) ||
                    baseType.equals(StandardTypes.INTEGER) ||
                    baseType.equals(StandardTypes.BIGINT)) {
                return new MapObjectKeyReader() {
                    @Override
                    public String read(Block block, int position)
                    {
                        return valueOf(type.getLong(block, position));
                    }
                };
            }
            else if (baseType.equals(StandardTypes.REAL)) {
                return new MapObjectKeyReader() {
                    @Override
                    public String read(Block block, int position)
                    {
                        return valueOf(intBitsToFloat((int) type.getLong(block, position)));
                    }
                };
            }
            else if (baseType.equals(StandardTypes.DOUBLE)) {
                return new MapObjectKeyReader() {
                    @Override
                    public String read(Block block, int position)
                    {
                        return valueOf(type.getDouble(block, position));
                    }
                };
            }
            else if (baseType.equals(StandardTypes.DECIMAL)) {
                DecimalType decimalType = (DecimalType) type;
                if (isShortDecimal(decimalType)) {
                    return new MapObjectKeyReader() {
                        @Override
                        public String read(Block block, int position)
                        {
                            return Decimals.toString(decimalType.getLong(block, position), decimalType.getScale());
                        }
                    };
                }
                else {
                    return new MapObjectKeyReader()
                    {
                        @Override
                        public String read(Block block, int position)
                        {
                            return valueOf(new BigDecimal(
                                    decodeUnscaledValue(type.getSlice(block, position)),
                                    decimalType.getScale()));
                        }
                    };
                }
            }
            else if (baseType.equals(StandardTypes.VARCHAR)) {
                return new MapObjectKeyReader() {
                    @Override
                    public String read(Block block, int position)
                    {
                        return type.getSlice(block, position).toStringUtf8();
                    }
                };
            }
            else {
                throw new PrestoException(INVALID_FUNCTION_ARGUMENT, format("Unsupported type: %s", type));
            }
        }
    }

    // given block and position, write to JsonGenerator
    public abstract static class JsonGeneratorWriter
    {
        // write a Json value into the JsonGenerator, provided by block and position
        public abstract void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
                throws IOException;

        // write a Json name/value pair inside the object, the name is provided by objectKey and the value is provided by block and position
        public abstract void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                throws IOException;

        public static JsonGeneratorWriter createJsonGeneratorWriter(Type type)
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
                return new JsonGeneratorArrayWriter((ArrayType) type);
            }
            if (type instanceof MapType) {
                return new JsonGeneratorMapWriter((MapType) type);
            }
            if (type instanceof RowType) {
                return new JsonGeneratorRowWriter((RowType) type);
            }

            throw new PrestoException(INVALID_FUNCTION_ARGUMENT, format("Unsupported type: %s", type));
        }

        private static class JsonGeneratorUnknownWriter
                extends JsonGeneratorWriter
        {
            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
                    throws IOException
            {
                jsonGenerator.writeNull();
            }

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                jsonGenerator.writeNullField(objectKey);
            }
        }

        private static class JsonGeneratorBooleanWriter
                extends JsonGeneratorWriter
        {
            private Type type;

            public JsonGeneratorBooleanWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
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

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    boolean value = type.getBoolean(block, position);
                    jsonGenerator.writeBooleanField(objectKey, value);
                }
            }
        }

        private static class JsonGeneratorLongWriter
                extends JsonGeneratorWriter
        {
            private Type type;

            public JsonGeneratorLongWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
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

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    long value = type.getLong(block, position);
                    jsonGenerator.writeNumberField(objectKey, value);
                }
            }
        }

        private static class JsonGeneratorRealWriter
                extends JsonGeneratorWriter
        {
            private Type type;

            public JsonGeneratorRealWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
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

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    float value = intBitsToFloat((int) type.getLong(block, position));
                    jsonGenerator.writeNumberField(objectKey, value);
                }
            }
        }

        private static class JsonGeneratorDoubleWriter
                extends JsonGeneratorWriter
        {
            private Type type;

            public JsonGeneratorDoubleWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
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

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    double value = type.getDouble(block, position);
                    jsonGenerator.writeNumberField(objectKey, value);
                }
            }
        }

        private static class JsonGeneratorShortDecimalWriter
                extends JsonGeneratorWriter
        {
            private DecimalType type;

            public JsonGeneratorShortDecimalWriter(DecimalType type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
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

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    BigDecimal value = BigDecimal.valueOf(type.getLong(block, position), type.getScale());
                    jsonGenerator.writeNumberField(objectKey, value);
                }
            }
        }

        private static class JsonGeneratorLongDeicmalWriter
                extends JsonGeneratorWriter
        {
            private DecimalType type;

            public JsonGeneratorLongDeicmalWriter(DecimalType type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
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

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    BigDecimal value = new BigDecimal(
                            decodeUnscaledValue(type.getSlice(block, position)),
                            type.getScale());
                    jsonGenerator.writeNumberField(objectKey, value);
                }
            }
        }

        private static class JsonGeneratorVarcharWriter
                extends JsonGeneratorWriter
        {
            private Type type;

            public JsonGeneratorVarcharWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
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

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    Slice value = type.getSlice(block, position);
                    jsonGenerator.writeStringField(objectKey, value.toStringUtf8());
                }
            }
        }

        private static class JsonGeneratorJsonWriter
                extends JsonGeneratorWriter
        {
            private Type type;

            public JsonGeneratorJsonWriter(Type type)
            {
                this.type = type;
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
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

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    Slice value = type.getSlice(block, position);
                    jsonGenerator.writeFieldName(objectKey);
                    jsonGenerator.writeRawValue(value.toStringUtf8());
                }
            }
        }

        private static class JsonGeneratorArrayWriter
                extends JsonGeneratorWriter
        {
            private ArrayType type;
            private JsonGeneratorWriter elementWriter;

            public JsonGeneratorArrayWriter(ArrayType type)
            {
                this.type = type;
                this.elementWriter = createJsonGeneratorWriter(type.getElementType());
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    Block arrayBlock = type.getObject(block, position);
                    jsonGenerator.writeStartArray();
                    for (int i = 0; i < arrayBlock.getPositionCount(); i++) {
                        elementWriter.writeJsonValue(jsonGenerator, arrayBlock, i);
                    }
                    jsonGenerator.writeEndArray();
                }
            }

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    Block arrayBlock = type.getObject(block, position);
                    jsonGenerator.writeFieldName(objectKey);
                    jsonGenerator.writeStartArray();
                    for (int i = 0; i < arrayBlock.getPositionCount(); i++) {
                        elementWriter.writeJsonValue(jsonGenerator, arrayBlock, i);
                    }
                    jsonGenerator.writeEndArray();
                }
            }
        }

        private static class JsonGeneratorMapWriter
                extends JsonGeneratorWriter
        {
            private MapType type;
            private MapObjectKeyReader keyReader;
            private JsonGeneratorWriter valueWriter;

            public JsonGeneratorMapWriter(MapType type)
            {
                this.type = type;
                this.keyReader = MapObjectKeyReader.createMapObjectKeyReader(type.getKeyType());
                this.valueWriter = createJsonGeneratorWriter(type.getValueType());
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    Block mapBlock = type.getObject(block, position);
                    jsonGenerator.writeStartObject();
                    for (int i = 0; i < mapBlock.getPositionCount(); i += 2) {
                        String objectKey = keyReader.read(mapBlock, i);
                        valueWriter.writeObjectKeyValuePair(jsonGenerator, objectKey, mapBlock, i + 1);
                    }
                    jsonGenerator.writeEndObject();
                }
            }

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    Block mapBlock = type.getObject(block, position);
                    Map<String, Integer> orderedKeyToValuePosition = new TreeMap<>();
                    for (int i = 0; i < mapBlock.getPositionCount(); i += 2) {
                        String nestedObjectKey = keyReader.read(mapBlock, i);
                        orderedKeyToValuePosition.put(nestedObjectKey, i + 1);
                    }

                    jsonGenerator.writeFieldName(objectKey);
                    jsonGenerator.writeStartObject();
                    for (Map.Entry<String, Integer> entry : orderedKeyToValuePosition.entrySet()) {
                        int valuePosition = entry.getValue();
                        valueWriter.writeObjectKeyValuePair(jsonGenerator, entry.getKey(), mapBlock, valuePosition);
                    }
                    jsonGenerator.writeEndObject();
                }
            }
        }

        private static class JsonGeneratorRowWriter
                extends JsonGeneratorWriter
        {
            private RowType type;
            private List<JsonGeneratorWriter> fieldWriters;

            public JsonGeneratorRowWriter(RowType type)
            {
                this.type = type;
                List<Type> fieldTypes = type.getTypeParameters();
                fieldWriters = new ArrayList<>(fieldTypes.size());
                for (int i = 0; i < fieldTypes.size(); i++) {
                    fieldWriters.add(createJsonGeneratorWriter(fieldTypes.get(i)));
                }
            }

            @Override
            public void writeJsonValue(JsonGenerator jsonGenerator, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNull();
                }
                else {
                    Block rowBlock = type.getObject(block, position);
                    jsonGenerator.writeStartArray();
                    for (int i = 0; i < rowBlock.getPositionCount(); i++) {
                        fieldWriters.get(i).writeJsonValue(jsonGenerator, rowBlock, i);
                    }
                    jsonGenerator.writeEndArray();
                }
            }

            @Override
            public void writeObjectKeyValuePair(JsonGenerator jsonGenerator, String objectKey, Block block, int position)
                    throws IOException
            {
                if (block.isNull(position)) {
                    jsonGenerator.writeNullField(objectKey);
                }
                else {
                    Block rowBlock = type.getObject(block, position);
                    jsonGenerator.writeFieldName(objectKey);
                    jsonGenerator.writeStartArray();
                    for (int i = 0; i < rowBlock.getPositionCount(); i++) {
                        fieldWriters.get(i).writeJsonValue(jsonGenerator, rowBlock, i);
                    }
                    jsonGenerator.writeEndArray();
                }
            }
        }
    }
}
