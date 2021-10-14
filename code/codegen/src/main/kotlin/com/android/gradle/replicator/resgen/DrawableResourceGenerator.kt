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

import com.android.gradle.replicator.resgen.util.FileTypes
import com.android.gradle.replicator.resgen.util.ResgenConstants
import com.android.gradle.replicator.resgen.util.VectorDrawableGenerator
import com.android.gradle.replicator.resgen.util.copyResourceFile
import com.android.gradle.replicator.resgen.util.genFileNameCharacters
import com.android.gradle.replicator.resgen.util.getFileType
import com.android.gradle.replicator.resgen.util.getRandomResource
import com.google.common.annotations.VisibleForTesting
import java.io.File
import kotlin.random.Random

class DrawableResourceGenerator (val random: Random, val constants: ResgenConstants): ResourceGenerator {

    @set:VisibleForTesting
    var numberOfResourceElements: Int?= null

    private val supportedFileTypes = listOf(
            FileTypes.PNG,
            FileTypes.NINE_PATCH,
            FileTypes.GIF,
            FileTypes.JPEG,
            FileTypes.WEBP
    )

    var imageFiles = 0
    var xmlFiles = 0

    override fun generateResource(
            number: Int,
            outputFolder: File,
            resourceQualifiers: List<String>,
            resourceExtension: String
    ) {
        repeat(number) {
            when (resourceExtension) {
                ".xml" ->  {
                    val outputFile = File(outputFolder, "xml${genFileNameCharacters(xmlFiles)}${resourceExtension}")
                    println("Generating ${outputFile.absolutePath}")
                    generateVectorDrawableResource(outputFile, resourceQualifiers)
                    xmlFiles++
                }
                else -> {
                    val outputFile = File(outputFolder, "image${genFileNameCharacters(imageFiles)}${resourceExtension}")
                    println("Generating ${outputFile.absolutePath}")
                    generateImageResource(outputFile, resourceQualifiers, resourceExtension)
                    imageFiles++
                }
            }
        }
    }

    private fun generateImageResource (
            outputFile: File,
            resourceQualifiers: List<String>,
            resourceExtension: String
    ) {
        val fileType = getFileType(resourceExtension)

        if (fileType == null || fileType !in supportedFileTypes) {
            println("w: unsupported file type $resourceExtension. Skipping.")
            return
        }

        val resourcePath = getRandomResource(random, fileType, resourceQualifiers) ?: return

        copyResourceFile(resourcePath, outputFile)
    }

    private fun selectNumberOfResourceElements(qualifiers: List<String>): Int {
        if (numberOfResourceElements != null) return numberOfResourceElements!!

        qualifiers.forEach {
            when (it) {
                "ldpi" -> return constants.vectorImage.MAX_VECTOR_IMAGE_LINES_SMALL
                "nodpi",
                "anydpi",
                "tvdpi",
                "mdpi"-> return constants.vectorImage.MAX_VECTOR_IMAGE_LINES_MEDIUM
                "hdpi",
                "xhdpi",
                "xxhdpi",
                "xxxhdpi" -> return constants.vectorImage.MAX_VECTOR_IMAGE_LINES_LARGE
                else -> {}
            }
        }
        return constants.vectorImage.MAX_VECTOR_IMAGE_SIZE_MEDIUM // NNNDPI also ends up here
    }

    private fun selectMaxImageSize(qualifiers: List<String>): Int {
        qualifiers.forEach {
            when (it) {
                "ldpi" -> return constants.vectorImage.MAX_VECTOR_IMAGE_SIZE_SMALL
                "nodpi",
                "anydpi",
                "tvdpi",
                "mdpi"-> return constants.vectorImage.MAX_VECTOR_IMAGE_SIZE_MEDIUM
                "hdpi",
                "xhdpi",
                "xxhdpi",
                "xxxhdpi" -> return constants.vectorImage.MAX_VECTOR_IMAGE_SIZE_LARGE
                else -> {}
            }
        }
        return constants.vectorImage.MAX_VECTOR_IMAGE_SIZE_MEDIUM // NNNDPI also ends up here
    }

    private fun generateVectorDrawableResource(
            outputFile: File,
            resourceQualifiers: List<String>
    ) {
        val generator = VectorDrawableGenerator(random)
        val xmlLines = generator.generateImage(selectNumberOfResourceElements(resourceQualifiers), selectMaxImageSize(resourceQualifiers))
        outputFile.writeText(xmlLines.joinToString(System.lineSeparator()))
    }
}