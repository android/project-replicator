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
package com.android.gradle.replicator.codegen.plugin

import com.google.gson.Gson
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import java.io.File

import java.nio.file.Files


abstract class AbstractCodeGenPlugin: Plugin<Project> {

    class ModuleApi(val apiModules: List<String>, val projectDependencies: List<String>)

    data class ProjectMetadata (
        val javaSources: Int,
        val kotlinSources: Int)

    fun calculateApiList(configuration: Configuration, topProjectName: String): ModuleApi {
        // construct our API modules list so we can filter them out from the implementation classpath.
        val apiModules = mutableSetOf<String>()
        val projectDependencies = mutableListOf<String>()
        configuration.allDependencies.forEach { dependency ->
            when (dependency) {
                is ProjectDependency -> {
                    val projectName = dependency.group?.removePrefix(topProjectName)?.replace('.', ':')
                    apiModules.add("$projectName:${dependency.name}")
                    projectDependencies.add("$projectName:${dependency.name}")
                }
                is ExternalModuleDependency -> {
                    apiModules.add("${dependency.group}:${dependency.name}")
                }
                else -> {
                    println("Ignored API module $dependency")
                }
            }
        }
        return ModuleApi(apiModules = apiModules.toList(), projectDependencies = projectDependencies.toList())
    }

    // read metadata file added to each project in json format
    fun loadModuleMetadata(project: Project): ProjectMetadata {
        var javaSources = 0
        var kotlinSources = 0
        val gson = Gson()
        val metadata = project.file("module-metadata.json")

        with(Files.newBufferedReader(metadata.toPath())) {
            val map: Map<String, Int> = gson.fromJson(this, MutableMap::class.java) as MutableMap<String, Int>
            if (map.containsKey("javaSources")) {
                javaSources = map["javaSources"]!!
            }
            if (map.containsKey("kotlinSources")) {
                kotlinSources = map["kotlinSources"]!!
            }
        }

        return ProjectMetadata(javaSources, kotlinSources)
    }
}