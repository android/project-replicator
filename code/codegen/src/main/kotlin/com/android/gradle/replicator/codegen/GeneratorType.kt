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
package com.android.gradle.replicator.codegen

import com.android.gradle.replicator.codegen.kotlin.KotlinClassGenerator
import java.lang.IllegalStateException
import kotlin.random.Random

/**
 * List all potential generator available.
 */
enum class GeneratorType {
    /**
     * Kotlin [SourceGenerator]
     */
    Kotlin {
        override fun initialize(params: GenerationParameters, random: Random) =
            GeneratorDriver(params, random) { printer, listeners ->
                KotlinClassGenerator(printer, listeners)
            }

    },

    /**
     * Java [SourceGenerator]
     */
    Java {
        override fun initialize(
                params: GenerationParameters,
                random: Random
        ) = throw IllegalStateException("Not Implemented")
    },

    /**
     * Mixed Java and Kotlin generator (mix is based on randomized selection of language).
     */
    Mixed {
        override fun initialize(
                params: GenerationParameters,
                random: Random
        ) = throw IllegalStateException("Not Implemented")
    };

    abstract fun initialize(
            params: GenerationParameters,
            random: Random
    ): SourceGenerator
}