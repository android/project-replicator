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

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

class AgpFunctionalTestPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(VersionPlugin::class.java)

        project.plugins.withType(JavaLibraryPlugin::class.java) {
            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            val functionalSourceSet = sourceSets.create("functionalTest")

            val pluginExtension = project.extensions.getByType(GradlePluginDevelopmentExtension::class.java)
            pluginExtension.testSourceSets(functionalSourceSet)

            project.configurations.getByName("functionalTestImplementation").extendsFrom(project.configurations.getByName("testImplementation"))

            val functionalTest = project.tasks.register("functionalTest", Test::class.java) {
                it.testClassesDirs = functionalSourceSet.output.classesDirs
                it.classpath = functionalSourceSet.runtimeClasspath
            }

            val agpClasspath = project.configurations.maybeCreate("agpClasspath")
            val outputFileLocation = project.layout.buildDirectory.file("agpclasspath.txt")

            val agpTask = project.tasks.register("agpTask", AgpClasspathTask::class.java) {
                it.classpath.from(agpClasspath.incoming.artifacts.artifactFiles)
                it.outputFile.set(outputFileLocation)
            }

            functionalTest.configure {
                @Suppress("UnstableApiUsage")
                it.systemProperty("agp.classpath", outputFileLocation.forUseAtConfigurationTime().get().asFile.absolutePath)
                it.environment("ANDROID_SDK_ROOT", System.getenv("ANDROID_SDK_ROOT"))
                it.environment("AGP_VERSION", Versions.agpVersion)
                it.environment("KOTLIN_VERSION", Versions.kotlinVersion)
                it.environment("GRADLE_VERSION", Versions.gradleVersion)
                it.dependsOn(agpTask)
            }

            project.dependencies.apply {
                add(agpClasspath.name, "com.android.tools.build:gradle:${Versions.agpVersion}")
                add(agpClasspath.name, "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}")
            }

            val initScriptLinter = project.tasks.register("initScriptLinter", DefaultTask::class.java) {
                it.doLast {
                    val pluginVersion = "${Versions.pluginArtifactId}:${Versions.pluginVersion}"
                    require(
                        project.rootProject.file("initscript/init.gradle").readText().contains(pluginVersion)
                    ) {
                        throw AssertionError("init script does not reference $pluginVersion")
                    }
                }
            }

            project.tasks.named("check") {
                it.dependsOn(functionalTest)
                it.dependsOn(initScriptLinter)
            }
        }
    }
}
