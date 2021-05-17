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
package com.android.gradle.replicator.codegen.plugin

import com.android.gradle.replicator.resgen.Main
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import kotlin.random.Random

abstract class GenerateResources: DefaultTask() {

    @get:InputFile
    abstract val resourceGenParamFile: RegularFileProperty

    @get:Input
    abstract val seed: Property<Int>

    @get:OutputDirectory
    abstract val androidOutputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val javaOutputDirectory: DirectoryProperty

    @TaskAction
    fun taskAction() {
        val randomizer = if (seed.isPresent) Random(seed.get()) else Random
        // generate files.
        Main().process(
                arrayOf(
                        "-resjson", resourceGenParamFile.get().asFile.absolutePath,
                        "-ao", androidOutputDirectory.get().asFile.absolutePath,
                        "-jo", javaOutputDirectory.get().asFile.absolutePath,
                        "-seed", randomizer.nextInt().toString()
                )
        )
        println("Done with $name")
    }
}
