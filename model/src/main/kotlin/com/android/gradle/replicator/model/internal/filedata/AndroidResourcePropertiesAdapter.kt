package com.android.gradle.replicator.model.internal.filedata

import com.android.gradle.replicator.model.internal.readArray
import com.android.gradle.replicator.model.internal.readObjectProperties
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

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
        val propertiesBuilder = ResourcePropertiesBuilder(resourceType!!)
        input.readObjectProperties { property ->
            when (property) {
                "qualifiers" -> propertiesBuilder.qualifiers = this.nextString()
                "extension" -> propertiesBuilder.extension = this.nextString()
                "quantity" -> propertiesBuilder.quantity = this.nextInt()
                "fileData" -> {
                    val fileData = mutableListOf<Long>()
                    this.readArray {
                        fileData.add(this.nextLong())
                    }
                    propertiesBuilder.fileData = fileData
                }
                "valuesFileList" -> {
                    val valuesMapPerFile = mutableListOf<ValuesMap>()
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
                        valuesMapPerFile.add(valuesMap)
                    }
                    propertiesBuilder.valuesMapPerFile = valuesMapPerFile
                }
            }
        }
        return propertiesBuilder.build()
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