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
class BuildFeaturesTest: BaseTest() {

    @Test
    fun testAidl() {
        runTest(
            pluginId = "com.android.application",
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
            pluginId = "com.android.application",
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
            pluginId = "com.android.library",
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
            pluginId = "com.android.application",
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
            pluginId = "com.android.application",
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
            pluginId = "com.android.application",
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
            pluginId = "com.android.application",
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
            pluginId = "com.android.library",
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
            pluginId = "com.android.application",
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
            pluginId = "com.android.application",
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
            pluginId = "com.android.application",
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
            pluginId = "com.android.application",
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
        pluginId: String,
        structureSnippet: String,
        expectedSnippet: String
    ) {
        val output = generateWithStructure(getStructure(pluginId, structureSnippet))

        val moduleBuildFile = File(File(output, "module1"), buildFileName)
        Truth.assertWithMessage(moduleBuildFile.absolutePath).that(moduleBuildFile.readText()).isEqualTo(
            getExpected(
                pluginId,
                expectedSnippet
            )
        )

        GradleRunner(output).runTasks("projects")
    }

    private fun getStructure(
        pluginId: String,
        features: String
    ): String = """
{
"gradle": "6.1.1",
"agp": "4.1.0",
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
    "$pluginId"
  ],
  "javaSources": {
    "fileCount": 1
  },
  "androidResources": {
    "animator": {},
    "anim": {},
    "color": {},
    "drawable": {},
    "mipmap": {},
    "layout": {},
    "menu": {},
    "raw": {},
    "values": {},
    "xml": {},
    "font": {}
  },
  "javaResources": {
    "fileCount": 0
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

    private fun getExpected(
        pluginId: String,
        features: String
    ): String = select(
        kts = """
            |plugins {
            |  id("$pluginId")
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
            $features
            |}
            |dependencies {
            |}
            |
        """.trimMargin(),
        groovy = """
            |plugins {
            |  id '$pluginId'
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
            $features
            |}
            |dependencies {
            |}
            |
        """.trimMargin())
}