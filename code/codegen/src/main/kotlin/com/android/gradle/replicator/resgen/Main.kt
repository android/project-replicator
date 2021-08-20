/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.AndroidResourcesInfo
import com.android.gradle.replicator.model.SourceFilesInfo
import com.android.gradle.replicator.model.internal.*
import com.android.gradle.replicator.model.internal.resources.AndroidResourceMap
import com.android.gradle.replicator.parsing.ArgsParser
import com.android.gradle.replicator.resgen.util.ResgenConstants
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import kotlin.random.Random


@Suppress("UNUSED_PARAMETER")
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val main= Main()
            if (args.size == 1 && args[0] == "-usage") {
                main.usage()
                return
            }
            main.process(args)
        }
    }
    var random: Random = Random(10)

    fun process(args: Array<String>) {

        val parser = ArgsParser()

        val androidOutputFolderOption = parser.option(longName = "androidOutput", shortName = "ao", argc = 1)
        val javaOutputFolderOption = parser.option(longName = "javaOutput", shortName = "jo", argc = 1)
        val resJsonOption = parser.option(longName = "resJson", shortName = "rj", argc = 1)
        val resgenConstantsFile = parser.option(longName = "generationProperties", shortName = "gp", argc = 1)
        val seedOption = parser.option(longName = "seed", shortName = "s", argc = 1)

        parser.parseArgs(args)

        val argumentsBuilder = ResourceGenerationParameters.Builder()

        resJsonOption.orNull?.first?.let { path ->
            File(path).let {
                if (!it.exists()) {
                    throw FileNotFoundException(it.toString())
                }
                val metadata = loadModuleMetadata(it)
                argumentsBuilder.setNumberOfAndroidResources(metadata.androidResources)
                argumentsBuilder.setNumberOfJavaResources(metadata.javaResources)
            }
        }
        seedOption.orNull?.first?.let {
            argumentsBuilder.setSeed(it.toInt())
        }

        val androidOutputFolder = File(checkNotNull(androidOutputFolderOption.orNull?.first))
        androidOutputFolder.deleteRecursively()
        androidOutputFolder.mkdirs()
        println("Generating android resources in $androidOutputFolder")

        val javaOutputFolder = File(checkNotNull(javaOutputFolderOption.orNull?.first))
        javaOutputFolder.deleteRecursively()
        javaOutputFolder.mkdirs()
        println("Generating java resources in $javaOutputFolder")

        val arguments = argumentsBuilder.build()

        val resgenConstants = ResgenConstants(resgenConstantsFile.orNull?.first?.let { File(it) })

        if (countResources(arguments.androidResourcesMap) > 0) {
            generateAndroidResources(
                    arguments.androidResourcesMap,
                    arguments,
                    androidOutputFolder,
                    resgenConstants
            )
        }
        if (arguments.numberOfJavaResources > 0) {
            generateJavaResources(
                    arguments.numberOfJavaResources,
                    arguments,
                    javaOutputFolder
            )
        }
    }

    private fun generateAndroidResources(
            resMap: AndroidResourceMap,
            parameters: ResourceGenerationParameters,
            outputFolder: File,
            resgenConstants: ResgenConstants) {
        resMap.forEach { resourceType ->
            val random = Random(parameters.seed)
            val generator = GeneratorDriver(random)
            resourceType.value.forEach { resourceProperties ->
                generator.generateResources(outputFolder, resourceType.key, resourceProperties, resgenConstants)
            }
        }
    }

    private fun generateJavaResources(
            numberOfResources: Int,
            parameters: ResourceGenerationParameters,
            outputFolder: File) {
        // To be implemented
        return
    }

    private data class ResourceMetadata (
            val androidResources: AndroidResourceMap,
            val javaResources: Int)

    // read metadata file added to each project in json format
    private fun loadModuleMetadata(resourceMetadataJson: File): ResourceMetadata {
        var androidResources: AndroidResourcesInfo = DefaultAndroidResourcesInfo(mutableMapOf())
        var javaResources: SourceFilesInfo = DefaultSourceFilesInfo(0)

        with(JsonReader(Files.newBufferedReader(resourceMetadataJson.toPath()))) {
            beginObject()
            while(hasNext()) {
                when (nextName()) {
                    "androidResources" -> androidResources = AndroidResourcesAdapter().read(this)
                    "javaResources" -> javaResources = SourceFilesAdapter().read(this)
                }
            }
            endObject()
        }

        return ResourceMetadata(androidResources.resourceMap, javaResources.fileCount)
    }

    private fun countResources(res: AndroidResourceMap): Int {
        var count = 0
        res.forEach { folder ->
            folder.value.forEach { resourceType ->
                count += resourceType.quantity
            }
        }
        return count
    }

    fun usage() {
        println("usage: Main <args>")
        println("\t-s/--seed : seed value for the randomizer")
        println("\t-rj/--resJson : classpath for all private (implementation) libraries, each element is a .jar file")
        println("\t-ao/--androidOutput : android resources output folder")
        println("\t-jo/--javaOutput : java resources output folder")
        println("\t-gp/--generationProperties : resource generation properties file")
    }
}