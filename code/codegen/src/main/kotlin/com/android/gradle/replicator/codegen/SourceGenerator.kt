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

/**
 * A SourceGenerator is capable of generating various types of source files for a particular language.
 *
 * Eventually, we should support generating Classes, Interfaces, Enums, Data classes.
 */
interface SourceGenerator {
    /**
     * Generate a class
     * @param packageName the class package.
     * @param className the class name.
     * @param printStream the stream to write the source code to.
     * @param listeners listeners for code generation events.
     */
    fun generateClass(
            packageName: String,
            className: String,
            printStream: PrettyPrintStream,
            listeners: List<CodeGenerationListener> = listOf()
    )
}