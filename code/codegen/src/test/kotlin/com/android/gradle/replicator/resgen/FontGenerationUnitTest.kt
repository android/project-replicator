package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.DefaultAndroidResourceProperties
import com.google.common.truth.Truth
import org.junit.Test
import java.io.File

class FontGenerationUnitTest: AbstractResourceGenerationTest() {
    @Test
    fun testFontGeneration() {
        val generator = FontResourceGenerator(resourceGenerationParams)

        val expectedChosenImages = mapOf(
                "font_aaaa.ttf" to
                        Pair(testFolder.root, getResourceFont("ttf", "AndroidClock.ttf")),
                "font_aaab.ttf" to
                        Pair(testFolder.root, getResourceFont("ttf", "NanumGothic.ttf")),
                "font_aaac.otf" to
                        Pair(testFolder.root, getResourceFont("otf", "NotoSansBassaVah-Regular.otf")),
                "font_aaad.otf" to
                        Pair(testFolder.root, getResourceFont("otf", "NotoSansBhaiksuki-Regular.otf")),
                "font_aaae.ttc" to
                        Pair(testFolder.root, getResourceFont("ttc", "NotoSansCJK-Regular.ttc")),
                "font_aaaf.ttc" to
                        Pair(testFolder.root, getResourceFont("ttc", "NotoSerifCJK-Regular.ttc"))
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "ttf",
                quantity = 2,
                fileData = listOf(5000, 1300000)
            ),
            outputFolder = testFolder.root
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "otf",
                quantity = 2,
                fileData = listOf(6332, 100000)
            ),
            outputFolder = testFolder.root
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "ttc",
                quantity = 2,
                fileData = listOf(19000000, 24000000)
            ),
            outputFolder = testFolder.root
        )

        Truth.assertThat(testFolder.root.listFiles()!!.asList().map { it.name }).containsExactly(
                "font_aaaa.ttf", "font_aaab.ttf", "font_aaac.otf", "font_aaad.otf", "font_aaae.ttc", "font_aaaf.ttc")
        expectedChosenImages.forEach {
            Truth.assertThat(File(it.value.first, it.key).readBytes()).isEqualTo(it.value.second.readBytes())
        }
    }
}