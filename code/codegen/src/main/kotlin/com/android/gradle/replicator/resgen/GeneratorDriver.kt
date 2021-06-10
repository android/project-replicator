package com.android.gradle.replicator.resgen

import java.io.File
import kotlin.random.Random

class GeneratorDriver (val random: Random) {
    // TODO: implement other generators
    private fun getGenerator(random: Random, resourceType: String): ResourceGenerator {
        return when(resourceType) {
            //"animator" ->
            //"anim" ->
            //"color" ->
            //"drawable" ->
            //"font" ->
            //"layout" ->
            //"menu" ->
            //"mipmap" ->
            //"raw" ->
            //"transition" ->
            "values" -> ValueResourceGenerator(random)
            //"xml" ->
            else -> throw RuntimeException("Unsupported resource type $resourceType")
        }
    }

    fun generateResources(outputFolder: File, resourceType: String, resourceProperties: AndroidResourceProperties) {
        val generator = getGenerator(random, resourceType)

        // empty qualifiers means the folder is unqualified, as in "mipmap" instead of "mipmap-hidpi"
        val qualifiedFolder =
                if (resourceProperties.qualifiers.isEmpty()) {
                    File(outputFolder, resourceType)
                } else {
                    File(outputFolder, "${resourceType}-${resourceProperties.qualifiers}")
                }
        qualifiedFolder.mkdirs()

        generator.generateResource(
                number = resourceProperties.quantity,
                outputFolder = qualifiedFolder,
                resourceQualifier = resourceProperties.qualifiers,
                resourceExtension = resourceProperties.extension
        )
    }
}