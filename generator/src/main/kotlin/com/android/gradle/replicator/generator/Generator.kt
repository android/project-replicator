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

package com.android.gradle.replicator.generator

import com.android.gradle.replicator.model.*
import com.android.gradle.replicator.model.internal.DefaultDependenciesInfo
import java.io.File

class Generator(private val params: Params) {
    interface Params {
        val jsonFile: File
        val destination: File
        val libraryFilter: File?
        val libraryAdditions: File?
    }

    private val libraryFilter = generateLibraryFilter()
    private val libraryAdditions = generateLibraryAdditions()

    fun generate() {
        val project = Serializer.instance().deserializeProject(params.jsonFile)

        println("Project: ':'")
        generateRootModule(project)

        val count = project.subModules.size
        val digitCount = count.toString().length
        val formatter = "%0${digitCount}d"

        var index = 1
        for (module in project.subModules) {
            println("Project(${formatter.format(index)}): '${module.path}'")
            generateModule(getFolder(module.path), module)
            index++
        }

        generateSettingsFile(project)
        generateGradleProperties(project.gradleProperties)

        println("Done.")
    }

    private data class RootPluginInfo(
        val plugin: PluginType,
        val applied: Boolean)

    private fun generateLibraryFilter(): Map<String, String> {
        return params.libraryFilter?.let { file: File ->
            file.readLines().filter { it.isNotEmpty() }.map {
                val split = it.split(" -> ")
                if (split.size == 1) {
                    split[0] to ""
                } else {
                    split[0] to split[1]
                }
            }.toMap()
        } ?: mapOf()
    }

    private fun generateLibraryAdditions(): Map<String, List<DependenciesInfo>> {
        return params.libraryAdditions?.let { file: File ->
            val entries = file.readLines().filter { it.isNotEmpty() }.map {
                val split = it.split(" ")
                if (split.size != 3) {
                    throw RuntimeException("badly formatter library addition line: '$it'")
                }

                split[0] to DefaultDependenciesInfo(
                    scope = split[1],
                    dependency = split[2],
                    type = DependencyType.EXTERNAL_LIBRARY
                )
            }

            val result = mutableMapOf<String, List<DependenciesInfo>>()

            entries.forEach {
                val list = result.computeIfAbsent(it.first) { mutableListOf() } as MutableList<DependenciesInfo>
                list.add(it.second)
            }

            result
        } ?: mapOf()
    }

    private fun generateRootModule(project: ProjectInfo) {
        val buildFile = File(params.destination, "build.gradle")

        val rootPlugins = project.rootModule.plugins

        // gather all the plugins from all the modules.
        val allPlugins = mutableSetOf<PluginType>()
        allPlugins.addAll(rootPlugins)
        for (module in project.subModules) {
            allPlugins.addAll(module.plugins)
        }

        // Because AGP does not support the new DSL, we need a mix and match of both of DSL.
        // First figure out the full list of plugins knowing whether it's applied to the root module or not.
        // build a list of plugins to put in the root module.
        val rootPluginInfos = allPlugins.map { RootPluginInfo(it, rootPlugins.contains(it)) }

        // now prepare the new DSL for all the plugins that support it.
        val pluginIds = rootPluginInfos.asSequence()
            .filter { it.plugin.useNewDsl }
            .map {
            val applyStr = if (it.applied) {
                ""
            } else {
                " apply false"
            }

            when (it.plugin) {
                PluginType.JAVA, PluginType.JAVA_LIBRARY, PluginType.APPLICATION -> {
                    if (it.applied) {
                        """    id("${it.plugin.id}")"""
                    } else {
                        ""
                    }
                }
                PluginType.KOTLIN_ANDROID, PluginType.KOTLIN_JVM, PluginType.KAPT -> {
                    """    id("${it.plugin.id}") version "${project.kotlinVersion}" $applyStr """
                }
                else -> throw RuntimeException("Unexpected plugin in root plugin infos.")
            }
        }.joinToString(separator = "\n")

        // now prepare the classpaths for plugins *not* using the new DSL
        val classpaths = allPlugins.asSequence()
            .filter { !it.useNewDsl }
            .mapNotNull {
                when (it) {
                    PluginType.ANDROID_APP, PluginType.ANDROID_LIB, PluginType.ANDROID_TEST, PluginType.ANDROID_DYNAMIC_FEATURE -> {
                        "com.android.tools.build:gradle:${project.agpVersion}"
                    }
                    PluginType.KOTLIN_ANDROID, PluginType.KOTLIN_JVM, PluginType.KAPT -> {
                        "org.jetbrains.kotlin:kotlin-gradle-plugin:${project.kotlinVersion}"
                    }
                    else -> null
                }
            }.map { "        classpath(\"$it\")" }
            .toSet()
            .joinToString(separator = "\n")

        // buildScript stuff.
        buildFile.appendText("""
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
$classpaths    
    }
}

plugins {
$pluginIds
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
""")

        // now the generic module info stuff
        generateModuleInfo(params.destination, project.rootModule)
    }

