package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.resgen.util.UniqueIdGenerator
import com.android.gradle.replicator.resgen.util.copyResourceFile
import com.android.gradle.replicator.resgen.util.getFileType
import com.android.gradle.replicator.resgen.util.getRandomResource
import java.io.File
import kotlin.random.Random

class FontResourceGenerator(
    private val random: Random,
    private val uniqueIdGenerator: UniqueIdGenerator): ResourceGenerator {

    override fun generateResource(
            number: Int,
            outputFolder: File,
            resourceQualifiers: List<String>,
            resourceExtension: String
    ) {
        if (getFileType(resourceExtension) == null) {
            println("Unsupported file type $resourceExtension")
            return
        }
        repeat(number) {
            val outputFile = File(outputFolder, "font_${uniqueIdGenerator.genIdByCategory("font.fileName")}.${resourceExtension}")
            when (resourceExtension) {
                "xml" ->  {
                    println("Generating ${outputFile.absolutePath}")
                    generateFontReferenceResource(outputFile, resourceQualifiers)
                }
                else -> {
                    println("Generating ${outputFile.absolutePath}")
                    generateFontResource(outputFile, resourceQualifiers, resourceExtension)
                }
            }
        }
    }

    private fun generateFontResource (
            outputFile: File,
            resourceQualifiers: List<String>,
            resourceExtension: String
    ) {
        val fileType = getFileType(resourceExtension)!!

        val resourcePath = getRandomResource(random, fileType, resourceQualifiers) ?: return

        copyResourceFile(resourcePath, outputFile)
    }

    private fun generateFontReferenceResource (
            outputFile: File,
            resourceQualifiers: List<String>
    ) {
        // TODO: Implement this (needs resource context)
    }
}