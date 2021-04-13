package com.android.gradle.replicator.model.internal.fixtures

import com.android.gradle.replicator.model.AndroidResourceFolders
import com.android.gradle.replicator.model.AndroidResourcesInfo
import com.android.gradle.replicator.model.internal.DefaultAndroidResourcesInfo

class AndroidResourcesBuilder {
    var fileCount = AndroidResourceFolders(mutableMapOf())

    fun toInfo(): AndroidResourcesInfo = DefaultAndroidResourcesInfo(fileCount)
}