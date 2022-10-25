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

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import com.android.gradle.replicator.codegen.Main

abstract class GenerateCode: DefaultTask() {

    @get:InputFile
    abstract val parameters: RegularFileProperty

    @get:InputFile
    abstract val resourceModelFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun taskAction() {
        // generate files.
        Main().process(
                arrayOf(
                        "--module", path.removeSuffix(":$name").removePrefix(":").replace(':', '_'),
                        "--resModel", resourceModelFile.get().asFile.absolutePath,
                        "-i", parameters.get().asFile.absolutePath,
                        "-o", outputDirectory.get().asFile.absolutePath
                )
        )
        println("Done with $name")
    }
}