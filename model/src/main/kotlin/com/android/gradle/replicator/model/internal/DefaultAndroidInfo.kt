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

import com.android.gradle.replicator.model.AndroidInfo
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class DefaultAndroidInfo(
    override val compileSdkVersion: String,
    override val minSdkVersion: Int,
    override val targetSdkVersion: Int
) : AndroidInfo

class AndroidAdapter: TypeAdapter<AndroidInfo>() {
    override fun write(output: JsonWriter, value: AndroidInfo) {
        output
            .beginObject()
            .name("compileSdkVersion").value(value.compileSdkVersion)
            .name("minSdkVersion").value(value.minSdkVersion)
            .name("targetSdkVersion").value(value.targetSdkVersion)
            .endObject()
    }

    override fun read(input: JsonReader): AndroidInfo {
        var compileSdkVersion: String? = null
        var minSdkVersion: Int? = null
        var targetSdkVersion: Int? = null

        input.readObjectProperties {
            when (it) {
                "compileSdkVersion" -> compileSdkVersion = nextString()
                "minSdkVersion" -> minSdkVersion = nextInt()
                "targetSdkVersion" -> targetSdkVersion = nextInt()
                else -> skipValue()
            }
        }

        return DefaultAndroidInfo(
            compileSdkVersion ?: "",
            minSdkVersion ?: 0,
            targetSdkVersion ?: 0)
    }
}