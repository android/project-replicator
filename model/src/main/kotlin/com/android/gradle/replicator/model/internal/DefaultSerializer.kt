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

package com.android.gradle.replicator.model.internal

import com.android.gradle.replicator.model.ModuleInfo
import com.android.gradle.replicator.model.ProjectInfo
import com.android.gradle.replicator.model.Serializer
import java.io.File

class DefaultSerializer: Serializer {
    override fun serialize(project: ProjectInfo): String = project.toJson()

    override fun serialize(module: ModuleInfo): String = module.toJson()

    override fun deserializeProject(content: String): ProjectInfo = content.fromJson(ProjectAdapter())

    override fun deserializeProject(content: File): ProjectInfo = content.readText().fromJson(ProjectAdapter())

    override fun deserializeModule(content: String): ModuleInfo = content.fromJson(ModuleAdapter())

    override fun deserializeModule(content: File): ModuleInfo = content.readText().fromJson(ModuleAdapter())
}