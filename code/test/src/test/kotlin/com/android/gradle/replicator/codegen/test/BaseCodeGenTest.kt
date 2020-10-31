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
package com.android.gradle.replicator.codegen.test

import com.android.gradle.replicator.codegen.gradlegen.groovy.GradleKotlinBuildFileGenerator
import com.android.gradle.replicator.codegen.PrettyPrintStream
import java.io.File
import java.io.PrintStream

open class BaseCodeGenTest(
        val projectDir: ()->File,
        val target: Target,
        val type: Type) {

    enum class Target { KOTLIN, JAVA, MIXED }

    enum class Type { GROOVY, KTS }

    protected val kotlinSourceFolder by lazy {
        File(projectDir(), "src/main/kotlin").also {
            it.mkdirs()
        }
    }

    protected fun generateProject(dependencies: List<String>) {
        when(type) {
            Type.GROOVY -> {
                PrintStream(File(projectDir(), "build.gradle")).use {
                    GradleKotlinBuildFileGenerator().generate(dependencies, PrettyPrintStream((it)))
                }
                PrintStream(File(projectDir(), "settings.gradle")).use {
                    it.println()
                }
            }
            else -> throw RuntimeException("Not implemented")
        }
    }

    protected fun addKotlinSource(name: String): File = File(kotlinSourceFolder, name)
}