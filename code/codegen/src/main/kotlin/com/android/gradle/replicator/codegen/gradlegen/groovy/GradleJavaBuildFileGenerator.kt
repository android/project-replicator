package com.android.gradle.replicator.codegen.gradlegen.groovy

import com.android.gradle.replicator.codegen.PrettyPrintStream
import com.android.gradle.replicator.codegen.gradlegen.BuildFileGenerator

class GradleJavaBuildFileGenerator: BuildFileGenerator {
    override fun generate(dependencies: List<String>, printer: PrettyPrintStream) {
        printer.addBlock("plugins {")
        printer.printlnIndented("id 'java'\n")
        printer.endBlock()
        printer.println()
        printer.addBlock("repositories { ")
        printer.printlnIndented("jcenter()")
        printer.endBlock()
        printer.addBlock("dependencies {")
        dependencies.forEach {
            printer.printlnIndented("implementation '$it'")
        }
        printer.endBlock()
    }
}