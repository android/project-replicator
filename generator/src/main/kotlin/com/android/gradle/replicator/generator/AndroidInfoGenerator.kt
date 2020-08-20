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

import com.android.gradle.replicator.model.AndroidInfo
import com.android.gradle.replicator.model.BuildFeaturesInfo
import java.io.File

internal fun AndroidInfo.generate(
    folder: File,
    buildFile: File,
    gradlePath: String,
    hasKotlin: Boolean) {

    // generate the android block

    buildFile.appendText("""
android {
    compileSdkVersion = "$compileSdkVersion"
    defaultConfig {
        minSdkVersion $minSdkVersion
        targetSdkVersion $targetSdkVersion
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
""")

    // For Kotlin projects
    if (hasKotlin) {
        buildFile.appendText(
            """
                |    kotlinOptions {
                |        jvmTarget = "1.8"
                |    } 
            """.trimMargin()
        )
    }

    buildFeatures.generateBuildFeatures(buildFile)

    // close Android block
    buildFile.appendText("\n}\n")

    // generate a main manifest.
    generateManifest(folder, gradlePath)
}

private fun generateManifest(folder: File, gradlePath: String) {
    val manifestFile = folder.join("src", "main", "AndroidManifest.xml")
    val parentFolder = manifestFile.parentFile
    parentFolder.createDirWithParents()

    // compute package name based on gradle path
    val packageName = gradlePath.split(":").filter { it.isNotBlank() }.joinToString(".")

    // add "pkg.android." prefix to package in order to guarantee at least 2 segments to the package since it's
    // by aapt
    manifestFile.writeText(""" 
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="pkg.android.$packageName">
            </manifest>
        """.trimIndent())
}

private fun BuildFeaturesInfo.generateBuildFeatures(buildFile: File) {
    if (!hasFeatures()) return

    buildFile.appendText("    buildFeatures {\n")

    aidl?.let {
        buildFile.appendText("        aidl = $it\n")
    }
    buildConfig?.let {
        buildFile.appendText("        buildConfig = $it\n")
    }
    androidResources?.let {
        buildFile.appendText("        androidResources = $it\n")
    }
    compose?.let {
        buildFile.appendText("        compose = $it\n")
    }
    dataBinding?.let {
        buildFile.appendText("        dataBinding = $it\n")
    }
    mlModelBinding?.let {
        buildFile.appendText("        mlModelBinding = $it\n")
    }
    prefab?.let {
        buildFile.appendText("        prefab = $it\n")
    }
    prefabPublishing?.let {
        buildFile.appendText("        prefabPublishing = $it\n")
    }
    renderScript?.let {
        buildFile.appendText("        renderScript = $it\n")
    }
    resValues?.let {
        buildFile.appendText("        resValues = $it\n")
    }
    shaders?.let {
        buildFile.appendText("        shaders = $it\n")
    }
    viewBinding?.let {
        buildFile.appendText("        viewBinding = $it\n")
    }

    buildFile.appendText("    }\n")

}
