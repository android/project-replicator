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

        Truth.assertThat(loadedModule).isEqualTo(module)
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
            fileCount = AndroidResourceFolders(mutableMapOf(
                    "mipmap" to AndroidResourceQualifiers(mutableMapOf(
                            "" to AndroidResourceExtensions(mutableMapOf(
                                    ".xml" to 3,
                                    ".png" to 2
                            )),
                            "hidpi" to AndroidResourceExtensions(mutableMapOf(
                                    ".xml" to 3,
                                    ".png" to 2
                            ))
                    )),
                    "values" to AndroidResourceQualifiers(mutableMapOf(
                            "" to AndroidResourceExtensions(mutableMapOf(
                                    ".xml" to 5
                            ))
                    ))
            ))
        }
        javaResources {
            fileCount = 3
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
        "mipmap": {
          "": {
            ".xml": 3,
            ".png": 2
          },
          "hidpi": {
            ".xml": 3,
            ".png": 2
          }
        },
        "values": {
          "": {
            ".xml": 5
          }
        }
      },
      "javaResources": {
        "fileCount": 3
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
