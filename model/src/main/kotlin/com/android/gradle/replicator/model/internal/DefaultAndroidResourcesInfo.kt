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

import com.android.gradle.replicator.model.AndroidResourcesInfo
import com.android.gradle.replicator.model.internal.filedata.AndroidResourceMap
import com.android.gradle.replicator.model.internal.filedata.AndroidResourcePropertiesAdapter
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class DefaultAndroidResourcesInfo(
        override val resourceMap: AndroidResourceMap
) : AndroidResourcesInfo

/* Resource folders fname-mod1, fname, fname-mod2 becomes:
 * "resources": {
 *     "fname" : [
 *         {
 *             "qualifiers": "",
 *             "extension": ".xml",
 *             "quantity": 2,
 *             "fileSizes": [
 *               5606,
 *               1702
 *             ]
 *         },
 *         {
 *             "qualifiers": "mod1",
 *             "extension": ".xml",
 *             "quantity": 3,
 *             "fileSizes": [
 *               5606,
 *               2293,
 *               1702
 *             ]
 *         },
 *         {
 *             "qualifiers": "mod2",
 *             "extension": ".xml",
 *             "quantity": 2,
 *             "fileSizes": [
 *               5606,
 *               1702
 *             ]
 *         },
 *         {
 *             "qualifiers": "mod2",
 *             "extension": ".png",
 *             "quantity": 1,
 *             "fileSizes": [
 *               5606
 *             ]
 *         }
 *     ],
 * ...
 * }
 */
class AndroidResourcesAdapter: TypeAdapter<AndroidResourcesInfo>() {
    override fun write(output: JsonWriter, value: AndroidResourcesInfo) {
        output.beginObject()
        val resourcePropertiesWriter = AndroidResourcePropertiesAdapter()
        for (resourceType in value.resourceMap) {
            output.name(resourceType.key).beginArray()
            for (resourceProperties in resourceType.value) {
                resourcePropertiesWriter.write(output, resourceProperties)
            }
            output.endArray()
        }
        output.endObject()
    }

    override fun read(input: JsonReader): AndroidResourcesInfo {
        val fileCount: AndroidResourceMap = mutableMapOf()
        // Read folder properties
        input.readObjectProperties { resourceType ->
            val resourcePropertiesReader = AndroidResourcePropertiesAdapter(resourceType)
            fileCount[resourceType] = mutableListOf()
            // Read resource properties
            this.readArray {
                fileCount[resourceType]!!.add(resourcePropertiesReader.read(this))
            }
        }

        return DefaultAndroidResourcesInfo(fileCount)
    }
}