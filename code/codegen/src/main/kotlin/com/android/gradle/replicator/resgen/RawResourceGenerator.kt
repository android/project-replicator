package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.resgen.util.UniqueIdGenerator
import com.android.gradle.replicator.resgen.util.copyResourceFile
import com.android.gradle.replicator.resgen.util.getFileType
import com.android.gradle.replicator.resgen.util.getRandomResource
import java.io.File
import kotlin.random.Random

class RawResourceGenerator (val random: Random): ResourceGenerator {
    var files = 0

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
            val outputFile = File(outputFolder, "raw_${UniqueIdGenerator.genIdByCategory("raw.fileName")}.${resourceExtension}")
            println("Generating ${outputFile.absolutePath}")
            generateRawResource(outputFile, resourceQualifiers, resourceExtension)
            files++
        }
    }

    private fun generateRawResource (
            outputFile: File,
            resourceQualifiers: List<String>,
            resourceExtension: String
    ) {
        val fileType = getFileType(resourceExtension)!!

        val resourcePath = getRandomResource(random, fileType, resourceQualifiers) ?: return

        copyResourceFile(resourcePath, outputFile)
    }
}