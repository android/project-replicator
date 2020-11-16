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
package com.android.gradle.replicator.codegen.kotlin

import com.android.gradle.replicator.codegen.ClassModel
import com.android.gradle.replicator.codegen.FieldModel
import com.android.gradle.replicator.codegen.ParamModel
import com.android.gradle.replicator.codegen.PrettyPrintStream
import com.android.gradle.replicator.codegen.TypeModelImpl
import com.google.common.truth.Truth
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.reflect.full.declaredFunctions

class KotlinClassGeneratorTest {

    private val outputStream = ByteArrayOutputStream()
    private val printer = PrettyPrintStream(PrintStream(outputStream))
    private val kotlinClassGenerator = KotlinClassGenerator(printer, listOf())
    private val stringClassModel = ClassModel(
            String::class,
            String::class.constructors.first(),
            String::class.declaredFunctions)

    @Test
    fun testClassGeneration() {
        kotlinClassGenerator.defineClass("com.foo.package", "FooClass") {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """package com.foo.package

@Suppress("UNUSED_PARAMETER")
class FooClass {
}
"""        )
    }

    @Test
    fun testIfBlockGeneration() {

        kotlinClassGenerator.ifBlock(
                {
                    printer.print("myString.equals(\"foo\")")
                }, {

                }, null)
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
"""if (myString.equals("foo")) {
}
"""        )
    }

    @Test
    fun testIfElseBlockGeneration() {

        kotlinClassGenerator.ifBlock(
                {
                    printer.print("myString.equals(\"foo\")")
                }, {

                }, {
            printer.printlnIndented("println(\"code is else block\")")
        } )
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """if (myString.equals("foo")) {
} else {
    println("code is else block")
}
"""
        )
    }

    @Test
    fun testLoop() {
        kotlinClassGenerator.loopBlock("i", 10) { }
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
"""for (i in 0..10) {
}
"""
        )
    }

    @Test
    fun testLambdaBlock() {
        kotlinClassGenerator.lambdaBlock({ printer.printIndented("listOf(1, 2, 3)") }) {
            printer.printlnIndented("println(it)")
        }
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
"""listOf(1, 2, 3).forEach {
    println(it)
}
"""
        )
    }

    @Test
    fun declareVariable() {
        kotlinClassGenerator.declareVariable(FieldModel("myVar1", stringClassModel, false), "\"Foo\"")
        kotlinClassGenerator.declareVariable(FieldModel("myVar2", stringClassModel, false), "\"Bar\"")
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """val myVar1: String = "Foo"
val myVar2: String = "Bar"
"""
        )
    }

    @Test
    fun declareNullableVariable() {
        kotlinClassGenerator.declareVariable(FieldModel("myVar1", stringClassModel, true), "\"Foo\"")
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """val myVar1: String? = "Foo"
"""
        )
    }

    @Test
    fun declareNoValueVariable() {
        kotlinClassGenerator.declareVariable(FieldModel("myVar1", stringClassModel, true))
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """val myVar1: String?
"""
        )
    }

    @Suppress("unused")
    @Test
    fun declareParameterizedVariable() {
        class ParameterizedType<T: Iterable<U>, U: CharSequence>
        kotlinClassGenerator.declareVariable(FieldModel("myVar1",
                ClassModel(ParameterizedType::class,
                        ParameterizedType::class.constructors.first(),
                        ParameterizedType::class.declaredFunctions
                ),
                false))
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """val myVar1: com.android.gradle.replicator.codegen.kotlin.KotlinClassGeneratorTest${'$'}declareParameterizedVariable${'$'}ParameterizedType<kotlin.collections.Iterable,kotlin.CharSequence>
"""
        )
    }

    @Test
    fun declareMethodWithMultipleParameters() {
        kotlinClassGenerator.declareMethod("method",
                listOf(
                        ParamModel("param0", stringClassModel, false),
                        ParamModel("param1",
                                ClassModel(
                                        Float::class,
                                        Float::class.constructors.first(),
                                        Float::class.declaredFunctions),
                                false)
                ),
                TypeModelImpl(stringClassModel, false),
        """"SomeValue"""") {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """
fun method(
param0: String,
param1: float
): String {
    return "SomeValue"
}
"""
        )
    }

    @Test
    fun declareMethodWithSingleParameter() {
        kotlinClassGenerator.declareMethod("method",
                listOf(
                        ParamModel("param0", stringClassModel, false)
                ),
                TypeModelImpl(stringClassModel, false),
                """"someString"""") {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """
fun method(
param0: String
): String {
    return "someString"
}
"""
        )
    }

    @Test
    fun declareMethodWithNullableReturnType() {
        kotlinClassGenerator.declareMethod("method",
                listOf(
                        ParamModel("param0", stringClassModel, false)
                ),
                TypeModelImpl(stringClassModel,true),
                """"someString"""") {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """
fun method(
param0: String
): String? {
    return "someString"
}
"""
        )
    }

    @Test
    fun declareMethodWithNullableParameter() {
        kotlinClassGenerator.declareMethod("method",
                listOf(
                        ParamModel("param0", stringClassModel, true)
                ),
                TypeModelImpl(stringClassModel, false),
                """"someString"""") {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """
fun method(
param0: String?
): String {
    return "someString"
}
"""
        )
    }

    @Test
    fun declareMethodWithUnitReturnType() {
        kotlinClassGenerator.declareMethod("method",
                listOf(
                        ParamModel("param0", stringClassModel, false)
                ),
                null,
                null) {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """
fun method(
param0: String
) {
}
"""
        )
    }

    @Test
    fun callMethodWithoutParameters() {
        kotlinClassGenerator.callFunction(
                FieldModel("local_field", stringClassModel, false),
                String::toString,
                listOf())
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """local_field.toString()"""
        )
    }

    @Test
    fun callMethodWithParameters() {
        kotlinClassGenerator.callFunction(
                FieldModel("local_field", stringClassModel, false),
                String::compareTo,
                listOf(""""someString""""))
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """local_field.compareTo("someString")"""
        )
    }

    private fun prettyPrint(outputStream: ByteArrayOutputStream) =
            outputStream.toString().replace("\t", "    ")
}