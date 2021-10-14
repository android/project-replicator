package com.android.gradle.replicator.resgen.util

import com.android.gradle.replicator.parsing.ArgsParser
import java.io.File

// Immutable constants
const val NUMBER_OF_ID_CHARACTERS = 4 //Number of base characters to use on unique identifiers

class ResgenConstants (propertyFile: File? = null) {
    data class VectorImageConstants (
        val MAX_VECTOR_IMAGE_SIZE_SMALL: Int,
        val MAX_VECTOR_IMAGE_SIZE_MEDIUM: Int,
        val MAX_VECTOR_IMAGE_SIZE_LARGE: Int,
        val MAX_VECTOR_IMAGE_LINES_SMALL: Int,
        val MAX_VECTOR_IMAGE_LINES_MEDIUM: Int,
        val MAX_VECTOR_IMAGE_LINES_LARGE: Int)

    data class ValuesConstants (
        val MAX_VALUES: Int,
        val MAX_ARRAY_ELEMENTS: Int,
        val MAX_STRING_WORD_COUNT: Int,
        val MAX_DIMENSION: Int) {
        val POSSIBLE_COLOR_DIGITS: List<Int> = listOf(3, 4, 6, 8)
        val DIMENSION_UNITS: List<String> = listOf("dp", "sp", "pt", "px", "mm", "in")
    }

    val vectorImage: VectorImageConstants
    val values: ValuesConstants

    init {
        val parser = ArgsParser()

        // Vector drawables
        val maxVectorImageSizeSmall =  parser.option(propertyName = "maxVectorImageSizeSmall", argc = 1)
        val maxVectorImageSizeMedium =  parser.option(propertyName = "maxVectorImageSizeMedium", argc = 1)
        val maxVectorImageSizeLarge =  parser.option(propertyName = "maxVectorImageSizeLarge", argc = 1)
        val maxVectorImageLinesSmall =  parser.option(propertyName = "maxVectorImageLinesSmall", argc = 1)
        val maxVectorImageLinesMedium =  parser.option(propertyName = "maxVectorImageLinesMedium", argc = 1)
        val maxVectorImageLinesLarge =  parser.option(propertyName = "maxVectorImageLinesLarge", argc = 1)

        // Values
        val maxValues =  parser.option(propertyName = "maxValues", argc = 1)
        val maxArrayElements =  parser.option(propertyName = "maxArrayElements", argc = 1)
        val maxStringWordCount =  parser.option(propertyName = "maxStringWordCount", argc = 1)
        val maxDimension =  parser.option(propertyName = "maxDimension", argc = 1)

        // Use default values if no property file exists
        propertyFile?.let {
            parser.parsePropertyFile(it)
        }

        // random(1, 2) is the same as requesting only the number 1, so to get the REAL max we need to add 1
        vectorImage = VectorImageConstants(
            MAX_VECTOR_IMAGE_SIZE_SMALL = (maxVectorImageSizeSmall.orNull?.first?.toInt() ?: 100) + 1,
            MAX_VECTOR_IMAGE_SIZE_MEDIUM = (maxVectorImageSizeMedium.orNull?.first?.toInt() ?: 150) + 1,
            MAX_VECTOR_IMAGE_SIZE_LARGE = (maxVectorImageSizeLarge.orNull?.first?.toInt() ?: 200) + 1,
            MAX_VECTOR_IMAGE_LINES_SMALL = (maxVectorImageLinesSmall.orNull?.first?.toInt() ?: 50) + 1,
            MAX_VECTOR_IMAGE_LINES_MEDIUM = (maxVectorImageLinesMedium.orNull?.first?.toInt() ?: 75) + 1,
            MAX_VECTOR_IMAGE_LINES_LARGE = (maxVectorImageLinesLarge.orNull?.first?.toInt() ?: 100) + 1
        )

        // same as above
        values = ValuesConstants(
            MAX_VALUES = (maxValues.orNull?.first?.toInt() ?: 21) + 1,
            MAX_ARRAY_ELEMENTS = (maxArrayElements.orNull?.first?.toInt() ?: 20) + 1,
            MAX_STRING_WORD_COUNT = (maxStringWordCount.orNull?.first?.toInt() ?: 20) + 1,
            MAX_DIMENSION = (maxDimension.orNull?.first?.toInt() ?: 2048) + 1
        )
    }
}