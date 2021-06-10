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

import com.google.gson.Gson
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Files
import kotlin.random.Random

abstract class GenerateCodegenParamsTask: DefaultTask() {

    @get:Classpath
    abstract val runtimeClasspath: ConfigurableFileCollection

    @get:Classpath
    abstract val codeGeneratedModuleApiClasspath: ConfigurableFileCollection

    @get:Classpath
    abstract val codeGeneratedModuleImplClasspath: ConfigurableFileCollection

    @get:Classpath
    abstract val apiJarFiles: ConfigurableFileCollection

    @get:Classpath
    abstract val implJarFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val paramsFile: RegularFileProperty

    @get:Input
    abstract val gradleDependencies: ListProperty<String>

    @get:InputFile
    abstract val moduleMetadataJson: RegularFileProperty

    @get:Input
    abstract val seed: Property<Int>

    @TaskAction
    fun action() {
        val outputFile = paramsFile.get().asFile
        println("Writing codegen params to ${outputFile.absolutePath}")
        val randomizer = if (seed.isPresent) Random(seed.get()) else Random

        val metadata = loadModuleMetadata(moduleMetadataJson.get().asFile)

        // each module will get a random seed.
        outputFile.writeText(
                """
                    seed=${randomizer.nextInt()}
                    runtimeClasspath=${runtimeClasspath.files.joinToString(separator = ",") { it.absolutePath }}
                    codeGeneratedModuleApiClasspath=${codeGeneratedModuleApiClasspath.files.joinToString(separator = ",") { it.absolutePath }}
                    codeGeneratedModuleImplClasspath=${codeGeneratedModuleImplClasspath.files.joinToString(separator = ",") { it.absolutePath }}
                    apiClasspath=${apiJarFiles.files.joinToString(separator = ",") { it.absolutePath }}
                    implClasspath=${implJarFiles.files.joinToString(separator = ",") { it.absolutePath }}
                    dependencies=${gradleDependencies.get().joinToString(separator = ",")}
                    nbOfKotlinFiles=${metadata.kotlinSources}
                    nbOfJavaFiles=${metadata.javaSources}
                """.trimIndent()
        )
    }

    private data class ModuleMetadata (
            val javaSources: Int,
            val kotlinSources: Int)

    // read metadata file added to each project in json format
    private fun loadModuleMetadata(moduleMetadataJson: File): ModuleMetadata {
        val gson = Gson()
        var moduleMetadata: ModuleMetadata

        with(Files.newBufferedReader(moduleMetadataJson.toPath())) {
            moduleMetadata = gson.fromJson(this, ModuleMetadata::class.java)
        }

        return moduleMetadata
    }
}
