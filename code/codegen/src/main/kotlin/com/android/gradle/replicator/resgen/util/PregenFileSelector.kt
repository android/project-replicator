package com.android.gradle.replicator.resgen.util

import java.io.File
import kotlin.random.Random

enum class FileTypes {
    PNG, NINE_PATCH, WEBP, JPEG, GIF, TEXT, JSON, TTF, OTF, TTC
}

private fun listResourceFiles(folder: String): List<File> {
    val loader = Thread.currentThread().contextClassLoader
    val url = loader.getResource(folder)!!
    val path: String = url.path
    return File(path).listFiles()?.asList() ?: listOf()
}

private fun getFolderFromType(type: FileTypes): String {
    val typeFolderName =  when(type) {
        FileTypes.PNG -> "images/png"
        FileTypes.NINE_PATCH -> "images/9png"
        FileTypes.WEBP -> "images/webp"
        FileTypes.JPEG -> "images/jpeg"
        FileTypes.GIF -> "images/gif"
        FileTypes.TEXT -> "txt"
        FileTypes.JSON -> "json"
        FileTypes.TTF -> "fonts/ttf"
        FileTypes.OTF -> "fonts/otf"
        FileTypes.TTC -> "fonts/ttc"
    }
    return "resgen/$typeFolderName"
}

fun getRandomResourceFile(random: Random, type: FileTypes, resourceQualifiers: List<String>): File? {
    val qualifierPrefixes = resourceQualifiers.filter {
        it.isNotEmpty()
    }

    val allFiles = listResourceFiles(getFolderFromType(type))

    if (allFiles.isEmpty()) {
        println("No pre-generated file found for $resourceQualifiers")
        return null
    }

    val filteredFiles = allFiles.filter {
        qualifierPrefixes.any { prefix ->
            it.name.startsWith(prefix)
        }
    }

    // If specific file does not exist, get generic one
    return if (filteredFiles.isEmpty()) allFiles.random(random) else filteredFiles.random(random)
}

fun getFileType(resourceExtension: String): FileTypes? {
    return when (resourceExtension) {
        ".png" -> FileTypes.PNG
        ".9.png" -> FileTypes.NINE_PATCH
        ".gif" -> FileTypes.GIF
        ".jpg" -> FileTypes.JPEG
        ".webp" -> FileTypes.WEBP
        ".txt" -> FileTypes.TEXT
        ".json" -> FileTypes.JSON
        ".ttf" -> FileTypes.TTF
        ".otf" -> FileTypes.OTF
        ".ttc" -> FileTypes.TTC
        else -> null
    }
}