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

import com.android.gradle.replicator.model.ProjectInfo
import com.android.gradle.replicator.model.internal.DefaultProjectInfo

class ProjectBuilder {
    var gradleVersion: String = ""
    var agpVersion: String = ""
    var kotlinVersion: String = ""
    var rootModule: ModuleBuilder = ModuleBuilder(path = ":")
    val subModules: MutableList<ModuleBuilder> = mutableListOf()
    var properties: MutableList<String> = mutableListOf()


    fun rootModule(action: ModuleBuilder.() -> Unit) {
        action(rootModule)
    }

    fun subModule(action: ModuleBuilder.() -> Unit) {
        val newModule = ModuleBuilder()
        action(newModule)
        subModules.add(newModule)
    }

    fun toInfo(): ProjectInfo =
            DefaultProjectInfo(
                gradleVersion = gradleVersion,
                agpVersion = agpVersion,
                kotlinVersion = kotlinVersion,
                rootModule = rootModule.toInfo(),
                subModules = subModules.map(ModuleBuilder::toInfo),
                gradleProperties = properties
            )
}