package com.android.gradle.replicator.resgen

import com.google.common.truth.Truth
import org.junit.Test
import java.io.File

class ValueGenerationUnitTest: AbstractResourceGenerationUnitTest() {
    @Test
    fun testValueGeneration() {
        val generator = ValueResourceGenerator(random)

        generator.numberOfResourceElements = 5

        generator.generateResource(
                number = 2,
                outputFolder = output.root,
                resourceQualifiers = listOf(),
                resourceExtension = ".xml"
        )

        val generatedValues1 = File(output.root, "values_aaa.xml").readText()
        val generatedValues2 = File(output.root, "values_aab.xml").readText()

        val expectedValues1 = """
            <resources>
                <string name="cool_delivery">nice constable writer wire android</string>
                <bool name="cookie_etc._the">true</bool>
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
}