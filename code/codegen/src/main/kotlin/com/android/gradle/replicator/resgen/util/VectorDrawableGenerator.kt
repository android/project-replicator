package com.android.gradle.replicator.resgen.util

import kotlin.random.Random

class VectorDrawableGenerator (val random: Random) {
    fun generateImage(numberOfPathVectors: Int, maxVectorImageSize: Int): List<String> {
        val lines = mutableListOf<String>()
        val width = random.nextInt(1, maxVectorImageSize)
        val height = random.nextInt(1, maxVectorImageSize)
        val viewportWidth = random.nextInt(width)
        val viewportHeight = random.nextInt(height)
        val hasBackground = random.nextBoolean()

        lines.addAll(startImage(width, height, viewportWidth, viewportHeight))

        if (hasBackground) {
            // Create background
            lines.addAll(path(
                    fillColor = genHex(numberOfDigits = 8, random = random),
                    pathData = "M0,0h${width}v${height}h-${width}z"
            ))
        }
        // Add random lines
        repeat(numberOfPathVectors) {
            lines.addAll(path(
                    fillColor = genHex(numberOfDigits = 8, random = random),
                    pathData = genStraightLinePathData(width, height),
                    strokeColor = genHex(numberOfDigits = 8, random = random),
                    strokeWidth = (random.nextInt(1000) / 1000.0).toString(),
                    fillAlpha = (random.nextInt(1000) / 1000.0).toString()
            ))
        }
        lines.add(endImage())
        return lines
    }

    private fun startImage(width: Int, height: Int, viewportWidth: Int, viewportHeight: Int): List<String> {
        return listOf(
            """<?xml version="1.0" encoding="utf-8"?>""",
            """<vector xmlns:android="http://schemas.android.com/apk/res/android"""",
            """    android:width="${width}dp"""",
            """    android:height="${height}dp"""",
            """    android:viewportWidth="$viewportWidth"""",
            """    android:viewportHeight="$viewportHeight">"""
        )
    }

    // TODO: add nonlinear strokes
    private fun path(
            fillColor: String,
            pathData: String,
            strokeColor: String? = null,
            strokeWidth: String? = null,
            fillAlpha: String? = null): List<String> {
        val lines = mutableListOf<String>()

        lines.addAll(listOf(
                """    <path""",
                """        android:fillColor="#$fillColor""""
        ))

        strokeColor?.let {
            lines.add("""        android:strokeColor="#$it"""")
        }

        strokeWidth?.let {
            lines.add("""        android:strokeWidth="$it"""")
        }

        fillAlpha?.let {
            lines.add("""        android:fillAlpha="$it"""")
        }

        lines.add("""        android:pathData="$pathData" />""")

        return lines
    }

    private fun endImage(): String {
        return "</vector>"
    }

    private fun genStraightLinePathData(width: Int, height: Int): String {
        // Line goes from xa, ya to xb, yb as such: Mxa,yaLxb,yb
        return "M${random.nextInt(width)},${random.nextInt(height)}L${random.nextInt(width)},${random.nextInt(height)}"
    }
}