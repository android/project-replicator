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

import com.android.gradle.replicator.model.AndroidResourceExtensions
import com.android.gradle.replicator.model.AndroidResourceFolders
import com.android.gradle.replicator.model.AndroidResourceQualifiers
import com.android.gradle.replicator.model.AndroidResourcesInfo
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

val ANDROID_RESOURCE_FOLDERS = mapOf(
        "animator" to listOf(".xml"),
        "anim" to listOf(".xml"),
        "color" to listOf(".xml"),
        "drawable" to listOf(".xml", ".png", ".9.png", ".jpg", ".gif", ".webp"),
        "font" to listOf(".ttf", ".otf", ".ttc", ".xml"),
        "layout" to listOf(".xml"),
        "menu" to listOf(".xml"),
        "mipmap" to listOf(".xml", ".png", ".9.png", ".jpg", ".gif", ".webp"),
        "raw" to listOf("*"),
        "transition" to listOf(".xml"),
        "values" to listOf(".xml"),
        "xml" to listOf(".xml")
)

data class DefaultAndroidResourcesInfo(
        override val fileCount: AndroidResourceFolders
) : AndroidResourcesInfo {
    override val asMap: MutableMap<String, MutableMap<String, MutableMap<String, Int>>> =
        fileCount.folders.mapValues { folder ->
                folder.value.qualifiers.mapValues { qualifier ->
                            qualifier.value.extensions
                } as MutableMap
        } as MutableMap
}

/* Resource folders fname-mod1, fname, fname-mod2 becomes:
 * "resources": {
 *     "fname" : {
 *         "":  {
 *             ".xml": 2
 *         },
 *         "mod1": {
 *             ".xml": 3
 *         },
 *         "mod2": {
 *             ".xml": 2,
 *             ".png": 1
 *         }
 *     },
 * ...
 * }
 */
class AndroidResourcesAdapter: TypeAdapter<AndroidResourcesInfo>() {
    override fun write(output: JsonWriter, value: AndroidResourcesInfo) {
        output.beginObject()
        for (folder in value.fileCount.folders) {
            val folderObject = output.name(folder.key).beginObject()
            for (qualifier in folder.value.qualifiers) {
                val qualifierObject = folderObject.name(qualifier.key).beginObject()
                for (extension in qualifier.value.extensions) {
                    qualifierObject.name(extension.key).value(extension.value)
                }
                qualifierObject.endObject()
            }
            folderObject.endObject()
        }
        output.endObject()
    }

    override fun read(input: JsonReader): AndroidResourcesInfo {
        val fileCount = AndroidResourceFolders(mutableMapOf())

        // Read folder properties
        input.readObjectProperties { folderName ->
            fileCount.folders[folderName] = AndroidResourceQualifiers(mutableMapOf())
            // Read modifier properties
            this.readObjectProperties { qualifierName ->
                fileCount.folders[folderName]!!.qualifiers[qualifierName] = AndroidResourceExtensions(mutableMapOf())
                this.readObjectProperties { extensionName ->
                    fileCount.folders[folderName]!!.qualifiers[qualifierName]!!.extensions[extensionName] = nextInt()
                }
            }
        }

        return DefaultAndroidResourcesInfo(fileCount)
    }
}