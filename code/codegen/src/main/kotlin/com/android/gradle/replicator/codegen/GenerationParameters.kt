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

import java.io.File

/**
 * Parameters for the generation for a module.
 */
data class GenerationParameters(
        /**
         * Runtime classpath for the module we generate code for. The runtime classpath should guarantee that any
         * classes imported by this module should be able to loaded successfully as it represents the transitive
         * dependencies of this module.
         */
        val runtimeClasspath: List<File>,
        /**
         * Compile classpath available to import public types from. This represents the 'api' configuration of this
         * module and types loaded from this classpath can be used as part of the public methods signatures of fields.
         * Types loaded from this classpath will undergo a 'verification' step to ensure they are suitable for use
         * in this code generator (have a usable constructor, public methods with usable parameter types, etc..)
         */
        val apiClasspath: List<File>,

        /**
         * Compile classpath available to import private types from. This represents the 'implementation' configuration
         * of this module and types loaded from this classpath can only be used in method implementations or private
         * methods signatures.
         * Types loaded from this classpath will undergo a 'verification' step to ensure they are suitable for use
         * in this code generator (have a usable constructor, public methods with usable parameter types, etc..)
         */
        val implClasspath: List<File>,

        /**
         * Since all generated types and methods are safe to be imported and use for this module code generation,
         * separate the 'api' configuration classpath with code generated modules to bypass verification.
         */
        val codeGeneratedModuleApiClasspath: List<File>,

        /**
         * Since all generated types and methods are safe to be imported and use for this module code generation,
         * separate the 'implementation' configuration classpath with code generated modules to bypass verification.
         */
        val codeGeneratedModuleImplClasspath: List<File>,

        /**
         * Java Language level supported for the generation.
         */
        val javaLanguageLevel: String,

        /**
         * Parameters specific to class generation.
         */
        val classGenerationParameters: ClassGenerationParameters,

        /**
         * Seed value for the randomizer
         */
        val seed: Int
) {
    class Builder {
        private val runtimeClasspath = mutableListOf<File>()
        private val apiClasspath = mutableListOf<File>()
        private val implClasspath = mutableListOf<File>()
        private val codeGeneratedModuleApiClasspath = mutableListOf<File>()
        private val codeGeneratedModuleImplClasspath = mutableListOf<File>()
        private var javaLanguageLevel: String? = null
        private val classGenerationParametersBuilder = ClassGenerationParameters.Builder()
        private var seed = 1


        fun addApiClasspathElement(element: File) {
            apiClasspath.add(element)
        }

        fun addCodeGeneratedModuleApiClasspathElement(element: File) {
            codeGeneratedModuleApiClasspath.add(element)
        }

        fun addCodeGeneratedModuleImplClasspathElement(element: File) {
            codeGeneratedModuleImplClasspath.add(element)
        }

        fun addImplClasspathElement(element: File) {
            implClasspath.add(element)
        }

        fun addRuntimeClasspathElement(file: File) {
            runtimeClasspath.add(file)
        }

        fun setSeed(seed: Int) { this.seed = seed }


        fun setJavaLanguageLevel(languageLevel: String) {
            this.javaLanguageLevel = languageLevel
        }

        fun build(): GenerationParameters =
                GenerationParameters(
                        runtimeClasspath = runtimeClasspath.toList(),
                        apiClasspath = apiClasspath.toList(),
                        codeGeneratedModuleApiClasspath = codeGeneratedModuleApiClasspath.toList(),
                        codeGeneratedModuleImplClasspath = codeGeneratedModuleImplClasspath.toList(),
                        implClasspath = implClasspath.toList(),
                        javaLanguageLevel = javaLanguageLevel ?: "1.8",
                        classGenerationParameters = classGenerationParametersBuilder.build(),
                        seed = seed
                )
    }
}

data class ClassGenerationParameters(
        // min number of instance variables
        val minNumberOfInstanceVars: Int,
        // max number of instance variables
        val maxNumberOfInstanceVars: Int,
        // min number of methods to generate
        val minNumberOfMethods: Int,
        // max number of methods of generate
        val maxNumberOfMethods: Int,

        // max number of parameters for method.
        val maxNumberOfMethodParameters: Int,
        // min number of code blocks in method declaration.
        val minNumberOfMethodBlocks: Int,
        // max number of code blocks in method declaration.
        val maxNumberOfMethodBlocks: Int,

        // max number of blocks in if() condition
        val maxNumberOfBlocksInIf: Int,
        // max number of blocks in ifElse() condition
        val maxNumberOfBlocksInIfElse: Int,

        // max number of blocks in lambda declaration.
        val maxNumberOfBlocksInLambda: Int,

        // max number of blocks in loop
        val maxNumberOfBlocksInLoop: Int
) {
    class Builder(
            var minNumberOfInstanceVars: Int = 1,
            var maxNumberOfInstanceVars: Int = 5,
            var minNumberOfMethods: Int = 1,
            var maxNumberOfMethods: Int = 5,
            var maxNumberOfMethodParameters: Int = 4,
            var minNumberOfMethodBlocks: Int = 0,
            var maxNumberOfMethodBlocks: Int = 5,
            var maxNumberOfBlocksInIf: Int = 2,
            var maxNumberOfBlocksInIfElse: Int = 2,
            var maxNumberOfBlocksInLambda: Int = 2,
            var maxNumberOfBlocksInLoop: Int = 4
    ) {

        fun build(): ClassGenerationParameters =
                ClassGenerationParameters(
                        minNumberOfInstanceVars = minNumberOfInstanceVars,
                        maxNumberOfInstanceVars = maxNumberOfInstanceVars,
                        minNumberOfMethods = minNumberOfMethods,
                        maxNumberOfMethods = maxNumberOfMethods,
                        maxNumberOfMethodParameters = maxNumberOfMethodParameters,
                        minNumberOfMethodBlocks = minNumberOfMethodBlocks,
                        maxNumberOfMethodBlocks = maxNumberOfMethodBlocks,
                        maxNumberOfBlocksInIf = maxNumberOfBlocksInIf,
                        maxNumberOfBlocksInIfElse = maxNumberOfBlocksInIfElse,
                        maxNumberOfBlocksInLambda = maxNumberOfBlocksInLambda,
                        maxNumberOfBlocksInLoop = maxNumberOfBlocksInLoop
                )
    }
}
