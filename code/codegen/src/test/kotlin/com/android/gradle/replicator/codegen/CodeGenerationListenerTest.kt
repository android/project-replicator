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
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter

class CodeGenerationListenerTest {

    @Mock
    lateinit var random: Random

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testAnnotationDecorations() {
        val params = ClassGenerationParameters.Builder(
                maxNumberOfInstanceVars = 1,
                maxNumberOfMethods = 1,
                maxNumberOfMethodBlocks = 1
        ).build()

        val listener = object : CodeGenerationListener {
            override fun classDeclaration(className: String, type: CodeGenerationListener.Type, typeParameters: List<KTypeParameter>, printWriter: PrettyPrintStream) {
                printWriter.printlnIndented("@org.junit.RunWith(JUnitRunner::class.java)")
            }

            override fun methodDeclaration(methodName: String, parametersTypes: List<Class<*>>, printWriter: PrettyPrintStream) {
                printWriter.printlnIndented("@org.junit.Test")
            }

            override fun instanceVariableDeclaration(fieldName: String, type: KClass<*>, printWriter: PrettyPrintStream) {
                printWriter.printlnIndented("@org.mockito.Mock")
            }

        }

        val outputStream = ByteArrayOutputStream()
        SingleClassGenerator(
                generator = KotlinClassGenerator(PrettyPrintStream(PrintStream(outputStream)), listOf(listener)),
                params = params,
                packageName = "foo.package",
                className = "FooClass",
                eligibleClasses = listOf(SingleClassGeneratorTest.ClassWithBooleanReturn::class),
                random = random).generate()

        Mockito.`when`(random.nextInt(1)).thenReturn(0) // use first import in list.

        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """package foo.package

@org.junit.RunWith(JUnitRunner::class.java)
@Suppress("UNUSED_PARAMETER")
class FooClass {
    @org.mockito.Mock
    val instance_var_1_0: com.android.gradle.replicator.codegen.SingleClassGeneratorTest${'$'}ClassWithBooleanReturn = com.android.gradle.replicator.codegen.SingleClassGeneratorTest.ClassWithBooleanReturn()

    @org.junit.Test
    fun method0(
        param0: com.android.gradle.replicator.codegen.SingleClassGeneratorTest${'$'}ClassWithBooleanReturn
    ) {
    }
}
"""
        )
    }

    private fun prettyPrint(outputStream: ByteArrayOutputStream) = outputStream.toString().replace("\t", "    ")
}