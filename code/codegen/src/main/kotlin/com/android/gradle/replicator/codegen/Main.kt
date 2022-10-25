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

import com.android.gradle.replicator.model.AndroidResourcesInfo
import com.android.gradle.replicator.model.FilesWithSizeMetadataInfo
import com.android.gradle.replicator.model.internal.AndroidResourcesAdapter
import com.android.gradle.replicator.model.internal.DefaultAndroidResourcesInfo
import com.android.gradle.replicator.model.internal.DefaultFilesWithSizeMetadataInfo
import com.android.gradle.replicator.model.internal.FilesWithSizeMetadataAdapter
import com.android.gradle.replicator.parsing.ArgsParser
import com.android.gradle.replicator.resourceModel.ResourceModel
import com.android.gradle.replicator.resourceModel.ResourceModelAdapter
import com.google.gson.stream.JsonReader
import java.io.File
import java.io.PrintStream
import java.nio.file.Files

fun main(args: Array<String>) {
    val main= Main()
    if (args.size == 1 && args[0] == "-usage") main.usage()
    main.process(args)
}

@Suppress("UNUSED_PARAMETER")
class Main {

    fun process(args: Array<String>) {

        val parser = ArgsParser()

        val pathToArgumentsFileOption = parser.option(longName = "argsFile", shortName = "i", argc = 1)
        val resourceModelFileOption = parser.option(longName = "resModel", shortName = "rm", argc = 1)
        val outputFolderOption = parser.option(longName = "outputFolder", shortName = "o", argc = 1)
        val moduleOption = parser.option(longName = "module", shortName = "m", argc = 1)
        val implClasspathElementOption = parser.option(
                longName = "implClassPath",
                shortName = "cp",
                propertyName = "implClasspath",
                argc = ArgsParser.UNLIMITED_ARGC)
        val apiOption = parser.option(
                longName = "apiClassPath",
                shortName = "api",
                propertyName = "apiClasspath",
                argc = ArgsParser.UNLIMITED_ARGC)
        val codeGenModuleApiClasspathOption = parser.option(
                propertyName = "codeGeneratedModuleApiClasspath",
                argc = ArgsParser.UNLIMITED_ARGC)
        val codeGenModuleImplClasspathOption = parser.option(
                propertyName = "codeGeneratedModuleImplClasspath",
                argc = ArgsParser.UNLIMITED_ARGC)
        val runtimeClasspathOption = parser.option(propertyName = "runtimeClasspath", argc = ArgsParser.UNLIMITED_ARGC)

        val seedOption = parser.option(longName = "seed", shortName = "s", propertyName = "seed", argc = 1)
        val nbOfJavaFilesOption = parser.option(propertyName = "nbOfJavaFiles", argc = 1)
        val nbOfKotlinFilesOption = parser.option(propertyName = "nbOfKotlinFiles", argc = 1)

        parser.parseArgs(args)

        val parametersBuilder = CodeGenerationParameters.Builder()
        val pathToArgumentsFile = pathToArgumentsFileOption.orNull?.asFile
        if (pathToArgumentsFile != null
            && pathToArgumentsFile.exists()) {
            parser.parsePropertyFile(pathToArgumentsFile)
        }
        val resourceModelFile = resourceModelFileOption.orNull?.asFile
        val resourceModel =
            if (resourceModelFile != null
                && resourceModelFile.exists()) {
                loadResourceModel(resourceModelFile)
            } else {
                ResourceModel()
            }
        buildParameters(
                parametersBuilder,
                apiOption.orNull?.argv,
                implClasspathElementOption.orNull?.argv,
                codeGenModuleApiClasspathOption.orNull?.argv,
                codeGenModuleImplClasspathOption.orNull?.argv,
                runtimeClasspathOption.orNull?.argv,
                seedOption.orNull?.first,
                nbOfJavaFilesOption.orNull?.first,
                nbOfKotlinFilesOption.orNull?.first
        )
        val kotlinGenerator: GeneratorType = GeneratorType.Kotlin
        val javaGenerator = GeneratorType.Java

        val outputFolder = outputFolderOption.asFile
        outputFolder.deleteRecursively()
        outputFolder.mkdirs()
        println("Generating in $outputFolder")

        val arguments = parametersBuilder.build()
        val moduleName = moduleOption.orNull?.first ?: "module"

        if (arguments.numberOfJavaSources > 0) {
            generateSources(
                    arguments.numberOfJavaSources,
                    javaGenerator,
                    arguments,
                    moduleName,
                    resourceModel,
                    outputFolder
            )
        }
        if (arguments.numberOfKotlinSources > 0) {
            generateSources(
                    arguments.numberOfKotlinSources,
                    kotlinGenerator,
                    arguments,
                    moduleName,
                    resourceModel,
                    outputFolder
            )
        }
    }

