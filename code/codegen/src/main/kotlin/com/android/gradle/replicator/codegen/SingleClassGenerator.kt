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

import kotlin.random.Random
import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.jvmErasure

class SingleClassGenerator(
        generator: ClassGenerator,
        private val params: ClassGenerationParameters,
        private val packageName: String,
        private val className: String,
        private val eligibleClasses: List<KClass<*>>,
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
            val parameterType = findSuitableType()
            parameters.add(
                ParamModel(
                    "param$j",
                    parameterType.type,
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
        val receiverType = findSuitableType()

        classGenerator.declareVariable(
            FieldModel("instance_var", receiverType.type, false),
            classGenerator.allocateValue(random, receiverType.type, receiverType.constructor))
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
        val methodCall = findMethodToCall()
        val allocatableType = methodCall.first
        val variableName = classGenerator.declareVariable(
                FieldModel("local_var", allocatableType.type, false),
                classGenerator.allocateValue(random, allocatableType.type, allocatableType.constructor)
        )
        classGenerator.indent()
        addMethodCall(variableName, methodCall.second)
        classGenerator.println()
    }

    /**
     * Information about a type that can be allocated through a constructor method reference.
     */
    private class AllocatableType(
            val type:KClass<out Any>,
            val constructor: KFunction<Any>
    )

    /**
     * Information about a method call, the variable containing the instance to be invoked, and the method to invoke.
     */
    private class MethodCall(
            val variable: FieldModel,
            val method: KFunction<*>
    )

    private fun findMethodToCall(): Pair<AllocatableType, KFunction<*>> {
        repeat(MaxTry) {
            val receiverType = findSuitableType()
            findSuitableMethodToCall(receiverType.type.declaredMemberFunctions)?.let {
                return@findMethodToCall receiverType to it
            }
        }
        return AllocatableType(String::class, String::class.constructors.first()) to String::toString
    }

    private fun addFunctionParameterMethodCall() {
        classGenerator.getLocalVariables().forEach { paramModel ->
            findSuitableMethodToCall(paramModel.type.declaredMemberFunctions)?.let {
                    classGenerator.indent()
                    addMethodCall(paramModel, it)
                    classGenerator.println()
                    return@addFunctionParameterMethodCall

            }
        }
    }

    private fun addMethodCall(receiver: FieldModel, method: KFunction<*>) {
        val parametersValues = method.parameters.filter {
            it.kind == KParameter.Kind.VALUE
        }.map {
            val prefix= if (it.isVararg) "*" else ""
            prefix + classGenerator.allocateValue(random, it.type.jvmErasure, it.findSuitableConstructor())
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
            classGenerator.ifBlock({ classGenerator.print("Object().equals(Object())") }, block, elseBlock)
        } else {
            classGenerator.ifBlock({ addMethodCall(methodToCall.variable, methodToCall.method) }, block, elseBlock)
        }
    }

    private fun addLambda() {
        val methodToCall = findMethodReturning(Iterable::class.createType(listOf(KTypeProjection(KVariance.OUT, Any::class.createType()))))
        val beforeBlock = if (methodToCall == null) {
            // TODO: fix this, it is kotlin code.
            { classGenerator.print("""listOf("1", "2", "3")""") }
        } else {
            { addMethodCall(methodToCall.variable, methodToCall.method) }
        }
        classGenerator.lambdaBlock(beforeBlock) {
            if (params.maxNumberOfBlocksInLambda > 0) {
                for (statementIndex in 0..random.nextInt(params.maxNumberOfBlocksInLambda)) {
                    addBlock()
                }
            }
        }
    }

    private fun findMethodReturning(desiredReturnType: KType): MethodCall? {
        repeat(MaxTry) {
            val methodCall = findMethodReturning(
                    classGenerator.getMethodParametersVariables().random(),
                    desiredReturnType
            )
            if (methodCall != null) return@findMethodReturning methodCall
        }
        repeat(MaxTry) {
            val methodCall = findMethodReturning(
                    classGenerator.getLocalVariables().random(),
                    desiredReturnType
            )
            if (methodCall != null) return@findMethodReturning methodCall
        }
        return null
    }

    private fun findMethodReturning(fieldModel: FieldModel, desiredReturnType: KType): MethodCall? {
        val methodsWithCorrectReturnType =
                fieldModel.type.functions.filter { method ->
//                            method.javaMethod?.declaringClass != Object::class.java
                            method.returnType == desiredReturnType
                }

        findSuitableMethodToCall(methodsWithCorrectReturnType)?.let {
            return MethodCall(fieldModel, it)
        }
        return null
    }

    companion object {
        /**
         * Number of times we should try to randomly select a suitable type from a collection.
         */
        private const val MaxTry = 10
    }

    private fun findSuitableType(): AllocatableType {
        val selectedType = eligibleClasses.random(random)
        val selectedConstructor = selectedType.findSuitableConstructor()
            ?: throw IllegalStateException("$selectedType does not have an eligible constructor, yet it was selected.")
        return AllocatableType(selectedType, selectedConstructor)
    }

    private fun findSuitableMethodToCall(methods: Collection<KFunction<*>>): KFunction<*>? {
        val suitableMethods = methods
            .filter {
                it.isNotDeprecated()
                        && it.isPublic()
                        && it.allParametersCanBeInstantiated()
                        && (it.parameters.any { parameter -> parameter.kind == KParameter.Kind.INSTANCE })
                }
        return suitableMethods.randomOrNull(random)
    }
}