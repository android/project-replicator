package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.resgen.util.genFileNameCharacters
import com.android.gradle.replicator.resgen.util.getFileType
import com.android.gradle.replicator.resgen.util.getRandomResourceFile
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
            val outputFile = File(outputFolder, "raw${genFileNameCharacters(files)}${resourceExtension}")
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

        val file = getRandomResourceFile(random, fileType, resourceQualifiers) ?: return

        file.copyTo(outputFile)
    }
}