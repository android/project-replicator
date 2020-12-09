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
import java.lang.IllegalArgumentException
import java.util.Properties
import kotlin.random.Random

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

        val argumentsBuilder = GenerationParameters.Builder()
        val pathToArgumentsFile = parsedArguments["-i"]
        if (pathToArgumentsFile!=null
            && pathToArgumentsFile.size > 0
            && File(pathToArgumentsFile[0]).exists()) {
            parseArgumentFile(File(pathToArgumentsFile[0]), argumentsBuilder)
        } else {
            parseArguments(parsedArguments, argumentsBuilder)
        }
        var generatorType: GeneratorType = GeneratorType.Kotlin
        parsedArguments["-gen"]?.let {
            if (it.size != 1) throw IllegalArgumentException("Only one generator supported per invocation.")
            generatorType = GeneratorType.valueOf(it.first())
        }

        val outputFolder = File(checkNotNull(parsedArguments["-o"]).first())
        outputFolder.deleteRecursively()
        outputFolder.mkdirs()
        println("Generating in $outputFolder")

        val generator = generatorType.initialize(argumentsBuilder.build(), Random)
        repeat(100) { count ->
            val className = "Class" + ('A'+ count/(26*26)) + ('A'+ (count/26)%26) + ('A'+ count%26)
            val sourceFolder = File(outputFolder, "com/android/example")
            sourceFolder.mkdirs()
            val outputFile = File(sourceFolder, "$className.kt")
            println("Generating kotlin source ${outputFile.absolutePath}")
            PrintStream(outputFile).use {
                generator.generateClass(
                        packageName = "com.android.example",
                        className = className,
                        printStream = PrettyPrintStream(it),
                        listeners = listOf())
            }
        }
    }

    private fun foo(foo:String) {
        println("foo")
    }

    private fun parseArguments(arguments: Map<String, List<String>>, parametersBuilder: GenerationParameters.Builder) {
        arguments["-cp"]?.forEach { path ->
            File(path).let {
                if (!it.exists()) {
                    throw FileNotFoundException(path)
                }
                parametersBuilder.addClasspathElement(it)
            }
        }
    }

    private fun parseArgumentFile(argumentsFile: File, parametersBuilder: GenerationParameters.Builder) {
        val arguments = FileReader(argumentsFile).use {
            Properties().also { properties -> properties.load(it) }
        }
        arguments["classpath"]?.toString()?.split(",")?.forEach {
            parametersBuilder.addClasspathElement(File(it))
        }
    }

    fun usage() {
        println("usage: Main <args>")
        println("\t-gen : type of generated [Kotlin, Java, Mixed]" )
        println("\t-cp : classpath for all libraries, each element is a .jar file")
    }
}