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

package com.android.gradle.replicator.generator.fixtures

import com.android.gradle.replicator.generator.BuildGenerator
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runners.Parameterized
import java.io.File

abstract class BaseTest {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "kts_{0}")
        fun kts(): List<Boolean> {
            return listOf(true, false)
        }
    }

    @Parameterized.Parameter(0)
    @JvmField
    var ktsMode: Boolean = false

    @get:Rule
    val testFolder = TemporaryFolder()

    protected fun generateWithStructure(structure: String): File {
        val jsonFile = testFolder.newFile()
        jsonFile.writeText(structure)
        val output = testFolder.newFolder()

        val params = TestParams(
            jsonFile = jsonFile,
            destination = output,
            kts = ktsMode
        )
        BuildGenerator(params).generate()

        return output
    }

    val buildFileName: String
        get() = if (ktsMode) "build.gradle.kts" else "build.gradle"

    val settingsFileName: String
        get() = if (ktsMode) "settings.gradle.kts" else "settings.gradle"

    fun select(kts: String, groovy: String): String = if (ktsMode) kts else groovy
}