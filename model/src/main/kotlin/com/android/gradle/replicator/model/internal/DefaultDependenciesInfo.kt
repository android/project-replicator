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

import com.android.gradle.replicator.model.DependenciesInfo
import com.android.gradle.replicator.model.DependencyType
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class DefaultDependenciesInfo(
    override val type: DependencyType,
    override val dependency: String,
    override val scope: String
) : DependenciesInfo

class DependenciesAdapter: TypeAdapter<DependenciesInfo>() {
    override fun write(output: JsonWriter, value: DependenciesInfo) {
        output
            .beginObject()
            .name(value.type.jsonValue).value(value.dependency)
            .name("method").value(value.scope)
            .endObject()
    }

    override fun read(input: JsonReader): DependenciesInfo {
        var type: DependencyType? = null
        var scope: String? = null
        var dependency: String? = null

        input.readObjectProperties {
            when (it) {
                DependencyType.MODULE.jsonValue -> {
                    type = DependencyType.MODULE
                    dependency = nextString()
                }
                DependencyType.EXTERNAL_LIBRARY.jsonValue -> {
                    type = DependencyType.EXTERNAL_LIBRARY
                    dependency = nextString()
                }
                "method" -> {
                    scope = nextString()
                }
                else -> skipValue()
            }
        }

        return DefaultDependenciesInfo(
            type = type ?: throw RuntimeException("failed to find type info for dependency '$scope"),
            dependency = dependency ?: throw RuntimeException("failed to find dependency name"),
            scope = scope ?: throw RuntimeException("failed to find scope for dependency '$dependency'")
        )
    }
}