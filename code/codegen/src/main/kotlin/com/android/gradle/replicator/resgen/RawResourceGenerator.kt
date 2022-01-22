package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.AbstractAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.DefaultAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.ResourcePropertyType
import com.android.gradle.replicator.resgen.resourceModel.ResourceData
import com.android.gradle.replicator.resgen.util.copyResourceFile
import com.android.gradle.replicator.resgen.util.getFileType
import com.android.gradle.replicator.resgen.util.getResourceClosestToSize
import java.io.File

class RawResourceGenerator (params: ResourceGenerationParams): ResourceGenerator(params) {
    override fun generateResource(
        properties: AbstractAndroidResourceProperties,
        outputFolder: File
    ) {
        // Sanity check
        if (properties.propertyType != ResourcePropertyType.DEFAULT) {
            throw RuntimeException ("Unexpected property type. Got ${properties.propertyType} instead of ${ResourcePropertyType.DEFAULT}")
        }
        if (getFileType(properties.extension) == null) {
            println("Unsupported file type $properties.extension")
            return
        }
        (properties as DefaultAndroidResourceProperties).fileData.forEach { fileSize ->
            val fileName = "raw_${params.uniqueIdGenerator.genIdByCategory("raw.fileName.${properties.qualifiers}")}"
            val outputFile = File(outputFolder, "$fileName.${properties.extension}")
            println("Generating ${outputFile.absolutePath}")
            generateRawResource(outputFile, properties.extension, fileSize)

            params.resourceModel.resourceList.add(
                ResourceData(
                    pkg = "",
                    name = fileName,
                    type = "raw",
                    extension = properties.extension,
                    qualifiers = properties.splitQualifiers)
            )
        }
    }

    // TODO: generate random bytes for unknown resource types
    private fun generateRawResource (
            outputFile: File,
            resourceExtension: String,
            fileSize: Long
    ) {
        val fileType = getFileType(resourceExtension)!!

        val resourcePath = getResourceClosestToSize(fileType, fileSize) ?: return

        copyResourceFile(resourcePath, outputFile)
    }
}