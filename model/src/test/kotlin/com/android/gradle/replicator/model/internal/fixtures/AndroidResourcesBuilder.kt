package com.android.gradle.replicator.model.internal.fixtures

import com.android.gradle.replicator.model.internal.filedata.AndroidResourceMap
import com.android.gradle.replicator.model.AndroidResourcesInfo
import com.android.gradle.replicator.model.internal.DefaultAndroidResourcesInfo

class AndroidResourcesBuilder {
    var resourceMap: AndroidResourceMap = mutableMapOf()

    fun toInfo(): AndroidResourcesInfo = DefaultAndroidResourcesInfo(resourceMap)
}