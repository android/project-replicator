package com.android.gradle.replicator.model.internal

import com.android.gradle.replicator.model.FilesWithSizeMetadataInfo
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class DefaultFilesWithSizeMetadataInfo(
    override val fileData: Map<String, List<Long>>
): FilesWithSizeMetadataInfo

/* Information about file sizes is separated by extension as such:
 * "assets": {
 *   ".xml": [200, 300, 400],
 *   ".stl": [500, 600, 700],
 * }
 */
class FilesWithSizeMetadataAdapter: TypeAdapter<FilesWithSizeMetadataInfo>() {
    override fun write(output: JsonWriter, value: FilesWithSizeMetadataInfo) {
        output.beginObject()
        for (extension in value.fileData) {
            output.name(extension.key).beginArray()
            for (size in extension.value) {
                output.value(size)
            }
            output.endArray()
        }
        output.endObject()
    }

    override fun read(input: JsonReader): FilesWithSizeMetadataInfo {
        val fileData = mutableMapOf<String, MutableList<Long>>()
        // Read folder properties
        input.readObjectProperties { extension ->
            fileData[extension] = mutableListOf()
            // Read resource properties
            this.readArray {
                fileData[extension]!!.add(this.nextLong())
            }
        }

        return DefaultFilesWithSizeMetadataInfo(fileData)
    }
}