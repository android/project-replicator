package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.resgen.util.ResgenConstants
import com.google.common.truth.Truth
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.io.File

class ValueGenerationUnitTest: AbstractResourceGenerationUnitTest() {
    @Test
    fun testValueGeneration() {
        val generator = ValueResourceGenerator(random, ResgenConstants())

        generator.numberOfResourceElements = 5

        generator.generateResource(
                number = 2,
                outputFolder = output.root,
                resourceQualifiers = listOf(),
                resourceExtension = "xml"
        )

        val generatedValues1 = File(output.root, "values_aaa.xml").readText()
        val generatedValues2 = File(output.root, "values_aab.xml").readText()

        @Language("xml")
        val expectedValues1 = """
            <resources>
                <string name="cool_delivery">nice constable writer wire android</string>
                <bool name="cookie_etc._the">true</bool>
                <string name="chocolate_vanilla_strawberry">party</string>
            </resources>""".trimIndent()

        @Language("xml")
        val expectedValues2 = """
            <resources>
                <string name="nice_constable">wire android face pie cookie etc. the name</string>
                <item type="id" name="chocolate_vanilla_strawberry"/>
                <bool name="cool_delivery">false</bool>
                <integer-array name="constable_writer_wire">
                    <item>52</item>
                    <item>53</item>
                    <item>54</item>
                    <item>55</item>
                    <item>56</item>
                    <item>57</item>
                    <item>58</item>
                    <item>59</item>
                    <item>60</item>
                </integer-array>
                <item type="id" name="pizza_party_cool"/>
            </resources>""".trimIndent()

        Truth.assertThat(generatedValues1).isEqualTo(expectedValues1)
        Truth.assertThat(generatedValues2).isEqualTo(expectedValues2)
    }
}