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

import java.io.PrintStream
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter

/**
 * Listener interface to be notified of code generation events.
 * This can be used to decorate the generated code with annotations.
 */
interface CodeGenerationListener {

    enum class Type { CLASS, INTERFACE }

    /**
     * Callback for class generation.
     * @param className name of the class being generated.
     * @param typeParameters list of generic types for the class if any.
     * @param printWriter [PrettyPrintStream] to decorate the class with annotations.
     */
    fun classDeclaration(className: String, type: Type, typeParameters: List<KTypeParameter>, printWriter: PrettyPrintStream)

    /**
     * callback for a method generation. The method will be declared right after the return of this method
     * on the provided [PrintStream]
     *
     * @param methodName name of the method being generated.
     * @param parametersTypes list of [Class] for the method parameters.
     * @param printWriter [PrettyPrintStream] to decorate the method with annotations.
     */
    fun methodDeclaration(methodName: String, parametersTypes: List<Class<*>>, printWriter: PrettyPrintStream)

    fun instanceVariableDeclaration(fieldName: String, type: KClass<*>, printWriter: PrettyPrintStream)
}