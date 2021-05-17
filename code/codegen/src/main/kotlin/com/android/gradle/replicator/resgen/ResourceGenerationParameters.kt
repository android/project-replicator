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

import com.google.gson.Gson
import java.io.File
import java.nio.file.Files

typealias AndroidResourceMap = Map<String, Map<String, Map<String, Int>>>

/**
 * Parameters for the generation for a module.
 */
data class ResourceGenerationParameters(
        /**
         * Seed value for the randomizer
         */
        val seed: Int,

        /**
         * Number of Android resource files that should be generated in this module, separated by type
         */
        val numberOfAndroidResources: AndroidResourceMap,

        /**
         * Number of Java resource files that should be generated in this module
         */
        val numberOfJavaResources: Int
) {
    class Builder {
        private var seed = 1
        private var nbOfAndroidResources: AndroidResourceMap = mapOf()
        private var nbOfJavaResources = 0

        fun setSeed(seed: Int) { this.seed = seed }

        fun setNumberOfAndroidResources(numberOfAndroidResources: AndroidResourceMap) { this.nbOfAndroidResources = numberOfAndroidResources }

        fun setNumberOfJavaResources(numberOfJavaResources: Int) { this.nbOfJavaResources = numberOfJavaResources }

        fun build(): ResourceGenerationParameters =
                ResourceGenerationParameters(
                        seed = seed,
                        numberOfAndroidResources = nbOfAndroidResources,
                        numberOfJavaResources = nbOfJavaResources
                )
    }
}