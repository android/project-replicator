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

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm")

    id("default-config")
}

abstract class GenerateParamsTask : DefaultTask() {
    @get:Classpath
    abstract val jarFiles: ListProperty<RegularFile>

    @get:OutputFile
    abstract val paramsFile: RegularFileProperty

    @get:Input
    abstract val gradleDependencies: ListProperty<String>

    @TaskAction
    fun action() {
        val outputFile = paramsFile.get().asFile
        println("Writing params to ${outputFile.absolutePath}")
        outputFile.writeText(
                """
                    classpath=${jarFiles.get().joinToString(separator=",") { it.asFile.absolutePath }}
                    dependencies=${gradleDependencies.get().joinToString(separator=",")}
                """.trimIndent()
        )
    }
}

val testClasspath: Configuration by configurations.creating
configurations.named("testClasspath") {
    isTransitive = true
}

val testParameters = File("$buildDir/test.params")

val generateTask = tasks.register("generate", GenerateParamsTask::class.java) {
    val config = configurations.get("testClasspath")
    val jarFiltered = config.elements.map {
        it.map {
            it.asFile
        }.filter {
            it.isFile
        }.map {
            layout.projectDirectory.file(it.absolutePath)
        }
    }

    config.allDependencies.forEach {
        gradleDependencies.add("${it.group}:${it.name}:${it.version}")
    }

    jarFiles.set(jarFiltered)
    paramsFile.set(testParameters)
}

tasks.test {
    systemProperty("parameter.file", testParameters.absolutePath)
}

tasks["test"].dependsOn(generateTask)

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(project(":code:codegen"))
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.13")

    testClasspath("junit:junit:4.13")
    testClasspath("com.google.guava:guava:30.0-jre")
}
