package com.firefly.utils.json;

import com.firefly.utils.BeanUtils;
import com.firefly.utils.exception.CommonRuntimeException;
import com.firefly.utils.json.io.JsonStringReader;
import com.firefly.utils.json.io.JsonStringWriter;
import com.firefly.utils.json.parser.GeneralJSONObjectStateMacine;
import com.firefly.utils.json.parser.ParserStateMachine;
import com.firefly.utils.json.serializer.SerialStateMachine;
import com.firefly.utils.lang.GenericTypeReference;

import java.io.IOException;
import java.lang.reflect.Type;

import static com.firefly.utils.json.parser.ComplexTypeParser.getImplClass;

public abstract class Json {

    public static String toJson(Object obj) {
        try (JsonWriter writer = new JsonStringWriter()) {
            SerialStateMachine.toJson(obj, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new CommonRuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObject(String json, Class<T> clazz) {
        try (JsonReader reader = new JsonStringReader(json)) {
            return (T) ParserStateMachine.toObject(reader, clazz, clazz);
        } catch (IOException e) {
            throw new CommonRuntimeException(e);
        }
    }

    public static <T> T toObject(String json, GenericTypeReference<T> typeReference) {
        return toObject(json, typeReference.getType());
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObject(String json, Type type) {
        try (JsonReader reader = new JsonStringReader(json)) {
            Class<?> extractedClass = BeanUtils.extractClass(type);
            return (T) ParserStateMachine.toObject(reader, getImplClass(extractedClass), type);
        } catch (IOException e) {
            throw new CommonRuntimeException(e);
        }
    }

    public static JsonObject toJsonObject(String json) {
        try (JsonReader reader = new JsonStringReader(json)) {
            return GeneralJSONObjectStateMacine.toJsonObject(reader);
        } catch (IOException e) {
            throw new CommonRuntimeException(e);
        }
    }

    public static JsonArray toJsonArray(String json) {
        try (JsonReader reader = new JsonStringReader(json)) {
            return GeneralJSONObjectStateMacine.toJsonArray(reader);
        } catch (IOException e) {
            throw new CommonRuntimeException(e);
        }
    }

}
