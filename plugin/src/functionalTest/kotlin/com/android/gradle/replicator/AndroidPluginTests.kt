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

class AndroidPluginTests {
    @Test
    fun `android-app in groovy`() {
        val setup = setupProject(type = BuildFileType.GROOVY) {
"""
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath "com.android.tools.build:gradle:$AGP_VERSION"
    }
"""
        }

        setup.buildFile.appendText("""
            apply plugin: "com.android.application"
            //apply plugin: "kotlin-kapt"
            
            android {
                compileSdkVersion = 30
                defaultConfig {
                    minSdkVersion = 24
                    targetSdkVersion = 30
                }
            }
        """.trimIndent())

        setup.runner.build()

        // Verify the result
        Truth.assertThat(setup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
            {
              "gradle": "$GRADLE_VERSION",
              "agp": "$AGP_VERSION",
              "kotlin": "n/a",
              "properties": [],
              "rootModule": {
                "path": ":",
                "plugins": [
                  "com.android.application"
                ],
                "javaSources": {
                  "fileCount": 0
                },
                "dependencies": [],
                "android": {
                  "compileSdkVersion": "android-30",
                  "minSdkVersion": 24,
                  "targetSdkVersion": 30,
                  "buildFeatures": {}
                }
              },
              "modules": []
            }
            """.trimIndent())
    }

    @Test
    fun `android-app plus kotlin in groovy`() {
        val setup = setupProject(type = BuildFileType.GROOVY) {
            """
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath "com.android.tools.build:gradle:$AGP_VERSION"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION"
    }
"""
        }

        setup.buildFile.appendText("""
            apply plugin: "com.android.application"
            apply plugin: "kotlin-android"
            
            android {
                compileSdkVersion = 30            
                defaultConfig {
                    minSdkVersion = 24
                    targetSdkVersion = 30
                }
            }
        """.trimIndent())

        setup.runner.build()

        // Verify the result
        Truth.assertThat(setup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
            {
              "gradle": "$GRADLE_VERSION",
              "agp": "$AGP_VERSION",
              "kotlin": "$KOTLIN_VERSION",
              "properties": [],
              "rootModule": {
                "path": ":",
                "plugins": [
                  "org.jetbrains.kotlin.android",
                  "com.android.application"
                ],
                "javaSources": {
                  "fileCount": 0
                },
                "kotlinSources": {
                  "fileCount": 0
                },
                "dependencies": [],
                "android": {
                  "compileSdkVersion": "android-30",
                  "minSdkVersion": 24,
                  "targetSdkVersion": 30,
                  "buildFeatures": {}
                }
              },
              "modules": []
            }
            """.trimIndent())
    }

}