package com.android.gradle.replicator.codegen.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import com.android.gradle.replicator.codegen.Main

abstract class GenerateCode: DefaultTask() {

    @get:InputFile
    abstract val parameters: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun taskAction() {
        // generate files.
        Main().process(
                arrayOf(
                        "-gen", "Java",
                        "-module", path.removeSuffix(":$name").removePrefix(":").replace(':', '_'),
                        "-i", parameters.get().asFile.absolutePath,
                        "-o", outputDirectory.get().asFile.absolutePath
                )
        )
        println("Done with $name")
    }
}