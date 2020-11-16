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
package com.android.gradle.replicator.codegen.test

import com.android.gradle.replicator.codegen.Main
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.io.FileReader
import java.util.*

@RunWith(Parameterized::class)
class MainTest(target: Target): BaseCodeGenTest(
        target,
        BuildScriptType.Groovy) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "target = {0}")
        fun testParameters(): Array<Target> = arrayOf(Target.Kotlin, Target.Java)


    }

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun simpleTest() {
        val parameterFile = System.getProperty("parameter.file")
        val sourceFolder = File(projectDir, "src/main/${target.toString().toLowerCase()}").also {
            it.mkdirs()
        }
        println("reading $parameterFile")
        val arguments = FileReader(parameterFile).use {
            Properties().also { properties -> properties.load(it) }
        }
        val dependencies = arguments["dependencies"]?.toString()?.split(",") ?: listOf()

        generateProject(projectDir, dependencies)

        // generate one file.
        Main().process(
            arrayOf(
                "-gen", target.toString(),
                "-i", System.getProperty("parameter.file"),
                "-o", sourceFolder.absolutePath
            )
        )
        val gradleRunner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("assemble")
            .forwardOutput()

        gradleRunner.build()
    }

    private val projectDir =
            File(File(System.getProperty("parameter.file")).parentFile.parentFile, "mainTest/$target").also {
                it.mkdirs()
            }
}