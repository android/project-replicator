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
 *
 */

package com.android.gradle.replicator

import com.google.common.truth.Truth
import kotlin.test.Test

@Suppress("FunctionName")
class KotlinPluginTests {
    @Test
    fun `org-jetbrains-kotlin-jvm in groovy`() {
        val setup = setupProject(
            type = BuildFileType.GROOVY,
            plugins = listOf(PluginInfo("org.jetbrains.kotlin.jvm", KOTLIN_VERSION))
        )

        setup.runner.build()

        // Verify the result
        Truth.assertThat(setup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
            {
              "gradle": "$GRADLE_VERSION",
              "agp": "n/a",
              "kotlin": "n/a",
              "properties": [],
              "rootModule": {
                "path": ":",
                "plugins": [
                  "java",
                  "org.jetbrains.kotlin.jvm"
                ],
                "javaSources": {
                  "fileCount": 0
                },
                "kotlinSources": {
                  "fileCount": 0
                },
                "dependencies": []
              },
              "modules": []
            }
            """.trimIndent())
    }

    @Test
    fun `org-jetbrains-kotlin-jvm in kts`() {
        val setup = setupProject(
            type = BuildFileType.KTS,
            plugins = listOf(PluginInfo("jvm", KOTLIN_VERSION, kotlin = true))
        )

        setup.runner.build()

        // Verify the result
        Truth.assertThat(setup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
            {
              "gradle": "$GRADLE_VERSION",
              "agp": "n/a",
              "kotlin": "n/a",
              "properties": [],
              "rootModule": {
                "path": ":",
                "plugins": [
                  "java",
                  "org.jetbrains.kotlin.jvm"
                ],
                "javaSources": {
                  "fileCount": 0
                },
                "kotlinSources": {
                  "fileCount": 0
                },
                "dependencies": []
              },
              "modules": []
            }
            """.trimIndent())
    }


    @Test
    fun `kotlin in groovy`() {
        val setup = setupProject(BuildFileType.GROOVY) {
"""
    repositories {
        jcenter()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION"
    }
"""
        }

        setup.buildFile.appendText("""
            apply plugin: "kotlin"
        """.trimIndent())

        setup.runner.build()

        // Verify the result
        Truth.assertThat(setup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
            {
              "gradle": "$GRADLE_VERSION",
              "agp": "n/a",
              "kotlin": "$KOTLIN_VERSION",
              "properties": [],
              "rootModule": {
                "path": ":",
                "plugins": [
                  "java",
                  "org.jetbrains.kotlin.jvm"
                ],
                "javaSources": {
                  "fileCount": 0
                },
                "kotlinSources": {
                  "fileCount": 0
                },
                "dependencies": []
              },
              "modules": []
            }
            """.trimIndent())
    }

    @Test
    fun `org-jetbrains-kotlin-kapt in groovy`() {
        val setup = setupProject(
            type = BuildFileType.GROOVY,
            plugins = listOf(
                PluginInfo("org.jetbrains.kotlin.jvm", KOTLIN_VERSION),
                PluginInfo("org.jetbrains.kotlin.kapt", KOTLIN_VERSION)
            )
        )

        setup.runner.build()

        // Verify the result
        Truth.assertThat(setup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
            {
              "gradle": "$GRADLE_VERSION",
              "agp": "n/a",
              "kotlin": "n/a",
              "properties": [],
              "rootModule": {
                "path": ":",
                "plugins": [
                  "java",
                  "org.jetbrains.kotlin.jvm",
                  "org.jetbrains.kotlin.kapt"
                ],
                "javaSources": {
                  "fileCount": 0
                },
                "kotlinSources": {
                  "fileCount": 0
                },
                "dependencies": []
              },
              "modules": []
            }
            """.trimIndent())
    }

    @Test
    fun `org-jetbrains-kotlin-kapt in kts`() {
        val setup = setupProject(
            type = BuildFileType.KTS,
            plugins = listOf(
                PluginInfo("jvm", KOTLIN_VERSION, kotlin = true),
                PluginInfo("kapt", KOTLIN_VERSION, kotlin = true)
            )
        )

        setup.runner.build()

        // Verify the result
        Truth.assertThat(setup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
            {
              "gradle": "$GRADLE_VERSION",
              "agp": "n/a",
              "kotlin": "n/a",
              "properties": [],
              "rootModule": {
                "path": ":",
                "plugins": [
                  "java",
                  "org.jetbrains.kotlin.jvm",
                  "org.jetbrains.kotlin.kapt"
                ],
                "javaSources": {
                  "fileCount": 0
                },
                "kotlinSources": {
                  "fileCount": 0
                },
                "dependencies": []
              },
              "modules": []
            }
            """.trimIndent())
    }

    @Test
    fun `kotlin-kapt in groovy`() {
        val setup = setupProject(BuildFileType.GROOVY) {
"""
    repositories {
        jcenter()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION"
    }
"""
        }

        setup.buildFile.appendText("""
            apply plugin: "kotlin"
            apply plugin: "kotlin-kapt"
        """.trimIndent())

        setup.runner.build()

        // Verify the result
        Truth.assertThat(setup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
            {
              "gradle": "$GRADLE_VERSION",
              "agp": "n/a",
              "kotlin": "$KOTLIN_VERSION",
              "properties": [],
              "rootModule": {
                "path": ":",
                "plugins": [
                  "java",
                  "org.jetbrains.kotlin.jvm",
                  "org.jetbrains.kotlin.kapt"
                ],
                "javaSources": {
                  "fileCount": 0
                },
                "kotlinSources": {
                  "fileCount": 0
                },
                "dependencies": []
              },
              "modules": []
            }
            """.trimIndent())
    }
}