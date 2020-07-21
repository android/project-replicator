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
 */

package com.android.gradle.replicator

import com.google.common.truth.Truth
import kotlin.test.Test

/**
 * Basic tests
 */
class BasicTest {
    @Test
    fun `can run task`() {
        val setup = setupProject(BuildFileType.GROOVY)

        setup.runner.build()

        // Verify the result
        Truth.assertThat(setup.projectDir.resolve("build/project-structure.json").readText()).isEqualTo("""
{
  "gradle": "6.6-milestone-2",
  "agp": "n/a",
  "kotlin": "n/a",
  "properties": [],
  "rootModule": {
    "path": ":",
    "plugins": [],
    "dependencies": []
  },
  "modules": []
}
""".trimIndent())
    }
}
