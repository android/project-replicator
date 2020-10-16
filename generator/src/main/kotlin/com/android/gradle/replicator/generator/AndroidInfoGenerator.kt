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

import com.android.gradle.replicator.generator.writer.DslWriter
import com.android.gradle.replicator.model.AndroidInfo
import com.android.gradle.replicator.model.BuildFeaturesInfo
import java.io.File

internal fun AndroidInfo.generate(
    folder: File,
    dslWriter: DslWriter,
    gradlePath: String,
    hasKotlin: Boolean) {

    // generate the android block
    dslWriter.block("android") {
        // FIXME?
        assign("compileSdkVersion", asString(compileSdkVersion))

        block("defaultConfig") {
            call("minSdkVersion", minSdkVersion)
            call("targetSdkVersion", targetSdkVersion)
        }

        block("compileOptions") {
            assign("sourceCompatibility", "JavaVersion.VERSION_1_8")
            assign("targetCompatibility", "JavaVersion.VERSION_1_8")
        }

        // For Kotlin projects
        if (hasKotlin) {
            block("kotlinOptions") {
                assign("jvmTarget", asString("1.8"))
            }
        }

        buildFeatures.generateBuildFeatures(dslWriter)
    }

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

private fun BuildFeaturesInfo.generateBuildFeatures(dslWriter: DslWriter) {
    if (!hasFeatures()) return

    dslWriter.block("buildFeatures") {
        aidl?.let {
            assign("aidl", it)
        }
        buildConfig?.let {
            assign("buildConfig", it)
        }
        androidResources?.let {
            assign("androidResources", it)
        }
        compose?.let {
            assign("compose", it)
        }
        dataBinding?.let {
            assign("dataBinding", it)
        }
        mlModelBinding?.let {
            assign("mlModelBinding", it)
        }
        prefab?.let {
            assign("prefab", it)
        }
        prefabPublishing?.let {
            assign("prefabPublishing", it)
        }
        renderScript?.let {
            assign("renderScript", it)
        }
        resValues?.let {
            assign("resValues", it)
        }
        shaders?.let {
            assign("shaders", it)
        }
        viewBinding?.let {
            assign("viewBinding", it)
        }
    }
}
