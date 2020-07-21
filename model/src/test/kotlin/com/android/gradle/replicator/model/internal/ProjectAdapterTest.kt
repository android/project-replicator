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

import com.android.gradle.replicator.model.DependencyType
import com.android.gradle.replicator.model.PluginType
import com.android.gradle.replicator.model.internal.fixtures.project
import com.google.common.truth.Truth
import org.junit.Test

class ProjectAdapterTest {

    @Test
    fun `test write empty project`() {
        Truth.assertThat(emptyProjectObject().toJson()).isEqualTo(EMPTY_PROJECT)
    }

    @Test
    fun `test load empty project`() {
        val project = emptyProjectObject()
        val loadedProject = EMPTY_PROJECT.fromJson(ProjectAdapter())

        Truth.assertThat(loadedProject).isEqualTo(project)
    }

    @Test
    fun `test load + write empty project`() {
        val loadedProject = EMPTY_PROJECT.fromJson(ProjectAdapter())

        Truth.assertThat(loadedProject.toJson()).isEqualTo(EMPTY_PROJECT)
    }

    @Test
    fun `test write full module`() {
        Truth.assertThat(fullProjectObject().toJson()).isEqualTo(FULL_PROJECT)
    }

    @Test
    fun `test load full module`() {
        val project = fullProjectObject()
        val loadedProject = FULL_PROJECT.fromJson(ProjectAdapter())

        Truth.assertThat(loadedProject).isEqualTo(project)
    }

    @Test
    fun `test load + write full module`() {
        val loadedProject = FULL_PROJECT.fromJson(ProjectAdapter())

        Truth.assertThat(loadedProject.toJson()).isEqualTo(FULL_PROJECT)
    }

    // --------------------------

    /**
     * this should match [EMPTY_MODULE]
     */
    private fun emptyProjectObject() = project {
        gradleVersion = "6.5"
        agpVersion = "4.2"
        kotlinVersion = "1.3.72"
    }

    /**
     * this should match [FULL_MODULE]
     */
    private fun fullProjectObject()= project {
        gradleVersion = "6.5"
        agpVersion = "4.2"
        kotlinVersion = "1.3.72"
        rootModule {
            path = ":foo"
            plugins = listOf(PluginType.ANDROID_APP, PluginType.KOTLIN_ANDROID)
            javaSources {
                fileCount = 1
            }
            kotlinSources {
                fileCount = 2
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
            }
        }
    }
}

private val EMPTY_PROJECT = """
    {
      "gradle": "6.5",
      "agp": "4.2",
      "kotlin": "1.3.72",
      "properties": [],
      "rootModule": {
        "path": ":",
        "plugins": [],
        "dependencies": []
      },
      "modules": []
    }
""".trimIndent()

private val FULL_PROJECT = """
    {
      "gradle": "6.5",
      "agp": "4.2",
      "kotlin": "1.3.72",
      "properties": [],
      "rootModule": {
        "path": ":foo",
        "plugins": [
          "com.android.application",
          "kotlin-android"
        ],
        "javaSources": {
          "fileCount": 1
        },
        "kotlinSources": {
          "fileCount": 2
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
          "targetSdkVersion": 30
        }
      },
      "modules": []
    }""".trimIndent()

