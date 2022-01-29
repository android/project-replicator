package com.android.gradle.replicator.resgen

import com.android.gradle.replicator.resgen.util.ResgenConstants
import com.google.common.truth.Truth.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ResgenConstantsUnitTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun testPropertyFileLoadsProperly() {
        @Language("properties")
        val propertiesText = """
            # Vector image generation
            maxVectorImageSizeSmall=1
            maxVectorImageSizeMedium=2
            maxVectorImageSizeLarge=3

            # Values generation
            maxValues=7
            maxArrayElements=8
            maxStringWordCount=9
            maxDimension=10""".trimIndent()

        val propertiesFile = temporaryFolder.newFile("constants.properties")
        propertiesFile.writeText(propertiesText)

        val resgenConstants = ResgenConstants(propertiesFile)

        // Actual constants are 1 larger because of how random(from, to) works
        assertThat(resgenConstants.vectorImage.MAX_VECTOR_IMAGE_SIZE_SMALL).isEqualTo(2)
        assertThat(resgenConstants.vectorImage.MAX_VECTOR_IMAGE_SIZE_MEDIUM).isEqualTo(3)
        assertThat(resgenConstants.vectorImage.MAX_VECTOR_IMAGE_SIZE_LARGE).isEqualTo(4)

        assertThat(resgenConstants.values.MAX_VALUES).isEqualTo(8)
        assertThat(resgenConstants.values.MAX_ARRAY_ELEMENTS).isEqualTo(9)
        assertThat(resgenConstants.values.MAX_STRING_WORD_COUNT).isEqualTo(10)
        assertThat(resgenConstants.values.MAX_DIMENSION).isEqualTo(11)
    }

    @Test
    fun testNoFileLoad() {
        val resgenConstants = ResgenConstants()

        // Actual constants are 1 larger because of how random(from, to) works
        assertThat(resgenConstants.vectorImage.MAX_VECTOR_IMAGE_SIZE_SMALL).isEqualTo(101)
        assertThat(resgenConstants.vectorImage.MAX_VECTOR_IMAGE_SIZE_MEDIUM).isEqualTo(151)
        assertThat(resgenConstants.vectorImage.MAX_VECTOR_IMAGE_SIZE_LARGE).isEqualTo(201)

        assertThat(resgenConstants.values.MAX_VALUES).isEqualTo(22)
        assertThat(resgenConstants.values.MAX_ARRAY_ELEMENTS).isEqualTo(21)
        assertThat(resgenConstants.values.MAX_STRING_WORD_COUNT).isEqualTo(21)
        assertThat(resgenConstants.values.MAX_DIMENSION).isEqualTo(2049)
    }
}