package com.android.gradle.replicator.model.internal.filedata

import com.android.gradle.replicator.model.internal.readArray
import com.android.gradle.replicator.model.internal.readObjectProperties
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

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

fun selectResourceProperties(
        resourceType: String,
        qualifiers: String,
        extension: String,
        quantity: Int,
        fileData: List<Long>? = null,
        valuesMapPerFile: List<ValuesMap>? = null): AbstractAndroidResourceProperties {
    return when (resourceType) {
        "values" -> ValuesAndroidResourceProperties(qualifiers, extension, quantity, valuesMapPerFile!!)
        else -> DefaultAndroidResourceProperties(qualifiers, extension, quantity, fileData!!)
    }
}

class AndroidResourcePropertiesAdapter(private val resourceType: String? = null):
    TypeAdapter<AbstractAndroidResourceProperties>() {
    override fun write(output: JsonWriter, value: AbstractAndroidResourceProperties) {
        output.beginObject()
        output.name("qualifiers").value(value.qualifiers)
        output.name("extension").value(value.extension)
        output.name("quantity").value(value.quantity)
        when (value.propertyType) {
            ResourcePropertyType.VALUES -> {
                writeValuesSpecializedData(output, value as ValuesAndroidResourceProperties)
            }
            ResourcePropertyType.DEFAULT -> {
                writeDefaultSpecializedData(output, value as DefaultAndroidResourceProperties)
            }
        }
        output.endObject()
    }

    override fun read(input: JsonReader): AbstractAndroidResourceProperties {
        var qualifiers: String? = null
        var extension: String? = null
        var quantity: Int? = null
        var fileData: MutableList<Long>? = null
        var valuesMapPerFile: MutableList<ValuesMap>? = null
        input.readObjectProperties { property ->
            when (property) {
                "qualifiers" -> qualifiers = this.nextString()
                "extension" -> extension = this.nextString()
                "quantity" -> quantity = this.nextInt()
                "fileData" -> {
                        fileData = mutableListOf()
                        this.readArray {
                                fileData!!.add(this.nextLong())
                        }
                }
                "valuesFileList" -> {
                    valuesMapPerFile = mutableListOf()
                    this.readArray {
                        val valuesMap = ValuesMap()
                        this.readObjectProperties { valueProperty ->
                            when (valueProperty) {
                                "stringCount" -> { valuesMap.stringCount = this.nextInt() }
                                "intCount" -> { valuesMap.intCount = this.nextInt() }
                                "boolCount" -> { valuesMap.boolCount = this.nextInt()  }
                                "colorCount" -> { valuesMap.colorCount = this.nextInt()  }
                                "dimenCount" -> { valuesMap.dimenCount = this.nextInt()  }
                                "idCount" -> { valuesMap.idCount = this.nextInt() }
                                "integerArrayCount" -> {
                                    this.readArray {
                                        valuesMap.integerArrayCount.add(this.nextInt())
                                    }
                                }
                                "arrayCount" -> {
                                    this.readArray {
                                        valuesMap.arrayCount.add(this.nextInt())
                                    }
                                }
                                "styleCount" -> {
                                    this.readArray {
                                        valuesMap.styleCount.add(this.nextInt())
                                    }
                                }
                            }
                        }
                        valuesMapPerFile!!.add(valuesMap)
                    }
                }
            }
        }
        return selectResourceProperties(
                resourceType = resourceType!!,
                qualifiers = qualifiers!!,
                extension = extension!!,
                quantity = quantity!!,
                fileData = fileData,
                valuesMapPerFile = valuesMapPerFile)
    }

    private fun writeValuesSpecializedData(output: JsonWriter, value: ValuesAndroidResourceProperties) {
        output.name("valuesFileList").beginArray()
        value.valuesMapPerFile.forEach { resourceFile ->
            output.beginObject()
            output.name("stringCount").value(resourceFile.stringCount)
            output.name("intCount").value(resourceFile.intCount)
            output.name("boolCount").value(resourceFile.boolCount)
            output.name("colorCount").value(resourceFile.colorCount)
            output.name("dimenCount").value(resourceFile.dimenCount)
            output.name("idCount").value(resourceFile.idCount)

            output.name("integerArrayCount").beginArray()
            resourceFile.integerArrayCount.forEach {
                output.value(it)
            }
            output.endArray()

            output.name("arrayCount").beginArray()
            resourceFile.arrayCount.forEach {
                output.value(it)
            }
            output.endArray()

            output.name("styleCount").beginArray()
            resourceFile.styleCount.forEach {
                output.value(it)
            }
            output.endArray()

            output.endObject()
        }
        output.endArray()
    }
    private fun writeDefaultSpecializedData(output: JsonWriter, value: DefaultAndroidResourceProperties) {
        output.name("fileData").beginArray()
        value.fileData.forEach { resourceFile ->
            output.value(resourceFile)
        }
        output.endArray()
    }
}