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

import com.android.gradle.replicator.resgen.util.*
import com.google.common.annotations.VisibleForTesting
import java.io.File
import java.lang.RuntimeException
import kotlin.random.Random

private const val MAX_VECTOR_IMAGE_SIZE_SMALL = 100
private const val MAX_VECTOR_IMAGE_SIZE_MEDIUM = 150
private const val MAX_VECTOR_IMAGE_SIZE_LARGE = 200
private const val MAX_VECTOR_IMAGE_LINES_SMALL = 50
private const val MAX_VECTOR_IMAGE_LINES_MEDIUM = 75
private const val MAX_VECTOR_IMAGE_LINES_LARGE = 100

class DrawableResourceGenerator (val random: Random): ResourceGenerator {

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
            throw RuntimeException("unsupported file type $resourceExtension")
        }

        val resourcePath = getRandomResource(random, fileType, resourceQualifiers) ?: return

        copyResourceFile(resourcePath, outputFile)
    }

    private fun selectNumberOfResourceElements(qualifiers: List<String>): Int {
        if (numberOfResourceElements != null) return numberOfResourceElements!!

        qualifiers.forEach {
            when (it) {
                "ldpi" -> return MAX_VECTOR_IMAGE_LINES_SMALL
                "nodpi",
                "anydpi",
                "tvdpi",
                "mdpi"-> return MAX_VECTOR_IMAGE_LINES_MEDIUM
                "hdpi",
                "xhdpi",
                "xxhdpi",
                "xxxhdpi" -> return MAX_VECTOR_IMAGE_LINES_LARGE
                else -> if (it.endsWith("dpi")) return MAX_VECTOR_IMAGE_LINES_MEDIUM // NNNDPI
            }
        }
        return MAX_VECTOR_IMAGE_SIZE_MEDIUM
    }

    private fun selectMaxImageSize(qualifiers: List<String>): Int {
        qualifiers.forEach {
            when (it) {
                "ldpi" -> return MAX_VECTOR_IMAGE_SIZE_SMALL
                "nodpi",
                "anydpi",
                "tvdpi",
                "mdpi"-> return MAX_VECTOR_IMAGE_SIZE_MEDIUM
                "hdpi",
                "xhdpi",
                "xxhdpi",
                "xxxhdpi" -> return MAX_VECTOR_IMAGE_SIZE_LARGE
                else -> if (it.endsWith("dpi")) return MAX_VECTOR_IMAGE_SIZE_MEDIUM // NNNDPI
            }
        }
        return MAX_VECTOR_IMAGE_SIZE_MEDIUM
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