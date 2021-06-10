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

import com.android.gradle.replicator.model.*
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class DefaultModuleInfo(
        override val path: String,
        override val plugins: List<PluginType>,
        override val javaSources: SourceFilesInfo?,
        override val kotlinSources: SourceFilesInfo?,
        override val androidResources: AndroidResourcesInfo?,
        override val javaResources: SourceFilesInfo?,
        override val dependencies: List<DependenciesInfo>,
        override val android: AndroidInfo?
) : ModuleInfo

class ModuleAdapter: TypeAdapter<DefaultModuleInfo>() {
    override fun write(output: JsonWriter, value: DefaultModuleInfo) {
        output.beginObject()

        output.name("path").value(value.path)

        value.plugins.toJsonArray("plugins", output) {
            value(it.id)
        }

        value.javaSources?.let {
            output.name("javaSources")
            SourceFilesAdapter().write(output, it)
        }

        value.kotlinSources?.let {
            output.name("kotlinSources")
            SourceFilesAdapter().write(output, it)
        }

        value.androidResources?.let {
            output.name("androidResources")
            AndroidResourcesAdapter().write(output, it)
        }

        value.javaResources?.let {
            output.name("javaResources")
            SourceFilesAdapter().write(output, it)
        }

        val adapter = DependenciesAdapter()
        value.dependencies.toJsonArray("dependencies", output) {
            adapter.write(this, it)
        }

        value.android?.let {
            output.name("android")
            AndroidAdapter().write(output, it)
        }

        output.endObject()
    }

    override fun read(input: JsonReader): DefaultModuleInfo {
        var path: String? = null
        var plugins: List<PluginType> = listOf()
        var javaSources: SourceFilesInfo? = null
        var kotlinSources: SourceFilesInfo? = null
        var androidResources: AndroidResourcesInfo? = null
        var javaResources: SourceFilesInfo? = null
        var dependencies: List<DependenciesInfo> = listOf()
        var androidInfo: AndroidInfo? = null

        val sourceFilesAdapter = SourceFilesAdapter()
        val resourcesAdapter = AndroidResourcesAdapter()

        input.readObjectProperties {
            when (it) {
                "path" -> path = nextString()
                "plugins" -> plugins = input.readArrayToList {
                    val id = nextString()
                    PluginType.values().firstOrNull { it.id == id || it.oldId == id }
                            ?: throw RuntimeException("Unable to find PluginType for value '$id'")
                }

                "javaSources" -> javaSources = sourceFilesAdapter.read(input)
                "kotlinSources" -> kotlinSources = sourceFilesAdapter.read(input)
                "androidResources" -> androidResources = resourcesAdapter.read(input)
                "javaResources" -> javaResources = sourceFilesAdapter.read(input)
                "dependencies" -> {
                    val dependenciesAdapter = DependenciesAdapter()
                    dependencies = input.readArrayToList {
                        dependenciesAdapter.read(this)
                    }
                }
                "android" -> androidInfo = AndroidAdapter().read(input)
                else -> skipValue()
            }
        }

        return DefaultModuleInfo(
            path = path ?: throw RuntimeException("Missing module path"),
            plugins = plugins,
            javaSources = javaSources,
            kotlinSources = kotlinSources,
            androidResources = androidResources,
            javaResources = javaResources,
            dependencies = dependencies,
            android = androidInfo
        )
    }
}