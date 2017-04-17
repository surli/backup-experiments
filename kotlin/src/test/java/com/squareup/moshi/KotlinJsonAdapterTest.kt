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
package com.squareup.moshi

import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.fail
import org.junit.Test
import java.util.Locale
import kotlin.annotation.AnnotationRetention.RUNTIME

class KotlinJsonAdapterTest {
  @Test fun allConstructorParameters() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapter.FACTORY).build()
    val jsonAdapter = moshi.adapter(AllConstructorParameters::class.java)

    val encoded = AllConstructorParameters(3, 5)
    assertThat(jsonAdapter.toJson(encoded)).isEqualTo("{\"a\":3,\"b\":5}")

    val decoded = jsonAdapter.fromJson("{\"a\":4,\"b\":6}")
    assertThat(decoded.a).isEqualTo(4)
    assertThat(decoded.b).isEqualTo(6)
  }

  class AllConstructorParameters(var a: Int, var b: Int)

  @Test fun allProperties() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapter.FACTORY).build()
    val jsonAdapter = moshi.adapter(AllProperties::class.java)

    val encoded = AllProperties()
    encoded.a = 3
    encoded.b = 5
    assertThat(jsonAdapter.toJson(encoded)).isEqualTo("{\"a\":3,\"b\":5}")

    val decoded = jsonAdapter.fromJson("{\"a\":3,\"b\":5}")
    assertThat(decoded.a).isEqualTo(3)
    assertThat(decoded.b).isEqualTo(5)
  }

  class AllProperties {
    var a: Int = -1
    var b: Int = -1
  }

  @Test fun constructorParametersAndProperties() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapter.FACTORY).build()
    val jsonAdapter = moshi.adapter(ConstructorParametersAndProperties::class.java)

    val encoded = ConstructorParametersAndProperties(3)
    encoded.b = 5
    assertThat(jsonAdapter.toJson(encoded)).isEqualTo("{\"a\":3,\"b\":5}")

    val decoded = jsonAdapter.fromJson("{\"a\":4,\"b\":6}")
    assertThat(decoded.a).isEqualTo(4)
    assertThat(decoded.b).isEqualTo(6)
  }

  class ConstructorParametersAndProperties(var a: Int) {
    var b: Int = -1
  }

  @Test fun constructorDefaults() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapter.FACTORY).build()
    val jsonAdapter = moshi.adapter(ConstructorDefaultValues::class.java)

    val encoded = ConstructorDefaultValues(3, 5)
    assertThat(jsonAdapter.toJson(encoded)).isEqualTo("{\"a\":3,\"b\":5}")

    val decoded = jsonAdapter.fromJson("{\"b\":6}")
    assertThat(decoded.a).isEqualTo(-1)
    assertThat(decoded.b).isEqualTo(6)
  }

  class ConstructorDefaultValues(var a: Int = -1, var b: Int = -2)

  @Test fun requiredValueAbsent() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapter.FACTORY).build()
    val jsonAdapter = moshi.adapter(RequiredValueAbsent::class.java)

    try {
      jsonAdapter.fromJson("{\"a\":4}")
      fail()
    } catch(expected: JsonDataException) {
      assertThat(expected).hasMessage("Required value b missing at $")
    }
  }

  class RequiredValueAbsent(var a: Int = 3, var b: Int)

  @Test fun duplicatedValue() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapter.FACTORY).build()
    val jsonAdapter = moshi.adapter(DuplicateValue::class.java)

    try {
      jsonAdapter.fromJson("{\"a\":4,\"a\":4}")
      fail()
    } catch(expected: JsonDataException) {
      assertThat(expected).hasMessage("Multiple values for a at $.a")
    }
  }

  class DuplicateValue(var a: Int = -1, var b: Int = -2)

  @Test fun explicitNull() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapter.FACTORY).build()
    val jsonAdapter = moshi.adapter(ExplicitNull::class.java)

    val encoded = ExplicitNull(null, 5)
    assertThat(jsonAdapter.toJson(encoded)).isEqualTo("{\"b\":5}")
    assertThat(jsonAdapter.serializeNulls().toJson(encoded)).isEqualTo("{\"a\":null,\"b\":5}")

    val decoded = jsonAdapter.fromJson("{\"a\":null,\"b\":6}")
    assertThat(decoded.a).isEqualTo(null)
    assertThat(decoded.b).isEqualTo(6)
  }

  class ExplicitNull(var a: Int?, var b: Int?)

  // TODO(jwilson): if a nullable field is absent, just do the obvious thing instead of crashing?
  @Test fun absentNull() {
    val moshi = Moshi.Builder().add(KotlinJsonAdapter.FACTORY).build()
    val jsonAdapter = moshi.adapter(AbsentNull::class.java)

    val encoded = AbsentNull(null, 5)
    assertThat(jsonAdapter.toJson(encoded)).isEqualTo("{\"b\":5}")
    assertThat(jsonAdapter.serializeNulls().toJson(encoded)).isEqualTo("{\"a\":null,\"b\":5}")

    try {
      jsonAdapter.fromJson("{\"b\":6}")
      fail()
    } catch(expected: JsonDataException) {
      assertThat(expected).hasMessage("Required value a missing at $")
    }
  }

  class AbsentNull(var a: Int?, var b: Int?)

  @Test fun constructorParameterWithQualifier() {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapter.FACTORY)
        .add(UppercaseJsonAdapter())
        .build()
    val jsonAdapter = moshi.adapter(ConstructorParameterWithQualifier::class.java)

    val encoded = ConstructorParameterWithQualifier("Android", "Banana")
    assertThat(jsonAdapter.toJson(encoded)).isEqualTo("{\"a\":\"ANDROID\",\"b\":\"Banana\"}")

    val decoded = jsonAdapter.fromJson("{\"a\":\"Android\",\"b\":\"Banana\"}")
    assertThat(decoded.a).isEqualTo("android")
    assertThat(decoded.b).isEqualTo("Banana")
  }

  class ConstructorParameterWithQualifier(@Uppercase var a: String, var b: String)

  @Test fun propertyWithQualifier() {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapter.FACTORY)
        .add(UppercaseJsonAdapter())
        .build()
    val jsonAdapter = moshi.adapter(PropertyWithQualifier::class.java)

    val encoded = PropertyWithQualifier()
    encoded.a = "Android"
    encoded.b = "Banana"
    assertThat(jsonAdapter.toJson(encoded)).isEqualTo("{\"a\":\"ANDROID\",\"b\":\"Banana\"}")

    val decoded = jsonAdapter.fromJson("{\"a\":\"Android\",\"b\":\"Banana\"}")
    assertThat(decoded.a).isEqualTo("android")
    assertThat(decoded.b).isEqualTo("Banana")
  }

  class PropertyWithQualifier {
    @Uppercase var a: String = ""
    var b: String = ""
  }

  // TODO(jwilson): honor @Json(name)
  // TODO(jwilson): platform types?
  // TODO(jwilson): supertypes?
  // TODO(jwilson): transient fields?
  // TODO(jwilson): resolve generic types?
  // TODO(jwilson): inaccessible constructors?

  @Retention(RUNTIME)
  @JsonQualifier
  annotation class Uppercase

  class UppercaseJsonAdapter {
    @ToJson fun toJson(@Uppercase s: String) : String {
      return s.toUpperCase(Locale.US)
    }
    @FromJson @Uppercase fun fromJson(s: String) : String {
      return s.toLowerCase(Locale.US)
    }
  }
}
