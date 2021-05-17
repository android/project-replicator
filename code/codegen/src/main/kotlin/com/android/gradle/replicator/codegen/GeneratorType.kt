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

import com.android.gradle.replicator.codegen.java.JavaClassGenerator
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
        override fun initialize(params: CodeGenerationParameters) =
            GeneratorDriver(params, Random(params.seed)) { printer, listeners ->
                KotlinClassGenerator(printer, listeners)
            }

        override fun classNameToSourceFileName(className: String) = "$className.kt"
    },

    /**
     * Java [SourceGenerator]
     */
    Java {
        override fun initialize(
                params: CodeGenerationParameters
        ) = GeneratorDriver(params, Random(params.seed)) { printer, listeners ->
            JavaClassGenerator(printer, listeners)
        }

        override fun classNameToSourceFileName(className: String) = "${className}.java"
    },

    /**
     * Mixed Java and Kotlin generator (mix is based on randomized selection of language).
     */
    Mixed {
        override fun initialize(
                params: CodeGenerationParameters
        ) = throw IllegalStateException("Not Implemented")

        override fun classNameToSourceFileName(className: String): String {
            TODO("Not yet implemented")
        }
    };

    abstract fun initialize(
            params: CodeGenerationParameters
    ): SourceGenerator

    abstract fun classNameToSourceFileName(className: String): String
}