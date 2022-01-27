package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.DefaultAndroidResourceProperties
import com.google.common.truth.Truth
import org.junit.Test
import java.io.File

class RawGenerationUnitTest: AbstractResourceGenerationTest() {
    @Test
    fun testRawGeneration() {
        val generator = RawResourceGenerator(resourceGenerationParams)

        val expectedChosenImages = mapOf(
                "raw_aaaa.txt" to
                        Pair(testFolder.root, getResource("txt", "bootleg_android.txt")),
                "raw_aaab.json" to
                        Pair(testFolder.root, getResource("json", "pizza_recipe.json")),
                "raw_aaac.png" to
                        Pair(testFolder.root, getResourceImage("png", "hdpi_bootleg_android.png")),
                "raw_aaad.jpg" to
                        Pair(testFolder.root, getResourceImage("jpeg", "hdpi_pizza.jpg"))
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "txt",
                quantity = 1,
                fileData = listOf(1535)
            ),
            outputFolder = testFolder.root
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "json",
                quantity = 1,
                fileData = listOf(600)
            ),
            outputFolder = testFolder.root
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "png",
                quantity = 1,
                fileData = listOf(318000)
            ),
            outputFolder = testFolder.root
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "jpg",
                quantity = 1,
                fileData = listOf(2300000)
            ),
            outputFolder = testFolder.root
        )

        Truth.assertThat(testFolder.root.listFiles()!!.asList().map { it.name }).containsExactly(
                "raw_aaaa.txt", "raw_aaab.json", "raw_aaac.png", "raw_aaad.jpg")
        expectedChosenImages.forEach {
            Truth.assertThat(File(it.value.first, it.key).readBytes()).isEqualTo(it.value.second.readBytes())
        }
    }
}