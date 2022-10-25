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
package com.android.gradle.replicator.resgen.util

import com.android.gradle.replicator.resourceModel.ResourceData
import com.android.gradle.replicator.resourceModel.ResourceModel
import com.android.gradle.replicator.resourceModel.ResourceTypes
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random

private val words = listOf (
        "pizza",
        "party",
        "cool",
        "delivery",
        "awesome",
        "nice",
        "constable",
        "writer",
        "wire",
        "android",
        "face",
        "pie",
        "cookie",
        "etc.",
        "the",
        "name",
        "max",
        "min",
        "chocolate",
        "vanilla",
        "strawberry"
)

fun genString(maxWordCount: Int, separator: String, random: Random): String {
    val stringValue = mutableListOf<String>()

    repeat(random.nextInt(1, maxWordCount + 1)) {
        stringValue.add(words.get(random.nextInt(words.size)))
    }

    return stringValue.joinToString(separator)
}

fun genName(random: Random): String {
    return genString(3, "_", random)
}

fun genUniqueName(random: Random, category: String, uniqueIdGenerator: UniqueIdGenerator): String {
    return "${genName(random)}_${uniqueIdGenerator.genIdByCategory(category)}"
}

fun genHex(numberOfDigits: Int, random: Random): String {
    val toHex = { byte: Byte -> "%02x".format(abs(byte.toInt())) }

    // Note: Bytes are 2 hex digits, ex: 9 = 09, 255 = FF, etc.
    return random.nextBytes(ceil(numberOfDigits / 2.0).toInt())
            .joinToString("") { byte -> toHex(byte) }
            .substring(0, numberOfDigits) // Trim if odd number of hex requested
}

fun genColor(constants: ResgenConstants, random: Random): String {
    /* Digits can be:
     * #RGB
     * #ARGB
     * #RRGGBB
     * #AARRGGBB
     */
    val numberOfDigits = constants.values.POSSIBLE_COLOR_DIGITS.random(random)
    return "#${genHex(numberOfDigits, random)}"
}

fun genDimen(constants: ResgenConstants, random: Random): String {
    return "${random.nextInt(constants.values.MAX_DIMENSION)}${constants.values.DIMENSION_UNITS.random(random)}"
}

fun genFloat(random: Random): String {
    return "${random.nextFloat()}"
}

fun genIdCharacters(count: Int, minFileNameCharacters: Int = 3): String {
    var current = count
    var characters = ""
    while (current > 0 || characters.length < minFileNameCharacters) {
        characters = 'a' + (current % 26) + characters
        current /= 26
    }
    return characters
}

fun genResourceOfType(random: Random, type: ResourceTypes, resourceModel: ResourceModel): ResourceData? {
    val filtered = resourceModel.resourceList.filter { it.type == type }
    if (filtered.isEmpty()) return null

    return filtered.random(random)
}

class UniqueIdGenerator {
    private val idCountByType = mutableMapOf<String, Int>()
    fun genIdByCategory(category: String): String {
        idCountByType.putIfAbsent(category, 0)
        idCountByType[category] = idCountByType[category]!! + 1
        return genIdCharacters(idCountByType[category]!! - 1, NUMBER_OF_ID_CHARACTERS)
    }
}