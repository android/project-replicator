package com.android.gradle.replicator.model.internal.fixtures

import com.android.gradle.replicator.model.FilesWithSizeMetadataInfo
import com.android.gradle.replicator.model.internal.DefaultFilesWithSizeMetadataInfo
import com.android.gradle.replicator.model.internal.filedata.FilesWithSizeMap

class FileWithSizeMetadataBuilder {
    var fileData: FilesWithSizeMap = mutableMapOf()

    fun toInfo(): FilesWithSizeMetadataInfo = DefaultFilesWithSizeMetadataInfo(fileData)
}