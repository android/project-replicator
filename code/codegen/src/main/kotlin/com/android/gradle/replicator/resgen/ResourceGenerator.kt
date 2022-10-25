/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.AbstractAndroidResourceProperties
import com.android.gradle.replicator.resourceModel.ResourceModel
import com.android.gradle.replicator.resgen.util.ResgenConstants
import com.android.gradle.replicator.resgen.util.UniqueIdGenerator
import java.io.File
import kotlin.random.Random

data class ResourceGenerationParams(
    val random: Random,
    val constants: ResgenConstants,
    val uniqueIdGenerator: UniqueIdGenerator,
    val resourceModel: ResourceModel
)

/**
 * A ResourceGenerator is capable of generating a single type of resource files.
 */
// TODO: return list of generated resources
// TODO: make sure resources don't have the same name
abstract class ResourceGenerator (protected val params: ResourceGenerationParams) {
    /**
     * Generate a resource
     * @param outputFile the output file.
     * @param resourceQualifier the subtype of resource (hidpi, night, etc.).
     * @param resourceExtension the extension of the resource (xml, png, etc.).
     */
    abstract fun generateResource(
        properties: AbstractAndroidResourceProperties,
        outputFolder: File
    )
}