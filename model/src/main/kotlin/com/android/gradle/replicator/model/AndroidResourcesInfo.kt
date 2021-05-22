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
 */

package com.android.gradle.replicator.model


/* Data classes to represent the hierarchy for android resources
 * Each resource folder type (values, mipmap, etc.) can have different qualified folders (hidpi, night, etc.)
 * and each of those qualified folders can have different file types in them (AKA extensions)
 */
data class AndroidResourceExtensions(
        val extensions: MutableMap<String, Int>
)
data class AndroidResourceQualifiers(
        val qualifiers: MutableMap<String, AndroidResourceExtensions>
)
data class AndroidResourceFolders(
        val folders: MutableMap<String, AndroidResourceQualifiers>
)

/**
 * Information about the number of android resource files in a [ModuleInfo]
 * Divided by folder name, qualifiers and  such as mipmap-hidpi
 */
interface AndroidResourcesInfo {
    val fileCount: AndroidResourceFolders
    val asMap: MutableMap<String, MutableMap<String, MutableMap<String, Int>>>
}