package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.AbstractAndroidResourceProperties
import com.android.gradle.replicator.resgen.resourceModel.ResourceModel
import com.android.gradle.replicator.resgen.util.ResgenConstants
import com.android.gradle.replicator.resgen.util.UniqueIdGenerator
import java.io.File
import kotlin.random.Random

class GeneratorDriver (val random: Random, private val uniqueIdGenerator: UniqueIdGenerator) {
    // TODO: implement other generators
    private fun getGenerator(random: Random, resourceType: String, constants: ResgenConstants, resourceModel: ResourceModel): ResourceGenerator? {
        val params = ResourceGenerationParams(random, constants, uniqueIdGenerator, resourceModel)
        return when(resourceType) {
            //"animator" ->
            //"anim" ->
            //"color" ->
            "drawable" -> DrawableResourceGenerator(params)
            "font" -> FontResourceGenerator(params)
            //"layout" ->
            //"menu" ->
            "mipmap" -> DrawableResourceGenerator(params)
            "raw" -> RawResourceGenerator(params)
            //"transition" ->
            "values" -> ValueResourceGenerator(params)
            //"xml" ->
            else -> null
        }
    }

    fun generateResources(
        outputFolder: File,
        resourceType: String,
        resourceProperties: AbstractAndroidResourceProperties,
        resgenConstants: ResgenConstants,
        resourceModel: ResourceModel) {

        // TODO: Generate resources based on property type (size matters, values, etc.)
        val generator = getGenerator(random, resourceType, resgenConstants, resourceModel)

        // empty qualifiers means the folder is unqualified, as in "mipmap" instead of "mipmap-hidpi"
        val qualifiedFolder =
                if (resourceProperties.qualifiers.isEmpty()) {
                    File(outputFolder, resourceType)
                } else {
                    File(outputFolder, "${resourceType}-${resourceProperties.qualifiers}")
                }
        qualifiedFolder.mkdirs()

        generator?.generateResource(
                properties = resourceProperties,
                outputFolder = qualifiedFolder
        ) ?: println("w: unsupported resource type $resourceType. Skipping.")
    }
}