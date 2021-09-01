package com.android.gradle.replicator.resgen

import com.google.common.truth.Truth
import org.junit.Test
import java.io.File

class FontGenerationUnitTest: AbstractResourceGenerationUnitTest() {
    @Test
    fun testFontGeneration() {
        val generator = FontResourceGenerator(random)

        val expectedChosenImages = mapOf(
                "font_aaa.ttf" to
                        Pair(output.root, getResourceFont("ttf", "AndroidClock.ttf")),
                "font_aab.ttf" to
                        Pair(output.root, getResourceFont("ttf", "NanumGothic.ttf")),
                "font_aac.otf" to
                        Pair(output.root, getResourceFont("otf", "NotoSansBassaVah-Regular.otf")),
                "font_aad.otf" to
                        Pair(output.root, getResourceFont("otf", "NotoSansBhaiksuki-Regular.otf")),
                "font_aae.ttc" to
                        Pair(output.root, getResourceFont("ttc", "NotoSansCJK-Regular.ttc")),
                "font_aaf.ttc" to
                        Pair(output.root, getResourceFont("ttc", "NotoSerifCJK-Regular.ttc"))
        )

        generator.generateResource(
                number = 2,
                outputFolder = output.root,
                resourceQualifiers = listOf(),
                resourceExtension = "ttf"
        )

        generator.generateResource(
                number = 2,
                outputFolder = output.root,
                resourceQualifiers = listOf(),
                resourceExtension = "otf"
        )

        generator.generateResource(
                number = 2,
                outputFolder = output.root,
                resourceQualifiers = listOf(),
                resourceExtension = "ttc"
        )

        Truth.assertThat(output.root.listFiles()!!.asList().map { it.name }).containsExactly(
                "font_aaa.ttf", "font_aab.ttf", "font_aac.otf", "font_aad.otf", "font_aae.ttc", "font_aaf.ttc")
        expectedChosenImages.forEach {
            Truth.assertThat(File(it.value.first, it.key).readBytes()).isEqualTo(it.value.second.readBytes())
        }
    }
}