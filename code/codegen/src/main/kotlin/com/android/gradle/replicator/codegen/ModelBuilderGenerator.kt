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

class ModelBuilderClassGenerator(
    private val generator: ClassGenerator
): ClassGenerator by generator {
    private var model: GeneratedClassModel? = null
    private val blocks = ArrayDeque<Block>()

    class Block {
        var declaredVariables = mutableListOf<ParamModel>()
        var loopsCount = 0

        fun nextVariableName(prefix: String): String = "$prefix${declaredVariables.size}"
    }

    override fun defineClass(packageName: String, name: String, classContent: () -> Unit) {
        val model = GeneratedClassModel(packageName, name)
        this.model = model
        val blockWithScope = {
            startBlockScope()
            classContent()
            model.fields.addAll(currentScope().declaredVariables)
            endBlockScope()
        }
        generator.defineClass(packageName, name, blockWithScope)
    }

    override fun declareMethod(namePrefix: String, args: List<ParamModel>, returnType: TypeModel?, returnValue: String?, methodBlock: () -> Unit) {
        val model = checkNotNull(model)
        val finalMethodName = "$namePrefix${model.methods.size}"
        model.methods.add(MethodModel(finalMethodName, args, returnType))
        startBlockScope()
        currentScope().declaredVariables.addAll(args)
        generator.declareMethod(finalMethodName, args, returnType, returnValue, methodBlock)
        endBlockScope()
    }

    override fun loopBlock(namePrefix: String, until: Int, block: (variableName: String) -> Unit) {
        currentScope().loopsCount++
        startBlockScope()
        val nbOfLoops = blocks.map { it.loopsCount }.sum()
        generator.loopBlock(namePrefix + ('A' + nbOfLoops/26) + ('a' + nbOfLoops%26), until, block)
        endBlockScope()
    }

    override fun lambdaBlock(beforeBlock: (() -> Unit)?, block: () -> Unit) {
        startBlockScope()
        generator.lambdaBlock(beforeBlock, block)
        endBlockScope()
    }

    override fun ifBlock(condition: () -> Unit, block: () -> Unit, elseBlock: (() -> Unit)?) {
        startBlockScope()
        val blockWithScoping: (() -> Unit)? = if (elseBlock != null) {
            {
                endBlockScope()
                startBlockScope()
                elseBlock()
            }
        } else null
        generator.ifBlock(condition,  block, blockWithScoping)
        endBlockScope()
    }

    override fun declareVariable(model: FieldModel, initialValue: String?): FieldModel {
        val scope = currentScope()
        val updatedModel = ParamModel(
            scope.nextVariableName("${model.name}_${blocks.size}_"),
            model.classModel,
            model.nullable,
            model.modifiers)
        scope.declaredVariables.add(updatedModel)
        return generator.declareVariable(updatedModel, initialValue)
    }

    fun getLocalVariables(): List<ParamModel> = mutableListOf<ParamModel>().also { list ->
        blocks.forEach { list.addAll(it.declaredVariables) }
    }

    fun getMethodParametersVariables(): List<ParamModel> = mutableListOf<ParamModel>().also { list ->
        blocks.forEach { list.addAll(it.declaredVariables.filter { it.name.startsWith("param") }) }
    }

    private fun startBlockScope() {
        blocks.addFirst(Block())
    }

    private fun endBlockScope() {
        blocks.removeFirst()
    }

    private fun currentScope() = blocks.first()
}