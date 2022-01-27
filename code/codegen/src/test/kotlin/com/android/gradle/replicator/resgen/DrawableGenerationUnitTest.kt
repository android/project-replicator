package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.model.internal.filedata.DefaultAndroidResourceProperties
import com.google.common.truth.Truth
import org.intellij.lang.annotations.Language
import org.junit.Test
import java.io.File

class DrawableGenerationUnitTest: AbstractResourceGenerationTest() {
    @Test
    fun testDrawableGeneration() {
        val generator = DrawableResourceGenerator(resourceGenerationParams)
        generator.numberOfResourceElements = 3

        val pngFolder = testFolder.newFolder("png")
        val pngQualifiedFolder = testFolder.newFolder("png-xxxhdpi")
        val jpgFolder = testFolder.newFolder("jpg")
        val ninePatchFolder = testFolder.newFolder("9png")
        val gifFolder = testFolder.newFolder("gif")
        val webpFolder = testFolder.newFolder("webp")
        val xmlFolder = testFolder.newFolder("xml")
        val xmlQualifiedFolder = testFolder.newFolder("xml-hidpi-v24")

        data class FileEq(val fname: String, val folder: File, val feq: File)
        val expectedChosenImages = listOf(
            FileEq("image_aaaa.png", pngFolder, getResourceImage("png", "anydpi_bootleg_android.png")),
            FileEq("image_aaab.png", pngFolder, getResourceImage("png", "anydpi_pizza.png")),
            FileEq("image_aaac.jpg", jpgFolder, getResourceImage("jpeg", "hdpi_bootleg_android.jpg")),
            FileEq("image_aaad.jpg", jpgFolder, getResourceImage("jpeg", "hdpi_pizza.jpg")),
            FileEq("image_aaae.9.png", ninePatchFolder, getResourceImage("9png", "ldpi_bootleg_android.9.png")),
            FileEq("image_aaaf.9.png", ninePatchFolder, getResourceImage("9png", "ldpi_pizza.9.png")),
            FileEq("image_aaag.gif", gifFolder, getResourceImage("gif", "mdpi_bootleg_android.gif")),
            FileEq("image_aaah.gif", gifFolder, getResourceImage("gif", "mdpi_pizza.gif")),
            FileEq("image_aaai.webp", webpFolder, getResourceImage("webp", "nodpi_bootleg_android.webp")),
            FileEq("image_aaaj.webp", webpFolder, getResourceImage("webp", "nodpi_pizza.webp")),
            FileEq("image_aaaa.png", pngQualifiedFolder, getResourceImage("png", "xxxhdpi_bootleg_android.png")),
            FileEq("image_aaab.png", pngQualifiedFolder, getResourceImage("png", "xxxhdpi_pizza.png"))
        )

        generator.generateResource(
                properties = DefaultAndroidResourceProperties(
                    qualifiers = "",
                    extension = "png",
                    quantity = 2,
                    fileData = listOf(228000, 3300000)
                ),
                outputFolder = pngFolder
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "jpg",
                quantity = 2,
                fileData = listOf(226000, 2300000)
            ),
            outputFolder = jpgFolder
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "9.png",
                quantity = 2,
                fileData = listOf(135000, 1900000)
            ),
            outputFolder = ninePatchFolder
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "gif",
                quantity = 2,
                fileData = listOf(295000, 931000)
            ),
            outputFolder = gifFolder
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "webp",
                quantity = 2,
                fileData = listOf(23000, 227000)
            ),
            outputFolder = webpFolder
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "xxxhdpi",
                extension = "png",
                quantity = 2,
                fileData = listOf(124000, 833000)
            ),
            outputFolder = pngQualifiedFolder
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "",
                extension = "xml",
                quantity = 2,
                fileData = listOf(22, 19)
            ),
            outputFolder = xmlFolder
        )

        generator.generateResource(
            properties = DefaultAndroidResourceProperties(
                qualifiers = "hidpi-v24",
                extension = "xml",
                quantity = 1,
                fileData = listOf(22)
            ),
            outputFolder = xmlQualifiedFolder
        )

        Truth.assertThat(pngFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aaaa.png", "image_aaab.png")
        Truth.assertThat(pngQualifiedFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aaaa.png", "image_aaab.png")
        Truth.assertThat(jpgFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aaac.jpg", "image_aaad.jpg")
        Truth.assertThat(ninePatchFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aaae.9.png", "image_aaaf.9.png")
        Truth.assertThat(gifFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aaag.gif", "image_aaah.gif")
        Truth.assertThat(webpFolder.listFiles()!!.asList().map{it.name}).containsExactly("image_aaai.webp", "image_aaaj.webp")
        Truth.assertThat(xmlFolder.listFiles()!!.asList().map{it.name}).containsExactly("vector_drawable_aaaa.xml", "vector_drawable_aaab.xml")
        Truth.assertThat(xmlQualifiedFolder.listFiles()!!.asList().map{it.name}).containsExactly("vector_drawable_aaaa.xml")

        expectedChosenImages.forEach {
            Truth.assertThat(File(it.folder, it.fname).readBytes()).isEqualTo(it.feq.readBytes())
        }

        @Language("xml")
        val expectedXmlAAAA = """
            <?xml version="1.0" encoding="utf-8"?>
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="1dp"
                android:height="2dp"
                android:viewportWidth="0"
                android:viewportHeight="1">
                <path
                    android:fillColor="#00010203"
                    android:pathData="M0,0h1v2h-1z" />
                <path
                    android:fillColor="#04050607"
                    android:strokeColor="#08090a0b"
                    android:strokeWidth="0.008"
                    android:fillAlpha="0.009"
                    android:pathData="M0,1L0,1" />
                <path
                    android:fillColor="#0c0d0e0f"
                    android:strokeColor="#10111213"
                    android:strokeWidth="0.014"
                    android:fillAlpha="0.015"
                    android:pathData="M0,1L0,1" />
                <path
                    android:fillColor="#14151617"
                    android:strokeColor="#18191a1b"
                    android:strokeWidth="0.02"
                    android:fillAlpha="0.021"
                    android:pathData="M0,1L0,1" />
            </vector>
        """.trimIndent()

        @Language("xml")
        val expectedXmlAAAB = """
            <?xml version="1.0" encoding="utf-8"?>
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="23dp"
                android:height="24dp"
                android:viewportWidth="1"
                android:viewportHeight="1">
                <path
                    android:fillColor="#1c1d1e1f"
                    android:strokeColor="#20212223"
                    android:strokeWidth="0.03"
                    android:fillAlpha="0.031"
                    android:pathData="M3,3L5,5" />
                <path
                    android:fillColor="#24252627"
                    android:strokeColor="#28292a2b"
                    android:strokeWidth="0.036"
                    android:fillAlpha="0.037"
                    android:pathData="M9,9L11,11" />
                <path
                    android:fillColor="#2c2d2e2f"
                    android:strokeColor="#30313233"
                    android:strokeWidth="0.042"
                    android:fillAlpha="0.043"
                    android:pathData="M15,15L17,17" />
            </vector>
        """.trimIndent()

        @Language("xml")
        val expectedXmlAAAAHDPI = """
            <?xml version="1.0" encoding="utf-8"?>
            <vector xmlns:android="http://schemas.android.com/apk/res/android"
                android:width="45dp"
                android:height="46dp"
                android:viewportWidth="1"
                android:viewportHeight="1">
                <path
                    android:fillColor="#34353637"
                    android:pathData="M0,0h45v46h-45z" />
                <path
                    android:fillColor="#38393a3b"
                    android:strokeColor="#3c3d3e3f"
                    android:strokeWidth="0.052"
                    android:fillAlpha="0.053"
                    android:pathData="M3,3L5,5" />
                <path
                    android:fillColor="#40414243"
                    android:strokeColor="#44454647"
                    android:strokeWidth="0.058"
                    android:fillAlpha="0.059"
                    android:pathData="M9,9L11,11" />
                <path
                    android:fillColor="#48494a4b"
                    android:strokeColor="#4c4d4e4f"
                    android:strokeWidth="0.064"
                    android:fillAlpha="0.065"
                    android:pathData="M15,15L17,17" />
            </vector>
        """.trimIndent()

        Truth.assertThat(File(xmlFolder, "vector_drawable_aaaa.xml").readText()).isEqualTo(expectedXmlAAAA)
        Truth.assertThat(File(xmlFolder, "vector_drawable_aaab.xml").readText()).isEqualTo(expectedXmlAAAB)
        Truth.assertThat(File(xmlQualifiedFolder, "vector_drawable_aaaa.xml").readText()).isEqualTo(expectedXmlAAAAHDPI)
    }
}