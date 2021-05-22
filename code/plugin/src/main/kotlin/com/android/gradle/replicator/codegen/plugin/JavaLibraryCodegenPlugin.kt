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

import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactView
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import kotlin.random.Random

@Suppress("UnstableApiUsage")
class JavaLibraryCodegenPlugin: AbstractCodeGenPlugin() {

    override fun apply(project: Project) {
        println("Java Library plugin applied !")

        val topProjectName by lazy {
            var current = project
            while (current.parent != null) current = current.parent!!
            current.name
        }

        val hasKotlinSources = project.pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")

        val generateTask = project.tasks.register(
                "generateCodegenParams",
                GenerateCodegenParamsTask::class.java) { task ->

            task.paramsFile.set(project.layout.buildDirectory.file("test.params"))

            // elements we publish to others, all of our public methods must only use types coming from these elements.
            val configApi = project.configurations.getAt("apiElements")
            val moduleApi = calculateApiList(configuration = configApi, topProjectName = topProjectName)

            // resolvable compile classpath, needs to be reduced.
            val config = project.configurations.getAt("compileClasspath")

            val filter = { componentIdentifier: ComponentIdentifier ->
                when (componentIdentifier) {
                    is ProjectComponentIdentifier -> moduleApi.apiModules.contains(componentIdentifier.projectPath)
                    is ModuleComponentIdentifier -> moduleApi.apiModules.contains("${componentIdentifier.group}:${componentIdentifier.module}")
                    else -> false
                }
            }

            // filter for modules present in the API configuration of this module.
            val apiFilter = { componentIdentifier: ComponentIdentifier ->
                filter(componentIdentifier)
            }

            // filter for modules present in the Implementation configuration of this module
            val implFilter = { componentIdentifier: ComponentIdentifier ->
                !filter(componentIdentifier)
            }

            // create an artifact collection for both Api and Implementation.
            val apiArtifacts = config.incoming.artifactView { t: ArtifactView.ViewConfiguration ->
                t.componentFilter(apiFilter)
            }.artifacts

            val implArtifacts = config.incoming.artifactView { t: ArtifactView.ViewConfiguration ->
                t.componentFilter(implFilter)
            }.artifacts

            val runtimeClasspath = project.configurations.getAt("runtimeClasspath")

            task.runtimeClasspath.from(runtimeClasspath.incoming.artifacts.artifactFiles)
            task.apiJarFiles.from(apiArtifacts.artifactFiles)
            task.paramsFile.set(project.layout.buildDirectory.file("test.params"))
            // Randomizer values should be set during project replication along the number of java and kotlin files.
            task.seed.set(Random.nextInt())

            task.moduleMetadataJson.set(project.file("module-metadata.json"))
        }

        val generateCodeTask = project.tasks.register(
                "generateCode",
                GenerateCode::class.java) { task ->

            task.parameters.set(generateTask.flatMap(GenerateCodegenParamsTask::paramsFile))
            task.outputDirectory.set(
                    project.layout.projectDirectory.dir("src/main/java")
            )
        }

        // generate the code before we start compiling it.
        project.tasks.findByName(if (hasKotlinSources) "compileKotlin" else "compileJava")!!.dependsOn(generateCodeTask)
    }
}