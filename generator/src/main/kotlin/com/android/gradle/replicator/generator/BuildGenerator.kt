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

import com.android.gradle.replicator.model.DependenciesInfo
import com.android.gradle.replicator.model.DependencyType
import com.android.gradle.replicator.model.ProjectInfo
import com.android.gradle.replicator.model.Serializer
import com.android.gradle.replicator.model.internal.DefaultDependenciesInfo
import java.io.File

/**
 * Generates all the files for a build.
 */
internal class BuildGenerator(private val params: Params) {
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

        val projectGenerator = ProjectGenerator(
            params.destination,
            libraryFilter,
            libraryAdditions
        )

        println("Project: ':'")
        projectGenerator.generateRootModule(project)

        val count = project.subModules.size
        val digitCount = count.toString().length
        val formatter = "%0${digitCount}d"

        var index = 1
        for (module in project.subModules) {
            println("Project(${formatter.format(index)}): '${module.path}'")
            projectGenerator.generateModule(getFolder(module.path), module)
            index++
        }

        generateSettingsFile(project)
        generateGradleProperties(project.gradleProperties)

        println("Done.")
    }

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
