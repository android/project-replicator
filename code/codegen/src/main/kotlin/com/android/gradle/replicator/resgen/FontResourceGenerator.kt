package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.AbstractAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.DefaultAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.ResourcePropertyType
import com.android.gradle.replicator.resgen.resourceModel.ResourceData
import com.android.gradle.replicator.resgen.util.copyResourceFile
import com.android.gradle.replicator.resgen.util.getFileType
import com.android.gradle.replicator.resgen.util.getResourceClosestToSize
import java.io.File

class FontResourceGenerator(params: ResourceGenerationParams): ResourceGenerator(params) {

    override fun generateResource(
        properties: AbstractAndroidResourceProperties,
        outputFolder: File
    ) {
        // Sanity check. This should not happen unless there is a bug in the metadata reader.
        if (properties.propertyType != ResourcePropertyType.DEFAULT) {
            throw RuntimeException ("Unexpected property type. Got ${properties.propertyType} instead of ${ResourcePropertyType.DEFAULT}")
        }
        if (getFileType(properties.extension) == null) {
            println("Unsupported file type $properties.extension")
            return
        }
        (properties as DefaultAndroidResourceProperties).fileData.forEach { fileSize ->
            val fileName = "font_${params.uniqueIdGenerator.genIdByCategory("font.fileName.${properties.qualifiers}")}"
            val outputFile = File(outputFolder, "$fileName.${properties.extension}")
            when (properties.extension) {
                "xml" ->  {
                    println("Generating ${outputFile.absolutePath}")
                    generateFontReferenceResource(outputFile, properties.splitQualifiers, fileSize)
                }
                else -> {
                    println("Generating ${outputFile.absolutePath}")
                    generateFontResource(outputFile, properties.extension, fileSize)
                }
            }
            params.resourceModel.resourceList.add(
                ResourceData(
                pkg = "",
                name = fileName,
                type = "font",
                extension = properties.extension,
                qualifiers = properties.splitQualifiers)
            )
        }
    }

    private fun generateFontResource (
            outputFile: File,
            resourceExtension: String,
            fileSize: Long
    ) {
        val fileType = getFileType(resourceExtension)!!

        val resourcePath = getResourceClosestToSize(fileType, fileSize) ?: return

        copyResourceFile(resourcePath, outputFile)
    }

    private fun generateFontReferenceResource (
            outputFile: File,
            resourceQualifiers: List<String>,
            fileSize: Long
    ) {
        // TODO: Implement this (needs resource context)
    }
}