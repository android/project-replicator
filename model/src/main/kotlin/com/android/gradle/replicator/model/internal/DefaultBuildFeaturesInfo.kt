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

import com.android.gradle.replicator.model.BuildFeaturesInfo
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class DefaultBuildFeaturesInfo(
    override val aidl: Boolean? = null,
    override val androidResources: Boolean? = null,
    override val buildConfig: Boolean? = null,
    override val compose: Boolean? = null,
    override val dataBinding: Boolean? = null,
    override val mlModelBinding: Boolean? = null,
    override val prefab: Boolean? = null,
    override val prefabPublishing: Boolean? = null,
    override val renderScript: Boolean? = null,
    override val resValues: Boolean? = null,
    override val shaders: Boolean? = null,
    override val viewBinding: Boolean? = null
): BuildFeaturesInfo

class BuildFeaturesInfoAdapter: TypeAdapter<BuildFeaturesInfo>() {
    override fun write(output: JsonWriter, value: BuildFeaturesInfo) {

        value.toJsonObject(output) {
            writeJsonBoolean(it.aidl, "aidl")
            writeJsonBoolean(it.androidResources, "androidResources")
            writeJsonBoolean(it.buildConfig, "buildConfig")
            writeJsonBoolean(it.compose, "compose")
            writeJsonBoolean(it.dataBinding, "dataBinding")
            writeJsonBoolean(it.mlModelBinding, "mlModelBinding")
            writeJsonBoolean(it.prefab, "prefab")
            writeJsonBoolean(it.prefabPublishing, "prefabPublishing")
            writeJsonBoolean(it.renderScript, "renderScript")
            writeJsonBoolean(it.resValues, "resValues")
            writeJsonBoolean(it.shaders, "shaders")
            writeJsonBoolean(it.viewBinding, "viewBinding")
        }
    }

    override fun read(input: JsonReader): BuildFeaturesInfo {
        var aidl: Boolean? = null
        var androidResources: Boolean? = null
        var buildConfig: Boolean? = null
        var compose: Boolean? = null
        var dataBinding: Boolean? = null
        var mlModelBinding: Boolean? = null
        var prefab: Boolean? = null
        var prefabPublishing: Boolean? = null
        var renderScript: Boolean? = null
        var resValues: Boolean? = null
        var shaders: Boolean? = null
        var viewBinding: Boolean? = null

        input.readObjectProperties {
            when (it) {
                "aidl" -> aidl = nextBoolean()
                "androidResources" -> androidResources = nextBoolean()
                "buildConfig" -> buildConfig = nextBoolean()
                "compose" -> compose = nextBoolean()
                "dataBinding" -> dataBinding = nextBoolean()
                "mlModelBinding" -> mlModelBinding = nextBoolean()
                "prefab" -> prefab = nextBoolean()
                "prefabPublishing" -> prefabPublishing = nextBoolean()
                "renderScript" -> renderScript = nextBoolean()
                "resValues" -> resValues = nextBoolean()
                "shaders" -> shaders = nextBoolean()
                "viewBinding" -> viewBinding = nextBoolean()
                else -> skipValue()
            }
        }

        return DefaultBuildFeaturesInfo(
            aidl,
            androidResources,
            buildConfig,
            compose,
            dataBinding,
            mlModelBinding,
            prefab,
            prefabPublishing,
            renderScript,
            resValues,
            shaders,
            viewBinding
        )
    }
}

