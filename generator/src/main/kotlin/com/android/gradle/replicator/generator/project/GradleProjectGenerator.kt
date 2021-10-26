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
import com.android.gradle.replicator.generator.manifest.ManifestGenerator
import com.android.gradle.replicator.generator.util.WildcardString
import com.android.gradle.replicator.generator.writer.DslWriter
import com.android.gradle.replicator.model.DependenciesInfo
import com.android.gradle.replicator.model.ModuleInfo
import com.android.gradle.replicator.model.PluginType
import com.android.gradle.replicator.model.ProjectInfo
import com.android.gradle.replicator.model.internal.AndroidResourcesAdapter
import com.android.gradle.replicator.model.internal.FilesWithSizeMetadataAdapter
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.LinkedList

class GradleProjectGenerator(
    private val destinationFolder: File,
    private val libraryFilter: Map<WildcardString, String>,
    private val libraryAdditions: Map<WildcardString, List<DependenciesInfo>>,
    private val dslWriter: DslWriter,
    private val resGenerator: ManifestGenerator
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

        // create module metadata files
        generateModuleMetadata(destinationFolder, project.rootModule)
        generateModuleResourceMetadata(destinationFolder, project.rootModule)

        // create generation constants file
        generateResgenDefaultConstants(destinationFolder)
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
        // create module metadata files
        generateModuleMetadata(folder, module)
        generateModuleResourceMetadata(folder, module)
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
        val leafModules = getLeafModules(project.subModules)
        leafModules.forEach {
            dslWriter.call("include", dslWriter.asString(it))
        }
    }

    private fun getLeafModules(modules: List<ModuleInfo>): List<String> {

        val ret = mutableListOf<String>()

        // Convert projects into a graph so we can trim all the non-leaf nodes since those are included automatically
        class Node(val path: String) {
            val children = mutableMapOf<String, Node>()
        }

        val root = Node(":")

        // Go through the graph and grab the corresponding node (and create it if it doesn't exist)
        // :module1:module2 becomes root -> module1 -> module2
        val createModuleNode = { module: String ->
            var currNode = root
            val coordinates = LinkedList<String>(module.split(":"))
            // The first coordinate is the root (":a:b" -> ["", "a", "b"]). Discard it.
            coordinates.pop()
            var currFullPath = ""
            // Go through the list of coordinates and traverse the graph, creating nodes as needed
            while (coordinates.isNotEmpty()) {
                val nextCoordinate = coordinates.pop()
                currFullPath += ":$nextCoordinate"
                if (!currNode.children.containsKey(nextCoordinate)) {
                    currNode.children[nextCoordinate] = Node(currFullPath)
                } else if (coordinates.isEmpty()) { // Node already exists. Module is duplicated
                    throw java.lang.RuntimeException("duplicated module $currFullPath")
                }
                currNode = currNode.children[nextCoordinate]!!
            }
        }

        // create the graph with all the modules
        modules.map { it.path }.sorted().forEach {
            createModuleNode(it)
        }

        // BFS
        val visit = { node: Node ->
            // If leaf node add it to includes
            if (node.children.isEmpty()) {
                ret.add(node.path)
            }
        }
        val frontier = LinkedList<Node>(listOf(root))
        while (frontier.isNotEmpty()) {
            val currNode = frontier.pop()
            visit(currNode)
            currNode.children.forEach {
                frontier.add(it.value)
            }
        }
        return ret
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
            manifestGenerator = resGenerator,
            gradlePath = module.path,
            hasKotlin = module.plugins.containsKotlin()
        )

        module.generateDependencies()
    }

    private fun generateModuleMetadata(
            folder: File,
            module: ModuleInfo
    ) {
        val metadataFile = folder.join("module-metadata.json")

        val metadataJson = JsonObject()
        metadataJson.addProperty("javaSources", module.javaSources?.fileCount ?: 0)
        metadataJson.addProperty("kotlinSources", module.kotlinSources?.fileCount ?: 0)

        metadataFile.writeBytes(GsonBuilder().setPrettyPrinting().create().toJson(metadataJson).toByteArray())
    }

    private fun generateModuleResourceMetadata(
            folder: File,
            module: ModuleInfo
    ) {
        val metadataFile = folder.join("resource-metadata.json")

        with(JsonWriter(FileWriter(metadataFile))) {
            this.setIndent("  ")
            beginObject()
            module.androidResources?.let {
                name("androidResources")
                AndroidResourcesAdapter().write(this, it)
            }

            module.javaResources?.let {
                name("javaResources")
                FilesWithSizeMetadataAdapter().write(this, module.javaResources!!)
            }

            module.assets?.let {
                name("assets")
                FilesWithSizeMetadataAdapter().write(this, module.assets!!)
            }

            endObject()
            this.flush()
        }
    }

    private fun generateResgenDefaultConstants(folder: File) {
        val generationPropertyFile = folder.join("generation.properties")

        val loader = Thread.currentThread().contextClassLoader
        loader.getResourceAsStream("project/resource-generator-constants.properties")!!.use {
            it.copyTo(FileOutputStream(generationPropertyFile))
        }
    }

    private fun matchLibraryFilter(lib: String): String? {
        for (i in libraryFilter) {
            if (i.key.matches(lib)) {
                return i.value
            }
        }
        return null
    }

    // Library additions need to match ALL patterns, not just the first
    private fun matchLibraryAdditions(module: String, wildcardMatch: Boolean): List<DependenciesInfo>? {
        val result = mutableListOf<DependenciesInfo>()
        for (i in libraryAdditions) {
            // Only match if wildcard match is on or key is not a wildcard (I.E. is a directly targeted module)
            if ((wildcardMatch || !i.key.isWildcard) && i.key.matches(module)) {
                result.addAll(i.value)
            }
        }
        return if (result.isEmpty()) null else result
    }

    private fun ModuleInfo.generateDependencies() {
        val moduleInfo = this
        dslWriter.block("dependencies") {
            var dependencyList = dependencies

            // Check if we need to add dependencies to this module
            // Only allow wildcard matching for modules that have some dependency to avoid adding dependencies
            // to intermediate modules
            matchLibraryAdditions(moduleInfo.path, dependencies.isNotEmpty())?.let { list ->
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
                    val replacement = matchLibraryFilter(dep.dependency) ?: dep.dependency
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