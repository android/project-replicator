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

import com.android.gradle.replicator.resgen.util.genFileNameCharacters
import com.android.gradle.replicator.resgen.util.genHex
import com.android.gradle.replicator.resgen.util.genName
import com.android.gradle.replicator.resgen.util.genString
import com.google.common.annotations.VisibleForTesting
import java.io.File
import kotlin.random.Random

class ValueResourceGenerator (val random: Random): ResourceGenerator {

    @set:VisibleForTesting
    var numberOfResourceValues: Int?= null

    override fun generateResource(
            number: Int,
            outputFolder: File,
            resourceQualifier: String,
            resourceExtension: String
    ) {
        var themesFiles = 0
        var valuesFiles = 0

        repeat(number) {
            // TODO: Randomize this
            val type = ResourceType.VALUES
            when (type) {
                ResourceType.THEME ->  {
                    val outputFile = File(outputFolder, "themes${genFileNameCharacters(themesFiles)}${resourceExtension}")
                    println("Generating ${outputFile.absolutePath}")
                    generateThemeResource(outputFile, resourceQualifier)
                    themesFiles++
                }
                ResourceType.VALUES -> {
                    val outputFile = File(outputFolder, "values${genFileNameCharacters(valuesFiles)}${resourceExtension}")
                    println("Generating ${outputFile.absolutePath}")
                    generateXmlValueResource(outputFile)
                    valuesFiles++
                }
            }
        }
    }

    private enum class ValueType {
        STRING, INT, BOOL, COLOR, DIMEN, ID, INT_ARRAY, TYPED_ARRAY
    }

    private enum class ResourceType {
        VALUES, THEME
    }

    private fun generateXmlValueResource (
            outputFile: File
    ) {
        val numberOfValues = numberOfResourceValues ?: random.nextInt(1, 21)
        val allValueTypes = ValueType.values()

        val xmlLines = mutableListOf<String>()

        xmlLines.add("<resources>")

        repeat(numberOfValues) {
            when(allValueTypes.get(random.nextInt(allValueTypes.size))) {
                ValueType.STRING -> xmlLines.add(stringBlock())
                ValueType.INT -> xmlLines.add(intBlock())
                ValueType.BOOL -> xmlLines.add(boolBlock())
                ValueType.COLOR -> xmlLines.add(colorBlock())
                ValueType.DIMEN -> xmlLines.add(dimenBlock())
                ValueType.ID -> xmlLines.add(idBlock())
                ValueType.INT_ARRAY -> xmlLines.addAll(intArrayBlock())
                ValueType.TYPED_ARRAY -> xmlLines.addAll(typedArrayBlock())
            }
        }

        xmlLines.add("</resources>")

        outputFile.writeText(xmlLines.joinToString(System.lineSeparator()))
    }

    private fun generateThemeResource (
            outputFile: File,
            resourceQualifier: String
    ) {
        // To be implemented
        return
    }

    private fun stringBlock (): String {
        val name = genName(random)
        val value = genString(
                20,
                separator = " ",
                random = random)
        return "    <string name=\"$name\">$value</string>"
    }

    private fun intBlock (): String {
        val name = genName(random)
        val value = random.nextInt()
        return "    <integer name=\"$name\">$value</integer>"
    }

    private fun boolBlock (): String {
        val name = genName(random)
        val value = random.nextBoolean()
        return "    <bool name=\"$name\">$value</bool>"
    }

    private fun colorBlock (): String {
        val name = genName(random)
        /* Digits can be:
         * #RGB
         * #ARGB
         * #RRGGBB
         * #AARRGGBB
         */
        val possibleDigits = listOf(3, 4, 6, 8)
        val numberOfDigits = possibleDigits.get(random.nextInt(possibleDigits.size))

        val value = genHex(numberOfDigits, random)

        return "    <color name=\"$name\">#$value</color>"
    }

    private fun dimenBlock (): String {
        val name = genName(random)
        val units = listOf("dp", "sp", "pt", "px", "mm", "in")
        val value = "${random.nextInt(2048)}${units.get(random.nextInt(units.size))}"

        return "    <dimen name=\"$name\">$value</dimen>"
    }

    private fun idBlock (): String {
        val name = genName(random)
        return "    <item type=\"id\" name=\"$name\"/>"
    }

    private fun intArrayBlock (): List<String> {
        val name = genName(random)
        val size = random.nextInt(20)
        val result = mutableListOf("    <integer-array name=\"$name\">")

        repeat(size) { count ->
            val value = random.nextInt()
            result.add("        <item>$value</item>")
        }
        result.add("    </integer-array>")
        return result
    }

    private fun typedArrayBlock (): List<String> {
        // to be implemented
        return listOf()
    }
}