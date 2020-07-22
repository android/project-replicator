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

import com.android.gradle.replicator.collectors.AndroidInfoInputs
import com.android.gradle.replicator.collectors.DefaultAndroidCollector
import com.android.gradle.replicator.model.DependencyType
import com.android.gradle.replicator.model.PluginType
import com.android.gradle.replicator.model.Serializer
import com.android.gradle.replicator.model.internal.DefaultDependenciesInfo
import com.android.gradle.replicator.model.internal.DefaultModuleInfo
import com.android.gradle.replicator.model.internal.DefaultSourceFilesInfo
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

abstract class GatherModuleInfoTask : DefaultTask() {
    @get:Input
    abstract val projectPath: Property<String>

    @get:Input
    abstract val plugins: ListProperty<PluginType>

    @get:Nested
    @get:Optional
    abstract val androidInputs: Property<AndroidInfoInputs>

    @get:Nested
    abstract val dependencies: ListProperty<DependenciesInfoInputs>

    @get:InputFiles
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val javaSourceSets: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile : RegularFileProperty

    @TaskAction
    fun action() {
        val pluginList: List<PluginType> = plugins.get()

        val androidInputs = androidInputs.orNull

        val javaSources = if (pluginList.containsJava() || pluginList.containsAndroid()) {
            getSourceFilesInfo("**/*.java", androidInputs)
        } else {
            null
        }

        val kotlinSources = if (pluginList.containsKotlin()) {
            getSourceFilesInfo("**/*.kt", androidInputs)
         } else {
            null
        }

        val moduleInfo = DefaultModuleInfo(
            path = projectPath.get(),
            plugins = plugins.get(),
            javaSources = javaSources,
            kotlinSources = kotlinSources,
            dependencies = dependencies.get().map { it.toInfo() },
            android = androidInputs?.toInfo()
        )

        outputFile.get().asFile.writeText(Serializer.instance().serialize(moduleInfo))
    }

    private fun getSourceFilesInfo(
        pattern: String,
        androidInputs: AndroidInfoInputs?
    ): DefaultSourceFilesInfo {
        var fileCount = javaSourceSets.asFileTree.matching {
            it.include(pattern)
        }.files.size

        fileCount += androidInputs?.javaFolders?.asFileTree?.matching {
            it.include(pattern)
        }?.files?.size ?: 0

        return DefaultSourceFilesInfo(fileCount)
    }

    @Suppress("UnstableApiUsage")
    class ConfigAction(private val project: Project) : Action<GatherModuleInfoTask> {
        override fun execute(task: GatherModuleInfoTask) {
            task.projectPath.set(project.path)
            task.projectPath.disallowChanges()

            // gather known plugins
            val pluginContainer = project.plugins
            val appliedPlugins = mutableSetOf<PluginType>()
            for (plugin in PluginType.values()) {
                if (pluginContainer.hasPlugin(plugin.id) || pluginContainer.hasPlugin(plugin.oldId)) {
                    appliedPlugins.add(plugin)
                }
            }
            task.plugins.set(appliedPlugins.toList().sorted())
            task.plugins.disallowChanges()

            // get android Inputs. This can be set to null if the plugin does not exist.
            if (appliedPlugins.containsAndroid()) {
                task.androidInputs.set(DefaultAndroidCollector().collectInfo(project))
            }
            task.androidInputs.disallowChanges()

            if (appliedPlugins.containsJava()) {
                val javaConvention = project.convention.findPlugin(JavaPluginConvention::class.java)
                if (javaConvention != null) {
                    val dirs = javaConvention.sourceSets.findByName(SourceSet.MAIN_SOURCE_SET_NAME)?.allSource?.srcDirs
                    dirs?.let {
                        task.javaSourceSets.from(it)
                    }
                }
            }
            task.javaSourceSets.disallowChanges()

            // dependencies
            task.dependencies.set(project.configurations.asSequence()
                .filter { relevantConfigurations.contains(it.name) }
                .flatMap { gatherDependencies(it).asSequence() }
                .toSet().toList().sortedBy { it.dependency })
            task.dependencies.disallowChanges()

            task.outputFile.set(project.layout.buildDirectory.file("local-module-info.json"))
            task.outputFile.disallowChanges()
        }
    }
}

// FIXME support build type/flavor/variant dependencies
private val relevantConfigurations = setOf(
    "api", "implementation", "classpath",
    "compile", "compileOnly",
    "runtime", "runtimeOnly",
    "testImplementation", "testCompile",
    "androidTestImplementation", "androidTestCompile",
    "annotationProcessor", "kapt",
    "package", "provided", "wearApp"
)

private fun gatherDependencies(config: Configuration): List<DependenciesInfoInputs> {
    val builder = mutableListOf<DependenciesInfoInputs>()

    for (dependency in config.dependencies) {
        val dep = when (dependency) {
            is ProjectDependency -> DependenciesInfoInputs(
                type = DependencyType.MODULE,
                dependency = dependency.dependencyProject.path,
                scope = config.name
            )
            is ModuleDependency -> DependenciesInfoInputs(
                type = DependencyType.EXTERNAL_LIBRARY,
                dependency = "${dependency.group}:${dependency.name}:${dependency.version}",
                scope = config.name
            )
            else -> null
        }

        dep?.let {
            builder.add(it)
        }
    }
    return builder
}

class DependenciesInfoInputs(
    @get:Input
    val type: DependencyType,
    @get:Input
    val dependency: String,
    @get:Input
    val scope: String
) {
    fun toInfo() = DefaultDependenciesInfo(type, dependency, scope)
}

private fun Iterable<PluginType>.containsAndroid() = any { it.isAndroid }
private fun Iterable<PluginType>.containsKotlin() = any { it.isKotlin }
private fun Iterable<PluginType>.containsJava() = any { it.isJava }
