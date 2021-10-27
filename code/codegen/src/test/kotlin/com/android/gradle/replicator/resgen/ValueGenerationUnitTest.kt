package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.resgen.util.ResgenConstants
import com.google.common.truth.Truth
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.io.File

class ValueGenerationUnitTest: AbstractResourceGenerationTest() {
    @Test
    fun testValueGeneration() {
        val generator = ValueResourceGenerator(random, ResgenConstants())

        generator.numberOfResourceElements = 5

        generator.generateResource(
                number = 2,
                outputFolder = testFolder.root,
                resourceQualifiers = listOf(),
                resourceExtension = "xml"
        )

        val generatedValues1 = File(testFolder.root, "values_aaaa.xml").readText()
        val generatedValues2 = File(testFolder.root, "values_aaab.xml").readText()

        @Language("xml")
        val expectedValues1 = """
            <resources>
                <string name="cool_delivery_aaaa">nice constable writer wire android</string>
                <bool name="cookie_etc._the_aaaa">true</bool>
                <string name="chocolate_vanilla_strawberry_aaab">party</string>
            </resources>""".trimIndent()

        @Language("xml")
        val expectedValues2 = """
            <resources>
                <string name="nice_constable_aaac">wire android face pie cookie etc. the name</string>
                <item type="id" name="chocolate_vanilla_strawberry_aaaa"/>
                <bool name="cool_delivery_aaab">false</bool>
                <integer-array name="constable_writer_wire_aaaa">
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
                <item type="id" name="pizza_party_cool_aaab"/>
            </resources>""".trimIndent()

        Truth.assertThat(generatedValues1).isEqualTo(expectedValues1)
        Truth.assertThat(generatedValues2).isEqualTo(expectedValues2)
    }
}