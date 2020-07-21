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

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationVariant
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.TaskProvider

const val USAGE_STRUCTURE = "android-debug-structure"
const val ARTIFACT_TYPE_MODULE_INFO = "android-debug-module-info"

const val ELEMENT_CONFIG_NAME = "structureElements"
const val CLASSPATH_CONFIG_NAME = "structureClasspath"

class ProjectReplicatorPlugin: Plugin<Project> {
    private var combineModuleTask: TaskProvider<CombineModuleInfoTask>? = null

    override fun apply(project: Project) {
        if (project == project.rootProject) {
            // create a configuration to consume the files
            val structureConfig = project.configurations.create(CLASSPATH_CONFIG_NAME).apply {
                isCanBeConsumed = false
                attributes.attribute(
                        Usage.USAGE_ATTRIBUTE,
                        project.objects.named(Usage::class.java,
                                              USAGE_STRUCTURE
                        ))
            }

            // go through all the sub modules add add as a dependencies
            val depHandler = project.dependencies
            project.subprojects.forEach {
                depHandler.add(CLASSPATH_CONFIG_NAME, it)
            }

            // create the task
            combineModuleTask = project.tasks.register(
                "getStructure",
                CombineModuleInfoTask::class.java,
                CombineModuleInfoTask.ConfigAction(
                    project,
                    structureConfig
                )
            )
        }

        // create the task that will gather the information about the module. Do this for all module, including the
        // root module, whether we have plugins or not.
        createGatherInfoTask(project)
    }

    @Suppress("UnstableApiUsage")
    private fun createGatherInfoTask(project: Project) {
        // create a configuration to publish the file
        val structureConfig = createPublishingConfiguration(project)

        // create the task
        val gatherModuleTask = project.tasks.register(
            "gatherModuleInfo",
            GatherModuleInfoTask::class.java,
            GatherModuleInfoTask.ConfigAction(project)
        )

        // publish the json file as an artifact
        structureConfig.outgoing.variants { variants: NamedDomainObjectContainer<ConfigurationVariant> ->
            variants.create(ARTIFACT_TYPE_MODULE_INFO) { variant ->
                variant.artifact(gatherModuleTask.flatMap { it.outputFile }) { artifact ->
                    artifact.type = ARTIFACT_TYPE_MODULE_INFO
                    artifact.builtBy(gatherModuleTask)
                }
            }
        }

        // if this the root project, register this output as an input to the combine task
        combineModuleTask?.configure {
            it.localModuleInfo.set(gatherModuleTask.flatMap { it.outputFile })
        }
    }

    private fun createPublishingConfiguration(project: Project): Configuration {
        return project.configurations.create(ELEMENT_CONFIG_NAME).apply {
            isCanBeResolved = false
            attributes.attribute(
                    Usage.USAGE_ATTRIBUTE,
                    project.objects.named(Usage::class.java,
                                          USAGE_STRUCTURE
                    ))
        }
    }
}
