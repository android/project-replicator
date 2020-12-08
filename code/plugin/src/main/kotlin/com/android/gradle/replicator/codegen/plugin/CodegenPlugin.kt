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
package com.android.gradle.replicator.codegen.plugin

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactView
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.internal.artifacts.ArtifactAttributes
import kotlin.random.Random

@Suppress("UnstableApiUsage")
class CodegenPlugin: Plugin<Project> {
    override fun apply(project: Project) {

        val topProjectName by lazy {
            var current = project
            while (current.parent != null) current = current.parent!!
            current.name
        }

        val generateTask = project.tasks.register(
                "generateCodegenParams",
                GenerateParamsTask::class.java) { task ->

            // elements we publish to others, all of our public methods must only use types coming from these elements.
            val configApi = project.configurations.getAt("debugApiElements")

            // construct our API modules list so we can filter them out from the implementation classpath.
            val apiModules = mutableSetOf<String>()
            val projectDependencies = mutableListOf<String>()
            configApi.allDependencies.forEach { dependency ->
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

            // resolvable compile classpath, needs to be reduced.
            val config = project.configurations.getAt("debugCompileClasspath")

            val filter = { componentIdentifier: ComponentIdentifier ->
                when (componentIdentifier) {
                    is ProjectComponentIdentifier -> apiModules.contains(componentIdentifier.projectPath)
                    is ModuleComponentIdentifier -> apiModules.contains("${componentIdentifier.group}:${componentIdentifier.module}")
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

            // we only care about classes artifact from aar.
            val attributeFilter: Action<AttributeContainer> = Action<AttributeContainer> {
                attributeContainer: AttributeContainer ->
                    attributeContainer.attribute(
                        ArtifactAttributes.ARTIFACT_FORMAT,
                        "android-classes-jar"
                )
            }

            // create an artifact collection that contains only the project dependencies, external dependencies
            // will be separated in a different artifact collection.
            val apiProjectArtifacts = config.incoming.artifactView { t: ArtifactView.ViewConfiguration ->
                t.componentFilter { componentIdentifier ->
                    when (componentIdentifier) {
                        is ProjectComponentIdentifier -> apiModules.contains(componentIdentifier.projectPath)
                        else -> false
                    }
                }
                t.attributes(attributeFilter)
            }.artifacts

            val implProjectArtifacts = config.incoming.artifactView { t: ArtifactView.ViewConfiguration ->
                t.componentFilter { componentIdentifier ->
                    when (componentIdentifier) {
                        is ProjectComponentIdentifier -> !apiModules.contains(componentIdentifier.projectPath)
                        else -> false
                    }
                }
                t.attributes(attributeFilter)
            }.artifacts

            // create an artifact collection for both Api and Implementation.
            val apiArtifacts = config.incoming.artifactView { t: ArtifactView.ViewConfiguration ->
                t.componentFilter(apiFilter)
                t.attributes(attributeFilter)
            }.artifacts

            val implArtifacts = config.incoming.artifactView { t: ArtifactView.ViewConfiguration ->
                t.componentFilter(implFilter)
                t.attributes(attributeFilter)
            }.artifacts

            // get the android APIs, this will be added to our classloader and API which is done automatically by AGP.
            val androidApis = project.configurations.getAt("androidApis").incoming.files

            config.allDependencies.forEach {
                task.gradleDependencies.add("${it.group}:${it.name}:${it.version}")
            }


            val runtimeClasspath = project.configurations.getAt("debugRuntimeClasspath")
            val runtimeClasspathArtifacts = runtimeClasspath.incoming.artifactView {
                it.attributes(attributeFilter)
            }.artifacts

            task.runtimeClasspath.from(runtimeClasspathArtifacts.artifactFiles)
            task.codeGeneratedModuleApiClasspath.from(apiProjectArtifacts.artifactFiles)
            task.codeGeneratedModuleImplClasspath.from(implProjectArtifacts.artifactFiles)
            task.apiJarFiles.from(apiArtifacts.artifactFiles)
            task.implJarFiles.from(androidApis, implArtifacts.artifactFiles)
            task.paramsFile.set(project.layout.buildDirectory.file("test.params"))
            // Randomizer values should be set during project replication along the number of java and kotlin files.
            task.seed.set(Random.nextInt())

            task.nbOfJavaFiles.set(10)

            // make sure we depend on our dependencies built artifacts so we have access to their generated classes.
            projectDependencies.forEach {
                // hack : use Variant API when dealing with android.
                task.dependsOn(if (project.tasks.findByName("$it:assembleDebug") != null) {
                    "$it:assembleDebug"
                } else "$it:assemble")
            }
        }

        val generateCodeTask = project.tasks.register(
                "generateCode",
                GenerateCode::class.java) { task ->

            task.parameters.set(generateTask.flatMap(GenerateParamsTask::paramsFile))
            task.outputDirectory.set(
                    project.layout.projectDirectory.dir("src/main/java")
            )
        }

        // generate the code before we start compiling it.
        // hack, use variant API.
        project.tasks.findByName("preBuild")!!.dependsOn(generateCodeTask)
    }
}