package com.android.gradle.replicator.resgen.resourceModel

data class ResourceData (
    val pkg: String,
    val name: String,
    val type: String,
    val extension: String,
    val qualifiers: List<String>
)

class ResourceModel {
    val resourceList: MutableList<ResourceData> = mutableListOf()
}