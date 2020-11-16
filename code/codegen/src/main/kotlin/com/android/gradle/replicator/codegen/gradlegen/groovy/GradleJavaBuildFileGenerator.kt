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
package com.android.gradle.replicator.codegen.gradlegen.groovy

import com.android.gradle.replicator.codegen.PrettyPrintStream
import com.android.gradle.replicator.codegen.gradlegen.BuildFileGenerator

class GradleJavaBuildFileGenerator: BuildFileGenerator {
    override fun generate(dependencies: List<String>, printer: PrettyPrintStream) {
        printer.addBlock("plugins {")
        printer.printlnIndented("id 'java'\n")
        printer.endBlock()
        printer.println()
        printer.addBlock("repositories { ")
        printer.printlnIndented("jcenter()")
        printer.endBlock()
        printer.addBlock("dependencies {")
        dependencies.forEach {
            printer.printlnIndented("implementation '$it'")
        }
        printer.endBlock()
    }
}