package com.android.gradle.replicator.resgen.util

import kotlin.random.Random

class VectorDrawableGenerator (val random: Random) {
    fun generateImage(numberOfPathVectors: Int, maxVectorImageSize: Int): List<String> {
        val lines = mutableListOf<String>()
        val width = random.nextInt(maxVectorImageSize)
        val height = random.nextInt(maxVectorImageSize)
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

    /* TODO: add other kinds of strokes, ex:
     *<path android:pathData="M31,63.928c0,0 6.4,-11 12.1,-13.1c7.2,-2.6 26,-1.4 26,-1.4l38.1,38.1L107,108.928l-32,-1L31,63.928z">
     *    <aapt:attr name="android:fillColor">
     *        <gradient
     *            android:endX="85.84757"
     *            android:endY="92.4963"
     *            android:startX="42.9492"
     *            android:startY="49.59793"
     *            android:type="linear">
     *            <item
     *                android:color="#44000000"
     *                android:offset="0.0" />
     *            <item
     *                android:color="#00000000"
     *                android:offset="1.0" />
     *        </gradient>
     *    </aapt:attr>
     *</path>
     */
    private fun path(
            fillColor: String,
            pathData: String,
            strokeColor: String = "",
            strokeWidth: String = "",
            fillAlpha: String = ""): List<String> {
        val lines = mutableListOf<String>()

        lines.addAll(listOf(
                """    <path""",
                """        android:fillColor="#$fillColor""""
        ))

        if (strokeColor.isNotEmpty()) {
            lines.add("""        android:strokeColor="#$strokeColor"""")
        }

        if (strokeWidth.isNotEmpty()) {
            lines.add("""        android:strokeWidth="$strokeWidth"""")
        }

        if (fillAlpha.isNotEmpty()) {
            lines.add("""        android:fillAlpha="$fillAlpha"""")
        }

        lines.add("""        android:pathData="$pathData" />""")

        return lines
    }

    private fun endImage(): String {
        return "</vector>"
    }

    private fun genStraightLinePathData(width: Int, height: Int): String {
        val fromVector = arrayListOf(random.nextInt(width), random.nextInt(height))
        val toVector = arrayListOf(random.nextInt(width), random.nextInt(height))
        return "M${fromVector[0]},${fromVector[1]}L${toVector[0]},${toVector[1]}"
    }
}