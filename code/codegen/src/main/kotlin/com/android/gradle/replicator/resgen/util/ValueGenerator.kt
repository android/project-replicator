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

import kotlin.math.ceil
import kotlin.random.Random
import kotlin.random.nextUBytes

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

// TODO: Make name based on count rather than random
fun genName(random: Random): String {
    return genString(3, "_", random)
}

@kotlin.ExperimentalUnsignedTypes
fun genHex(numberOfDigits: Int, random: Random): String {
    // Bytes are 2 hex digits
    return random.nextUBytes(ceil(numberOfDigits/2.0).toInt())
            .map { "%02x".format(it.toInt()) }
            .joinToString("")
            .substring(0, numberOfDigits)
}

fun genFileNameCharacters(count: Int, minFileNameCharacters: Int = 3): String {
    var current = count
    var characters = ""
    while (current > 0 || characters.length < minFileNameCharacters) {
        characters = 'A' + (current % 26) + characters
        current /= 26
    }
    return characters
}