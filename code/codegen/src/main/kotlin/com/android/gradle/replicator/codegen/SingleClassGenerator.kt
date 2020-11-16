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

import java.lang.reflect.Modifier
import kotlin.random.Random
import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.jvm.kotlinFunction

class SingleClassGenerator(
        generator: ClassGenerator,
        private val params: ClassGenerationParameters,
        private val packageName: String,
        private val className: String,
        private val apiClassPicker: ImportClassPicker,
        private val implClassPicker: ImportClassPicker,
        private val random: Random
) {

    private val classGenerator = ModelBuilderClassGenerator(generator)

    /**
     * Generate a single class using the provided generation parameters.
     */
    fun generate() {
        classGenerator.defineClass(packageName, className) {

            if (params.maxNumberOfInstanceVars > 0) {
                for (i in 0..random.nextInt(
                        params.minNumberOfInstanceVars,
                        params.maxNumberOfInstanceVars
                )) {
                    addInstanceVariable()
                }
            }

            if (params.maxNumberOfMethods > 0) {
                for (i in 0..random.nextInt(
                        params.minNumberOfMethods,
                        params.maxNumberOfMethods
                )) {
                    addMethod()
                }
            }
        }
    }

    private fun addMethod() {
        // generate parameters.
        val parameters = mutableListOf<ParamModel>()
        for (j in 0..random.nextInt(0, params.maxNumberOfMethodParameters)) {
            val parameterType = findSuitableType(apiClassPicker)
            parameters.add(
                ParamModel(
                    "param$j",
                    parameterType,
                    false))
        }
        classGenerator.declareMethod("method", parameters, null, null) {
            for (blockNumber in params.minNumberOfMethods..random.nextInt(params.maxNumberOfMethodBlocks)) {
                addBlock()
            }
        }
    }

    private fun addBlock() {
        when(random.nextInt(10)) {
            in 0..1 -> addLoopBlock()
            in 2..6 -> addMethodCall()
            7 -> addLambda()
            in 8..9 -> addIfStatement()
            else -> println("No statement !")
        }
    }

    private fun addLoopBlock() {
        classGenerator.loopBlock("i", random.nextInt(7)) {
            if (params.maxNumberOfBlocksInLoop > 0) {
                for (i in 0..Random.nextInt(params.maxNumberOfBlocksInLoop)) {
                    addBlock()
                }
            }
        }
    }

    private fun addInstanceVariable(): KClass<*> {
        val receiverType = findSuitableType(apiClassPicker)

        classGenerator.declareVariable(
            FieldModel("instance_var", receiverType, false, Modifier.PRIVATE),
            classGenerator.allocateValue(random, false, receiverType))
        return receiverType.type
    }

    private fun addMethodCall() {
        when(random.nextInt(10)) {
            in 0..6 -> addLocalVariableAndMethodCall()
            in 7..9 -> addFunctionParameterMethodCall()
            else -> println("No statement !")
        }
    }

    private fun addLocalVariableAndMethodCall() {
        val allocatableType = findSuitableType(implClassPicker)
        val variableName = classGenerator.declareVariable(
                FieldModel("local_var", allocatableType, false),
                classGenerator.allocateValue(random, false, allocatableType)
        )
        classGenerator.indent()
        addMethodCall(variableName, allocatableType.callableMethods.random(random))
        classGenerator.println()
    }


    /**
     * Information about a method call, the variable containing the instance to be invoked, and the method to invoke.
     */
    private class MethodCall(
            val variable: FieldModel,
            val method: KFunction<*>
    )

    private fun addFunctionParameterMethodCall() {
        classGenerator.getLocalVariables().forEach { paramModel ->
            if (paramModel.classModel.type.simpleName?.contains("String") == false) {
                findSuitableMethodToCall(paramModel.classModel.type.declaredMemberFunctions)?.let {
                    classGenerator.indent()
                    addMethodCall(paramModel, it)
                    classGenerator.println()
                    return@addFunctionParameterMethodCall
                }
            }
        }
    }

    private fun addMethodCall(receiver: FieldModel, method: KFunction<*>) {
        addInlineMethodCall(receiver, method)
        classGenerator.addLineDelimiter()
    }

    private fun addInlineMethodCall(receiver: FieldModel, method: KFunction<*>) {
        val parametersValues = method.parameters.filter {
            it.kind == KParameter.Kind.VALUE
        }.map {
            classGenerator.allocateValue(random, it.isVararg, it.type.jvmErasure.toTypeModel())
        }
        classGenerator.callFunction(
                receiver,
                method,
                parametersValues
        )
    }

    private fun addIfStatement() {
        val methodToCall = findMethodReturning(Boolean::class.createType())
        val shouldGenerateElseBlock = random.nextBoolean()
        val block = {
            if (params.maxNumberOfBlocksInIf > 0) {
                for (statementIndex in 0..random.nextInt(params.maxNumberOfBlocksInIf - 1)) {
                    addBlock()
                }
            }
        }
        val elseBlock: (() -> Unit)? = if (shouldGenerateElseBlock && params.maxNumberOfBlocksInIfElse > 0) {
            {
                for (statementIndex in 0..random.nextInt(params.maxNumberOfBlocksInIfElse - 1)) {
                    addBlock()
                }
            }
        } else null

        if (methodToCall == null) {
            val allocatedValue = classGenerator.allocateValue(random, false,
                    ClassModel(
                            Object::class,
                            Object::class.primaryConstructor!!,
                            listOf()))
            classGenerator.ifBlock({ classGenerator.print("${allocatedValue}.equals(${allocatedValue})") }, block, elseBlock)
        } else {
            classGenerator.ifBlock({ addInlineMethodCall(methodToCall.variable, methodToCall.method) }, block, elseBlock)
        }
    }

    private fun addLambda() {
        val methodToCall = findMethodReturning(Iterable::class.createType(listOf(KTypeProjection(KVariance.OUT, Any::class.createType()))))
        val beforeBlock: (() -> Unit)? = if (methodToCall != null) {
            { addMethodCall(methodToCall.variable, methodToCall.method) }
        } else null
        classGenerator.lambdaBlock(beforeBlock) {
            if (params.maxNumberOfBlocksInLambda > 0) {
                for (statementIndex in 0..random.nextInt(params.maxNumberOfBlocksInLambda)) {
                    addBlock()
                }
            }
        }
    }

    private fun findMethodReturning(desiredReturnType: KType): MethodCall? {
        return findMethodReturning(classGenerator.getMethodParametersVariables(), desiredReturnType)
                ?: findMethodReturning(classGenerator.getLocalVariables(), desiredReturnType)
    }

    private fun findMethodReturning(fieldModels: List<FieldModel>, desiredReturnType: KType): MethodCall? {
        val startIndex = random.nextInt(fieldModels.size)
        var methodCall: MethodCall?
        var currentIndex = startIndex
        do {
            methodCall = findMethodReturning(fieldModels[currentIndex], desiredReturnType)
            currentIndex = (currentIndex + 1) % fieldModels.size
        } while (methodCall == null && startIndex != currentIndex)
        return methodCall
    }

    private fun findMethodReturning(fieldModel: FieldModel, desiredReturnType: KType): MethodCall? {
        // TODO : add support for looking up java methods. be aware of the issues related to kotlin reflection.
//        fieldModel.classModel.type.java.methods.filter {
//            method -> method.returnType.typeName == desiredReturnType.javaType.typeName
//        }
//        val methodsWithCorrectReturnType =
//            fieldModel.classModel.type.declaredFunctions.filter { method ->
//                         method.returnType == desiredReturnType
//            }
//
//        findSuitableMethodToCall(methodsWithCorrectReturnType)?.let {
//            return MethodCall(fieldModel, it)
//        }
        return null
    }

    private val stringClassModel = ClassModel(String::class,
            String::class.constructors.first(),
            String::class.java.methods.mapNotNull {
                try { it.kotlinFunction } catch (t: Throwable) { null } })

    private fun findSuitableType(classPicker: ImportClassPicker): ClassModel<*> {
        val selectedType = classPicker.pickClass(random)
                ?: apiClassPicker.pickClass(random)

        return selectedType ?: stringClassModel
    }

    private fun findSuitableMethodToCall(methods: Collection<KFunction<*>>): KFunction<*>? {
        val suitableMethods = methods
            .filter {
                it.isNotDeprecated()
                        && it.isPublic()
                        && it.allParametersCanBeInstantiated(mutableListOf<Class<*>>())
                        && (it.parameters.any { parameter -> parameter.kind == KParameter.Kind.INSTANCE })
                }
        return suitableMethods.randomOrNull(random)
    }
}