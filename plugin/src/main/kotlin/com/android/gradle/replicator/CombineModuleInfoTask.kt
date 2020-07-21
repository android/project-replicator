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
 */

package com.android.gradle.replicator

import com.android.gradle.replicator.model.Serializer
import com.android.gradle.replicator.model.internal.DefaultProjectInfo
import com.android.gradle.replicator.model.toAnonymized
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*

abstract class CombineModuleInfoTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val subModules: ConfigurableFileCollection

    // optional module info if root project is also a module with plugins applied
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    @get:Optional
    abstract val localModuleInfo: RegularFileProperty

    @get:Input
    abstract val properties: ListProperty<String>

    @get:OutputFile
    abstract val outputStructure: RegularFileProperty

    @get:OutputFile
    abstract val outputMapping: RegularFileProperty

    @TaskAction
    fun action() {
        val serializer = Serializer.instance()

        val projectInfo = DefaultProjectInfo(
            gradleVersion = project.gradle.gradleVersion,
            agpVersion = findAgpVersion(),
            kotlinVersion = findKotlinVersion(),
            rootModule = serializer.deserializeModule(localModuleInfo.get().asFile),
            subModules = this.subModules.map { serializer.deserializeModule(it) },
            gradleProperties = properties.get()
        )

        // Remove PII
        val anonymizedInfo = projectInfo.toAnonymized()

        val structureFile = outputStructure.get().asFile
        structureFile.writeText(Serializer.instance().serialize(anonymizedInfo.projectInfo))
        println("Structure written at: $structureFile")

        // write mapping file
        val mappingFile = outputMapping.get().asFile
        mappingFile.writeText(anonymizedInfo.mapping.entries.map { "${it.key} -> ${it.value}" }.joinToString(separator = "\n"))
        println("Mapping written at: $mappingFile")
    }

    class ConfigAction(private val project: Project,
            private val structureConfig: Configuration) : Action<CombineModuleInfoTask> {

        @Suppress("UnstableApiUsage")
        override fun execute(task: CombineModuleInfoTask) {
            task.outputStructure.set(project.layout.buildDirectory.file("project-structure.json"))
            task.outputStructure.disallowChanges()

            task.outputMapping.set(project.layout.buildDirectory.file("project-mapping.txt"))
            task.outputMapping.disallowChanges()

            task.subModules.from(structureConfig
                    .incoming
                    .artifactView { config ->
                        config.attributes { container ->
                            container.attribute<String>(
                                Attribute.of("artifactType", String::class.java),
                                ARTIFACT_TYPE_MODULE_INFO)
                        }
                    }
                    .artifacts
                    .artifactFiles)
            task.subModules.disallowChanges()

            // handle properties
            val propertyMap = project.extensions.extraProperties.properties
            for (entry in propertyMap.entries) {
                if ((entry.key.startsWith("android.") && entry.key != "android.agp.version.check.performed") ||
                    entry.key.startsWith("org.gradle.")
                ) {
                    task.properties.add("${entry.key}=${entry.value}")
                }
            }
            task.properties.disallowChanges()
        }
    }

    private fun findAgpVersion(): String {
        for (config in project.buildscript.configurations) {
            for (dep in config.allDependencies) {
                if (dep.group == "com.android.tools.build" && dep.name == "gradle")
                    return dep.version!!
            }
        }
        return "n/a"
    }

    private fun findKotlinVersion(): String {
        for (config in project.buildscript.configurations) {
            for (dep in config.allDependencies) {
                if (dep.group == "org.jetbrains.kotlin" && dep.name == "kotlin-gradle-plugin")
                    return dep.version!!
            }
        }
        return "n/a"
    }
}

