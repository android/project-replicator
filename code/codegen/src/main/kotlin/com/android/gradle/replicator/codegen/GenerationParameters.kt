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
 * Parameters for the generation
 */
data class GenerationParameters(
        /**
         * classpath available to import types from.
         */
        val classpath: List<File>,
        /**
         * Java Language level supported for the generation.
         */
        val javaLanguageLevel: String,

        /**
         * Parameters specific to class generation.
         */
        val classGenerationParameters: ClassGenerationParameters
) {
    class Builder {
        private val classpath = mutableListOf<File>()
        private var javaLanguageLevel: String? = null
        val classGenerationParametersBuilder = ClassGenerationParameters.Builder()


        fun addClasspathElement(element: File) {
            classpath.add(element)
        }

        fun setJavaLanguageLevel(languageLevel: String) {
            this.javaLanguageLevel = languageLevel
        }

        fun build(): GenerationParameters =
                GenerationParameters(
                        classpath = classpath.toList(),
                        javaLanguageLevel = javaLanguageLevel ?: "1.8",
                        classGenerationParameters = classGenerationParametersBuilder.build()
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
