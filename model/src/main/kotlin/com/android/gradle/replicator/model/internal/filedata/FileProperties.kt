package com.android.gradle.replicator.model.internal.filedata

// Supported file types of each resource. Not exhaustive.
val ANDROID_RESOURCE_FOLDER_CONVENTION = mapOf(
        "animator" to listOf("xml"),
        "anim" to listOf("xml"),
        "color" to listOf("xml"),
        "drawable" to listOf("xml", "png", "9.png", "jpg", "gif", "webp"),
        "font" to listOf("ttf", "otf", "ttc", "xml"),
        "layout" to listOf("xml"),
        "menu" to listOf("xml"),
        "mipmap" to listOf("xml", "png", "9.png", "jpg", "gif", "webp"),
        "navigation" to listOf("xml"),
        "raw" to listOf("*"),
        "transition" to listOf("xml"),
        "values" to listOf("xml"),
        "xml" to listOf("xml")
)

/* Data classes to represent the hierarchy for android resources
 * Each resource folder type (values, mipmap, etc.) can have different qualified folders (hidpi, night, etc.)
 * and each of those qualified folders can have different file types in them (AKA extensions)
 * Each element in the resource map is a list of resources to generate for a given folder type, and
 * the elements in the list contain qualifiers, extension and quantity of resources of a particular subtype to generate
 */
abstract class AbstractAndroidResourceProperties (val qualifiers: String, val extension: String, val quantity: Int) {
        abstract val propertyType: ResourcePropertyType

        val splitQualifiers: List<String>
            get() = if (qualifiers.isEmpty()) listOf() else qualifiers.split("-")
}

enum class ResourcePropertyType {
        VALUES,
        DEFAULT
}

data class ValuesMap (
        var stringCount: Int = 0,
        var intCount: Int = 0,
        var boolCount: Int = 0,
        var colorCount: Int = 0,
        var dimenCount: Int = 0,
        var idCount: Int = 0,
        var integerArrayCount: MutableList<Int> = mutableListOf(),
        var arrayCount: MutableList<Int> = mutableListOf(),
        var styleCount: MutableList<Int> = mutableListOf())

// Values need to know how many of each value are in the files
class ValuesAndroidResourceProperties (
        qualifiers: String,
        extension: String,
        quantity: Int,
        val valuesMapPerFile: List<ValuesMap>): AbstractAndroidResourceProperties(qualifiers, extension, quantity) {
                override val propertyType = ResourcePropertyType.VALUES
}

// Images and other resources need the file sizes and XML files need the number of lines to properly replicate
class DefaultAndroidResourceProperties (
        qualifiers: String,
        extension: String,
        quantity: Int,
        val fileData: List<Long>): AbstractAndroidResourceProperties(qualifiers, extension, quantity) {
                override val propertyType = ResourcePropertyType.DEFAULT
}

typealias AndroidResourceMap = MutableMap<String, MutableList<AbstractAndroidResourceProperties>>
typealias FilesWithSizeMap = Map<String, List<Long>>