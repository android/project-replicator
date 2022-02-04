package com.android.gradle.replicator.resourceModel

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class ResourceModelAdapter: TypeAdapter<ResourceModel>() {
    override fun write(output: JsonWriter, value: ResourceModel) {
        output.beginObject()

        output.name("resourceList").beginArray()
        value.resourceList.forEach { resource ->
            output.beginObject()

            output.name("module").value(resource.pkg)
            output.name("name").value(resource.name)
            output.name("type").value(resource.type.toString())
            output.name("extension").value(resource.extension)

            output.name("qualifiers").beginArray()
            resource.qualifiers.forEach { qualifier ->
                output.value(qualifier)
            }
            output.endArray()

            output.endObject()
        }
        output.endArray()

        output.endObject()
    }

    override fun read(input: JsonReader): ResourceModel {
        val resourceModel = ResourceModel()
        val readProperties = { reader: JsonReader, consumer: (String) -> Unit ->
            reader.beginObject()
            while (reader.hasNext()) {
                consumer(reader.nextName())
            }
            reader.endObject()
        }
        val readArray = { reader: JsonReader, consumer: () -> Unit ->
            reader.beginArray()
            while (reader.hasNext()) {
                consumer()
            }
            reader.endArray()
        }

        readProperties(input) {
            if (it != "resourceList") {
                throw RuntimeException("malformed resource model file")
            }
            readArray(input) {
                lateinit var module: String
                lateinit var name: String
                lateinit var type: String
                lateinit var extension: String
                lateinit var qualifiers: MutableList<String>

                readProperties(input) { property ->
                    when (property) {
                        "module" -> {
                            module = input.nextString()
                        }
                        "name" -> {
                            name = input.nextString()
                        }
                        "type" -> {
                            type = input.nextString()
                        }
                        "extension" -> {
                            extension = input.nextString()
                        }
                        "qualifiers" -> {
                            qualifiers = mutableListOf()
                            readArray(input) {
                                qualifiers.add(input.nextString())
                            }
                        }
                    }
                }
                resourceModel.resourceList.add(ResourceData(
                    pkg = module,
                    name = name,
                    type = ResourceTypes.fromString(type),
                    extension = extension,
                    qualifiers = qualifiers
                ))
            }
        }

        return resourceModel
    }
}