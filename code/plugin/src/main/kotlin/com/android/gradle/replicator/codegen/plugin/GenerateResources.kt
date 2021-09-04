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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import kotlin.random.Random

abstract class GenerateResources: DefaultTask() {

    @get:InputFile
    abstract val parameters: RegularFileProperty

    @get:Input
    abstract val seed: Property<Int>

    @get:InputFile
    abstract val generationProperties: RegularFileProperty

    @get:OutputDirectory
    abstract val androidOutputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val javaOutputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val assetOutputDirectory: DirectoryProperty

    @TaskAction
    fun taskAction() {
        val randomizer = if (seed.isPresent) Random(seed.get()) else Random
        // generate files.
        Main().process(
                arrayOf(
                        "--resJson", parameters.get().asFile.absolutePath,
                        "--androidOutput", androidOutputDirectory.get().asFile.absolutePath,
                        "--javaOutput", javaOutputDirectory.get().asFile.absolutePath,
                        "--assetOutput", assetOutputDirectory.get().asFile.absolutePath,
                        "--generationProperties", generationProperties.get().asFile.absolutePath,
                        "--seed", randomizer.nextInt().toString()
                )
        )
        println("Done with $name")
    }
}
