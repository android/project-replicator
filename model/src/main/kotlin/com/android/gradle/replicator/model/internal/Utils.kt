/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.gradle.replicator.model.internal

import com.android.gradle.replicator.model.ModuleInfo
import com.android.gradle.replicator.model.ProjectInfo
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

fun ModuleInfo.toJson(): String {
    val gson = GsonBuilder()
        // need to use the actual type, not an interface which is totally lame
        .registerTypeAdapter(DefaultModuleInfo::class.java, ModuleAdapter())
        .setPrettyPrinting()
        .create()
    return gson.toJson(this)
}

fun ProjectInfo.toJson(): String {
    val gson = GsonBuilder()
        // need to use the actual type, not an interface which is totally lame
        .registerTypeAdapter(DefaultProjectInfo::class.java, ProjectAdapter())
        .setPrettyPrinting()
        .create()
    return gson.toJson(this)
}

private inline fun <reified T> T.toJson(adapter: TypeAdapter<T>) : String {
    val gson = GsonBuilder()
        .registerTypeAdapter(T::class.java, adapter)
        .setPrettyPrinting()
        .create()
    return gson.toJson(this)
}

inline fun <reified T> String.fromJson(adapter: TypeAdapter<T>): T {
    val gson = GsonBuilder()
        .registerTypeAdapter(T::class.java, adapter)
        .create()

    val recordType = object : TypeToken<T>() {}.type
    return gson.fromJson(this, recordType)
}

/**
 * Writes a json array
 *
 * @param name the name of the array
 * @param writer the [JsonWriter]
 * @param action the action to write each items into the writer
 */
internal inline fun <T> Iterable<T>.toJsonArray(
    name: String,
    writer: JsonWriter,
    action: JsonWriter.(T) -> Unit
) {
    writer.name(name).beginArray()
    forEach {
        action(writer, it)
    }
    writer.endArray()
}


internal inline fun JsonReader.readObjectProperties(consumer: JsonReader.(String) -> Unit) {
    beginObject()
    while (hasNext()) {
        consumer(this, nextName())
    }
    endObject()
}

internal inline fun JsonReader.readArray(objectReader: JsonReader.() -> Unit) {
    beginArray()
    while (hasNext()) {
        objectReader()
    }
    endArray()
}

internal inline fun <T> JsonReader.readArrayToList(objectReader: JsonReader.() -> T): List<T> {
    val list = mutableListOf<T>()
    beginArray()
    while (hasNext()) {
        list.add(objectReader())
    }
    endArray()

    return list
}

internal inline fun <T> JsonReader.readArrayToSet(objectReader: JsonReader.() -> T): Set<T> {
    val set = mutableSetOf<T>()
    beginArray()
    while (hasNext()) {
        set.add(objectReader())
    }
    endArray()

    return set
}
