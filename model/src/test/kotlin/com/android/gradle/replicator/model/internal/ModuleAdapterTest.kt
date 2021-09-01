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
import com.android.gradle.replicator.model.internal.fixtures.module
import com.android.gradle.replicator.model.internal.filedata.AbstractAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.SizeMattersAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.ValuesAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.ResourcePropertyType
import com.android.gradle.replicator.model.internal.filedata.ValuesMap
import com.google.common.truth.Truth
import org.junit.Test

internal class ModuleAdapterTest {

    @Test
    fun `test write empty module`() {
        Truth.assertThat(emptyModuleObject().toJson()).isEqualTo(EMPTY_MODULE)
    }

    @Test
    fun `test load empty module`() {
        val module = emptyModuleObject()
        val loadedModule = EMPTY_MODULE.fromJson(ModuleAdapter())

        Truth.assertThat(loadedModule).isEqualTo(module)
    }

    @Test
    fun `test load + write empty module`() {
        val loadedModule = EMPTY_MODULE.fromJson(ModuleAdapter())

        Truth.assertThat(loadedModule.toJson()).isEqualTo(EMPTY_MODULE)
    }

    @Test
    fun `test write full module`() {
        Truth.assertThat(fullModuleObject().toJson()).isEqualTo(FULL_MODULE)
    }

    @Test
    fun `test load full module`() {
        val module = fullModuleObject()
        val loadedModule = FULL_MODULE.fromJson(ModuleAdapter())

        assertModuleInfoEquals(loadedModule, module)
    }

    @Test
    fun `test load + write full module`() {
        val loadedModule = FULL_MODULE.fromJson(ModuleAdapter())

        Truth.assertThat(loadedModule.toJson()).isEqualTo(FULL_MODULE)
    }

    // --------------------------

    /**
     * this should match [EMPTY_MODULE]
     */
    private fun emptyModuleObject() = module {
        path = ":foo"
    }

    /**
     * this should match [FULL_MODULE]
     */
    private fun fullModuleObject() = module {
        path = ":foo"
        plugins = listOf(PluginType.ANDROID_APP, PluginType.KOTLIN_ANDROID)
        javaSources {
            fileCount = 1
        }
        kotlinSources {
            fileCount = 2
        }
        androidResources {
            resourceMap = mutableMapOf(
                    "mipmap" to mutableListOf<AbstractAndroidResourceProperties>(
                            SizeMattersAndroidResourceProperties(
                                qualifiers = "",
                                extension = ".xml",
                                quantity = 3,
                                fileSizes = listOf(64, 128, 256)
                            ),
                            SizeMattersAndroidResourceProperties(
                                qualifiers = "",
                                extension = ".png",
                                quantity = 2,
                                fileSizes = listOf(512, 1024)
                            ),
                            SizeMattersAndroidResourceProperties(
                                qualifiers = "hidpi",
                                extension = ".xml",
                                quantity = 3,
                                fileSizes = listOf(2048, 4096, 8192)
                            ),
                            SizeMattersAndroidResourceProperties(
                                qualifiers = "hidpi",
                                extension = ".png",
                                quantity = 2,
                                fileSizes = listOf(16384, 32768)
                            )
                    ),
                    "values" to mutableListOf<AbstractAndroidResourceProperties>(
                            ValuesAndroidResourceProperties(
                                qualifiers = "",
                                extension = ".xml",
                                quantity = 5,
                                valuesMapPerFile = listOf(
                                    ValuesMap(stringCount = 5),
                                    ValuesMap(stringCount = 2, intCount = 6),
                                    ValuesMap(stringCount = 2, intCount = 6, colorCount = 1),
                                    ValuesMap(stringCount = 2, intCount = 6, colorCount = 2, dimenCount = 1),
                                    ValuesMap(stringCount = 2, intCount = 6, colorCount = 3, dimenCount = 2, idCount = 1)
                                )
                            )
                    )
            )
        }

        javaResources {
            fileData = mapOf("json" to listOf<Long>(3, 4, 5))
        }
        assets {
            fileData = mapOf("png" to listOf<Long>(3, 4, 5))
        }

        dependencies = listOf(
            DefaultDependenciesInfo(DependencyType.MODULE, "module1", "api"),
            DefaultDependenciesInfo(DependencyType.MODULE, "module2", "implementation"),
            DefaultDependenciesInfo(DependencyType.EXTERNAL_LIBRARY, "lib:foo:1.0", "api")
        )

        android {
            compileSdkVersion = "android-30"
            minSdkVersion = 24
            targetSdkVersion = 30

            buildFeatures {
                androidResources = true
                compose = true
            }
        }
    }

