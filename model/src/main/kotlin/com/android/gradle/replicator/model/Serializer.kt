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

import com.android.gradle.replicator.model.internal.DefaultSerializer
import java.io.File

interface Serializer {

    /**
     * Serialize the given [ProjectInfo]
     */
    fun serialize(project: ProjectInfo): String

    /**
     * Serialize the given [ModuleInfo]
     */
    fun serialize(module: ModuleInfo): String

    /**
     * Deserialize the given project content as a string into a [ProjectInfo]
     */
    fun deserializeProject(content: String): ProjectInfo

    /**
     * Deserialize the given project content as a [File] into a [ProjectInfo]
     */
    fun deserializeProject(content: File): ProjectInfo

    /**
     * Deserialize the given module content as a string into a [ModuleInfo]
     */
    fun deserializeModule(content: String): ModuleInfo

    /**
     * Deserialize the given module content as a [File] into a [ModuleInfo]
     */
    fun deserializeModule(content: File): ModuleInfo

    companion object {
        /**
         * Gets an instance of a [Serializer]
         */
        fun instance(): Serializer = DefaultSerializer()
    }
}