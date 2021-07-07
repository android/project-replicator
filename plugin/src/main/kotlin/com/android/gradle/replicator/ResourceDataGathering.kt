package com.android.gradle.replicator

import com.android.gradle.replicator.model.internal.resources.AbstractAndroidResourceProperties
import com.android.gradle.replicator.model.internal.resources.AndroidDefaultResourceProperties
import com.android.gradle.replicator.model.internal.resources.AndroidSizeMattersResourceProperties
import com.android.gradle.replicator.model.internal.resources.AndroidValuesResourceProperties
import java.io.File


// Method to create the correct data class. Need to make this so it scans the files appropriately
fun processResourceFiles(resourceType: String, qualifiers: String, extension: String, resourceFiles: Set<File>):
        AbstractAndroidResourceProperties {
    return when(resourceType) {
        "values" -> getValuesResourceData(qualifiers, extension, resourceFiles)
        "drawable",
        "mipmap",
        "raw" -> getSizeMattersResourceData(qualifiers, extension, resourceFiles)
        else -> getDefaultResourceData(qualifiers, extension, resourceFiles)
    }
}

// Methods to parse specific resources
fun getValuesResourceData(qualifiers: String, extension: String, resourceFiles: Set<File>): AndroidValuesResourceProperties {
    return AndroidValuesResourceProperties(
            qualifiers = qualifiers,
            extension = extension,
            quantity = resourceFiles.size,
            valueTypeCount = mutableListOf<Map<String, Int>>().apply {
                resourceFiles.forEach {
                    this.add(parseValuesFile(it))
                }
            }
    )

}
fun getSizeMattersResourceData(qualifiers: String, extension: String, resourceFiles: Set<File>):
        AndroidSizeMattersResourceProperties {
    return AndroidSizeMattersResourceProperties(
            qualifiers = qualifiers,
            extension = extension,
            quantity = resourceFiles.size,
            fileSizes = mutableListOf<Long>().apply {
                resourceFiles.forEach {
                    this.add(it.length())
                }
            }
    )
}

fun getDefaultResourceData(qualifiers: String, extension: String, resourceFiles: Set<File>): AbstractAndroidResourceProperties {
    return AndroidDefaultResourceProperties(
            qualifiers = qualifiers,
            extension = extension,
            quantity = resourceFiles.size
    )
}

fun parseValuesFile(valuesFile: File): Map<String, Int> {
    return mapOf()
}