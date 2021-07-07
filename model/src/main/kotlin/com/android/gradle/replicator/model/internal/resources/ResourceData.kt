package com.android.gradle.replicator.model.internal.resources

// Supported file types of each resource. Not exhaustive.
val ANDROID_RESOURCE_FOLDER_CONVENTION = mapOf(
        "animator" to listOf(".xml"),
        "anim" to listOf(".xml"),
        "color" to listOf(".xml"),
        "drawable" to listOf(".xml", ".png", ".9.png", ".jpg", ".gif", ".webp"),
        "font" to listOf(".ttf", ".otf", ".ttc", ".xml"),
        "layout" to listOf(".xml"),
        "menu" to listOf(".xml"),
        "mipmap" to listOf(".xml", ".png", ".9.png", ".jpg", ".gif", ".webp"),
        "raw" to listOf("*"),
        "transition" to listOf(".xml"),
        "values" to listOf(".xml"),
        "xml" to listOf(".xml")
)

/* Data classes to represent the hierarchy for android resources
 * Each resource folder type (values, mipmap, etc.) can have different qualified folders (hidpi, night, etc.)
 * and each of those qualified folders can have different file types in them (AKA extensions)
 * Each element in the resource map is a list of resources to generate for a given folder type, and
 * the elements in the list contain qualifiers, extension and quantity of resources of a particular subtype to generate
 */
abstract class AbstractAndroidResourceProperties (val qualifiers: String, val extension: String, val quantity: Int)

class AndroidDefaultResourceProperties (
        qualifiers: String,
        extension: String,
        quantity: Int): AbstractAndroidResourceProperties(qualifiers, extension, quantity)

// Values need to know how many of each value are in the files
class AndroidValuesResourceProperties (
        qualifiers: String,
        extension: String,
        quantity: Int,
        val valueTypeCount: List<Map<String, Int>>): AbstractAndroidResourceProperties(qualifiers, extension, quantity)

// Images and other resources need the file sizes to properly replicate
class AndroidSizeMattersResourceProperties (
        qualifiers: String,
        extension: String,
        quantity: Int,
        val fileSizes: List<Long>): AbstractAndroidResourceProperties(qualifiers, extension, quantity)

typealias AndroidResourceMap = MutableMap<String, MutableList<AbstractAndroidResourceProperties>>

fun selectResourceProperties(
        resourceType: String,
        qualifiers: String,
        extension: String,
        quantity: Int,
        fileSizes: List<Long>? = null,
        valueTypeCount: List<Map<String, Int>>? = null): AbstractAndroidResourceProperties {
    return when (resourceType) {
        "values" -> AndroidValuesResourceProperties(qualifiers, extension, quantity, valueTypeCount!!)
        "drawable",
        "mipmap",
        "raw" -> AndroidSizeMattersResourceProperties(qualifiers, extension, quantity, fileSizes!!)
        else -> AndroidDefaultResourceProperties(qualifiers, extension, quantity)
    }
}