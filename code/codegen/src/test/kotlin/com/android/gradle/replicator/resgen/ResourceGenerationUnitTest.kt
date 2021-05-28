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

import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import java.io.File
import kotlin.random.Random

// TODO: Parameterize this test
class ResourceGenerationUnitTest {

    @Mock
    lateinit var random: Random

    @get:Rule
    val output = TemporaryFolder()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    var nextIntValue = 0

    var nextBytesValue: Byte = 0

    private fun nextInt(from: Int, to: Int): Int {
        val ret = nextIntValue
        nextIntValue++
        return ret % (to - from) + from
    }

    private fun nextInt(to: Int): Int {
        val ret = nextIntValue
        nextIntValue++
        return ret % to
    }

    private fun nextInt(): Int {
        val ret = nextIntValue
        nextIntValue++
        return ret
    }


    private fun nextBytes(number: Int): ByteArray {
        val ret = mutableListOf<Byte>()
        repeat(number) {
            ret.add(nextBytesValue)
            nextBytesValue++
        }
        return ret.toByteArray()
    }

    private fun mockRandom(random: Random) {
        Mockito.`when`(random.nextInt()).thenAnswer { invocation: InvocationOnMock ->
            nextInt()
        }
        Mockito.`when`(random.nextInt(anyInt())).thenAnswer { invocation: InvocationOnMock ->
            val toIntArg = invocation.arguments[0] as Int
            nextInt(toIntArg)
        }
        Mockito.`when`(random.nextInt(anyInt(), anyInt())).thenAnswer { invocation: InvocationOnMock ->
            val fromIntArg = invocation.arguments[0] as Int
            val toIntArg = invocation.arguments[1] as Int
            nextInt(fromIntArg, toIntArg)
        }
        Mockito.`when`(random.nextBytes(anyInt())).thenAnswer { invocation: InvocationOnMock ->
            val uBytesSizeArg = invocation.arguments[0] as Int
            nextBytes(uBytesSizeArg)
        }
    }

    @Test
    fun testValueGeneration() {
        mockRandom(random)
        val generator = ValueResourceGenerator(random)

        generator.numberOfResourceElements = 5

        generator.generateResource(
                number = 2,
                outputFolder = output.root,
                resourceQualifiers = listOf(),
                resourceExtension = ".xml"
        )

        val generatedValues1 = File(output.root, "valuesAAA.xml").readText()
        val generatedValues2 = File(output.root, "valuesAAB.xml").readText()

        val expectedValues1 = """
            <resources>
                <string name="cool_delivery">nice constable writer wire android</string>
                <bool name="cookie_etc._the">false</bool>
                <string name="chocolate_vanilla_strawberry">party cool</string>
                <string name="nice_constable">wire android face pie cookie etc. the name max</string>
            </resources>""".trimIndent()

        val expectedValues2 = """
            <resources>
                <integer-array name="vanilla">
                    <item>42</item>
                </integer-array>
                <color name="delivery_awesome_nice">#000</color>
                <integer name="android_face_pie">54</integer>
                <string name="max">chocolate vanilla strawberry pizza party cool delivery awesome nice constable writer wire android face pie cookie etc. the name max</string>
            </resources>""".trimIndent()

        Truth.assertThat(generatedValues1).isEqualTo(expectedValues1)
        Truth.assertThat(generatedValues2).isEqualTo(expectedValues2)
    }

    @Test
    fun testDrawableGeneration() {
        mockRandom(random)
        val generator = DrawableResourceGenerator(random)

        generator.generateResource(
                number = 2,
                outputFolder = output.root,
                resourceQualifiers = listOf(),
                resourceExtension = ".png"
        )

        /*
        val generatedValues1 = File(output.root, "valuesAAA.xml").readText()
        val generatedValues2 = File(output.root, "valuesAAB.xml").readText()

        val expectedValues1 = """
            <resources>
                <string name="cool_delivery">nice constable writer wire android</string>
                <bool name="cookie_etc._the">false</bool>
                <string name="chocolate_vanilla_strawberry">party cool</string>
                <string name="nice_constable">wire android face pie cookie etc. the name max</string>
            </resources>""".trimIndent()

        val expectedValues2 = """
            <resources>
                <integer-array name="vanilla">
                    <item>42</item>
                </integer-array>
                <color name="delivery_awesome_nice">#000</color>
                <integer name="android_face_pie">54</integer>
                <string name="max">chocolate vanilla strawberry pizza party cool delivery awesome nice constable writer wire android face pie cookie etc. the name max</string>
            </resources>""".trimIndent()

        Truth.assertThat(generatedValues1).isEqualTo(expectedValues1)
        Truth.assertThat(generatedValues2).isEqualTo(expectedValues2)
        */
    }
}