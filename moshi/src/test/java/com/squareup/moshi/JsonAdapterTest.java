/*
 * Copyright (C) 2017 Square, Inc.
 *
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
 */
package com.squareup.moshi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public final class JsonAdapterTest {
  @Parameter public JsonCodecFactory factory;

  @Parameters(name = "{0}")
  public static List<Object[]> parameters() {
    return JsonCodecFactory.factories();
  }

  @Test public void lenient() throws Exception {
    JsonAdapter<Double> lenient = new JsonAdapter<Double>() {
      @Override public Double fromJson(JsonReader reader) throws IOException {
        return reader.nextDouble();
      }

      @Override public void toJson(JsonWriter writer, Double value) throws IOException {
        writer.value(value);
      }
    }.lenient();

    JsonReader reader = factory.newReader("[-Infinity, NaN, Infinity]");
    reader.beginArray();
    assertThat(lenient.fromJson(reader)).isEqualTo(Double.NEGATIVE_INFINITY);
    assertThat(lenient.fromJson(reader)).isNaN();
    assertThat(lenient.fromJson(reader)).isEqualTo(Double.POSITIVE_INFINITY);
    reader.endArray();

    JsonWriter writer = factory.newWriter();
    writer.beginArray();
    lenient.toJson(writer, Double.NEGATIVE_INFINITY);
    lenient.toJson(writer, Double.NaN);
    lenient.toJson(writer, Double.POSITIVE_INFINITY);
    writer.endArray();
    assertThat(factory.json()).isEqualTo("[-Infinity,NaN,Infinity]");
  }

  @Test public void nullSafe() throws Exception {
    JsonAdapter<String> toUpperCase = new JsonAdapter<String>() {
      @Override public String fromJson(JsonReader reader) throws IOException {
        return reader.nextString().toUpperCase(Locale.US);
      }

      @Override public void toJson(JsonWriter writer, String value) throws IOException {
        writer.value(value.toUpperCase(Locale.US));
      }
    }.nullSafe();

    JsonReader reader = factory.newReader("[\"a\", null, \"c\"]");
    reader.beginArray();
    assertThat(toUpperCase.fromJson(reader)).isEqualTo("A");
    assertThat(toUpperCase.fromJson(reader)).isNull();
    assertThat(toUpperCase.fromJson(reader)).isEqualTo("C");
    reader.endArray();

    JsonWriter writer = factory.newWriter();
    writer.beginArray();
    toUpperCase.toJson(writer, "a");
    toUpperCase.toJson(writer, null);
    toUpperCase.toJson(writer, "c");
    writer.endArray();
    assertThat(factory.json()).isEqualTo("[\"A\",null,\"C\"]");
  }

  @Test public void failOnUnknown() throws Exception {
    JsonAdapter<String> alwaysSkip = new JsonAdapter<String>() {
      @Override public String fromJson(JsonReader reader) throws IOException {
        reader.skipValue();
        throw new AssertionError();
      }

      @Override public void toJson(JsonWriter writer, String value) throws IOException {
        throw new AssertionError();
      }
    }.failOnUnknown();

    JsonReader reader = factory.newReader("[\"a\"]");
    reader.beginArray();
    try {
      alwaysSkip.fromJson(reader);
      fail();
    } catch (JsonDataException expected) {
      assertThat(expected).hasMessage("Cannot skip unexpected STRING at $[0]");
    }
    assertThat(reader.nextString()).isEqualTo("a");
    reader.endArray();
  }

  @Test public void indent() throws Exception {
    assumeTrue(factory.encodesToBytes());

    JsonAdapter<List<String>> indent = new JsonAdapter<List<String>>() {
      @Override public List<String> fromJson(JsonReader reader) throws IOException {
        throw new AssertionError();
      }

      @Override public void toJson(JsonWriter writer, List<String> value) throws IOException {
        writer.beginArray();
        for (String s : value) {
          writer.value(s);
        }
        writer.endArray();
      }
    }.indent("\t\t\t");

    JsonWriter writer = factory.newWriter();
    indent.toJson(writer, Arrays.asList("a", "b", "c"));
    assertThat(factory.json()).isEqualTo(""
        + "[\n"
        + "\t\t\t\"a\",\n"
        + "\t\t\t\"b\",\n"
        + "\t\t\t\"c\"\n"
        + "]");
  }

  @Test public void serializeNulls() throws Exception {
    JsonAdapter<Map<String, String>> serializeNulls = new JsonAdapter<Map<String, String>>() {
      @Override public Map<String, String> fromJson(JsonReader reader) throws IOException {
        throw new AssertionError();
      }

      @Override public void toJson(JsonWriter writer, Map<String, String> map) throws IOException {
        writer.beginObject();
        for (Map.Entry<String, String> entry : map.entrySet()) {
          writer.name(entry.getKey()).value(entry.getValue());
        }
        writer.endObject();
      }
    }.serializeNulls();

    JsonWriter writer = factory.newWriter();
    serializeNulls.toJson(writer, Collections.<String, String>singletonMap("a", null));
    assertThat(factory.json()).isEqualTo("{\"a\":null}");
  }
}
