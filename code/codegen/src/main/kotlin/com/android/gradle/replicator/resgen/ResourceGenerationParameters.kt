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

import com.android.gradle.replicator.model.internal.filedata.AndroidResourceMap
import com.android.gradle.replicator.model.internal.filedata.FilesWithSizeMap

/**
 * Parameters for the generation for a module.
 */
data class ResourceGenerationParameters(
    /**
         * Seed value for the randomizer
         */
        val seed: Int,

    /**
         * Number of Android resource files that should be generated in this module, separated by type, with metadata
         */
        val androidResourcesMap: AndroidResourceMap,

    /**
         * Number of Java resource files that should be generated in this module, separated by type, with metadata
         */
        val javaResourcesMap: FilesWithSizeMap,

    /**
         * Number of Java resource files that should be generated in this module, separated by type, with metadata
         */
        val assetsMap: FilesWithSizeMap
) {
    class Builder {
        private var seed = 1
        private var androidResourcesMap: AndroidResourceMap = mutableMapOf()
        private var javaResourcesMap: FilesWithSizeMap = mapOf()
        private var assetsMap: FilesWithSizeMap = mapOf()

        fun setSeed(seed: Int) { this.seed = seed }

        fun setAndroidResourcesMap(androidResourcesMap: AndroidResourceMap) { this.androidResourcesMap = androidResourcesMap }

        fun setJavaResourcesMap(javaResourcesMap: FilesWithSizeMap) { this.javaResourcesMap = javaResourcesMap }

        fun setAssetsMap(assetsMap: FilesWithSizeMap) { this.assetsMap = assetsMap }

        fun build(): ResourceGenerationParameters =
                ResourceGenerationParameters(
                    seed = seed,
                    androidResourcesMap = androidResourcesMap,
                    javaResourcesMap = javaResourcesMap,
                    assetsMap = assetsMap
                )
    }
}