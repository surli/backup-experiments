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

import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.AbstractMap.SimpleEntry
import kotlin.collections.Map.Entry
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

/**
 * This class encodes Kotlin classes using their properties. It decodes them by first invoking the
 * constructor, and then by setting any additional properties that exist, if any.
 */
class KotlinJsonAdapter<T> private constructor(
    val constructor: KFunction<T>,
    val bindings: List<Binding<T, Any?>?>,
    val options : JsonReader.Options) : JsonAdapter<T>() {

  override fun fromJson(reader: JsonReader): T {
    val constructorSize = constructor.parameters.size

    // Read each value into its slot in the array.
    val values = Array<Any?>(bindings.size) { ABSENT_VALUE }
    reader.beginObject()
    while (reader.hasNext()) {
      val index = reader.selectName(options)
      val binding = if (index != -1) bindings[index] else null
      if (binding == null) {
        reader.nextName()
        reader.skipValue()
        continue
      }
      if (values[index] != ABSENT_VALUE) {
        throw JsonDataException(
            "Multiple values for ${constructor.parameters[index].name} at ${reader.path}")
      }
      val value = binding.adapter.fromJson(reader)
      values[index] = value
    }
    reader.endObject()

    // Call the constructor using a Map so that absent optionals get defaults.
    for (i in 0 until constructorSize) {
      if (!constructor.parameters[i].isOptional && values[i] == ABSENT_VALUE) {
        throw JsonDataException(
            "Required value ${constructor.parameters[i].name} missing at ${reader.path}")
      }
    }
    val result = constructor.callBy(IndexedParameterMap(constructor.parameters, values))

    // Set remaining properties.
    for (i in constructorSize until bindings.size) {
      bindings[i]?.set(result, values[i])
    }

    return result
  }

  override fun toJson(writer: JsonWriter, value: T) {
    writer.beginObject()
    for (binding in bindings) {
      if (binding == null) continue

      writer.name(binding.name)
      val propertyValue = binding.get(value)
      binding.adapter.toJson(writer, propertyValue)
    }
    writer.endObject()
  }

  override fun toString(): String {
    return "KotlinJsonAdapter(${constructor.returnType})"
  }

  data class Binding<K, P>(
      val name : String,
      val property : KMutableProperty1<K, P>,
      val adapter : JsonAdapter<P>,
      val constructorParameter : KParameter?) {

    fun get(value: K): P {
      return property.get(value)
    }

    fun set(result: K, value: P) {
      if (value != ABSENT_VALUE) {
        property.set(result, value)
      }
    }
  }

  /** A simple [Map] that uses parameter indexes instead of sorting or hashing. */
  class IndexedParameterMap(val parameterKeys: List<KParameter>, val parameterValues: Array<Any?>)
    : AbstractMap<KParameter, Any?>() {

    override val entries: Set<Entry<KParameter, Any?>>
      get() {
        val allPossibleEntries = parameterKeys.mapIndexed { index, value ->
          SimpleEntry<KParameter, Any?>(value, parameterValues[index])
        }
        return allPossibleEntries.filterTo(LinkedHashSet<Entry<KParameter, Any?>>()) {
          it.value != ABSENT_VALUE
        }
      }

    override fun containsKey(key: KParameter): Boolean {
      return parameterValues[key.index] != ABSENT_VALUE
    }

    override fun get(key: KParameter): Any? {
      val value = parameterValues[key.index]
      return if (value != ABSENT_VALUE) value else null
    }
  }

  companion object {
    /** Classes annotated with this are eligible for this adapter. */
    private val KOTLIN_METADATA = Class.forName("kotlin.Metadata") as Class<out Annotation>

    /**
     * Placeholder value used when a field is absent from the JSON. Note that this code
     * distinguishes between absent values and present-but-null values.
     */
    private val ABSENT_VALUE = object : Any() {}

    val FACTORY: JsonAdapter.Factory = object : JsonAdapter.Factory {
      override fun create(
          type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (!annotations.isEmpty()) return null

        val rawType = Types.getRawType(type)
        val platformType = ClassJsonAdapter.isPlatformType(rawType)
        if (platformType) return null

        if (!rawType.isAnnotationPresent(KOTLIN_METADATA)) return null

        val kotlinRawType = rawType.kotlin

        val constructor = kotlinRawType.primaryConstructor ?: return null
        val parametersByName = constructor.parameters.associateBy { parameter -> parameter.name }

        val bindingsByName = LinkedHashMap<String, Binding<Any, Any?>>()


        val properties = kotlinRawType.memberProperties
        for (property in properties) {
          if (Modifier.isTransient(property.javaField?.modifiers ?: 0)) continue

          val parameter = parametersByName[property.name]

          var allAnnotations = property.annotations
          var jsonAnnotation = property.findAnnotation<Json>()

          if (parameter != null) {
            allAnnotations += parameter.annotations
            if (jsonAnnotation == null) {
              jsonAnnotation = parameter.findAnnotation<Json>()
            }
          }

          val name = jsonAnnotation?.name ?: property.name

          val adapter = moshi.adapter<Any>(
              property.returnType.javaType, Util.jsonAnnotations(allAnnotations.toTypedArray()))

          if (property is KMutableProperty1) {
            property.isAccessible = true
            bindingsByName.put(property.name, Binding(
                name, property as KMutableProperty1<Any, Any?>, adapter, parameter))
          }
        }

        val bindings = ArrayList<Binding<Any, Any?>?>()

        for (parameter in constructor.parameters) {
          val binding = bindingsByName.remove(parameter.name)
          if (binding == null && !parameter.isOptional) {
            throw IllegalArgumentException(
                "No property for required constructor parameter ${parameter.name}")
          }
          bindings.add(binding)
        }

        bindings.addAll(bindingsByName.values)

        val options = JsonReader.Options.of(*bindings.map { it?.name ?: "\u0000" }.toTypedArray())
        return KotlinJsonAdapter(constructor, bindings, options)
      }
    }
  }
}
