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

import com.android.gradle.replicator.codegen.ClassGenerator
import com.android.gradle.replicator.codegen.CodeGenerationListener
import com.android.gradle.replicator.codegen.FieldModel
import com.android.gradle.replicator.codegen.ParamModel
import com.android.gradle.replicator.codegen.PrettyPrintStream
import com.android.gradle.replicator.codegen.TypeModel
import com.android.gradle.replicator.codegen.findSuitableConstructor
import java.lang.IllegalStateException
import java.lang.reflect.Modifier
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

class KotlinClassGenerator(
        private val printer: PrettyPrintStream,
        private val listeners: List<CodeGenerationListener>
): ClassGenerator {

    override fun defineClass(packageName: String, name: String, classContent: () -> Unit) {
        printer.printlnIndented("package $packageName")
        printer.println()
        listeners.forEach {listener ->
            listener.
            classDeclaration(name, CodeGenerationListener.Type.CLASS, listOf(), printer)
        }
        printer.printlnIndented("@Suppress(\"UNUSED_PARAMETER\")")
        printer.addBlock("class $name {")
        classContent()
        printer.endBlock()
    }

    override fun declareMethod(namePrefix: String, args: List<ParamModel>, returnType: TypeModel?, returnValue: String?, methodBlock: () -> Unit) {
        printer.println()
        listeners.forEach { listener ->
            listener.methodDeclaration(namePrefix, args.map { paramModel -> paramModel.javaClass }, printer)
        }
        printer.addBlock("fun $namePrefix(${
            args.joinToString(
                separator = ",\n",
                prefix = "\n"
            ) { "${it.name}: ${it.toKotlinDeclaration()}${if (it.nullable) "?" else ""}" }
        }",
        if (returnType == null) ") {" else "): ${returnType.toKotlinDeclaration()}${if (returnType.nullable) "?" else ""} {")
        methodBlock()
        returnType?.let {
            printer.printlnIndented("return $returnValue")
        }
        printer.endBlock()
    }


    private fun TypeModel.toKotlinDeclaration(): String =
            type.toKotlinDeclaration()

    private fun KClass<*>.toKotlinDeclaration(): String =
        when {
            typeParameters.isNotEmpty() -> "${jvmName}${
                typeParameters.joinToString(
                    separator = ",",
                    prefix = "<",
                    postfix = ">"
                ) {
                    it.upperBounds[0].jvmErasure.qualifiedName ?: "*"
                }
            }"
            java.name.startsWith("java.lang") -> java.simpleName
            else -> jvmName
        }

    override fun loopBlock(namePrefix: String, until: Int, block: (variableName: String) -> Unit) {
        printer.addBlock("for ($namePrefix in 0..$until) {")
        block(namePrefix)
        printer.endBlock()
    }

    override fun ifBlock(condition: () -> Unit, block: () -> Unit, elseBlock: (() -> Unit)?) {
        printer.printIndented("if (")
        condition()
        printer.println(") {")
        printer.addBlock()
        block()
        if (elseBlock != null) {
            printer.endBlock("} else {")
            printer.addBlock()
            elseBlock()
        }
        printer.endBlock()
    }

    override fun declareVariable(model: FieldModel, initialValue: String?): FieldModel {
        listeners.forEach { listener ->
            listener.instanceVariableDeclaration(model.name, model.type, printer)
        }
        if (model.modifiers.and(Modifier.PRIVATE) != 0) {
            printer.printIndented("private ")
        }
        printer.printIndented("val ${model.name}: ${model.toKotlinDeclaration()}")
        if (model.nullable) printer.print("?")
        if (initialValue == null) printer.println()
        else printer.println(" = $initialValue")
        return model
    }

    override fun println() = printer.println()

    override fun indent() = printer.printIndented("")

    override fun print(vararg strings: String) {
        strings.forEach(printer::print)
    }

    override fun callFunction(receiver: FieldModel, function: KFunction<*>, parameterValues: List<String>) {
        val typeParameters = if (function.typeParameters.isEmpty()) ""
        else function.typeParameters.joinToString(
            separator = ",",
            prefix = "<",
            postfix = ">"
        ) {
            it.upperBounds[0].jvmErasure.qualifiedName ?: "*"
        }
        printer.print(
                "${receiver.name}${if (receiver.nullable) "?." else "."}${function.name}$typeParameters(${parameterValues.joinToString(", ")})")
    }

    override fun lambdaBlock(beforeBlock: () -> Unit, block: () -> Unit) {
        printer.printIndented("")
        beforeBlock()
        printer.println(".forEach {")
        printer.addBlock()
        block()
        printer.endBlock()
    }

    override fun allocateValue(random: Random, type: KClass<*>, constructor: KFunction<Any>?): String {
        return when (type.simpleName) {
            "String" -> """"SomeString""""
            "Double" -> random.nextFloat().toString()
            "Float" -> random.nextFloat().toString()
            "Long" -> random.nextLong().toString()
            "Int" -> random.nextInt(0, 100).toString()
            "Char" -> """'C'"""
            "Boolean" -> random.nextBoolean().toString()
            "Class" -> "this.javaClass"
            else -> {
                val selectorConstructor = constructor ?: type.findSuitableConstructor()
                ?: throw IllegalStateException("Selected class $type does not have a public constructor")
                val parametersValue = selectorConstructor.parameters.joinToString {
                    allocateValue(random, it.type.jvmErasure, null)
                }
                return "${type.qualifiedName}($parametersValue)"
            }
        }
    }
}