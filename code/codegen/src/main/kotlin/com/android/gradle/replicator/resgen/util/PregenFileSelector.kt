package com.android.gradle.replicator.resgen.util

import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

enum class FileTypes {
    PNG, NINE_PATCH, WEBP, JPEG, GIF, TEXT, JSON, TTF, OTF, TTC
}

private fun listResourceFiles(folder: String): List<String> {
    val reflections = Reflections("resgen", ResourcesScanner())
    return reflections.getResources { true }.filter { it.startsWith(folder) }
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

fun getRandomResource(random: Random, type: FileTypes, resourceQualifiers: List<String>): String? {
    val qualifierPrefixes = resourceQualifiers.filter {
        it.isNotEmpty()
    }

    val allFiles = listResourceFiles(getFolderFromType(type))

    if (allFiles.isEmpty()) {
        System.err.println("No pre-generated $type file found")
        return null
    }

    val filteredFiles = allFiles.filter {
        qualifierPrefixes.any { prefix ->
            it.split("/").last().startsWith(prefix)
        }
    }

    // If specific file does not exist, get generic one
    return if (filteredFiles.isEmpty()) allFiles.sorted().random(random) else filteredFiles.sorted().random(random)
}

fun copyResourceFile(resourcePath: String, output: File) {
    println("Copying from ${resourcePath.split("/").last()}")
    val loader = Thread.currentThread().contextClassLoader
    loader.getResourceAsStream(resourcePath)!!.copyTo(FileOutputStream(output))
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