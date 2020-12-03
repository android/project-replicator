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
package com.android.gradle.replicator.codegen

import com.android.gradle.replicator.codegen.kotlin.KotlinClassGenerator
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.random.Random

class SingleClassGeneratorTest {

    @Mock
    lateinit var random: Random

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun ifBlockGeneration() {
        val params = ClassGenerationParameters.Builder(
                maxNumberOfInstanceVars = 0,
                maxNumberOfMethods = 1,
                maxNumberOfMethodBlocks = 1,
                maxNumberOfBlocksInIf = 0,
                maxNumberOfBlocksInIfElse = 0
        ).build()

        val outputStream = ByteArrayOutputStream()
        `when`(random.nextInt(1)).thenReturn(
                /* first import */ 0,
                /* generate one block */1
        )
        `when`(random.nextInt(10)).thenReturn(/* generate a if block */ 8)
        `when`(random.nextFloat()).thenReturn(/* parameter to the function call */0.5f)

        SingleClassGenerator(
                KotlinClassGenerator(PrettyPrintStream(PrintStream(outputStream)), listOf()),
                params,
                "foo.package",
                "FooClass",
                listOf(ClassWithBooleanReturn::class),
                random
        ).generate()

        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
"""package foo.package

@Suppress("UNUSED_PARAMETER")
class FooClass {

    fun method0(
        param0: com.android.gradle.replicator.codegen.SingleClassGeneratorTest${'$'}ClassWithBooleanReturn
    ) {
        if (param0.someFunction("SomeString", 0.5)) {
        }
    }
}
"""
        )
    }

    @Test
    fun ifElseBlockGeneration() {
        val params = ClassGenerationParameters.Builder(
                maxNumberOfInstanceVars = 0,
                maxNumberOfMethods = 1,
                maxNumberOfMethodBlocks = 1,
                maxNumberOfBlocksInLoop = 0,
                maxNumberOfBlocksInIf = 1,
                maxNumberOfBlocksInIfElse = 1
        ).build()

        val outputStream = ByteArrayOutputStream()
        `when`(random.nextInt(1)).thenReturn(
                /* use first import */0,
                /* generate one block */1)
        `when`(random.nextInt(10)).thenReturn(
                /* add if block */8,
                /* but nothing in the else block */ 0)
        `when`(random.nextBoolean()).thenReturn(/* generate an Else block */true)
        `when`(random.nextFloat()).thenReturn(/* parameter value */ 0.5f)

        SingleClassGenerator(
                KotlinClassGenerator(PrettyPrintStream(PrintStream(outputStream)), listOf()),
                params,
                "foo.package",
                "FooClass",
                listOf(ClassWithBooleanReturn::class),
                random
        ).generate()

        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """package foo.package

@Suppress("UNUSED_PARAMETER")
class FooClass {

    fun method0(
        param0: com.android.gradle.replicator.codegen.SingleClassGeneratorTest${'$'}ClassWithBooleanReturn
    ) {
        if (param0.someFunction("SomeString", 0.5)) {
            for (iAb in 0..0) {
            }
        } else {
            for (iAb in 0..0) {
            }
        }
    }
}
"""
        )
    }

    @Test
    fun lambdaGeneration() {
        val params = ClassGenerationParameters.Builder(
                maxNumberOfInstanceVars = 0,
                maxNumberOfMethods = 1,
                maxNumberOfMethodBlocks = 1,
                maxNumberOfBlocksInLambda = 0
        ).build()

        val outputStream = ByteArrayOutputStream()
        `when`(random.nextInt(1)).thenReturn(
                /* use first import */ 0,
                /* generate lambda */ 1)
        `when`(random.nextInt(10)).thenReturn(
                /* generate lambda */ 7)

        SingleClassGenerator(
                KotlinClassGenerator(PrettyPrintStream(PrintStream(outputStream)), listOf()),
                params,
                "foo.package",
                "FooClass",
                listOf(ClassWithBooleanReturn::class),
                random
        ).generate()

        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """package foo.package

@Suppress("UNUSED_PARAMETER")
class FooClass {

    fun method0(
        param0: com.android.gradle.replicator.codegen.SingleClassGeneratorTest${'$'}ClassWithBooleanReturn
    ) {
        listOf("1", "2", "3").forEach {
        }
    }
}
"""
        )
    }

    @Test
    fun loopBlockGeneration() {
        val params = ClassGenerationParameters.Builder(
                maxNumberOfInstanceVars = 0,
                maxNumberOfMethods = 1,
                maxNumberOfMethodBlocks = 1,
                maxNumberOfBlocksInLoop = 0
        ).build()

        val outputStream = ByteArrayOutputStream()
        `when`(random.nextInt(1)).thenReturn(
                /* use first import */0,
                /* generate 1 block */1)
        `when`(random.nextInt(10)).thenReturn(/* generate a loop */0)
        `when`(random.nextInt(7)).thenReturn(/* upper bound for the loop */5)

        SingleClassGenerator(
                KotlinClassGenerator(PrettyPrintStream(PrintStream(outputStream)), listOf()),
                params,
                "foo.package",
                "FooClass",
                listOf(ClassWithBooleanReturn::class),
                random
        ).generate()

        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """package foo.package

@Suppress("UNUSED_PARAMETER")
class FooClass {

    fun method0(
        param0: com.android.gradle.replicator.codegen.SingleClassGeneratorTest${'$'}ClassWithBooleanReturn
    ) {
        for (iAb in 0..5) {
        }
    }
}
"""
        )
    }

    @Test
    fun addMethodCall() {
        val params = ClassGenerationParameters.Builder(
                maxNumberOfInstanceVars = 0,
                maxNumberOfMethods = 1,
                maxNumberOfMethodBlocks = 1
        ).build()

        val outputStream = ByteArrayOutputStream()
        `when`(random.nextInt(1)).thenReturn(
                /* first import */ 0,
                /*  one parameter to the method */ 1,
                /* zero block in the method */ 0
        )
        `when`(random.nextInt(10)).thenReturn(
                /* generate a method call */ 2,
                /* generate with a local var */ 5)

        SingleClassGenerator(
                KotlinClassGenerator(PrettyPrintStream(PrintStream(outputStream)), listOf()),
                params,
                "foo.package",
                "FooClass",
                listOf(ClassWithBooleanReturn::class),
                random
        ).generate()

        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """package foo.package

@Suppress("UNUSED_PARAMETER")
class FooClass {

    fun method0(
        param0: com.android.gradle.replicator.codegen.SingleClassGeneratorTest${'$'}ClassWithBooleanReturn
    ) {
        val local_var_2_1: com.android.gradle.replicator.codegen.SingleClassGeneratorTest${'$'}ClassWithBooleanReturn = com.android.gradle.replicator.codegen.SingleClassGeneratorTest.ClassWithBooleanReturn()
        local_var_2_1.someFunction("SomeString", 0.0)
    }
}
"""
        )
    }


    private fun prettyPrint(outputStream: ByteArrayOutputStream) =
            outputStream.toString().replace("\t", "    ")

    class ClassWithBooleanReturn {
        @Suppress("unused")
        fun someFunction(param0: String, param1: Float): Boolean {
            return param0 == param1.toString()
        }
    }
}