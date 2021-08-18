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

package com.android.gradle.replicator

import org.gradle.testkit.runner.GradleRunner
import java.io.File

val GRADLE_VERSION = System.getenv("GRADLE_VERSION") ?: "failed to find Gradle version"
val KOTLIN_VERSION = System.getenv("KOTLIN_VERSION") ?: "failed to find Kotlin version"
val AGP_VERSION = System.getenv("AGP_VERSION") ?: "failed to find AGP version"

class ProjectSetup(
    val projectDir: File,
    val buildFile: File,
    val runner: GradleRunner
)

enum class BuildFileType {
    GROOVY, KTS
}

fun BuildFileType.settingsFile(): String {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    return when (this) {
        BuildFileType.KTS -> "settings.gradle.kts"
        BuildFileType.GROOVY -> "settings.gradle"
        else -> throw RuntimeException("unsupported '$this'")
    }
}

fun BuildFileType.buidFile(): String {
    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    return when (this) {
        BuildFileType.KTS -> "build.gradle.kts"
        BuildFileType.GROOVY -> "build.gradle"
        else -> throw RuntimeException("unsupported '$this'")
    }
}

data class PluginInfo(
    val id: String,
    val version: String? = null,
    val kotlin: Boolean = false,
    val apply: Boolean = true
)

/**
 * Sets up the project.
 *
 * DO NOT USE DEFAULT PARAM VALUES AS THIS BREAKS TRACE INSPECTION
 *
 * @param buildscript a provider for the content of the `buildscript` block
 */
fun setupProject(type: BuildFileType): ProjectSetup {
    // pass one since we are adding one method to the trace
    return setupProject(type = type, traceOffset = 1, plugins = listOf(), buildscript = null)
}

/**
 * Sets up the project.
 *
 * DO NOT USE DEFAULT PARAM VALUES AS THIS BREAKS TRACE INSPECTION
 *
 * @param buildscript a provider for the content of the `buildscript` block
 */
fun setupProject(type: BuildFileType, plugins: List<PluginInfo>): ProjectSetup {
    // pass one since we are adding one method to the trace
    return setupProject(type = type, traceOffset = 1, plugins = plugins, buildscript = null)
}

/**
 * Sets up the project.
 *
 * DO NOT USE DEFAULT PARAM VALUES AS THIS BREAKS TRACE INSPECTION
 *
 * @param buildscript a provider for the content of the `buildscript` block
 */
fun setupProject(type: BuildFileType, plugins: List<PluginInfo>, buildscript: () -> String): ProjectSetup {
    // pass one since we are adding one method to the trace
    return setupProject(type = type, traceOffset = 1, plugins = plugins, buildscript = buildscript)
}

/**
 * Sets up the project.
 *
 * DO NOT USE DEFAULT PARAM VALUES AS THIS BREAKS TRACE INSPECTION
 *
 * @param buildscript a provider for the content of the `buildscript` block
 */
fun setupProject(type: BuildFileType, buildscript: () -> String): ProjectSetup {
    // pass one since we are adding one method to the trace
    return setupProject(type = type, traceOffset = 1, plugins = listOf(), buildscript = buildscript)
}

/**
 * Sets up the project.
 *
 * DO NOT USE DEFAULT PARAM VALUES AS THIS BREAKS TRACE INSPECTION
 *
 * @param buildscript a provider for the content of the `buildscript` block
 */
fun setupProject(type: BuildFileType, traceOffset: Int, buildscript: () -> String): ProjectSetup {
    // pass one since we are adding one method to the trace
    return setupProject(type = type, traceOffset = traceOffset, plugins = listOf(), buildscript = buildscript)
}

/**
 * Sets up the project.
 *
 * DO NOT USE DEFAULT PARAM VALUES AS THIS BREAKS TRACE INSPECTION
 *
 * @param traceOffset indicate which caller's name is used for the folder name. 0 means the direct caller to
 *                    [setupProject]. 1 indicates the parent of the caller, and so on.
 * @param buildscript a provider for the content of the `buildscript` block
 */
@Suppress("UnstableApiUsage")
fun setupProject(
    type: BuildFileType,
    traceOffset: Int,
    plugins: List<PluginInfo>,
    buildscript: (() -> String)?
): ProjectSetup {
    // Setup the test build
    val projectDir = File("build/functionalTest/${getName(traceOffset)}")
    if (projectDir.exists()) {
        projectDir.deleteRecursively()
    }
    projectDir.mkdirs()
    projectDir.resolve(type.settingsFile()).writeText("")
    val buildFile = projectDir.resolve(type.buidFile())

    val buildScriptBlock = if (buildscript != null) {
        when (type) {
            BuildFileType.GROOVY ->
                """
buildscript {
${buildscript()}
}
                """
            BuildFileType.KTS ->
                """
buildscript {
${buildscript()}
}
                """
        }
    } else ""

    val customPlugins = plugins.map {
        val versionStr = if (it.version != null) {
            " version \"${it.version}\""
        } else ""

        val applyStr = if (it.apply == false) {
            "apply false"
        } else ""

        val keyword = if (it.kotlin) "kotlin" else "id"

        "    $keyword(\"${it.id}\")$versionStr$applyStr"
    }.joinToString(separator = "\n")

    buildFile.writeText("""
$buildScriptBlock
plugins {
    id("com.android.gradle.project.replicator")
$customPlugins
}
        """)

    // Run the build
    val runner = GradleRunner.create()
    runner.forwardOutput()
    runner.withPluginClasspath()
    runner.withArguments("getStructure", "--stacktrace")

    val property: String? = System.getProperty("agp.classpath")
    property?.let {
        val newCP = File(it).readLines().map { File(it) }
        val oldCP = runner.pluginClasspath
        runner.withPluginClasspath(newCP + oldCP)
    }

    val env = runner.environment
    val newEnv = mutableMapOf<String, String>().also {
        it["ANDROID_SDK_ROOT"] = System.getenv("ANDROID_SDK_ROOT")
    }
    env?.let { newEnv.putAll(it) }
    runner.withEnvironment(newEnv)

    runner.withProjectDir(projectDir)

    return ProjectSetup(
        projectDir = projectDir,
        buildFile = buildFile,
        runner = runner)
}

private fun getName(traceOffset: Int): String {
    val e = RuntimeException("")
    // get the right method from the trace. Usually this is the caller of setupProject but if this is deeper
    // into some calls, traceOffset allows getting a different one.
    val trace: StackTraceElement = e.stackTrace[2 + traceOffset]
    return "${trace.className.split(".").last()}.${trace.methodName.replace(" ", "_")}"
}