    private fun generateSources(
            numberOfSources: Int,
            generatorType: GeneratorType,
            parameters: CodeGenerationParameters,
            moduleName: String,
            resourceModel: ResourceModel,
            outputFolder: File) {
        val generator = generatorType.initialize(parameters)
        repeat(numberOfSources) { count ->
            val suffix = if (generatorType == GeneratorType.Java) "Java" else ""
            val className = "Class" + ('A'+ count/(26*26)) + ('A'+ (count/26)%26) + ('A'+ count%26) + suffix
            val sourceFolder = File(outputFolder, "com/android/example/$moduleName")
            sourceFolder.mkdirs()
            val outputFile = File(sourceFolder, generatorType.classNameToSourceFileName(className))
            println("Generating ${generatorType.name} source ${outputFile.absolutePath}")
            PrintStream(outputFile).use {
                generator.generateClass(
                        moduleName = "com.android.example.${moduleName}",
                        packageName = "com.android.example.${moduleName}",
                        className = className,
                        printStream = PrettyPrintStream(it),
                        listeners = listOf(),
                        resourceModel = resourceModel)
            }
        }
    }

    // read generated android resource model
    private fun loadResourceModel(resourceModelFile: File): ResourceModel {
        var resourceModel: ResourceModel

        with(JsonReader(Files.newBufferedReader(resourceModelFile.toPath()))) {
            resourceModel = ResourceModelAdapter().read(this)
        }

        return resourceModel
    }

    private fun buildParameters(
            parametersBuilder: CodeGenerationParameters.Builder,
            apiClasspath: List<String>?,
            implClasspath: List<String>?,
            codeGeneratedModuleApiClasspath: List<String>?,
            codeGeneratedModuleImplClasspath: List<String>?,
            runtimeClasspath: List<String>?,
            seed: String?,
            nbOfJavaFiles: String?,
            nbOfKotlinFiles: String?
    ) {
        apiClasspath?.forEach {
            parametersBuilder.addApiClasspathElement(File(it))
        }
        implClasspath?.forEach {
            parametersBuilder.addImplClasspathElement(File(it))
        }
        codeGeneratedModuleApiClasspath?.forEach {
            parametersBuilder.addCodeGeneratedModuleApiClasspathElement(File(it))
        }
        codeGeneratedModuleImplClasspath?.forEach {
            parametersBuilder.addCodeGeneratedModuleImplClasspathElement(File(it))
        }
        runtimeClasspath?.forEach {
            parametersBuilder.addRuntimeClasspathElement(File(it))
        }
        seed?.also {
            parametersBuilder.setSeed((it).toInt())
        }
        nbOfJavaFiles?.also {
            parametersBuilder.setNumberOfJavaSources((it).toInt())
        }
        nbOfKotlinFiles?.also {
            parametersBuilder.setNumberOfKotlinSources((it).toInt())
        }
    }

    fun usage() {
        println("usage: Main <args>")
        println("\t-seed : seed value for the randomizer")
        println("\t-cp : classpath for all private (implementation) libraries, each element is a .jar file")
        println("\t-api : classpath for all public (api) libraries, each element is a .jar file")
    }
}