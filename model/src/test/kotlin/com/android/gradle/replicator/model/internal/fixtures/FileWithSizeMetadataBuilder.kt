package com.android.gradle.replicator.model.internal.fixtures

import com.android.gradle.replicator.model.FilesWithSizeMetadataInfo
import com.android.gradle.replicator.model.internal.DefaultFilesWithSizeMetadataInfo

class FileWithSizeMetadataBuilder {
    var fileData: Map<String, List<Long>> = mutableMapOf()

    fun toInfo(): FilesWithSizeMetadataInfo = DefaultFilesWithSizeMetadataInfo(fileData)
}