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
package com.android.gradle.replicator.codegen.java

import com.android.gradle.replicator.codegen.AbstractTypeModel
import com.android.gradle.replicator.codegen.ClassGenerator
import com.android.gradle.replicator.codegen.ClassModel
import com.android.gradle.replicator.codegen.CodeGenerationListener
import com.android.gradle.replicator.codegen.FieldModel
import com.android.gradle.replicator.codegen.ParamModel
import com.android.gradle.replicator.codegen.PrettyPrintStream
import com.android.gradle.replicator.codegen.TypeModel
import com.android.gradle.replicator.codegen.findSuitableConstructor
import com.android.gradle.replicator.codegen.toTypeModel
import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

class JavaClassGenerator(
    private val printer: PrettyPrintStream,
    private val listeners: List<CodeGenerationListener>
): ClassGenerator {

    private val numberOfLambdas = AtomicInteger(0)

    override fun defineClass(packageName: String, name: String, classContent: () -> Unit) {
        printer.printlnIndented("package $packageName;")
        printer.println()
        listeners.forEach {listener ->
            listener.
            classDeclaration(name, CodeGenerationListener.Type.CLASS, listOf(), printer)
        }
        printer.addBlock("public class $name {")
        classContent()
        printer.endBlock()
    }

    override fun declareVariable(model: FieldModel, initialValue: String?): FieldModel {
        listeners.forEach { listener ->
            listener.instanceVariableDeclaration(model.name, model.classModel.type, printer)
        }
        if (model.modifiers.and(Modifier.PRIVATE) != 0) {
            printer.printIndented("private ")
        }
        printer.printIndented("${model.toJavaDeclaration()} ${model.name}")
        if (initialValue != null) printer.print(" = $initialValue")
        printer.println(";")
        return model
    }

    override fun declareMethod(namePrefix: String, args: List<ParamModel>, returnType: TypeModel?, returnValue: String?, methodBlock: () -> Unit) {
        printer.println()
        listeners.forEach { listener ->
            listener.methodDeclaration(namePrefix, args.map { paramModel -> paramModel.javaClass }, printer)
        }
        printer.addBlock("public ${returnType?.toJavaDeclaration() ?:"void"} $namePrefix(${
            args.joinToString(
                    separator = ",\n",
                    prefix = "\n",
                    postfix = ") throws Throwable {"
            ) { "${it.toJavaDeclaration()} ${it.name}" }
        }")
        methodBlock()
        returnType?.let {
            printer.printlnIndented("return $returnValue;")
        }
        printer.endBlock()
    }

    override fun loopBlock(namePrefix: String, until: Int, block: (indexName: String) -> Unit) {
        printer.addBlock("for (int $namePrefix = 0; $namePrefix < $until; $namePrefix++) {")
        block(namePrefix)
        printer.endBlock()
    }

    override fun lambdaBlock(beforeBlock: (() -> Unit)?, block: () -> Unit) {
        printer.printIndented("")
        beforeBlock?.invoke() ?: printer.print("java.util.Collections.emptyList()")
        printer.println(".forEach( lambda${numberOfLambdas.getAndIncrement()} -> {")
        printer.addBlock()
        printer.printlnIndented("try {")
        printer.addBlock()
        block()
        printer.endBlock("} catch(Throwable e) { } // ignore")
        printer.endBlock("});")
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

    override fun callFunction(receiver: FieldModel, function: KFunction<*>, parameterValues: List<String>) {
        // let's avoid Kotlin automatic translation of method names like "intValue()" to "toInt()"
        val functionName = function.javaMethod?.name ?: function.name
        printer.print(
                "${receiver.name}.${functionName}(${parameterValues.joinToString(", ")})")
    }

    override fun allocateValue(random: Random, isVararg: Boolean, classModel: AbstractTypeModel<*>): String {
        val javaType = classModel.type.java
        return when (javaType.name) {
            "java.lang.String" -> """"SomeString""""
            "double" -> "${random.nextDouble()}d"
            "java.lang.Double" -> "${random.nextDouble()}d"
            "float" -> "${random.nextFloat()}f"
            "java.lang.Float" -> "${random.nextFloat()}f"
            "long" -> "${random.nextInt()}l"
            "java.lang.Long" -> "${random.nextInt()}l"
            "short" -> "(short) ${random.nextInt(128).toString()}"
            "java.lang.Short" -> "(short) ${random.nextInt(128).toString()}"
            "byte" -> """(byte) ${random.nextBytes(1)[0]}"""
            "java.lang.Byte" -> """(byte) ${random.nextBytes(1)[0]}"""
            "int" -> random.nextInt(0, 100).toString()
            "java.lang.Integer" -> random.nextInt(0, 100).toString()
            "char" -> """'C'"""
            "java.lang.Character" -> """'C'"""
            "boolean" -> random.nextBoolean().toString()
            "java.lang.Boolean" -> random.nextBoolean().toString()
            "java.lang.Class" -> "this.getClass()"
            else -> {
                if (javaType.isArray) {
                    return("new ${printArrayConstruction(random, javaType)}")
                }
                val selectorConstructor = classModel.constructor
                val parametersValue = selectorConstructor.parameters.joinToString {
                    allocateValue(random, it.isVararg, it.type.jvmErasure.toTypeModel())
                }
                val className = javaType.canonicalName
                return "new ${className}($parametersValue)"
            }
        }
    }

    private fun printArrayConstruction(random: Random, type: Class<*>): String {
        return if (type.componentType.isArray) "${printArrayConstruction(random, type.componentType)}[${random.nextInt(0, 100)}]"
            else "${type.componentType.name}[${random.nextInt(0, 100)}]"
    }

    override fun println() = printer.println()

    override fun indent() = printer.printIndented("")

    override fun print(vararg strings: String) {
        strings.forEach(printer::print)
    }

    override fun addLineDelimiter() {
        printer.println(";")
    }

    private fun TypeModel.toJavaDeclaration(): String =
            classModel.type.toJavaDeclaration()

    private fun KClass<*>.toJavaDeclaration(): String =
            when {
                typeParameters.isNotEmpty() -> "$qualifiedName${
                    typeParameters.joinToString(
                            separator = ",",
                            prefix = "<",
                            postfix = ">"
                    ) {
                        val qualifiedName = it.upperBounds[0].jvmErasure.qualifiedName
                        if (qualifiedName == null || qualifiedName == "kotlin.Any")
                            "Object" 
                        else "? extends ${it.upperBounds[0].jvmErasure.jvmName.replace('$', '.')}"
                    }
                }"
                else -> javaObjectType.name
            }
}