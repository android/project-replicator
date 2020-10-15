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
import com.android.gradle.replicator.generator.fixtures.TestParams
import com.google.common.truth.Truth
import org.junit.Test
import java.io.File

class BasicAndroidWithKotlinTest: BaseTest() {

    @Test
    fun test() {
        val output = generateWithStructure(projectStructure)

        val rootBuildFile = File(output, "build.gradle")
        Truth.assertWithMessage(rootBuildFile.absolutePath).that(rootBuildFile.readText()).isEqualTo("""
            |buildscript {
            |  repositories {
            |    google()
            |    jcenter()
            |  }
            |  dependencies {
            |    classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72'
            |    classpath 'com.android.tools.build:gradle:4.0.1'
            |  }
            |}
            |plugins {
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
        """.trimMargin())

        val settingsFile = File(output, "settings.gradle")
        Truth.assertWithMessage(settingsFile.absolutePath).that(settingsFile.readText()).isEqualTo("""
            |include ':module1'
            |
        """.trimMargin())

        val moduleBuildFile = File(File(output, "module1"), "build.gradle")

        Truth.assertWithMessage(moduleBuildFile.absolutePath).that(moduleBuildFile.readText()).isEqualTo("""
            |apply plugin: 'com.android.application'
            |apply plugin: 'kotlin-android'
            |android {
            |  compileSdkVersion = 'android-30'
            |  defaultConfig {
            |    minSdkVersion = 21
            |    targetSdkVersion = 30
            |  }
            |  compileOptions {
            |    sourceCompatibility = JavaVersion.VERSION_1_8
            |    targetCompatibility = JavaVersion.VERSION_1_8
            |  }
            |  kotlinOptions {
            |    jvmTarget = '1.8'
            |  }
            |}
            |dependencies {
            |}
            |""".trimMargin())
    }

    private val projectStructure: String = """
{
  "gradle": "6.1.1",
  "agp": "4.0.1",
  "kotlin": "1.3.72",
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
        "kotlin-android",
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