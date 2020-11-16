package com.android.gradle.replicator.codegen.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import kotlin.random.Random

abstract class GenerateParamsTask: DefaultTask() {

    @get:Classpath
    abstract val runtimeClasspath: ConfigurableFileCollection

    @get:Classpath
    abstract val codeGeneratedModuleApiClasspath: ConfigurableFileCollection

    @get:Classpath
    abstract val codeGeneratedModuleImplClasspath: ConfigurableFileCollection

    @get:Classpath
    abstract val apiJarFiles: ConfigurableFileCollection

    @get:Classpath
    abstract val implJarFiles: ConfigurableFileCollection

    @get:OutputFile
    abstract val paramsFile: RegularFileProperty

    @get:Input
    abstract val gradleDependencies: ListProperty<String>

    @get:Input
    abstract val seed: Property<Int>

    @TaskAction
    fun action() {
        val outputFile = paramsFile.get().asFile
        println("Writing params to ${outputFile.absolutePath}")
        val randomizer = if (seed.isPresent) Random(seed.get()) else Random
        // each module will get a random seed.
        outputFile.writeText(
                """
                    seed=${randomizer.nextInt()}
                    runtimeClasspath=${runtimeClasspath.files.joinToString(separator = ",") { it.absolutePath }}
                    codeGeneratedModuleApiClasspath=${codeGeneratedModuleApiClasspath.files.joinToString(separator = ",") { it.absolutePath }}
                    codeGeneratedModuleImplClasspath=${codeGeneratedModuleImplClasspath.files.joinToString(separator = ",") { it.absolutePath }}
                    apiClasspath=${apiJarFiles.files.joinToString(separator = ",") { it.absolutePath }}
                    implClasspath=${implJarFiles.files.joinToString(separator = ",") { it.absolutePath }}
                    dependencies=${gradleDependencies.get().joinToString(separator = ",")}
                """.trimIndent()
        )
    }
}
