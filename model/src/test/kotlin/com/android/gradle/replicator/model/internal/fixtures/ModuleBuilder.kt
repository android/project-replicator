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

package com.android.gradle.replicator.model.internal.fixtures

import com.android.gradle.replicator.model.DependenciesInfo
import com.android.gradle.replicator.model.PluginType
import com.android.gradle.replicator.model.internal.DefaultModuleInfo

class ModuleBuilder(var path: String = "") {
    var plugins: List<PluginType> = listOf()
    var javaSources: SourceFilesBuilder? = null
    var kotlinSources: SourceFilesBuilder? = null
    var androidResources: AndroidResourcesBuilder? = null
    var javaResources: SourceFilesBuilder? = null
    var dependencies: List<DependenciesInfo> = listOf()
    var android: AndroidBuilder? = null

    fun javaSources(action: SourceFilesBuilder.() -> Unit) {
        action(javaSources ?: SourceFilesBuilder().also { javaSources = it })
    }

    fun kotlinSources(action: SourceFilesBuilder.() -> Unit) {
        action(kotlinSources ?: SourceFilesBuilder().also { kotlinSources = it })
    }

    fun androidResources(action: AndroidResourcesBuilder.() -> Unit) {
        action(androidResources ?: AndroidResourcesBuilder().also { androidResources = it })
    }

    fun javaResources(action: SourceFilesBuilder.() -> Unit) {
        action(javaResources ?: SourceFilesBuilder().also { javaResources = it })
    }

    fun android(action: AndroidBuilder.() -> Unit) {
        action(android ?: AndroidBuilder().also { android = it })
    }

    fun toInfo() = DefaultModuleInfo(
        path,
        plugins,
        javaSources?.toInfo(),
        kotlinSources?.toInfo(),
        androidResources?.toInfo(),
        javaResources?.toInfo(),
        dependencies,
        android?.toInfo()
    )
}