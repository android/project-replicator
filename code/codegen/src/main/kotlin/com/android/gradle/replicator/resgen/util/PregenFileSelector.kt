package com.android.gradle.replicator.resgen.util

import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.random.Random

enum class FileTypes {
    PNG, NINE_PATCH, WEBP, JPEG, GIF, TEXT, JSON, TTF, OTF, TTC
}

/* Singleton cache of file sizes, so we don't recalculate them every time. Example:
 * {
 *   PNG: [("somefile.png", 523), ("anotherfile.png", 346), ("yetanotherfile.png", 456)],
 *
 *   WEBP: [("somefile.webp", 1237), ("anotherfile.webp", 2342), ("yetanotherfile.webp", 123)]
 * }
 */
private data class FileData(val name: String, val size: Long)
private val fileSizeCache = mutableMapOf<FileTypes, MutableList<FileData>>()

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

fun getRandomResourceFromQualifiers(random: Random, type: FileTypes, resourceQualifiers: List<String>): String? {
    val qualifierPrefixes = resourceQualifiers.filter(String::isNotEmpty)

    // Cache is faster if it exists
    val allFiles =
        if (fileSizeCache[type] == null) listResourceFiles(getFolderFromType(type))
        else fileSizeCache[type]!!.map { it.name }

    if (allFiles.isEmpty()) {
        println("e: no pre-generated $type file found")
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

fun getResourceClosestToSize(type: FileTypes, size: Long): String? {
    if (fileSizeCache[type] == null) {
        // Calculate cache
        val allFiles = listResourceFiles(getFolderFromType(type))

        fileSizeCache[type] = mutableListOf()

        val loader = Thread.currentThread().contextClassLoader
        allFiles.forEach { fileName ->
            // getContentLengthLong gets the uncompressed file size
            fileSizeCache[type]!!.add(
                FileData(fileName, loader.getResource(fileName)!!.openConnection().contentLengthLong))
        }
    }
    val cachedFiles = fileSizeCache[type]!!
    if (cachedFiles.isEmpty()) {
        println("e: no pre-generated $type file found")
        return null
    }
    val closest = cachedFiles.minBy { abs(it.size - size) }!!
    println("closest file to $size is ${closest.name} ${closest.size}")
    return closest.name
}

fun copyResourceFile(resourcePath: String, output: File) {
    println("Copying from ${resourcePath.split("/").last()}")
    val loader = Thread.currentThread().contextClassLoader
    loader.getResourceAsStream(resourcePath)!!.use { it.copyTo(FileOutputStream(output)) }
}

fun getFileType(resourceExtension: String): FileTypes? {
    return when (resourceExtension) {
        "png" -> FileTypes.PNG
        "9.png" -> FileTypes.NINE_PATCH
        "gif" -> FileTypes.GIF
        "jpg" -> FileTypes.JPEG
        "webp" -> FileTypes.WEBP
        "txt" -> FileTypes.TEXT
        "json" -> FileTypes.JSON
        "ttf" -> FileTypes.TTF
        "otf" -> FileTypes.OTF
        "ttc" -> FileTypes.TTC
        else -> null
    }
}