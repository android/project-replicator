package com.android.gradle.replicator.model.internal.filedata

class ResourcePropertiesBuilder (val resourceType: String) {
    lateinit var qualifiers: String
    lateinit var extension: String
    var quantity: Int? = null
    var fileData: List<Long>? = null
    var valuesMapPerFile: List<ValuesMap>? = null

    fun build(): AbstractAndroidResourceProperties {
        return when (resourceType) {
            "values" -> ValuesAndroidResourceProperties(qualifiers, extension, quantity!!, valuesMapPerFile!!)
            else -> DefaultAndroidResourceProperties(qualifiers, extension, quantity!!, fileData!!)
        }
    }
}