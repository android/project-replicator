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

import com.android.gradle.replicator.model.PluginType
import java.io.File

internal fun Iterable<PluginType>.containsAndroid() = any { it.isAndroid }
internal fun Iterable<PluginType>.containsKotlin() = any { it.isKotlin }
internal fun Iterable<PluginType>.containsJava() = any { it.isJava }

internal fun File.createDirWithParents() {
    // attempt to create first.
    // if failure only throw if folder does not exist.
    // This makes this method able to create the same folder(s) from different thread
    if (!mkdirs() && !isDirectory) {
        throw RuntimeException("Cannot create directory $this")
    }
}

/**
 * Joins a list of path segments to a given File object.
 *
 * @param paths the segments.
 * @return a new File object.
 */
internal fun File.join(paths: List<String>): File {
    val p = paths.filter { it.isNotBlank() }
    return if (p.isEmpty()) {
        this
    } else {
        File(this, p.joinToString(separator = File.separator))
    }
}

/**
 * Joins a list of path segments to a given File object.
 *
 * @param paths the segments.
 * @return a new File object.
 */
internal fun File.join(vararg paths: String): File {
    val p = paths.filter { it.isNotBlank() }
    return if (p.isEmpty()) {
        this
    } else {
        File(this, p.joinToString(separator = File.separator))
    }
}
