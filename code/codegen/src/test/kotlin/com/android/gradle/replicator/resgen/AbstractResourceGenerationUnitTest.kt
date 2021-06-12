/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.gradle.replicator.resgen

import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.invocation.InvocationOnMock
import java.io.File
import kotlin.random.Random

abstract class AbstractResourceGenerationUnitTest {

    @Mock
    lateinit var random: Random

    @get:Rule
    val output = TemporaryFolder()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockRandom(random)
    }

    private var nextIntValue = 0

    private var nextBytesValue: Byte = 0

    private var nextBoolValue: Boolean = false

    private fun nextInt(from: Int, to: Int): Int {
        val ret = nextIntValue
        nextIntValue++
        return ret % (to - from) + from
    }

    private fun nextInt(to: Int): Int {
        val ret = nextIntValue
        nextIntValue++
        return ret % to
    }

    private fun nextInt(): Int {
        val ret = nextIntValue
        nextIntValue++
        return ret
    }


    private fun nextBytes(number: Int): ByteArray {
        val ret = mutableListOf<Byte>()
        repeat(number) {
            ret.add(nextBytesValue)
            nextBytesValue++
        }
        return ret.toByteArray()
    }

    private fun nextBoolean(): Boolean {
        nextBoolValue = !nextBoolValue
        return nextBoolValue
    }

    private fun mockRandom(random: Random) {
        Mockito.`when`(random.nextInt()).thenAnswer { invocation: InvocationOnMock ->
            nextInt()
        }
        Mockito.`when`(random.nextInt(anyInt())).thenAnswer { invocation: InvocationOnMock ->
            val toIntArg = invocation.arguments[0] as Int
            nextInt(toIntArg)
        }
        Mockito.`when`(random.nextInt(anyInt(), anyInt())).thenAnswer { invocation: InvocationOnMock ->
            val fromIntArg = invocation.arguments[0] as Int
            val toIntArg = invocation.arguments[1] as Int
            nextInt(fromIntArg, toIntArg)
        }
        Mockito.`when`(random.nextBytes(anyInt())).thenAnswer { invocation: InvocationOnMock ->
            val uBytesSizeArg = invocation.arguments[0] as Int
            nextBytes(uBytesSizeArg)
        }
        Mockito.`when`(random.nextBoolean()).thenAnswer { invocation: InvocationOnMock ->
            nextBoolean()
        }
    }

    protected fun getResource(resourceType: String, resourceName: String): File {
        val loader = Thread.currentThread().contextClassLoader
        val url = loader.getResource("resgen/$resourceType/$resourceName")!!
        val path: String = url.path
        return File(path)
    }

    protected fun getResourceImage(imageType: String, imageName: String): File {
        return getResource("images/$imageType", imageName)
    }

    protected fun getResourceFont(fontType: String, fontName: String): File {
        return getResource("fonts/$fontType", fontName)
    }
}