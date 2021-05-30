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

import com.android.gradle.replicator.model.AndroidResourceProperties
import com.android.gradle.replicator.model.AndroidResourceMap
import com.android.gradle.replicator.model.AndroidResourcesInfo
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

val ANDROID_RESOURCE_FOLDERS = mapOf(
        "animator" to listOf(".xml"),
        "anim" to listOf(".xml"),
        "color" to listOf(".xml"),
        "drawable" to listOf(".xml", ".png", ".9.png", ".jpg", ".gif"),
        "font" to listOf(".ttf", ".otf", ".ttc"),
        "layout" to listOf(".xml"),
        "menu" to listOf(".xml"),
        "mipmap" to listOf(".xml", ".png", ".9.png", ".jpg", ".gif", ".webp"),
        "raw" to listOf("*"),
        "transition" to listOf(".xml"),
        "values" to listOf(".xml"),
        "xml" to listOf(".xml")
)

data class DefaultAndroidResourcesInfo(
        override val resourceMap: AndroidResourceMap
) : AndroidResourcesInfo

/* Resource folders fname-mod1, fname, fname-mod2 becomes:
 * "resources": {
 *     "fname" : [
 *         {
 *             "qualifiers": "",
 *             "extension": ".xml",
 *             "quantity": 2
 *         },
 *         {
 *             "qualifiers": "mod1",
 *             "extension": ".xml",
 *             "quantity": 3
 *         },
 *         {
 *             "qualifiers": "mod2",
 *             "extension": ".xml",
 *             "quantity": 2
 *         },
 *         {
 *             "qualifiers": "mod2",
 *             "extension": ".png",
 *             "quantity": 1
 *         }
 *     ],
 * ...
 * }
 */
class AndroidResourcesAdapter: TypeAdapter<AndroidResourcesInfo>() {
    override fun write(output: JsonWriter, value: AndroidResourcesInfo) {
        output.beginObject()
        for (resourceType in value.resourceMap) {
            val folderObject = output.name(resourceType.key).beginArray()
            for (resourceProperties in resourceType.value) {
                val resourceObject = folderObject.beginObject()
                resourceObject.name("qualifiers").value(resourceProperties.qualifiers)
                resourceObject.name("extension").value(resourceProperties.extension)
                resourceObject.name("quantity").value(resourceProperties.quantity)
                resourceObject.endObject()
            }
            folderObject.endArray()
        }
        output.endObject()
    }

    override fun read(input: JsonReader): AndroidResourcesInfo {
        val fileCount: AndroidResourceMap = mutableMapOf()

        // Read folder properties
        input.readObjectProperties { folderName ->
            fileCount[folderName] = mutableListOf()
            // Read resource properties
            this.readArray {
                var qualifiers: String? = null
                var extension: String? = null
                var quantity: Int? = null
                this.readObjectProperties { property ->
                    when (property) {
                        "qualifiers" -> qualifiers = this.nextString()
                        "extension" -> extension = this.nextString()
                        "quantity" -> quantity = this.nextInt()
                    }
                }
                fileCount[folderName]!!.add(AndroidResourceProperties(qualifiers!!, extension!!, quantity!!))
            }
        }

        return DefaultAndroidResourcesInfo(fileCount)
    }
}