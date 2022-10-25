package com.android.gradle.replicator.resourceModel

enum class ResourceTypes {
    // Values
    VALUES_COLOR,
    VALUES_INT,
    VALUES_BOOL,
    VALUES_ID,
    VALUES_STRING,
    VALUES_INT_ARRAY,
    VALUES_TYPED_ARRAY,
    VALUES_DIMEN,
    VALUES_STYLE,

    FONT,
    MIPMAP,
    DRAWABLE,
    RAW,
    LAYOUT;

    fun referenceString(): String {
        return when (this) {
            VALUES_COLOR -> "color"
            VALUES_INT -> "integer"
            VALUES_BOOL -> "bool"
            VALUES_ID -> "id"
            VALUES_STRING -> "string"
            VALUES_INT_ARRAY -> "array"
            VALUES_TYPED_ARRAY -> "array"
            VALUES_DIMEN -> "dimen"
            VALUES_STYLE -> "style"

            FONT -> "font"
            MIPMAP -> "mipmap"
            DRAWABLE -> "drawable"
            RAW -> "raw"
            LAYOUT -> "layout"
        }
    }

    companion object {
        fun fromString(s: String): ResourceTypes {
            return when (s) {
                "VALUES_COLOR" -> VALUES_COLOR
                "VALUES_INT" -> VALUES_INT
                "VALUES_BOOL" -> VALUES_BOOL
                "VALUES_ID" -> VALUES_ID
                "VALUES_STRING" -> VALUES_STRING
                "VALUES_INT_ARRAY" -> VALUES_INT_ARRAY
                "VALUES_TYPED_ARRAY" -> VALUES_TYPED_ARRAY
                "VALUES_DIMEN" -> VALUES_DIMEN
                "VALUES_STYLE" -> VALUES_STYLE

                "FONT" -> FONT
                "MIPMAP" -> MIPMAP
                "DRAWABLE" -> DRAWABLE
                "RAW" -> RAW
                "LAYOUT" -> LAYOUT
                else -> throw RuntimeException("invalid resource type $s")
            }
        }
    }
}

data class ResourceData (
    val pkg: String,
    val name: String,
    val type: ResourceTypes,
    val extension: String,
    val qualifiers: List<String>
) {
    val reference: String
        get() = "@${type.referenceString()}/${name}"
}

class ResourceModel {
    val resourceList: MutableList<ResourceData> = mutableListOf()
}