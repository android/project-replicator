/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.android.gradle.replicator.generator

import com.google.common.truth.Truth
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.fail

class MainTest {

    @get:Rule
    val thrown: ExpectedException = ExpectedException.none()

    @get:Rule
    val testFolder = TemporaryFolder()

    @Test
    fun `test no params`() {
        val byteArrayOutputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteArrayOutputStream))
        val params = Main.parseArgs(arrayOf())

        Truth.assertWithMessage("Check that params is null due to no args")
            .that(params)
            .isNull()
        Truth.assertWithMessage("Check usage print")
            .that(byteArrayOutputStream.toString().trim())
            .isEqualTo(Main.USAGE)
    }

    @Test
    fun `test wrong param`() {
        thrown.expectMessage("Unknown option: foo")
        Main.parseArgs(arrayOf("foo"))
    }

    @Test
    fun `test basic params, long form`() {
        val destination = testFolder.newFolder()
        val structureFile = testFolder.newFile()

        val params = Main.parseArgs(
            arrayOf(
                "--structure", structureFile.absolutePath,
                "--destination", destination.absolutePath
            )
        ) ?: fail("params is null")

        Truth.assertWithMessage("Destination param").that(params.destination).isEqualTo(destination)
        Truth.assertWithMessage("Structure param").that(params.jsonFile).isEqualTo(structureFile)
    }

    @Test
    fun `test basic params, short form`() {
        val destination = testFolder.newFolder()
        val structureFile = testFolder.newFile()

        val params = Main.parseArgs(
            arrayOf(
                "-s", structureFile.absolutePath,
                "-d", destination.absolutePath
            )
        ) ?: fail("params is null")

        Truth.assertWithMessage("Destination param").that(params.destination).isEqualTo(destination)
        Truth.assertWithMessage("Structure param").that(params.jsonFile).isEqualTo(structureFile)
    }

    @Test
    fun `test library params, long form`() {
        val destination = testFolder.newFolder()
        val structureFile = testFolder.newFile()
        val filterLibraries = testFolder.newFile()
        val addLibraries = testFolder.newFile()

        val params = Main.parseArgs(
            arrayOf(
                "--structure", structureFile.absolutePath,
                "--destination", destination.absolutePath,
                "--filter-libraries", filterLibraries.absolutePath,
                "--add-libraries", addLibraries.absolutePath
            )
        ) ?: fail("params is null")

        Truth.assertWithMessage("Destination param").that(params.destination).isEqualTo(destination)
        Truth.assertWithMessage("Structure param").that(params.jsonFile).isEqualTo(structureFile)
        Truth.assertWithMessage("filter-libraries param").that(params.libraryFilter).isEqualTo(filterLibraries)
        Truth.assertWithMessage("add-libraries param").that(params.libraryAdditions).isEqualTo(addLibraries)
    }

    @Test
    fun `test library params, short form`() {
        val destination = testFolder.newFolder()
        val structureFile = testFolder.newFile()
        val filterLibraries = testFolder.newFile()
        val addLibraries = testFolder.newFile()

        val params = Main.parseArgs(
            arrayOf(
                "-s", structureFile.absolutePath,
                "-d", destination.absolutePath,
                "-f", filterLibraries.absolutePath,
                "-a", addLibraries.absolutePath
            )
        ) ?: fail("params is null")

        Truth.assertWithMessage("Destination param").that(params.destination).isEqualTo(destination)
        Truth.assertWithMessage("Structure param").that(params.jsonFile).isEqualTo(structureFile)
        Truth.assertWithMessage("filter-libraries param").that(params.libraryFilter).isEqualTo(filterLibraries)
        Truth.assertWithMessage("add-libraries param").that(params.libraryAdditions).isEqualTo(addLibraries)
    }

    @Test
    fun `test wrong type for destination`() {
        val destination = testFolder.newFile()

        thrown.expectMessage("Location for option --destination is not a folder: ${destination.absolutePath}")

        Main.parseArgs(
            arrayOf(
                "--destination", destination.absolutePath
            )
        )
    }

    @Test
    fun `test missing folder for destination`() {
        val file = File(testFolder.root, "destination")

        thrown.expectMessage("Location for option --destination does not exist: ${file.absolutePath}")

        Main.parseArgs(
            arrayOf(
                "--destination", file.absolutePath
            )
        )
    }

    @Test
    fun `test wrong type for structure`() {
        val structure = testFolder.newFolder()

        thrown.expectMessage("Location for option --structure is not a file: ${structure.absolutePath}")

        Main.parseArgs(
            arrayOf(
                "--structure", structure.absolutePath
            )
        )
    }

    @Test
    fun `test missing folder for structure`() {
        val file = File(testFolder.root, "file.txt")

        thrown.expectMessage("Location for option --structure does not exist: ${file.absolutePath}")

        Main.parseArgs(
            arrayOf(
                "--structure", file.absolutePath
            )
        )
    }

    @Test
    fun `test wrong type for filter-libraries`() {
        val filterLibraries = testFolder.newFolder()

        thrown.expectMessage("Location for option --filter-libraries is not a file: ${filterLibraries.absolutePath}")

        Main.parseArgs(
            arrayOf(
                "--filter-libraries", filterLibraries.absolutePath
            )
        )
    }

    @Test
    fun `test missing folder for filter-libraries`() {
        val file = File(testFolder.root, "file.txt")

        thrown.expectMessage("Location for option --filter-libraries does not exist: ${file.absolutePath}")

        Main.parseArgs(
            arrayOf(
                "--filter-libraries", file.absolutePath
            )
        )
    }

    @Test
    fun `test wrong type for add-libraries`() {
        val addLibraries = testFolder.newFolder()

        thrown.expectMessage("Location for option --add-libraries is not a file: ${addLibraries.absolutePath}")

        Main.parseArgs(
            arrayOf(
                "--add-libraries", addLibraries.absolutePath
            )
        )
    }

    @Test
    fun `test missing folder for add-libraries`() {
        val file = File(testFolder.root, "file.txt")

        thrown.expectMessage("Location for option --add-libraries does not exist: ${file.absolutePath}")

        Main.parseArgs(
            arrayOf(
                "--add-libraries", file.absolutePath
            )
        )
    }
}