    private fun generateModule(
        folder: File,
        module: ModuleInfo
    ) {
        val buildFile = File(folder, "build.gradle")

        val newDslPlugin = module.plugins.filter { it.useNewDsl }
        if (newDslPlugin.isNotEmpty()) {
            buildFile.appendText("plugins {\n")
            for (plugin in newDslPlugin) {
                buildFile.appendText("    id(\"${plugin.id}\")\n")
            }
            buildFile.appendText("}\n")
        }

        val oldDslPlugin = module.plugins.filter { !it.useNewDsl }.sortedBy { it.last }
        for (plugin in oldDslPlugin) {
            buildFile.appendText("apply plugin: \"${plugin.id}\"\n")
        }

        generateModuleInfo(folder, module)
    }

    private fun generateModuleInfo(
        folder: File,
        module: ModuleInfo
    ) {
        val buildFile = File(folder, "build.gradle")

        module.android?.generate(
            folder = folder,
            buildFile = buildFile,
            gradlePath = module.path,
            hasKotlin = module.plugins.containsKotlin()
        )

        module.generateDependencies(buildFile)
    }

    private fun ModuleInfo.generateDependencies(
        buildFile: File
    ) {
        buildFile.appendText("\ndependencies {\n")

        var dependencyList = dependencies

        // check if we need to add dependencies to this module
        libraryAdditions[this.path]?.let { list ->
            println("\tAdding dependencies to $path")
            list.forEach {
                println("\t\t- ${it.dependency}(${it.scope})")
            }
            dependencyList = dependencyList + list
        }

        // first gather the scope in order to sort by them
        val scopes = dependencyList.asSequence().map { it.scope }.toSortedSet()

        // this is not very efficient, but good enough here.
        for (scope in scopes) {
            for (dep in dependencyList.filter { it.scope == scope}) {
                // search for replacement. If none, use the original value
                val replacement = libraryFilter[dep.dependency] ?: dep.dependency
                // empty string means ignored dependency
                if (replacement.isEmpty()) {
                    println("\tIgnoring ${dep.dependency}")
                    continue
                }

                buildFile.appendText("    ${dep.scope}(${dep.type.getString(replacement)})\n")
            }
        }
        buildFile.appendText("}\n")
    }

    private fun generateSettingsFile(project: ProjectInfo) {
        println("Generate settings.gradle")
        val settingsGradle = File(params.destination, "settings.gradle")
        settingsGradle.writeText(project.subModules.map { "include(\"${it.path}\")" }.sorted().joinToString("\n"))
    }

    private fun generateGradleProperties(properties: List<String>) {
        if (properties.isEmpty()) return

        println("Generate gradle.properties")
        val settingsGradle = File(params.destination, "gradle.properties")
        settingsGradle.writeText(properties.sorted().joinToString("\n"))
    }

    private fun getFolder(path: String): File {
        return params.destination.join(path.split(":")).also {
            it.createDirWithParents()
        }
    }
}
