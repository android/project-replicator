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

package com.android.gradle.replicator.generator

import com.android.gradle.replicator.generator.fixtures.BaseTest
import com.android.gradle.replicator.generator.fixtures.GradleRunner
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class BasicAndroidTestPreMarker: BaseTest() {

    companion object {
        private const val TEST_STRUCTURE = """
{
  "gradle": "6.1.1",
  "agp": "4.0.1",
  "kotlin": "n/a",
  "properties": [],
  "rootModule": {
    "path": ":",
    "plugins": [],
    "dependencies": []
  },
  "modules": [
    {
      "path": ":module1",
      "plugins": [
        "com.android.application"
      ],
      "javaSources": {
        "fileCount": 1
      },
      "dependencies": [],
      "android": {
        "compileSdkVersion": "android-30",
        "minSdkVersion": 21,
        "targetSdkVersion": 30,
        "buildFeatures": {}
      }
    }
  ]
}            
"""

    }

    @Test
    fun testOutput() {
        val output = generateWithStructure(TEST_STRUCTURE)

        val rootBuildFile = File(output, buildFileName)
        Truth.assertWithMessage(rootBuildFile.absolutePath).that(rootBuildFile.readText()).isEqualTo(
            select(
                kts = """
                    |buildscript {
                    |  repositories {
                    |    google()
                    |    jcenter()
                    |  }
                    |  dependencies {
                    |    classpath("com.android.tools.build:gradle:4.0.1")
                    |  }
                    |}
                    |allprojects {
                    |  repositories {
                    |    google()
                    |    jcenter()
                    |    maven {
                    |      url = uri("https://jitpack.io")
                    |    }
                    |  }
                    |}
                    |dependencies {
                    |}
                    |
                """.trimMargin(),
                groovy = """
                    |buildscript {
                    |  repositories {
                    |    google()
                    |    jcenter()
                    |  }
                    |  dependencies {
                    |    classpath 'com.android.tools.build:gradle:4.0.1'
                    |  }
                    |}
                    |allprojects {
                    |  repositories {
                    |    google()
                    |    jcenter()
                    |    maven {
                    |      url 'https://jitpack.io'
                    |    }
                    |  }
                    |}
                    |dependencies {
                    |}
                    |
                """.trimMargin()
            )
        )

        val settingsFile = File(output, settingsFileName)
        Truth.assertWithMessage(settingsFile.absolutePath).that(settingsFile.readText()).isEqualTo(
            select(
                kts = """
                    |include(":module1")
                    |
                """.trimMargin(),
                groovy = """
                    |include ':module1'
                    |
                """.trimMargin()
            )
        )

        val moduleBuildFile = File(File(output, "module1"), buildFileName)
        Truth.assertWithMessage(moduleBuildFile.absolutePath).that(moduleBuildFile.readText()).isEqualTo(
            select(
                kts = """
                    |plugins {
                    |  id("com.android.application")
                    |}
                    |android {
                    |  compileSdkVersion = "android-30"
                    |  defaultConfig {
                    |    minSdkVersion(21)
                    |    targetSdkVersion(30)
                    |  }
                    |  compileOptions {
                    |    sourceCompatibility = JavaVersion.VERSION_1_8
                    |    targetCompatibility = JavaVersion.VERSION_1_8
                    |  }
                    |}
                    |dependencies {
                    |}
                    |
                """.trimMargin(),
                groovy = """
                    |plugins {
                    |  id 'com.android.application'
                    |}
                    |android {
                    |  compileSdkVersion = 'android-30'
                    |  defaultConfig {
                    |    minSdkVersion 21
                    |    targetSdkVersion 30
                    |  }
                    |  compileOptions {
                    |    sourceCompatibility = JavaVersion.VERSION_1_8
                    |    targetCompatibility = JavaVersion.VERSION_1_8
                    |  }
                    |}
                    |dependencies {
                    |}
                    |
                """.trimMargin()
            )
        )
    }

    @Test
    fun runOutput() {
        GradleRunner(generateWithStructure(TEST_STRUCTURE)).runTasks("projects")
    }
}