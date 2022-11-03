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
import com.android.gradle.replicator.resgen.resourceModel.ResourceData
import com.android.gradle.replicator.resgen.resourceModel.ResourceDataType
import com.android.gradle.replicator.resgen.util.genHex
import com.android.gradle.replicator.resgen.util.genString
import com.android.gradle.replicator.resgen.util.genUniqueName
import java.io.File

class ValueResourceGenerator (params: ResourceGenerationParams): ResourceGenerator(params) {

    override fun generateResource(
        properties: AbstractAndroidResourceProperties,
        outputFolder: File
    ) {
        // Sanity check. This should not happen unless there is a bug in the metadata reader.
        if (properties.propertyType != ResourcePropertyType.VALUES) {
            throw RuntimeException ("Unexpected property type. Got ${properties.propertyType} instead of ${ResourcePropertyType.VALUES}")
        }
        (properties as ValuesAndroidResourceProperties).valuesMapPerFile.forEach {
            // TODO: Sanity check style count vs other types
            if (it.styleCount.size > 0)  { // Theme resource
                val outputFile = File(outputFolder, "themes_${params.uniqueIdGenerator.genIdByCategory("values.fileName.theme")}.${properties.extension}")
                println("Generating ${outputFile.absolutePath}")
                generateThemeResource(outputFile, properties.splitQualifiers, it)
            } else { // XML values resource
                val outputFile = File(outputFolder, "values_${params.uniqueIdGenerator.genIdByCategory("values.fileName.values")}.${properties.extension}")
                println("Generating ${outputFile.absolutePath}")
                generateXmlValueResource(outputFile, properties.splitQualifiers, it)
            }
        }
    }

    private fun generateXmlValueResource (
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
            xmlLines.addAll(intArrayBlock(resourceQualifiers))
        }

        valuesMap.arrayCount.forEach {
            xmlLines.addAll(typedArrayBlock(resourceQualifiers))
        }

        xmlLines.add("</resources>")

        outputFile.writeText(xmlLines.joinToString(System.lineSeparator()))
    }

    private fun generateThemeResource (
            outputFile: File,
            resourceQualifiers: List<String>,
            valuesMap: ValuesMap
    ) {
        // To be implemented
        return
    }

    private fun stringBlock (resourceQualifiers: List<String>): String {
        val name = generateName(ResourceDataType.STRING, resourceQualifiers)
        val value = genString(
            params.constants.values.MAX_STRING_WORD_COUNT,
            separator = " ",
            random = params.random)

        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceDataType.STRING.typeName,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <string name=\"$name\">$value</string>"
    }

    private fun intBlock (resourceQualifiers: List<String>): String {
        val name = generateName(ResourceDataType.INT, resourceQualifiers)
        val value = params.random.nextInt()

        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceDataType.INT.typeName,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <integer name=\"$name\">$value</integer>"
    }

    private fun boolBlock (resourceQualifiers: List<String>): String {
        val name = generateName(ResourceDataType.BOOL, resourceQualifiers)
        val value = params.random.nextBoolean()

        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceDataType.BOOL.typeName,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <bool name=\"$name\">$value</bool>"
    }

    private fun colorBlock (resourceQualifiers: List<String>): String {
        val name = generateName(ResourceDataType.COLOR, resourceQualifiers)
        /* Digits can be:
         * #RGB
         * #ARGB
         * #RRGGBB
         * #AARRGGBB
         */
        val numberOfDigits = params.constants.values.POSSIBLE_COLOR_DIGITS.random(params.random)

        val value = genHex(numberOfDigits, params.random)

        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceDataType.COLOR.typeName,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <color name=\"$name\">#$value</color>"
    }

    private fun dimenBlock (resourceQualifiers: List<String>): String {
        val name = generateName(ResourceDataType.DIMEN, resourceQualifiers)
        val value = "${params.random.nextInt(params.constants.values.MAX_DIMENSION)}${params.constants.values.DIMENSION_UNITS.random(params.random)}"
        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceDataType.DIMEN.typeName,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )

        return "    <dimen name=\"$name\">$value</dimen>"
    }

    private fun idBlock (resourceQualifiers: List<String>): String {
        val name = generateName(ResourceDataType.ID, resourceQualifiers)
        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceDataType.ID.typeName,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )
        return "    <item type=\"id\" name=\"$name\"/>"
    }

    private fun intArrayBlock (resourceQualifiers: List<String>): List<String> {
        val name = generateName(ResourceDataType.INT_ARRAY, resourceQualifiers)
        val size = params.random.nextInt(params.constants.values.MAX_ARRAY_ELEMENTS)
        val result = mutableListOf("    <integer-array name=\"$name\">")

        repeat(size) {
            val value = params.random.nextInt()
            result.add("        <item>$value</item>")
        }
        result.add("    </integer-array>")
        params.resourceModel.resourceList.add(
            ResourceData(
                pkg = "",
                name = name,
                type = ResourceDataType.INT_ARRAY.typeName,
                extension = "xml",
                qualifiers = resourceQualifiers)
        )
        return result
    }

    private fun typedArrayBlock (resourceQualifiers: List<String>): List<String> {
        // to be implemented
        return listOf()
    }

    private fun generateName(type: ResourceDataType, resourceQualifiers: List<String>): String {
        val availableNames = params.resourceModel.resourceList
            .filter { it.type == type.typeName }
            .partition { it.qualifiers == resourceQualifiers }
            .let { (thisQualifier, allOtherStrings) ->
                val takenNames = thisQualifier.mapTo(mutableSetOf()) { it.name }

                allOtherStrings.mapTo(mutableSetOf()) { it.name } - takenNames
            }

        return availableNames.minOrNull() ?: genUniqueName(
            params.random,
            "values.resName.${type.typeName}.${resourceQualifiers}",
            params.uniqueIdGenerator
        )
    }
}