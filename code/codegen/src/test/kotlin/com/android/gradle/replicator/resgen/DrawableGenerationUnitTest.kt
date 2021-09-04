package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.resgen.util.ResgenConstants
import com.google.common.truth.Truth
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.io.File

class DrawableGenerationUnitTest: AbstractResourceGenerationTest() {
    @Test
    fun testDrawableGeneration() {
        val generator = DrawableResourceGenerator(random, ResgenConstants())
        generator.numberOfResourceElements = 3

        val pngFolder = testFolder.newFolder("png")
        val jpgFolder = testFolder.newFolder("jpg")
        val ninePatchFolder = testFolder.newFolder("9png")
        val gifFolder = testFolder.newFolder("gif")
        val webpFolder = testFolder.newFolder("webp")
        val xmlFolder = testFolder.newFolder("xml")

        val expectedChosenImages = mapOf(
                "image_aaa.png" to
                        Pair(pngFolder, getResourceImage("png", "anydpi_bootleg_android.png")),
                "image_aab.png" to
                        Pair(pngFolder, getResourceImage("png", "anydpi_pizza.png")),
                "image_aac.jpg" to
                        Pair(jpgFolder, getResourceImage("jpeg", "hdpi_bootleg_android.jpg")),
                "image_aad.jpg" to
                        Pair(jpgFolder, getResourceImage("jpeg", "hdpi_pizza.jpg")),
                "image_aae.9.png" to
                        Pair(ninePatchFolder, getResourceImage("9png", "ldpi_bootleg_android.9.png")),
                "image_aaf.9.png" to
                        Pair(ninePatchFolder, getResourceImage("9png", "ldpi_pizza.9.png")),
                "image_aag.gif" to
                        Pair(gifFolder, getResourceImage("gif", "mdpi_bootleg_android.gif")),
                "image_aah.gif" to
                        Pair(gifFolder, getResourceImage("gif", "mdpi_pizza.gif")),
                "image_aai.webp" to
                        Pair(webpFolder, getResourceImage("webp", "nodpi_bootleg_android.webp")),
                "image_aaj.webp" to
                        Pair(webpFolder, getResourceImage("webp", "nodpi_pizza.webp")),
                "image_aak.png" to
                        Pair(pngFolder, getResourceImage("png", "xxxhdpi_bootleg_android.png")),
                "image_aal.png" to
                        Pair(pngFolder, getResourceImage("png", "xxxhdpi_pizza.png"))
        )

        generator.generateResource(
                number = 2,
                outputFolder = pngFolder,
                resourceQualifiers = listOf(),
                resourceExtension = "png"
        )

        generator.generateResource(
                number = 2,
                outputFolder = jpgFolder,
                resourceQualifiers = listOf(),
                resourceExtension = "jpg"
        )

        generator.generateResource(
                number = 2,
                outputFolder = ninePatchFolder,
                resourceQualifiers = listOf(),
                resourceExtension = "9.png"
        )

        generator.generateResource(
                number = 2,
                outputFolder = gifFolder,
                resourceQualifiers = listOf(),
                resourceExtension = "gif"
        )

        generator.generateResource(
                number = 2,
                outputFolder = webpFolder,
                resourceQualifiers = listOf(),
                resourceExtension = "webp"
        )

        generator.generateResource(
                number = 2,
                outputFolder = pngFolder,
                resourceQualifiers = listOf("xxxhdpi"),
                resourceExtension = "png"
        )

        generator.generateResource(
                number = 2,
                outputFolder = xmlFolder,
                resourceQualifiers = listOf(),
                resourceExtension = "xml"
        )

        generator.generateResource(
                number = 1,
                outputFolder = xmlFolder,
                resourceQualifiers = listOf("hidpi-v24"),
                resourceExtension = "xml"
        )

        Truth.assertThat(pngFolder.listFiles()!!.asList().map{it.name}).containsExactly(
                "image_aaa.png", "image_aab.png", "image_aak.png", "image_aal.png")
        Truth.assertThat(jpgFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aac.jpg", "image_aad.jpg")
        Truth.assertThat(ninePatchFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aae.9.png", "image_aaf.9.png")
        Truth.assertThat(gifFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aag.gif", "image_aah.gif")
        Truth.assertThat(webpFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aai.webp", "image_aaj.webp")
        Truth.assertThat(xmlFolder.listFiles()!!.asList().map{it.name}).containsExactly("xml_aaa.xml", "xml_aab.xml", "xml_aac.xml")

        expectedChosenImages.forEach {
            Truth.assertThat(File(it.value.first, it.key).readBytes()).isEqualTo(it.value.second.readBytes())
        }

        @Language("xml")
        val expectedXmlAAA = """
            <?xml version="1.0" encoding="utf-8"?>
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="13dp"
                android:height="14dp"
                android:viewportWidth="1"
                android:viewportHeight="1">
                <path
                    android:fillColor="#00010203"
                    android:pathData="M0,0h13v14h-13z" />
                <path
                    android:fillColor="#04050607"
                    android:strokeColor="#08090a0b"
                    android:strokeWidth="0.02"
                    android:fillAlpha="0.021"
                    android:pathData="M3,3L5,5" />
                <path
                    android:fillColor="#0c0d0e0f"
                    android:strokeColor="#10111213"
                    android:strokeWidth="0.026"
                    android:fillAlpha="0.027"
                    android:pathData="M9,9L11,11" />
                <path
                    android:fillColor="#14151617"
                    android:strokeColor="#18191a1b"
                    android:strokeWidth="0.032"
                    android:fillAlpha="0.033"
                    android:pathData="M2,1L4,3" />
            </vector>
        """.trimIndent()

        @Language("xml")
        val expectedXmlAAB = """
            <?xml version="1.0" encoding="utf-8"?>
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="35dp"
                android:height="36dp"
                android:viewportWidth="1"
                android:viewportHeight="1">
                <path
                    android:fillColor="#1c1d1e1f"
                    android:strokeColor="#20212223"
                    android:strokeWidth="0.042"
                    android:fillAlpha="0.043"
                    android:pathData="M3,3L5,5" />
                <path
                    android:fillColor="#24252627"
                    android:strokeColor="#28292a2b"
                    android:strokeWidth="0.048"
                    android:fillAlpha="0.049"
                    android:pathData="M9,9L11,11" />
                <path
                    android:fillColor="#2c2d2e2f"
                    android:strokeColor="#30313233"
                    android:strokeWidth="0.054"
                    android:fillAlpha="0.055"
                    android:pathData="M15,15L17,17" />
            </vector>
        """.trimIndent()

        @Language("xml")
        val expectedXmlAAC = """
            <?xml version="1.0" encoding="utf-8"?>
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="57dp"
                android:height="58dp"
                android:viewportWidth="1"
                android:viewportHeight="1">
                <path
                    android:fillColor="#34353637"
                    android:pathData="M0,0h57v58h-57z" />
                <path
                    android:fillColor="#38393a3b"
                    android:strokeColor="#3c3d3e3f"
                    android:strokeWidth="0.064"
                    android:fillAlpha="0.065"
                    android:pathData="M3,3L5,5" />
                <path
                    android:fillColor="#40414243"
                    android:strokeColor="#44454647"
                    android:strokeWidth="0.07"
                    android:fillAlpha="0.071"
                    android:pathData="M9,9L11,11" />
                <path
                    android:fillColor="#48494a4b"
                    android:strokeColor="#4c4d4e4f"
                    android:strokeWidth="0.076"
                    android:fillAlpha="0.077"
                    android:pathData="M15,15L17,17" />
            </vector>
        """.trimIndent()

        Truth.assertThat(File(xmlFolder, "xml_aaa.xml").readText()).isEqualTo(expectedXmlAAA)
        Truth.assertThat(File(xmlFolder, "xml_aab.xml").readText()).isEqualTo(expectedXmlAAB)
        Truth.assertThat(File(xmlFolder, "xml_aac.xml").readText()).isEqualTo(expectedXmlAAC)
    }
}