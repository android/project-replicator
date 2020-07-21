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
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class DefaultProjectInfo(
    override val gradleVersion: String,
    override val agpVersion: String,
    override val kotlinVersion: String,
    override val rootModule: ModuleInfo,
    override val subModules: List<ModuleInfo>,
    override val gradleProperties: List<String>
) : ProjectInfo

class ProjectAdapter : TypeAdapter<DefaultProjectInfo>() {
    override fun write(output: JsonWriter, value: DefaultProjectInfo) {
        output.beginObject()

        output.name("gradle").value(value.gradleVersion)
        output.name("agp").value(value.agpVersion)
        output.name("kotlin").value(value.kotlinVersion)

        value.gradleProperties.toJsonArray(name = "properties", writer = output) {
            value(it)
        }

        output.name("rootModule")
        ModuleAdapter().write(output, value.rootModule as DefaultModuleInfo)

        value.subModules.toJsonArray(name = "modules", writer = output) {
            ModuleAdapter().write(this, it as DefaultModuleInfo)
        }

        output.endObject()
    }

    override fun read(input: JsonReader): DefaultProjectInfo {
        var gradleVersion: String? = null
        var agpVersion: String? = null
        var kotlinVersion: String? = null
        var rootModule: DefaultModuleInfo? = null
        var subModules: List<DefaultModuleInfo> = listOf()
        var properties: List<String> = listOf()

        val moduleAdapter = ModuleAdapter()

        input.readObjectProperties {
            when (it) {
                "gradle" -> gradleVersion = nextString()
                "agp" -> agpVersion = nextString()
                "kotlin" -> kotlinVersion = nextString()
                "rootModule" -> rootModule = moduleAdapter.read(input)
                "modules" -> {
                    subModules = input.readArrayToList {
                        moduleAdapter.read(this)
                    }
                }
                "properties" -> {
                    properties = input.readArrayToList {
                        nextString()
                    }
                }
                else -> skipValue()
            }
        }

        return DefaultProjectInfo(
            gradleVersion = gradleVersion ?: throw RuntimeException("Missing gradle version"),
            agpVersion = agpVersion ?: throw RuntimeException("Missing agp version"),
            kotlinVersion = kotlinVersion ?: throw RuntimeException("Missing kotlin version"),
            rootModule = rootModule ?: throw RuntimeException("Missing root module info"),
            subModules = subModules,
            gradleProperties = properties
        )
    }
}
