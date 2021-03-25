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

package com.android.gradle.replicator.generator.project

import com.android.gradle.replicator.generator.containsAndroid
import com.android.gradle.replicator.generator.containsKotlin
import com.android.gradle.replicator.generator.generate
import com.android.gradle.replicator.generator.join
import com.android.gradle.replicator.generator.resources.ResourceGenerator
import com.android.gradle.replicator.generator.writer.DslWriter
import com.android.gradle.replicator.model.DependenciesInfo
import com.android.gradle.replicator.model.ModuleInfo
import com.android.gradle.replicator.model.PluginType
import com.android.gradle.replicator.model.ProjectInfo
import com.google.gson.JsonObject
import java.io.File

class GradleProjectGenerator(
    private val destinationFolder: File,
    private val libraryFilter: Map<String, String>,
    private val libraryAdditions: Map<String, List<DependenciesInfo>>,
    private val dslWriter: DslWriter,
    private val resGenerator: ResourceGenerator
): ProjectGenerator {

    override fun generateRootModule(project: ProjectInfo) {
        dslWriter.newBuildFile(destinationFolder)

        val rootPlugins = project.rootModule.plugins
        val allPlugins = project.getAllPlugins()

        val requiresBuildscript = allPlugins.any { !it.useNewDsl(project) }

        if (requiresBuildscript) {
            dslWriter.block("buildscript") {
                block("repositories") {
                    google()
                    jcenter()
                }
                block("dependencies") {
                    // now prepare the classpaths for plugins *not* using the new DSL
                    val classpaths = allPlugins.asSequence()
                        .filter { !it.useNewDsl(project) }
                        .mapNotNull {
                            when (it) {
                                PluginType.ANDROID_APP, PluginType.ANDROID_LIB, PluginType.ANDROID_TEST, PluginType.ANDROID_DYNAMIC_FEATURE -> {
                                    "com.android.tools.build:gradle:${project.agpVersion}"
                                }
                                else -> throw RuntimeException("Unexpected plugin requiring buildscript: ${it.id}")
                            }
                        }.toSet()

                    for (cp in classpaths) {
                        call("classpath", asString(cp))
                    }
                }
            }
        }

        // Because some AGP versions do not support the new DSL, we need a mix and match of both of DSL.
        // First figure out the full list of plugins knowing whether it's applied to the root module or not.
        // build a list of plugins to put in the root module.
        val rootPluginInfos = allPlugins
            .asSequence()
            .map { RootPluginInfo(plugin = it, applied = rootPlugins.contains(it)) }
            .filter { (it.plugin.useNewDsl(project) && it.plugin.requireVersions) || it.applied }
            .sortedBy { it.plugin.priority }
            .toList()

        if (rootPluginInfos.isNotEmpty()) {
            dslWriter.block("plugins") {
                for (pluginInfo in rootPluginInfos) {
                    val version: String? = when (pluginInfo.plugin) {
                        PluginType.JAVA, PluginType.JAVA_LIBRARY, PluginType.APPLICATION -> null
                        PluginType.KOTLIN_ANDROID, PluginType.KOTLIN_JVM, PluginType.KAPT -> project.kotlinVersion
                        PluginType.ANDROID_APP, PluginType.ANDROID_LIB, PluginType.ANDROID_TEST, PluginType.ANDROID_DYNAMIC_FEATURE -> project.agpVersion
                        else -> throw RuntimeException("Unexpected plugin in root plugin infos.")
                    }

                    pluginInBlock(pluginInfo.plugin, version, pluginInfo.applied)
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

        // create module metadata file
        generateModuleMetadata(destinationFolder, project.rootModule)
    }

    private fun PluginType.useNewDsl(info: ProjectInfo): Boolean {
        return if (isAndroid) {
            val version = useNewDslSince ?: throw RuntimeException("Android plugin without 'useNewDslSince' value: $id")
            info.agpVersion >= version
        } else  {
            true
        }
    }

    override fun generateModule(
        folder: File,
        module: ModuleInfo
    ) {
        dslWriter.newBuildFile(folder)

        if (module.plugins.isNotEmpty()) {
            dslWriter.block("plugins") {
                for (plugin in module.plugins.sortedBy { it.priority }) {
                    dslWriter.pluginInBlock(plugin)
                }
            }
        }

        generateModuleInfo(folder, module)
        // create module metadata file
        generateModuleMetadata(folder, module)
    }

    override fun generateSettingsFile(project: ProjectInfo) {
        println("Generate settings.gradle")
        dslWriter.newSettingsFile(destinationFolder)

        val plugins = project.getAllPlugins()
        if (plugins.containsAndroid() && PluginType.ANDROID_APP.useNewDsl(project)) {
            dslWriter.block("pluginManagement") {
                dslWriter.block("repositories") {
                    gradlePluginPortal()
                    google()
                }
            }
        }

        project.subModules.map { it.path }.sorted().forEach {
            dslWriter.call("include", dslWriter.asString(it))
        }
    }

    override fun close() {
        dslWriter.flush()
    }

    private fun ProjectInfo.getAllPlugins(): Set<PluginType> {
        val allPlugins = mutableSetOf<PluginType>()
        allPlugins.addAll(rootModule.plugins)
        for (module in subModules) {
            allPlugins.addAll(module.plugins)
        }

        return allPlugins
    }

    private fun generateModuleInfo(
        folder: File,
        module: ModuleInfo
    ) {
        module.android?.generate(
            folder = folder,
            dslWriter = dslWriter,
            resourceGenerator = resGenerator,
            gradlePath = module.path,
            hasKotlin = module.plugins.containsKotlin()
        )

        module.generateDependencies()
    }

    internal fun generateModuleMetadata(
            folder: File,
            module: ModuleInfo
    ) {
        val metadataFile = folder.join("module-metadata.json")

        val metadataJson = JsonObject()
        metadataJson.addProperty("javaSources", module.javaSources?.fileCount ?: 0)
        metadataJson.addProperty("kotlinSources", module.kotlinSources?.fileCount ?: 0)

        metadataFile.writeBytes(metadataJson.toString().toByteArray())
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
                for (dep in dependencyList.filter { it.scope == scope }) {
                    // search for replacement. If none, use the original value
                    val replacement = libraryFilter[dep.dependency] ?: dep.dependency
                    // empty string means ignored dependency
                    if (replacement.isEmpty()) {
                        println("\tIgnoring ${dep.dependency}")
                        continue
                    }

                    call(rewriteDependencyScope(dep.scope), dep.type.getString(replacement, dslWriter::asString))
                }
            }
        }
    }

    private fun rewriteDependencyScope(scope: String): String = when (scope) {
        "provided" -> "compileOnly"
        else -> scope
    }

    private data class RootPluginInfo(
        val plugin: PluginType,
        val applied: Boolean)
}