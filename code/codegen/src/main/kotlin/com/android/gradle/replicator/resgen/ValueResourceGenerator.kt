/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.AbstractAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.ResourcePropertyType
import com.android.gradle.replicator.model.internal.filedata.ValuesAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.ValuesMap
import com.android.gradle.replicator.resgen.util.StyleItemGenerator
import com.android.gradle.replicator.resgen.util.genColor
import com.android.gradle.replicator.resgen.util.genDimen
import com.android.gradle.replicator.resourceModel.ResourceData
import com.android.gradle.replicator.resgen.util.genHex
import com.android.gradle.replicator.resgen.util.genName
import com.android.gradle.replicator.resgen.util.genString
import com.android.gradle.replicator.resgen.util.genUniqueName
import com.android.gradle.replicator.resourceModel.ResourceTypes
import com.google.common.annotations.VisibleForTesting
import java.io.File

class ValueResourceGenerator (params: ResourceGenerationParams): ResourceGenerator(params) {

    @set:VisibleForTesting
    var numberOfResourceElements: Int?= null

    override fun generateResource(
        properties: AbstractAndroidResourceProperties,
        outputFolder: File
    ) {
        // Sanity check. This should not happen unless there is a bug in the metadata reader.
        if (properties.propertyType != ResourcePropertyType.VALUES) {
            throw RuntimeException ("Unexpected property type. Got ${properties.propertyType} instead of ${ResourcePropertyType.VALUES}")
        }
        (properties as ValuesAndroidResourceProperties).valuesMapPerFile.forEach {
            val outputFile = File(outputFolder, "values_${params.uniqueIdGenerator.genIdByCategory("values.fileName")}.${properties.extension}")
            println("Generating ${outputFile.absolutePath}")
            generateValuesResource(outputFile, properties.splitQualifiers, it)
        }
    }

    private fun generateValuesResource (
            outputFile: File,
            resourceQualifiers: List<String>,
            valuesMap: ValuesMap
    ) {
        // TODO: Randomize order of elements

        val xmlLines = mutableListOf<String>()

        xmlLines.add("<resources>")

        repeat(valuesMap.stringCount) {
            xmlLines.add(stringBlock(resourceQualifiers))
        }

        repeat(valuesMap.intCount) {
            xmlLines.add(intBlock(resourceQualifiers))
        }

        repeat(valuesMap.boolCount) {
            xmlLines.add(boolBlock(resourceQualifiers))
        }

        repeat(valuesMap.colorCount) {
            xmlLines.add(colorBlock(resourceQualifiers))
        }

        repeat(valuesMap.dimenCount) {
            xmlLines.add(dimenBlock(resourceQualifiers))
        }

        repeat(valuesMap.idCount) {
            xmlLines.add(idBlock(resourceQualifiers))
        }

        valuesMap.integerArrayCount.forEach {
            xmlLines.addAll(intArrayBlock(resourceQualifiers, it))
        }

        valuesMap.arrayCount.forEach {
            xmlLines.addAll(typedArrayBlock(resourceQualifiers, it))
        }

        valuesMap.styleCount.forEach {
            xmlLines.addAll(styleBlock(resourceQualifiers, it))
        }

        xmlLines.add("</resources>")

        outputFile.writeText(xmlLines.joinToString(System.lineSeparator()))
    }

    // TODO: Add style parents and special styles
    private fun styleBlock (resourceQualifiers: List<String>, size: Int? = null): List<String> {
        val actualSize = size ?: params.random.nextInt(params.constants.values.MAX_ARRAY_ELEMENTS)
        val name = genUniqueName(params.random, "values.resName.style", params.uniqueIdGenerator)
        val xmlLines = mutableListOf(
            "    <style name=\"$name\">")
        val itemGenerator = StyleItemGenerator(params.resourceModel, params.constants)

        repeat(actualSize) {
            val item = itemGenerator.generateStyleItem(params.random)
            item?.let { // skip invalid items
                xmlLines.add(
                    "        <item name=\"${it.name}\">${it.value}</item>"
                )
            }
        }
        xmlLines.add("    </style>")

        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceTypes.VALUES_STYLE,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return xmlLines
    }

    private fun stringBlock (resourceQualifiers: List<String>): String {
        val name = genUniqueName(params.random, "values.resName.string.${resourceQualifiers}", params.uniqueIdGenerator)
        val value = genString(
            params.constants.values.MAX_STRING_WORD_COUNT,
            separator = " ",
            random = params.random)

        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceTypes.VALUES_STRING,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <string name=\"$name\">$value</string>"
    }

    private fun intBlock (resourceQualifiers: List<String>): String {
        val name = genUniqueName(params.random, "values.resName.int.${resourceQualifiers}", params.uniqueIdGenerator)
        val value = params.random.nextInt()

        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceTypes.VALUES_INT,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <integer name=\"$name\">$value</integer>"
    }

    private fun boolBlock (resourceQualifiers: List<String>): String {
        val name = genUniqueName(params.random, "values.resName.bool.${resourceQualifiers}", params.uniqueIdGenerator)
        val value = params.random.nextBoolean()

        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceTypes.VALUES_BOOL,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <bool name=\"$name\">$value</bool>"
    }

    private fun colorBlock (resourceQualifiers: List<String>): String {
        val name = genUniqueName(params.random, "values.resName.color.${resourceQualifiers}", params.uniqueIdGenerator)

        val value = genColor(params.constants, params.random)

        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceTypes.VALUES_COLOR,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <color name=\"$name\">$value</color>"
    }

    private fun dimenBlock (resourceQualifiers: List<String>): String {
        val name = genUniqueName(params.random, "values.resName.dimen.${resourceQualifiers}", params.uniqueIdGenerator)
        val value = genDimen(params.constants, params.random)
        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceTypes.VALUES_DIMEN,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <dimen name=\"$name\">$value</dimen>"
    }

    private fun idBlock (resourceQualifiers: List<String>): String {
        val name = genUniqueName(params.random, "values.resName.id.${resourceQualifiers}", params.uniqueIdGenerator)
        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceTypes.VALUES_ID,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )
        return "    <item type=\"id\" name=\"$name\"/>"
    }

    private fun intArrayBlock (resourceQualifiers: List<String>, size: Int? = null): List<String> {
        val name = genUniqueName(params.random, "values.resName.intArray.${resourceQualifiers}", params.uniqueIdGenerator)
        val actualSize = size ?: params.random.nextInt(params.constants.values.MAX_ARRAY_ELEMENTS)
        val result = mutableListOf("    <integer-array name=\"$name\">")

        repeat(actualSize) {
            val value = params.random.nextInt()
            result.add("        <item>$value</item>")
        }
        result.add("    </integer-array>")
        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceTypes.VALUES_INT_ARRAY,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )
        return result
    }

    private fun typedArrayBlock (resourceQualifiers: List<String>, size: Int? = null): List<String> {
        // to be implemented
        return listOf()
    }
}