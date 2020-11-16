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

import com.android.gradle.replicator.codegen.java.JavaClassGenerator
import com.android.gradle.replicator.codegen.kotlin.KotlinClassGenerator
import com.google.common.truth.Truth
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.reflect.full.declaredFunctions

class JavaClassGeneratorTest {

    private val outputStream = ByteArrayOutputStream()
    private val printer = PrettyPrintStream(PrintStream(outputStream))
    private val javaClassGenerator = JavaClassGenerator(printer, listOf())
    private val stringClassModel = ClassModel(
            String::class,
            String::class.constructors.first(),
            String::class.declaredFunctions)

    @Test
    fun testClassGeneration() {
        javaClassGenerator.defineClass("com.foo.package", "FooClass") {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """package com.foo.package;

public class FooClass {
}
"""        )
    }

    @Test
    fun testIfBlockGeneration() {

        javaClassGenerator.ifBlock(
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

        javaClassGenerator.ifBlock(
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
        javaClassGenerator.loopBlock("i", 10) { }
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """for (int i = 0; i < 10; i++) {
}
"""
        )
    }

    @Test
    fun testLambdaBlock() {
        javaClassGenerator.lambdaBlock(null) {
            printer.printlnIndented("println(it)")
        }
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """java.util.Collections.emptyList().forEach( lambda0 -> {
    try {
        println(it)
    } catch(Throwable e) { } // ignore
});
"""
        )
    }

    @Test
    fun testLambdaBlockWithBlock() {
        javaClassGenerator.lambdaBlock({
            printer.printIndented("System.getProperties()") }) {
            printer.printlnIndented("println(it)")
        }
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """System.getProperties().forEach( lambda0 -> {
    try {
        println(it)
    } catch(Throwable e) { } // ignore
});
"""
        )
    }

    @Test
    fun declareVariable() {
        javaClassGenerator.declareVariable(FieldModel("myVar1", stringClassModel, false), "\"Foo\"")
        javaClassGenerator.declareVariable(FieldModel("myVar2", stringClassModel, false), "\"Bar\"")
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """java.lang.String myVar1 = "Foo";
java.lang.String myVar2 = "Bar";
"""
        )
    }

    @Test
    fun declareNullableVariable() {
        javaClassGenerator.declareVariable(FieldModel("myVar1", stringClassModel, true), "\"Foo\"")
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """java.lang.String myVar1 = "Foo";
"""
        )
    }

    @Test
    fun declareNoValueVariable() {
        javaClassGenerator.declareVariable(FieldModel("myVar1", stringClassModel, true))
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """java.lang.String myVar1;
"""
        )
    }

    @Test
    fun declareMethodWithMultipleParameters() {
        javaClassGenerator.declareMethod("method",
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
public java.lang.String method(
java.lang.String param0,
java.lang.Float param1) throws Throwable {
    return "SomeValue";
}
"""
        )
    }

    @Test
    fun declareMethodWithSingleParameter() {
        javaClassGenerator.declareMethod("method",
                listOf(
                        ParamModel("param0", stringClassModel, false)
                ),
                TypeModelImpl(stringClassModel, false),
                """"someString"""") {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """
public java.lang.String method(
java.lang.String param0) throws Throwable {
    return "someString";
}
"""
        )
    }

    @Test
    fun declareMethodWithNullableReturnType() {
        javaClassGenerator.declareMethod("method",
                listOf(
                        ParamModel("param0", stringClassModel, false)
                ),
                TypeModelImpl(stringClassModel,true),
                """"someString"""") {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """
public java.lang.String method(
java.lang.String param0) throws Throwable {
    return "someString";
}
"""
        )
    }

    @Test
    fun declareMethodWithNullableParameter() {
        javaClassGenerator.declareMethod("method",
                listOf(
                        ParamModel("param0", stringClassModel, true)
                ),
                TypeModelImpl(stringClassModel, false),
                """"someString"""") {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """
public java.lang.String method(
java.lang.String param0) throws Throwable {
    return "someString";
}
"""
        )
    }

    @Test
    fun declareMethodWithUnitReturnType() {
        javaClassGenerator.declareMethod("method",
                listOf(
                        ParamModel("param0", stringClassModel, false)
                ),
                null,
                null) {}
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """
public void method(
java.lang.String param0) throws Throwable {
}
"""
        )
    }

    @Test
    fun callMethodWithoutParameters() {
        javaClassGenerator.callFunction(
                FieldModel("local_field", stringClassModel, false),
                String::toString,
                listOf())
        Truth.assertThat(prettyPrint(outputStream)).isEqualTo(
                """local_field.toString()"""
        )
    }

    @Test
    fun callMethodWithParameters() {
        javaClassGenerator.callFunction(
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