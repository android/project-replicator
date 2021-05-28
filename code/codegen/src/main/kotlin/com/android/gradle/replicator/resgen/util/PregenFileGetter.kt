package com.android.gradle.replicator.resgen.util

import java.io.File
import kotlin.random.Random

enum class FileTypes {
    PNG, NINE_PATCH, WEBP, JPEG, GIF
}

enum class ResourceQualifiers {
    LDPI, MDPI, HDPI, XHDPI, XXHDPI, XXXHDPI, ANYDPI, NODPI, TVDPI, NNNDPI
}

private fun listResourceFiles(folder: String): List<File> {
    val loader = Thread.currentThread().contextClassLoader
    val url = loader.getResource(folder)!!
    val path: String = url.path
    return File(path).listFiles()?.asList() ?: listOf()
}

private fun getFolderFromType(type: FileTypes): String {
    val typeFolderName =  when(type) {
        FileTypes.PNG -> "png"
        FileTypes.NINE_PATCH -> "9png"
        FileTypes.WEBP -> "webp"
        FileTypes.JPEG -> "jpeg"
        FileTypes.GIF -> "gif"
    }
    return "resgen/images/$typeFolderName"
}

fun getRandomResourceFile(random: Random, type: FileTypes, qualifier: ResourceQualifiers?): File {
    val qualifierPrefix =  when(qualifier) {
        ResourceQualifiers.LDPI -> "ldpi"
        ResourceQualifiers.MDPI -> "mdpi"
        ResourceQualifiers.HDPI -> "hdpi"
        ResourceQualifiers.XHDPI -> "xhdpi"
        ResourceQualifiers.XXHDPI -> "xxhdpi"
        ResourceQualifiers.XXXHDPI -> "xxxhdpi"
        ResourceQualifiers.ANYDPI -> "anydpi"
        ResourceQualifiers.NODPI -> "nodpi"
        ResourceQualifiers.TVDPI -> "tvdpi"
        ResourceQualifiers.NNNDPI -> "nnndpi"
        else -> ""
    }

    val allFiles = listResourceFiles(getFolderFromType(type))

    val filteredFiles = allFiles.filter { it.name.startsWith(qualifierPrefix) }

    // If specific file does not exist, get generic one
    return if (filteredFiles.isEmpty()) allFiles.random(random) else filteredFiles.random(random)
}