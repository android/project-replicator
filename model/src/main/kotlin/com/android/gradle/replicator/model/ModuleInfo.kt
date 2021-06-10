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

/**
 * represents a sub-project in a multi-project build.
 */
interface ModuleInfo {
    /**
     * The full path of the gradle project
     */
    val path: String

    /**
     * List of plugins applied
     */
    val plugins: List<PluginType>

    /**
     * Source amount information for java files. null for kotlin only modules
     */
    val javaSources: SourceFilesInfo?

    /**
     * Kotlin amount information for kotlin files. null for java only modules
     */
    val kotlinSources: SourceFilesInfo?

    /**
     * Number of each android resource files.
     */
    val androidResources: AndroidResourcesInfo?

    /**
     * Number of each java resource files.
     */
    val javaResources: SourceFilesInfo?

    /**
     * List of direct dependencies with their scope
     */
    val dependencies: List<DependenciesInfo>

    /**
     * Android information. null for non-Android modules
     */
    val android: AndroidInfo?
}