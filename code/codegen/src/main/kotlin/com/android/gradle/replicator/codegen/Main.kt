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

import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.PrintStream
import java.util.Properties

fun main(args: Array<String>) {
    val main= Main()
    if (args.size == 1 && args[0] == "-usage") main.usage()
    main.process(args)
}

@Suppress("UNUSED_PARAMETER")
class Main {

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

        val argumentsBuilder = CodeGenerationParameters.Builder()
        val pathToArgumentsFile = parsedArguments["-i"]
        if (pathToArgumentsFile!=null
            && pathToArgumentsFile.size > 0
            && File(pathToArgumentsFile[0]).exists()) {
            parseArgumentFile(File(pathToArgumentsFile[0]), argumentsBuilder)
        } else {
            parseArguments(parsedArguments, argumentsBuilder)
        }
        val kotlinGenerator: GeneratorType = GeneratorType.Kotlin
        val javaGenerator = GeneratorType.Java

        val outputFolder = File(checkNotNull(parsedArguments["-o"]).first())
        outputFolder.deleteRecursively()
        outputFolder.mkdirs()
        println("Generating in $outputFolder")

        val arguments = argumentsBuilder.build()
        val moduleName = parsedArguments["-module"]?.first() ?: "module"

        if (arguments.numberOfJavaSources > 0) {
            generateSources(
                    arguments.numberOfJavaSources,
                    javaGenerator,
                    arguments,
                    moduleName,
                    outputFolder
            )
        }
        if (arguments.numberOfKotlinSources > 0) {
            generateSources(
                    arguments.numberOfKotlinSources,
                    kotlinGenerator,
                    arguments,
                    moduleName,
                    outputFolder
            )
        }
    }

    private fun generateSources(
            numberOfSources: Int,
            generatorType: GeneratorType,
            parameters: CodeGenerationParameters,
            moduleName: String,
            outputFolder: File) {
        val generator = generatorType.initialize(parameters)
        repeat(numberOfSources) { count ->
            val className = "Class" + ('A'+ count/(26*26)) + ('A'+ (count/26)%26) + ('A'+ count%26)
            val sourceFolder = File(outputFolder, "com/android/example/$moduleName")
            sourceFolder.mkdirs()
            val outputFile = File(sourceFolder, generatorType.classNameToSourceFileName(className))
            println("Generating ${generatorType.name} source ${outputFile.absolutePath}")
            PrintStream(outputFile).use {
                generator.generateClass(
                        packageName = "com.android.example.${moduleName}",
                        className = className,
                        printStream = PrettyPrintStream(it),
                        listeners = listOf())
            }
        }
    }

    private fun parseArguments(arguments: Map<String, List<String>>, parametersBuilder: CodeGenerationParameters.Builder) {
        arguments["-cp"]?.forEach { path ->
            File(path).let {
                if (!it.exists()) {
                    throw FileNotFoundException(path)
                }
                parametersBuilder.addImplClasspathElement(it)
            }
        }
        arguments["-api"]?.forEach { path ->
            File(path).let {
                if (!it.exists()) {
                    throw FileNotFoundException(path)
                }
                parametersBuilder.addApiClasspathElement(it)
            }
        }
    }

    private fun parseArgumentFile(argumentsFile: File, parametersBuilder: CodeGenerationParameters.Builder) {
        val arguments = FileReader(argumentsFile).use {
            Properties().also { properties -> properties.load(it) }
        }
        arguments["apiClasspath"]?.toString()?.split(",")?.forEach {
            parametersBuilder.addApiClasspathElement(File(it))
        }
        arguments["implClasspath"]?.toString()?.split(",")?.forEach {
            parametersBuilder.addImplClasspathElement(File(it))
        }
        arguments["codeGeneratedModuleApiClasspath"]?.toString()?.split(",")?.forEach {
            parametersBuilder.addCodeGeneratedModuleApiClasspathElement(File(it))
        }
        arguments["codeGeneratedModuleImplClasspath"]?.toString()?.split(",")?.forEach {
            parametersBuilder.addCodeGeneratedModuleImplClasspathElement(File(it))
        }
        arguments["runtimeClasspath"]?.toString()?.split(",")?.forEach {
            parametersBuilder.addRuntimeClasspathElement(File(it))
        }
        arguments["seed"]?.also {
            parametersBuilder.setSeed((it as String).toInt())
        }
        arguments["nbOfJavaFiles"]?.also {
            parametersBuilder.setNumberOfJavaSources((it as String).toInt())
        }
        arguments["nbOfKotlinFiles"]?.also {
            parametersBuilder.setNumberOfKotlinSources((it as String).toInt())
        }
    }

    fun usage() {
        println("usage: Main <args>")
        println("\t-seed : seed value for the randomizer")
        println("\t-cp : classpath for all private (implementation) libraries, each element is a .jar file")
        println("\t-api : classpath for all public (api) libraries, each element is a .jar file")
    }
}