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

import com.google.gson.Gson
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
        var lastKey = ""
        val parsedArguments = args.fold(mutableMapOf()) { acc: MutableMap<String, MutableList<String>>, s: String ->
            acc.apply {
                if (s.startsWith('-')) {
                    this[s] = mutableListOf()
                    lastKey = s
                }
                else this[lastKey]?.add(s)
            }
        }

        val argumentsBuilder = ResourceGenerationParameters.Builder()
        parseArguments(parsedArguments, argumentsBuilder)

        val androidOutputFolder = File(checkNotNull(parsedArguments["-ao"]).first())
        androidOutputFolder.deleteRecursively()
        androidOutputFolder.mkdirs()
        println("Generating android resources in $androidOutputFolder")

        val javaOutputFolder = File(checkNotNull(parsedArguments["-jo"]).first())
        javaOutputFolder.deleteRecursively()
        javaOutputFolder.mkdirs()
        println("Generating java resources in $javaOutputFolder")

        val arguments = argumentsBuilder.build()

        if (countResources(arguments.numberOfAndroidResources) > 0) {
            generateAndroidResources(
                    arguments.numberOfAndroidResources,
                    arguments,
                    androidOutputFolder
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

    // TODO: implement other generators
    private fun getGenerator(random: Random, folderType: String): ResourceGenerator {
        return when(folderType) {
            //"animator" ->
            //"anim" ->
            //"color" ->
            //"drawable" ->
            //"font" ->
            //"layout" ->
            //"menu" ->
            //"mipmap" ->
            //"raw" ->
            //"transition" ->
            "values" -> ValueResourceGenerator(random)
            //"xml" ->
            else -> throw RuntimeException("Unsupported resource type $folderType")
        }
    }

    private fun generateAndroidResources(
            resMap: AndroidResourceMap,
            parameters: ResourceGenerationParameters,
            outputFolder: File) {
        resMap.forEach { folder ->
            val random = Random(parameters.seed)
            val generator = getGenerator(random, folder.key)
            folder.value.forEach { qualifier ->
                // empty qualifiers means the folder is unqualified, as in "mipmap" instead of "mipmap-hidpi"
                val qualifiedFolder =
                        if (qualifier.key.isEmpty()) {
                            File(outputFolder, folder.key)
                        } else {
                            File(outputFolder, "${folder.key}-${qualifier.key}")
                        }
                qualifiedFolder.mkdirs()

                qualifier.value.forEach { extension ->
                    generator.generateResource(
                            number = extension.value,
                            outputFolder = qualifiedFolder,
                            resourceQualifier = qualifier.key,
                            resourceExtension = extension.key
                    )
                }
            }
        }
    }

    private fun generateJavaResources(
            numberOfResources: Int,
            parameters: ResourceGenerationParameters,
            outputFolder: File) {
        repeat(numberOfResources) { count ->
        }
        return
    }

    private fun parseArguments(arguments: Map<String, List<String>>, parametersBuilder: ResourceGenerationParameters.Builder) {
        arguments["-resjson"]?.first().let { path ->
            File(path!!).let {
                if (!it.exists()) {
                    throw FileNotFoundException(path)
                }
                val metadata = loadModuleMetadata(it)
                parametersBuilder.setNumberOfAndroidResources(metadata.androidResources)
                parametersBuilder.setNumberOfJavaResources(metadata.javaResources)
            }
        }
        arguments["-seed"]?.first()?.let { seed ->
            parametersBuilder.setSeed(seed.toInt())
        }
    }

    private data class ResourceMetadata (
            val androidResources: AndroidResourceMap,
            val javaResources: Int)

    // read metadata file added to each project in json format
    private fun loadModuleMetadata(resourceMetadataJson: File): ResourceMetadata {
        val gson = Gson()
        var resourceMetadata: ResourceMetadata

        with(Files.newBufferedReader(resourceMetadataJson.toPath())) {
            resourceMetadata = gson.fromJson(this, ResourceMetadata::class.java)
        }

        return resourceMetadata
    }

    private fun countResources(res: AndroidResourceMap): Int {
        var count = 0
        res.forEach { folder ->
            folder.value.forEach { qualifier ->
                qualifier.value.forEach { extension ->
                    count += extension.value
                }
            }
        }
        return count
    }

    fun usage() {
        println("usage: Main <args>")
        println("\t-seed : seed value for the randomizer")
        println("\t-resjson : classpath for all private (implementation) libraries, each element is a .jar file")
        println("\t-ao : android resources output folder")
        println("\t-jo : java resources output folder")
    }
}