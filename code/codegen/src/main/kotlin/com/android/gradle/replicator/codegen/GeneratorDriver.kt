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
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

class GeneratorDriver(
        private val parameters: GenerationParameters,
        private val random: Random,
        private val generatorAllocator: (printer: PrettyPrintStream, listeners: List<CodeGenerationListener>) -> ClassGenerator
): SourceGenerator {

    data class ModuleImport(
            val origin: File,
            val classes: List<String>
    )

    private val modules: List<ModuleImport> by lazy {
        val modulesList = mutableListOf<ModuleImport>()
        parameters.classpath.forEach {file ->
            val eligibleClasses = mutableListOf<String>()
            JarFile(file).use { jarFile: JarFile ->
                jarFile.entries().iterator().forEach { jarEntry ->
                    // so far I do not handle inner types.
                    if (isEntryAnEligibleClass(jarEntry.name)) {
                        val className = entryNameToClassName(jarEntry.name)
                        eligibleClasses.add(className)
                    }
                }
            }
            if (eligibleClasses.isNotEmpty()) {
                modulesList.add(ModuleImport(file, eligibleClasses))
            }
        }
        modulesList
    }

    private val classLoader: URLClassLoader by lazy {
        URLClassLoader(
                parameters.classpath.map { it.toURI().toURL() }.toTypedArray(),
                this.javaClass.classLoader
        )
    }

    private val eligibleClasses: List<KClass<*>> by lazy {
        val listOfClasses = mutableListOf<KClass<*>>()
        modules.forEach { moduleImport ->
            moduleImport.classes.forEach {
                val loadedClass = classLoader.loadClass(it)
                if (!loadedClass.isInterface
                        && !loadedClass.isAnnotation
                        && Modifier.isPublic(loadedClass.modifiers)) {
                    val selectedType = loadedClass.kotlin
                    if (!selectedType.isAbstract && selectedType.isNotDeprecated()) {
                        val selectedConstructor = selectedType.findSuitableConstructor()
                        if (selectedConstructor != null)
                            listOfClasses.add(selectedType)
                    }
                }
            }
        }
        println("I have ${listOfClasses.size} classes to work with")
        listOfClasses
    }

    override fun generateClass(
            packageName: String,
            className: String,
            printStream: PrettyPrintStream,
            listeners: List<CodeGenerationListener>) {
        SingleClassGenerator(
                generator = generatorAllocator(printStream, listeners),
                packageName = packageName,
                className = className,
                eligibleClasses = eligibleClasses,
                random = random,
                params = parameters.classGenerationParameters
        ).generate()
    }

    private fun isEntryAnEligibleClass(entryName: String) =
            entryName.endsWith(".class") && !entryName.contains('$')

    private fun entryNameToClassName(entryName: String): String =
            entryName.substringBefore(".class").replace('/', '.')

}

fun KAnnotatedElement.isNotDeprecated() = !isDeprecated()

fun KAnnotatedElement.isDeprecated() =
        annotations.firstOrNull {
            it.annotationClass.simpleName == "Deprecated"
        } != null

fun <T: Any> KClass<T>.findSuitableConstructor(): KFunction<T>? {
    return constructors.firstOrNull { constructor ->
        !constructor.isAbstract
                && constructor.isNotDeprecated()
                && constructor.visibility == KVisibility.PUBLIC
                && constructor.parameters.all { parameter ->
            // make sure the constructor does not take an instance of the type (usually called a Parent).
            parameter.type.jvmErasure.qualifiedName != this.qualifiedName && parameter.canBeInstantiated() }
    }
}

fun KParameter.canBeInstantiated() =
        !type.jvmErasure.isAbstract &&
                (findSuitableConstructor() != null || type.jvmErasure.javaPrimitiveType != null)

fun KParameter.findSuitableConstructor()= type.jvmErasure.findSuitableConstructor()

fun KFunction<*>.isPublic() =
        if (visibility == null)
            java.lang.reflect.Modifier.isPublic(javaMethod?.modifiers ?: 0)
        else visibility == kotlin.reflect.KVisibility.PUBLIC

fun KFunction<*>.allParametersCanBeInstantiated() =
        parameters.filter {
            parameter -> parameter.kind == KParameter.Kind.VALUE
        }.all { parameter -> parameter.canBeInstantiated() }


