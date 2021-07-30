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
import com.android.gradle.replicator.model.internal.resources.AndroidResourceMap
import com.android.gradle.replicator.model.internal.resources.ResourcePropertyType
import com.android.gradle.replicator.model.internal.resources.AndroidValuesResourceProperties
import com.android.gradle.replicator.model.internal.resources.AndroidSizeMattersResourceProperties
import com.android.gradle.replicator.model.internal.resources.ValuesMap
import com.android.gradle.replicator.model.internal.resources.selectResourceProperties
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
        for (resourceType in value.resourceMap) {
            output.name(resourceType.key).beginArray()
            for (resourceProperties in resourceType.value) {
                output.beginObject()
                output.name("qualifiers").value(resourceProperties.qualifiers)
                output.name("extension").value(resourceProperties.extension)
                output.name("quantity").value(resourceProperties.quantity)
                when (resourceProperties.propertyType) {
                    ResourcePropertyType.VALUES -> {
                        output.name("valuesFileList").beginArray()
                        (resourceProperties as AndroidValuesResourceProperties).valuesMapPerFile.forEach { resourceFile ->
                            output.beginObject()
                            output.name("stringCount").value(resourceFile.stringCount)
                            output.name("intCount").value(resourceFile.intCount)
                            output.name("boolCount").value(resourceFile.boolCount)
                            output.name("colorCount").value(resourceFile.colorCount)
                            output.name("dimenCount").value(resourceFile.dimenCount)
                            output.name("idCount").value(resourceFile.idCount)

                            output.name("integerArrayCount").beginArray()
                            resourceFile.integerArrayCount.forEach {
                                output.value(it)
                            }
                            output.endArray()

                            output.name("arrayCount").beginArray()
                            resourceFile.arrayCount.forEach {
                                output.value(it)
                            }
                            output.endArray()

                            output.name("styleCount").beginArray()
                            resourceFile.styleCount.forEach {
                                output.value(it)
                            }
                            output.endArray()

                            output.endObject()
                        }
                        output.endArray()
                    }
                    ResourcePropertyType.SIZE_MATTERS -> {
                        output.name("fileSizes").beginArray()
                        (resourceProperties as AndroidSizeMattersResourceProperties).fileSizes.forEach { resourceFile ->
                            output.value(resourceFile)
                        }
                        output.endArray()
                    }
                    ResourcePropertyType.DEFAULT -> {} // No additional information
                }
                output.endObject()
            }
            output.endArray()
        }
        output.endObject()
    }

    override fun read(input: JsonReader): AndroidResourcesInfo {
        val fileCount: AndroidResourceMap = mutableMapOf()

        // Read folder properties
        input.readObjectProperties { resourceType ->
            fileCount[resourceType] = mutableListOf()
            // Read resource properties
            this.readArray {
                var qualifiers: String? = null
                var extension: String? = null
                var quantity: Int? = null
                var fileSizes: MutableList<Long>? = null
                var valuesMapPerFile: MutableList<ValuesMap>? = null
                this.readObjectProperties { property ->
                    when (property) {
                        "qualifiers" -> qualifiers = this.nextString()
                        "extension" -> extension = this.nextString()
                        "quantity" -> quantity = this.nextInt()
                        "fileSizes" -> {
                            fileSizes = mutableListOf()
                            this.readArray {
                                fileSizes!!.add(this.nextLong())
                            }
                        }
                        "valuesFileList" -> {
                            valuesMapPerFile = mutableListOf()
                            this.readArray {
                                val valuesMap = ValuesMap()
                                this.readObjectProperties { valueProperty ->
                                    when (valueProperty) {
                                        "stringCount" -> { valuesMap.stringCount = this.nextInt() }
                                        "intCount" -> { valuesMap.intCount = this.nextInt() }
                                        "boolCount" -> { valuesMap.boolCount = this.nextInt()  }
                                        "colorCount" -> { valuesMap.colorCount = this.nextInt()  }
                                        "dimenCount" -> { valuesMap.dimenCount = this.nextInt()  }
                                        "idCount" -> { valuesMap.idCount = this.nextInt() }
                                        "integerArrayCount" -> {
                                            this.readArray {
                                                valuesMap.integerArrayCount.add(this.nextInt())
                                            }
                                        }
                                        "arrayCount" -> {
                                            this.readArray {
                                                valuesMap.arrayCount.add(this.nextInt())
                                            }
                                        }
                                        "styleCount" -> {
                                            this.readArray {
                                                valuesMap.styleCount.add(this.nextInt())
                                            }
                                        }
                                    }
                                }
                                valuesMapPerFile!!.add(valuesMap)
                            }
                        }
                    }
                }
                fileCount[resourceType]!!.add(selectResourceProperties(
                    resourceType = resourceType,
                    qualifiers = qualifiers!!,
                    extension = extension!!,
                    quantity = quantity!!,
                    fileSizes = fileSizes,
                    valuesMapPerFile = valuesMapPerFile))
            }
        }

        return DefaultAndroidResourcesInfo(fileCount)
    }
}