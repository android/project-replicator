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
import com.google.common.truth.Truth
import org.junit.Test
import java.io.File

class BuildFeaturesTest: BaseTest() {

    @Test
    fun testAidl() {
        runTest(
            structureSnippet = """
                "aidl": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    aidl = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testBuildConfig() {
        runTest(
            structureSnippet = """
                "buildConfig": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    buildConfig = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testAndroidResources() {
        runTest(
            structureSnippet = """
                "androidResources": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    androidResources = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testCompose() {
        runTest(
            structureSnippet = """
                "compose": true
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    compose = true
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testDataBinding() {
        runTest(
            structureSnippet = """
                "dataBinding": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    dataBinding = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testML() {
        runTest(
            structureSnippet = """
                "mlModelBinding": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    mlModelBinding = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testPrefab() {
        runTest(
            structureSnippet = """
                "prefab": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    prefab = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testPrefabPublishing() {
        runTest(
            structureSnippet = """
                "prefabPublishing": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    prefabPublishing = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testRenderScript() {
        runTest(
            structureSnippet = """
                "renderScript": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    renderScript = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testResValues() {
        runTest(
            structureSnippet = """
                "resValues": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    resValues = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testShaders() {
        runTest(
            structureSnippet = """
                "shaders": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    shaders = false
            |  }
            """.trimIndent()
        )
    }

    @Test
    fun testViewBinding() {
        runTest(
            structureSnippet = """
                "viewBinding": false
            """.trimIndent(),
            expectedSnippet = """
            |  buildFeatures {
            |    viewBinding = false
            |  }
            """.trimIndent()
        )
    }

    // -------------

    private fun runTest(
        structureSnippet: String,
        expectedSnippet: String
    ) {
        val output = generateWithStructure(getStructure(structureSnippet))

        val moduleBuildFile = File(File(output, "module1"), "build.gradle")
        Truth.assertWithMessage(moduleBuildFile.absolutePath).that(moduleBuildFile.readText()).isEqualTo(
            getExpected(
                expectedSnippet
            )
        )
    }

    private fun getStructure(features: String): String = """
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
    "buildFeatures": {
      $features
    }
  }
}
]
}            
"""

    private fun getExpected(features: String): String = """
        |apply plugin: 'com.android.application'
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
        $features
        |}
        |dependencies {
        |}
        |""".trimMargin()
}