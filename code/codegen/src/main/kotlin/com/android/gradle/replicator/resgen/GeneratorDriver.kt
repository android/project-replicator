package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.resources.AbstractAndroidResourceProperties
import java.io.File
import kotlin.random.Random

class GeneratorDriver (val random: Random) {
    // TODO: implement other generators
    private fun getGenerator(random: Random, resourceType: String): ResourceGenerator? {
        return when(resourceType) {
            //"animator" ->
            //"anim" ->
            //"color" ->
            "drawable" -> DrawableResourceGenerator(random)
            "font" -> FontResourceGenerator(random)
            //"layout" ->
            //"menu" ->
            "mipmap" -> DrawableResourceGenerator(random)
            "raw" -> RawResourceGenerator(random)
            //"transition" ->
            "values" -> ValueResourceGenerator(random)
            //"xml" ->
            else -> null
        }
    }

    fun generateResources(outputFolder: File, resourceType: String, resourceProperties: AbstractAndroidResourceProperties) {
        // TODO: Separate resource properties by type
        val generator = getGenerator(random, resourceType)

        // empty qualifiers means the folder is unqualified, as in "mipmap" instead of "mipmap-hidpi"
        val qualifiedFolder =
                if (resourceProperties.qualifiers.isEmpty()) {
                    File(outputFolder, resourceType)
                } else {
                    File(outputFolder, "${resourceType}-${resourceProperties.qualifiers}")
                }
        qualifiedFolder.mkdirs()

        generator?.generateResource(
                number = resourceProperties.quantity,
                outputFolder = qualifiedFolder,
                // resources can have more than one qualifier
                resourceQualifiers =
                if (resourceProperties.qualifiers.isEmpty()) listOf() else resourceProperties.qualifiers.split("-"),
                resourceExtension = resourceProperties.extension
        ) ?: println("w: unsupported resource type $resourceType. Skipping.")
    }
}