package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.AbstractAndroidResourceProperties
import com.android.gradle.replicator.resgen.util.ResgenConstants
import com.android.gradle.replicator.resgen.util.UniqueIdGenerator
import java.io.File
import kotlin.random.Random

class GeneratorDriver (val random: Random, private val uniqueIdGenerator: UniqueIdGenerator) {
    // TODO: implement other generators
    private fun getGenerator(random: Random, resourceType: String, constants: ResgenConstants): ResourceGenerator? {
        return when(resourceType) {
            //"animator" ->
            //"anim" ->
            //"color" ->
            "drawable" -> DrawableResourceGenerator(random, constants, uniqueIdGenerator)
            "font" -> FontResourceGenerator(random, uniqueIdGenerator)
            //"layout" ->
            //"menu" ->
            "mipmap" -> DrawableResourceGenerator(random, constants, uniqueIdGenerator)
            "raw" -> RawResourceGenerator(random, uniqueIdGenerator)
            //"transition" ->
            "values" -> ValueResourceGenerator(random, constants, uniqueIdGenerator)
            //"xml" ->
            else -> null
        }
    }

    fun generateResources(
        outputFolder: File,
        resourceType: String,
        resourceProperties: AbstractAndroidResourceProperties,
        resgenConstants: ResgenConstants) {

        // TODO: Generate resources based on property type (size matters, values, etc.)
        val generator = getGenerator(random, resourceType, resgenConstants)

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