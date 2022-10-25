package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.ValuesAndroidResourceProperties
import com.android.gradle.replicator.model.internal.filedata.ValuesMap
import com.google.common.truth.Truth
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.io.File

class ValueGenerationUnitTest: AbstractResourceGenerationTest() {
    @Test
    fun testValueGeneration() {
        val generator = ValueResourceGenerator(resourceGenerationParams)

        generator.numberOfResourceElements = 5

        generator.generateResource(
            properties = ValuesAndroidResourceProperties(
                qualifiers = "",
                extension = "xml",
                quantity = 2,
                valuesMapPerFile = listOf(
                    ValuesMap(
                        stringCount = 2,
                        intCount = 3,
                        boolCount = 4,
                        colorCount = 5,
                        dimenCount = 6,
                        idCount = 7,
                        integerArrayCount = mutableListOf(3, 4),
                        arrayCount = mutableListOf(5, 6),
                        styleCount = mutableListOf()
                    ),
                    ValuesMap(
                        stringCount = 7,
                        intCount = 6,
                        boolCount = 5,
                        colorCount = 4,
                        dimenCount = 3,
                        idCount = 2,
                        integerArrayCount = mutableListOf(5, 6),
                        arrayCount = mutableListOf(3, 4),
                        styleCount = mutableListOf()
                    ),
                    ValuesMap(
                        stringCount = 0,
                        intCount = 0,
                        boolCount = 0,
                        colorCount = 0,
                        dimenCount = 0,
                        idCount = 0,
                        integerArrayCount = mutableListOf(),
                        arrayCount = mutableListOf(),
                        styleCount = mutableListOf(4, 5, 6)
                    )
                )
            ),
            outputFolder = testFolder.root
        )

        val generatedValues1 = File(testFolder.root, "values_aaaa.xml").readText()
        val generatedValues2 = File(testFolder.root, "values_aaab.xml").readText()
        val generatedValues3 = File(testFolder.root, "values_aaac.xml").readText()

        @Language("xml")
        val expectedValues1 = """
            <resources>
                <string name="party_aaaa">delivery awesome nice</string>
                <string name="writer_aaab">android face pie cookie etc. the name max min</string>
                <integer name="vanilla_aaaa">20</integer>
                <integer name="party_aaab">23</integer>
                <integer name="awesome_aaac">26</integer>
                <bool name="writer_aaaa">true</bool>
                <bool name="android_face_pie_aaab">false</bool>
                <bool name="etc._aaac">true</bool>
                <bool name="name_max_min_aaad">false</bool>
                <color name="vanilla_aaaa">#0001</color>
                <color name="party_aaab">#020</color>
                <color name="awesome_aaac">#04050607</color>
                <color name="writer_aaad">#08090a</color>
                <color name="face_aaae">#0b0c</color>
                <dimen name="etc._aaaa">56px</dimen>
                <dimen name="min_chocolate_aaab">61pt</dimen>
                <dimen name="party_aaac">65dp</dimen>
                <dimen name="nice_constable_aaad">70in</dimen>
                <dimen name="face_aaae">74px</dimen>
                <dimen name="the_name_aaaf">79pt</dimen>
                <item type="id" name="vanilla_aaaa"/>
                <item type="id" name="pizza_party_cool_aaab"/>
                <item type="id" name="awesome_aaac"/>
                <item type="id" name="constable_writer_wire_aaad"/>
                <item type="id" name="face_aaae"/>
                <item type="id" name="cookie_etc._the_aaaf"/>
                <item type="id" name="max_aaag"/>
                <integer-array name="chocolate_vanilla_strawberry_aaaa">
                    <item>105</item>
                    <item>106</item>
                    <item>107</item>
                </integer-array>
                <integer-array name="awesome_aaab">
                    <item>110</item>
                    <item>111</item>
                    <item>112</item>
                    <item>113</item>
                </integer-array>
            </resources>""".trimIndent()

        @Language("xml")
        val expectedValues2 = """
            <resources>
                <string name="face_aaac">cookie etc. the name max min chocolate vanilla strawberry pizza party cool</string>
                <string name="awesome_aaad">constable writer wire android face pie</string>
                <string name="etc._aaae">name max min chocolate vanilla strawberry pizza party cool delivery awesome nice constable writer wire</string>
                <string name="face_aaaf">cookie etc. the name max min chocolate vanilla strawberry pizza party cool</string>
                <string name="awesome_aaag">constable writer wire android face pie</string>
                <string name="etc._aaah">name max min chocolate vanilla strawberry pizza party cool delivery awesome nice constable writer wire</string>
                <string name="face_aaai">cookie etc. the name max min chocolate vanilla strawberry pizza party cool</string>
                <integer name="awesome_aaad">215</integer>
                <integer name="writer_aaae">218</integer>
                <integer name="face_aaaf">221</integer>
                <integer name="etc._aaag">224</integer>
                <integer name="max_aaah">227</integer>
                <integer name="vanilla_aaai">230</integer>
                <bool name="party_aaae">true</bool>
                <bool name="delivery_awesome_nice_aaaf">false</bool>
                <bool name="writer_aaag">true</bool>
                <bool name="android_face_pie_aaah">false</bool>
                <bool name="etc._aaai">true</bool>
                <color name="name_max_min_aaaf">#0d0e</color>
                <color name="strawberry_pizza_aaag">#0f10</color>
                <color name="delivery_awesome_nice_aaah">#111213</color>
                <color name="wire_android_aaai">#141516</color>
                <dimen name="cookie_etc._the_aaag">267mm</dimen>
                <dimen name="chocolate_vanilla_strawberry_aaah">273mm</dimen>
                <dimen name="delivery_awesome_nice_aaai">279mm</dimen>
                <item type="id" name="android_face_pie_aaah"/>
                <item type="id" name="etc._aaai"/>
                <integer-array name="name_max_min_aaac">
                    <item>291</item>
                    <item>292</item>
                    <item>293</item>
                    <item>294</item>
                    <item>295</item>
                </integer-array>
                <integer-array name="delivery_awesome_nice_aaad">
                    <item>300</item>
                    <item>301</item>
                    <item>302</item>
                    <item>303</item>
                    <item>304</item>
                    <item>305</item>
                </integer-array>
            </resources>""".trimIndent()

        @Language("xml")
        val expectedValues3 = """
            <resources>
                <style name="etc._aaaa">
                    <item name="android:colorBackground">#171819</item>
                    <item name="android:secondaryContentAlpha">0.0</item>
                    <item name="android:textColorSecondary">@color/vanilla_aaaa</item>
                    <item name="colorPrimarySurface">#1a1b1c</item>
                </style>
                <style name="nice_constable_aaab">
                    <item name="android:disabledAlpha">0.0</item>
                    <item name="colorControlActivated">#1d1e1f</item>
                    <item name="colorPrimaryVariant">#2021</item>
                    <item name="colorPrimary">@color/wire_android_aaai</item>
                    <item name="dividerHorizontal">@dimen/party_aaac</item>
                </style>
                <style name="party_aaac">
                    <item name="colorControlHighlight">#222</item>
                    <item name="colorPrimaryVariant">@color/party_aaab</item>
                    <item name="android:secondaryContentAlpha">0.0</item>
                    <item name="selectableItemBackground">@dimen/cookie_etc._the_aaag</item>
                    <item name="android:textColorSecondary">@color/vanilla_aaaa</item>
                    <item name="android:textColorPrimary">@color/writer_aaad</item>
                </style>
            </resources>""".trimIndent()
        Truth.assertThat(generatedValues1).isEqualTo(expectedValues1)
        Truth.assertThat(generatedValues2).isEqualTo(expectedValues2)
        Truth.assertThat(generatedValues3).isEqualTo(expectedValues3)
    }
}