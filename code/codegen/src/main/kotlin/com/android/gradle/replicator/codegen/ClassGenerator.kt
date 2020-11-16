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
import kotlin.reflect.KFunction

/**
 * Interface for generating class sources in a programming language.
 *
 * A class is defined with instance variables and methods which all have some blocks of code.
 */
interface ClassGenerator {
    /**
     * Defines the class being generated
     * @param packageName the class package name.
     * @param name the class name, must be unique in the package.
     * @param classContent call back to define the class content within the scope of the class declaration.
     */
    fun defineClass(packageName: String, name: String, classContent: () -> Unit)

    /**
     * Declare a variable in the current scope.
     * @param model the variable model
     * @param initialValue value obtained by [allocateValue] to initialize the field with.
     * @return updated model with final declared variable name
     */
    fun declareVariable(model: FieldModel, initialValue: String? = null): FieldModel

    /**
     * Declare a method in the current scope.
     * @param namePrefix name prefix to generate a unique method name in the current scope.
     * @param args arguments for a method.
     * @param returnType method return type.
     * @param returnValue method return value as a String.
     * @param methodBlock call back to define the method code using one the block construction helpers like [loopBlock]
     */
    fun declareMethod(namePrefix: String, args: List<ParamModel>, returnType: TypeModel?, returnValue: String?, methodBlock: () -> Unit)

    /**
     * Adds a simple loop block in the current scope.
     * @param namePrefix name prefix to generate a unique index name for the loop.
     * @param block the callback to define the loop code content. The parameter passed to the lamba is the final
     * variable name for the loop index.
     */
    fun loopBlock(namePrefix: String, until: Int, block: (indexName: String) -> Unit)

    /**
     * Adds a simple [Iterable.forEach]lambda block in the current scope.
     * @param beforeBlock this is the block allowing for a lambda call, like setting up a collection so
     * [Iterable.forEach] can be called. If not provided, the generator should create it.
     */
    fun lambdaBlock(beforeBlock: (() -> Unit)?, block: () -> Unit)

    /**
     * Adds an if-then-else statement to the current scope.
     * @param condition the condition for the if statement.
     * @param block callback defining the block that will be executed when [condition] is true.
     * @param elseBlock optional callback defining the block that will be executed with [condition] is false. If null,
     * there is no else block in the if statement.
     */
    fun ifBlock(condition: () -> Unit, block: () -> Unit, elseBlock: (() -> Unit)?)

    /**
     * call a function
     * @param receiver model for the variable containing the instance of the receiver.
     * @param function [KFunction] descriptor of the function to be called.
     * @param parameterValues list of parameter values that should be passed when invoking the function.
     */
    fun callFunction(receiver: FieldModel, function: KFunction<*>, parameterValues: List<String>)

    /**
     * Allocate a value for a type.
     * @param random random value provider.
     * @param classModel the [ClassModel] of the value to be allocated.
     * @return the value or code to instantiate a value to be inserted in the class generation.
     */
    fun allocateValue(random: Random,
                      isVararg: Boolean,
                      classModel: AbstractTypeModel<*>): String

    /**
     * Prints a new line
     */
    fun println()

    /**
     * Prints some white space corresponding to the current scope indentation level.
     */
    fun indent()

    /**
     * Prints the strings to the output
     * @param strings 0 to many strings to be printed.
     */
    fun print(vararg strings: String)

    fun addLineDelimiter()
}