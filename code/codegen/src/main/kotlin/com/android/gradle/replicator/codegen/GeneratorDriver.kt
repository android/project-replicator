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

import java.io.File
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.random.Random
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

class GeneratorDriver(
        private val parameters: CodeGenerationParameters,
        private val random: Random,
        private val generatorAllocator: (printer: PrettyPrintStream, listeners: List<CodeGenerationListener>) -> ClassGenerator
): SourceGenerator {

    /**
     * Generate a single class
     *
     * @param packageName the package name for the class to generate.
     * @param className the class name to generate.
     * @param printStream the [PrettyPrintStream] to use to generate the code.
     * @param listeners optional list of code generation listeners to further customize generated code.
     */
    override fun generateClass(
            packageName: String,
            className: String,
            printStream: PrettyPrintStream,
            listeners: List<CodeGenerationListener>) {
        SingleClassGenerator(
                generator = generatorAllocator(printStream, listeners),
                packageName = packageName,
                className = className,
                random = random,
                apiClassPicker = apiImportClassPicker,
                implClassPicker = implementationImportClassPicker,
                params = parameters.classGenerationParameters
        ).generate()
    }

    /**
     * Imported module or dependency with the jar file location and the classes names present in that jar file.
     */
    data class ModuleImport(
            val origin: File,
            val classes: List<String>
    )

    /**
     * list of [ModuleImport] of modules that are part of the api configuration of this module
     */
    private val apiModules: List<ModuleImport> by lazy {
        createModuleList(parameters.apiClasspath)
    }

    /**
     * list of [ModuleImport] of modules (with code generated content) that are part of the 'api' configuration of
     * this module
     */
    private val codeGeneratedApiModules: List<ModuleImport> by lazy {
        createModuleList(parameters.codeGeneratedModuleApiClasspath)
    }

    /**
     * list of [ModuleImport] of modules (with code generated content) that are part of the 'impl' configuration of
     * this module
     */
    private val codeGeneratedImplModules: List<ModuleImport> by lazy {
        createModuleList(parameters.codeGeneratedModuleImplClasspath)
    }

    /**
     * list of [ModuleImport] of modules/dependencies that are part of the implementation configuration of this module.
     */
    private val implModules: List<ModuleImport> by lazy {
        createModuleList(parameters.implClasspath)
    }

    /**
     * To be able to load successfully all imported classes, we use the runtime classpath to create the class loader.
     */
    private val combinedClassLoader by lazy {
        URLClassLoader(parameters.runtimeClasspath.map {
            it.toURI().toURL()
        }.toTypedArray())
    }

    /**
     * [ImportClassPicker] that picks classes that are part of the api configuration. This means those types can be used
     * in any public facing API this code will generate.
     */
    private val apiImportClassPicker by lazy {
        val useOnlyProjectClasses = codeGeneratedApiModules.isNotEmpty()
        println("Use only Code generated modules for API $useOnlyProjectClasses : ${codeGeneratedApiModules.size}")
        ImportClassPicker(combinedClassLoader ,
                if (useOnlyProjectClasses) codeGeneratedApiModules else apiModules,
                !useOnlyProjectClasses)
    }

    /**
     * [ImportClassPicker] that picks classes that are part of the implementation configuration. This means those types
     * can only be used in method implementations or private fields but cannot be visible to modules importing the
     * generated code.
     */
    private val implementationImportClassPicker by lazy {
        val useOnlyProjectClasses = codeGeneratedImplModules.isNotEmpty()
        println("Use only Code generated modules for Impl $useOnlyProjectClasses : ${codeGeneratedImplModules.size}")
        ImportClassPicker(combinedClassLoader,
                if (useOnlyProjectClasses) codeGeneratedImplModules else implModules,
                !useOnlyProjectClasses)
    }

    /**
     * parse a classpath and create a list of [ModuleImport] for each jar file with the list of class file content
     * for each jar.
     */
    private fun createModuleList(classpath: List<File>): List<ModuleImport> {
        val modulesList = mutableListOf<ModuleImport>()
        classpath
                .forEach { file ->
                    val eligibleClasses = mutableListOf<String>()
                    if (!file.exists()) return@forEach
                    if (file.isFile) {
                        JarFile(file).use { jarFile: JarFile ->
                            jarFile.entries().iterator().forEach { jarEntry ->
                                // so far I do not handle inner types.
                                if (isEntryAnEligibleClass(jarEntry.name)) {
                                    val className = entryNameToClassName(jarEntry.name)
                                    eligibleClasses.add(className)
                                }
                            }
                        }
                    } else {
                        file.walk().forEach {
                            if (isEntryAnEligibleClass(it.name)) {
                                val className = entryNameToClassName(it.relativeTo(file).path)
                                eligibleClasses.add(className)
                            }
                        }
                    }
                    if (eligibleClasses.isNotEmpty()) {
                        modulesList.add(ModuleImport(file, eligibleClasses))
                    }
                }
        return modulesList
    }

    private fun isEntryAnEligibleClass(entryName: String) =
            entryName.endsWith(".class")
                    && !entryName.contains("R.class")
                    && !entryName.contains("module-info")
                    && !entryName.contains('$')

    private fun entryNameToClassName(entryName: String): String =
            entryName.substringBefore(".class").replace('/', '.')

}