    private fun assertModuleInfoEquals(subj: DefaultModuleInfo, obj: DefaultModuleInfo) {
        Truth.assertThat(subj.path).isEqualTo(obj.path)
        Truth.assertThat(subj.plugins).isEqualTo(obj.plugins)
        Truth.assertThat(subj.javaSources).isEqualTo(obj.javaSources)
        Truth.assertThat(subj.kotlinSources).isEqualTo(obj.kotlinSources)
        Truth.assertThat(subj.javaResources).isEqualTo(obj.javaResources)
        Truth.assertThat(subj.dependencies).isEqualTo(obj.dependencies)
        Truth.assertThat(subj.android).isEqualTo(obj.android)

        // Manually compare resources
        subj.androidResources?.resourceMap?.forEach { subjResourceType ->
            Truth.assertThat(obj.androidResources!!.resourceMap).containsKey(subjResourceType.key)
            val objResourceType = obj.androidResources!!.resourceMap.get(subjResourceType.key)!!

            subjResourceType.value.forEachIndexed { index, subjResourceProperties ->
                val objResourceProperties = objResourceType[index]
                Truth.assertThat(subjResourceProperties.propertyType).isEqualTo(objResourceProperties.propertyType)
                Truth.assertThat(subjResourceProperties.qualifiers).isEqualTo(objResourceProperties.qualifiers)
                Truth.assertThat(subjResourceProperties.extension).isEqualTo(objResourceProperties.extension)
                Truth.assertThat(subjResourceProperties.quantity).isEqualTo(objResourceProperties.quantity)

                when (subjResourceProperties.propertyType) {
                    ResourcePropertyType.VALUES -> {
                        val realSubjProperties = subjResourceProperties as ValuesAndroidResourceProperties
                        val realObjProperties = objResourceProperties as ValuesAndroidResourceProperties

                        Truth.assertThat(realSubjProperties.valuesMapPerFile).isEqualTo(realObjProperties.valuesMapPerFile)
                    }
                    ResourcePropertyType.SIZE_MATTERS -> {
                        val realSubjProperties = subjResourceProperties as SizeMattersAndroidResourceProperties
                        val realObjProperties = objResourceProperties as SizeMattersAndroidResourceProperties

                        Truth.assertThat(realSubjProperties.fileSizes).isEqualTo(realObjProperties.fileSizes)}
                    ResourcePropertyType.DEFAULT -> { } // Nothing more
                }
            }
        }
    }
}

private val EMPTY_MODULE = """
    {
      "path": ":foo",
      "plugins": [],
      "dependencies": []
    }
""".trimIndent()

private val FULL_MODULE = """
    {
      "path": ":foo",
      "plugins": [
        "com.android.application",
        "org.jetbrains.kotlin.android"
      ],
      "javaSources": {
        "fileCount": 1
      },
      "kotlinSources": {
        "fileCount": 2
      },
      "androidResources": {
        "mipmap": [
          {
            "qualifiers": "",
            "extension": ".xml",
            "quantity": 3,
            "fileSizes": [
              64,
              128,
              256
            ]
          },
          {
            "qualifiers": "",
            "extension": ".png",
            "quantity": 2,
            "fileSizes": [
              512,
              1024
            ]
          },
          {
            "qualifiers": "hidpi",
            "extension": ".xml",
            "quantity": 3,
            "fileSizes": [
              2048,
              4096,
              8192
            ]
          },
          {
            "qualifiers": "hidpi",
            "extension": ".png",
            "quantity": 2,
            "fileSizes": [
              16384,
              32768
            ]
          }
        ],
        "values": [
          {
            "qualifiers": "",
            "extension": ".xml",
            "quantity": 5,
            "valuesFileList": [
              {
                "stringCount": 5,
                "intCount": 0,
                "boolCount": 0,
                "colorCount": 0,
                "dimenCount": 0,
                "idCount": 0,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              },
              {
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 0,
                "dimenCount": 0,
                "idCount": 0,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              },
              {
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 1,
                "dimenCount": 0,
                "idCount": 0,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              },
              {
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 2,
                "dimenCount": 1,
                "idCount": 0,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              },
              {
                "stringCount": 2,
                "intCount": 6,
                "boolCount": 0,
                "colorCount": 3,
                "dimenCount": 2,
                "idCount": 1,
                "integerArrayCount": [],
                "arrayCount": [],
                "styleCount": []
              }
            ]
          }
        ]
      },
      "javaResources": {
        "json": [
          3,
          4,
          5
        ]
      },
      "assets": {
        "png": [
          3,
          4,
          5
        ]
      },
      "dependencies": [
        {
          "moduleName": "module1",
          "method": "api"
        },
        {
          "moduleName": "module2",
          "method": "implementation"
        },
        {
          "library": "lib:foo:1.0",
          "method": "api"
        }
      ],
      "android": {
        "compileSdkVersion": "android-30",
        "minSdkVersion": 24,
        "targetSdkVersion": 30,
        "buildFeatures": {
          "androidResources": true,
          "compose": true
        }
      }
    }
""".trimIndent()
