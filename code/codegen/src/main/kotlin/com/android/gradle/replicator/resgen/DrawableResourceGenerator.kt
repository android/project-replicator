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
import com.android.gradle.replicator.resgen.util.ResourceQualifiers
import com.android.gradle.replicator.resgen.util.genFileNameCharacters
import com.android.gradle.replicator.resgen.util.getRandomResourceFile
import com.google.common.annotations.VisibleForTesting
import java.io.File
import java.lang.RuntimeException
import kotlin.random.Random

class DrawableResourceGenerator (val random: Random): ResourceGenerator {

    @set:VisibleForTesting
    var numberOfResourceElements: Int?= null

    override fun generateResource(
            number: Int,
            outputFolder: File,
            resourceQualifiers: List<String>,
            resourceExtension: String
    ) {
        var imageFiles = 0
        var xmlFiles = 0

        var qualifier: ResourceQualifiers? = null

        for(it in resourceQualifiers) {
            when (it) {
                "nodpi" -> qualifier = ResourceQualifiers.NODPI
                "anydpi" -> qualifier = ResourceQualifiers.ANYDPI
                "ldpi" -> qualifier = ResourceQualifiers.LDPI
                "mdpi" -> qualifier = ResourceQualifiers.MDPI
                "hdpi" -> qualifier = ResourceQualifiers.HDPI
                "xhdpi" -> qualifier = ResourceQualifiers.XHDPI
                "xxhdpi" -> qualifier = ResourceQualifiers.XXHDPI
                "xxxhdpi" -> qualifier = ResourceQualifiers.XXXHDPI
                else -> {}
            }
            // Only want the first one. These qualifiers are mutually exclusive
            if (qualifier != null) break
        }
        repeat(number) {
            //val type = ResourceType.values().random(random)
            when (resourceExtension) {
                ".xml" ->  {
                    val outputFile = File(outputFolder, "xml${genFileNameCharacters(xmlFiles)}${resourceExtension}")
                    println("Generating ${outputFile.absolutePath}")
                    generateXmlResource(outputFile, qualifier)
                    xmlFiles++
                }
                else -> {
                    val outputFile = File(outputFolder, "image${genFileNameCharacters(imageFiles)}${resourceExtension}")
                    println("Generating ${outputFile.absolutePath}")
                    generateImageResource(outputFile, qualifier, resourceExtension)
                    imageFiles++
                }
            }
        }
    }

    private fun generateImageResource (
            outputFile: File,
            qualifier: ResourceQualifiers?,
            resourceExtension: String
    ) {
        val fileType = when (resourceExtension) {
            ".png" -> FileTypes.PNG
            ".9.png" -> FileTypes.NINE_PATCH
            ".gif" -> FileTypes.GIF
            ".jpg" -> FileTypes.JPEG
            else -> throw RuntimeException("unsupported file type $resourceExtension")
        }

        val image = getRandomResourceFile(random, fileType, qualifier)

        image.copyTo(outputFile)
    }

    private fun generateXmlResource(
            outputFile: File,
            qualifiers: ResourceQualifiers?
    ) {
        // TODO: Implement this
        //outputFile.writeText(xmlLines.joinToString(System.lineSeparator()))
    }
}