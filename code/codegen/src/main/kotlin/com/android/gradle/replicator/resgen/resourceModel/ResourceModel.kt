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

enum class ResourceDataType(val typeName: String) {
    STRING("values_string"),
    BOOL("values_bool"),
    INT("values_int"),
    COLOR("values_color"),
    DIMEN("values_dimen"),
    INT_ARRAY("values_int_array"),
    ID("values_id"),
}