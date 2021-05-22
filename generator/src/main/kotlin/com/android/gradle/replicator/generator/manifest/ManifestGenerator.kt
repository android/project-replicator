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

package com.android.gradle.replicator.generator.manifest

import com.android.gradle.replicator.generator.createDirWithParents
import com.android.gradle.replicator.generator.join
import java.io.File

class ManifestGenerator {

    internal fun generateManifest(folder: File, packageName: String) {
        val manifestFile = folder.join("src", "main", "AndroidManifest.xml")
        val parentFolder = manifestFile.parentFile
        parentFolder.createDirWithParents()

        manifestFile.writeText(""" 
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="$packageName">
            </manifest>
        """.trimIndent())
    }

}