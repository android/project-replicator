/*
 * Copyright (C) 2021 The Android Open Source Project
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
 *
 */
package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.AbstractAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.DefaultAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.ResourcePropertyType
import com.android.gradle.replicator.resgen.resourceModel.ResourceData
import com.android.gradle.replicator.resgen.util.FileTypes
import com.android.gradle.replicator.resgen.util.VectorDrawableGenerator
import com.android.gradle.replicator.resgen.util.copyResourceFile
import com.android.gradle.replicator.resgen.util.getFileType
import com.android.gradle.replicator.resgen.util.getResourceClosestToSize
import com.google.common.annotations.VisibleForTesting
import java.io.File

class DrawableResourceGenerator (params: ResourceGenerationParams): ResourceGenerator(params) {

    @set:VisibleForTesting
    var numberOfResourceElements: Long?= null

    private val supportedFileTypes = listOf(
            FileTypes.PNG,
            FileTypes.NINE_PATCH,
            FileTypes.GIF,
            FileTypes.JPEG,
            FileTypes.WEBP
    )

    override fun generateResource(
            properties: AbstractAndroidResourceProperties,
            outputFolder: File
    ) {
        // Sanity check. This should not happen unless there is a bug in the metadata reader.
        if (properties.propertyType != ResourcePropertyType.DEFAULT) {
            throw RuntimeException ("Unexpected property type. Got ${properties.propertyType} instead of ${ResourcePropertyType.DEFAULT}")
        }
        (properties as DefaultAndroidResourceProperties).fileData.forEach { fileData ->
            // TODO: generate unique IDs only by qualifiers or folder name so the same resource appears on hdpi and mdpi
            when (properties.extension) {
                "xml" ->  {
                    val fileName = "vector_drawable_${params.uniqueIdGenerator.genIdByCategory("drawable.fileName.vectorDrawable.${properties.qualifiers}")}"
                    val outputFile = File(outputFolder, "$fileName.${properties.extension}")
                    println("Generating ${outputFile.absolutePath}")
                    generateVectorDrawableResource(outputFile, properties.splitQualifiers, fileData)
                    params.resourceModel.resourceList.add(ResourceData(
                        pkg = "",
                        name = fileName,
                        type = "drawable",
                        extension = properties.extension,
                        qualifiers = properties.splitQualifiers))
                }
                else -> {
                    val fileName = "image_${params.uniqueIdGenerator.genIdByCategory("drawable.fileName.image.${properties.qualifiers}")}"
                    val outputFile = File(outputFolder, "$fileName.${properties.extension}")
                    println("Generating ${outputFile.absolutePath}")
                    generateImageResource(outputFile, properties.extension, fileData)
                    params.resourceModel.resourceList.add(ResourceData(
                        pkg = "",
                        name = fileName,
                        type = "drawable",
                        extension = properties.extension,
                        qualifiers = properties.splitQualifiers))
                }
            }
        }
    }

    private fun generateImageResource (
            outputFile: File,
            resourceExtension: String,
            fileSize: Long
    ) {
        val fileType = getFileType(resourceExtension)

        if (fileType == null || fileType !in supportedFileTypes) {
            println("w: unsupported file type $resourceExtension. Skipping.")
            return
        }

        val resourcePath = getResourceClosestToSize(fileType, fileSize) ?: return

        copyResourceFile(resourcePath, outputFile)
    }

    private fun selectMaxImageSize(qualifiers: List<String>): Int {
        qualifiers.forEach {
            when (it) {
                "ldpi" -> return params.constants.vectorImage.MAX_VECTOR_IMAGE_SIZE_SMALL
                "nodpi",
                "anydpi",
                "tvdpi",
                "mdpi"-> return params.constants.vectorImage.MAX_VECTOR_IMAGE_SIZE_MEDIUM
                "hdpi",
                "xhdpi",
                "xxhdpi",
                "xxxhdpi" -> return params.constants.vectorImage.MAX_VECTOR_IMAGE_SIZE_LARGE
                else -> {}
            }
        }
        return params.constants.vectorImage.MAX_VECTOR_IMAGE_SIZE_MEDIUM // NNNDPI also ends up here
    }

    private fun generateVectorDrawableResource(
            outputFile: File,
            resourceQualifiers: List<String>,
            fileLines: Long
    ) {
        val generator = VectorDrawableGenerator(params.random)
        val xmlLines = generator.generateImage(
            generator.getPathVectorsFromLines(fileLines),
            selectMaxImageSize(resourceQualifiers))
        outputFile.writeText(xmlLines.joinToString(System.lineSeparator()))
    }
}