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

import com.android.gradle.replicator.generator.writer.DslWriter
import com.android.gradle.replicator.model.DependenciesInfo
import com.android.gradle.replicator.model.ModuleInfo
import com.android.gradle.replicator.model.PluginType
import com.android.gradle.replicator.model.ProjectInfo
import java.io.File

class ProjectGenerator(
    private val destinationFolder: File,
    private val libraryFilter: Map<String, String>,
    private val libraryAdditions: Map<String, List<DependenciesInfo>>,
    private val dslWriter: DslWriter
) {

    internal fun generateRootModule(project: ProjectInfo) {
        dslWriter.newBuildFile(destinationFolder)

        val rootPlugins = project.rootModule.plugins
        // gather all the plugins from all the modules.
        val allPlugins = mutableSetOf<PluginType>()
        allPlugins.addAll(rootPlugins)
        for (module in project.subModules) {
            allPlugins.addAll(module.plugins)
        }

        val requiresBuildscript = allPlugins.any { !it.useNewDsl }

        if (requiresBuildscript) {
            dslWriter.block("buildscript") {
                block("repositories") {
                    google()
                    jcenter()
                }
                block("dependencies") {
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
                        }.toSet()

                    for (cp in classpaths) {
                        call("classpath", asString(cp))
                    }
                }
            }
        }

        val requiresPluginsBlock = allPlugins.any { it.useNewDsl && it.requireVersions }
        if (requiresPluginsBlock) {
            dslWriter.block("plugins") {
                // Because AGP does not support the new DSL, we need a mix and match of both of DSL.
                // First figure out the full list of plugins knowing whether it's applied to the root module or not.
                // build a list of plugins to put in the root module.
                val rootPluginInfos = allPlugins.map { RootPluginInfo(it, rootPlugins.contains(it)) }

                // now prepare the new DSL for all the plugins that support it.
                rootPluginInfos.asSequence()
                    .filter { it.plugin.useNewDsl }
                    .forEach { pluginInfo ->
                        val p: Pair<String, String?>? = when (pluginInfo.plugin) {
                            PluginType.JAVA, PluginType.JAVA_LIBRARY, PluginType.APPLICATION -> {
                                if (pluginInfo.applied) {
                                    pluginInfo.plugin.id to null
                                } else {
                                    null
                                }
                            }
                            PluginType.KOTLIN_ANDROID, PluginType.KOTLIN_JVM, PluginType.KAPT -> {
                                pluginInfo.plugin.id to project.kotlinVersion
                            }
                            else -> throw RuntimeException("Unexpected plugin in root plugin infos.")
                        }

                        p?.let {
                            pluginInBlock(p.first, p.second, pluginInfo.applied)
                        }

                    }
            }
        }

        dslWriter.block("allprojects") {
            block("repositories") {
                google()
                jcenter()
                block("maven") {
                    url("https://jitpack.io")
                }
            }
        }

        // now the generic module info stuff
        generateModuleInfo(destinationFolder, project.rootModule)
    }

    internal fun generateModule(
        folder: File,
        module: ModuleInfo
    ) {
        dslWriter.newBuildFile(folder)

        if (module.plugins.isNotEmpty()) {
            dslWriter.block("plugins") {
                for (plugin in module.plugins.sortedBy { it.priority }) {
                    dslWriter.pluginInBlock(plugin.id)
                }
            }
        }

        generateModuleInfo(folder, module)
    }

    internal fun generateSettingsFile(project: ProjectInfo) {
        println("Generate settings.gradle")
        dslWriter.newSettingsFile(destinationFolder)

        project.subModules.map { it.path }.sorted().forEach {
            dslWriter.call("include", dslWriter.asString(it))
        }
    }

    private fun generateModuleInfo(
        folder: File,
        module: ModuleInfo
    ) {
        module.android?.generate(
            folder = folder,
            dslWriter = dslWriter,
            gradlePath = module.path,
            hasKotlin = module.plugins.containsKotlin()
        )

        module.generateDependencies()
    }


    private fun ModuleInfo.generateDependencies() {
        val moduleInfo = this
        dslWriter.block("dependencies") {
            var dependencyList = dependencies

            // check if we need to add dependencies to this module
            libraryAdditions[moduleInfo.path]?.let { list ->
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

                    call(dep.scope, dep.type.getString(replacement, dslWriter::asString))
                }
            }
        }
    }

    private data class RootPluginInfo(
        val plugin: PluginType,
        val applied: Boolean)
}