/**
 * Return true if the annotated element is not deprecated
 */
fun KAnnotatedElement.isNotDeprecated() = !isDeprecated()

/**
 * Return true if the annotated element is deprecated.
 */
fun KAnnotatedElement.isDeprecated() =
        annotations.firstOrNull {
            it.annotationClass.simpleName == "Deprecated"
        } != null

/**
 * Find a suitable constructor to use for code generation on a [KClass].
 *
 * A constructor can be used when it is public and all of its parameters can in turn be instantiated with a suitable
 * constructor.
 *
 * @param visitedClasses list of classes we already visited when looking for a suitable constructors. There can be
 * cyclic references between types and their constructor parameters that makes it impossible for code generation to
 * properly issue code that will instantiate the type.
 */
fun <T: Any> KClass<T>.findSuitableConstructor(visitedClasses: MutableList<Class<*>>): KFunction<T>? {
    visitedClasses.add(this.java)
    return constructors.firstOrNull { constructor ->
        try {
            constructor.javaConstructor?.exceptionTypes?.isEmpty() ?: true
        } catch (e: Throwable) {
            true
        }
                && !constructor.isAbstract
                && constructor.isNotDeprecated()
                && constructor.visibility == KVisibility.PUBLIC
                && constructor.parameters.isEmpty()
    }
            ?: constructors.firstOrNull { constructor ->
        try {
            constructor.javaConstructor?.exceptionTypes?.isEmpty() ?: true
        } catch (e: Throwable) {
            true
        }
                && !constructor.isAbstract
                && constructor.isNotDeprecated()
                && constructor.visibility == KVisibility.PUBLIC
                && constructor.parameters.all { parameter ->
            // make sure the constructor does not take an instance of the type (usually called a Parent).
            !visitedClasses.contains<Class<*>>(parameter.type.jvmErasure.java)  && parameter.canBeInstantiated(visitedClasses)
        }
    }
}

/**
 * @return true the when a method parameter can be instantiated by generated code.
 */
fun KParameter.canBeInstantiated(visitedClasses: MutableList<Class<*>>) =
        !type.jvmErasure.isAbstract &&
            (findSuitableConstructor(visitedClasses) != null || type.jvmErasure.javaPrimitiveType != null)

/**
 * @return true when a method parameter has a constructor we can call through generated code.
 */
fun KParameter.findSuitableConstructor(visitedClasses: MutableList<Class<*>>) =
        type.jvmErasure.findSuitableConstructor(visitedClasses)

/**
 * @return true if the function is public in java and or kotlin sense.
 */
fun KFunction<*>.isPublic() =
        if (visibility == null)
            Modifier.isPublic(javaMethod?.modifiers ?: 0)
        else visibility == kotlin.reflect.KVisibility.PUBLIC

/**
 * @return true if all parameters of the passed function can be instantiated.
 */
fun KFunction<*>.allParametersCanBeInstantiated(visitedClasses: MutableList<Class<*>>) =
        parameters.filter {
            parameter -> parameter.kind == KParameter.Kind.VALUE
        }.all { parameter -> parameter.canBeInstantiated(visitedClasses) }