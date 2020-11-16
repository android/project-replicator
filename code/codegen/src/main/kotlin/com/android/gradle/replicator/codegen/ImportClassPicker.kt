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
package com.android.gradle.replicator.codegen

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.jvmErasure

/**
 * Utility class to randomly pick a class from a list of imported modules and a classloader.
 *
 * The class picker will use and environment which is defined by a class loader capable of loading classes and
 * a list of modules each with a list of class names that can be loaded by the previously mentioned class loader.
 *
 * The picked class is constrained by a few factors to be able to be used in code generation. In particular, the class
 * must have the following attributes :
 * <ul>
 *     <li> must be public
 *     <li> must be a class, non abstract
 *     <li> must have a valid constructor that takes arguments that are themselves following the same constraints.
 *     <li> must not be deprecated.
 *     <li> must have at least one function that can be called from generated code (with parameters all following the
 *     same constraints).
 * </ul>
 */
open class ImportClassPicker(
        private val classLoader: ClassLoader,
        private val modules: List<GeneratorDriver.ModuleImport>,
        private val verifyClasses: Boolean = true) {

    /**
     * set to true if we cannot pick any class in this environment, to avoid rescanning needlessly
     */
    private val emptyPicker = AtomicBoolean(false)

    /**
     * pick a class using the [random] randomizer.
     *
     * @param random the randomizer to use to pick up the class.
     * @return a picked class that can be used to generate code with or null if none can be found.
     */
    open fun pickClass(random: Random): ClassModel<*>? {
        if (emptyPicker.get() || modules.isEmpty()) return null
        var moduleIndex = random.nextInt(modules.size)
        val startModuleIndex = moduleIndex
        var selectedModule = modules[moduleIndex]
        var classIndex = random.nextInt(selectedModule.classes.size)
        var startClassIndexInModule = classIndex
        var className = selectedModule.classes[classIndex]
        var loadedClass= loadClass(className)
        var classModel = if (loadedClass != null) {
            if (verifyClasses) isClassEligible(loadedClass) else loadModel(loadedClass)
        } else null
        while (classModel == null) {
            classIndex = (classIndex + 1) % selectedModule.classes.size
            if (classIndex == startClassIndexInModule) {
                moduleIndex = (moduleIndex + 1) % modules.size
                if (moduleIndex == startModuleIndex) {
                    emptyPicker.set(true)
                    return null
                }
                selectedModule = modules[moduleIndex]
                classIndex = random.nextInt(selectedModule.classes.size)
                startClassIndexInModule = classIndex
            }
            className = selectedModule.classes[classIndex]
            loadedClass= loadClass(className)
            if (loadedClass != null) {
                classModel = isClassEligible(loadedClass)
            }
        }
        return classModel
    }

    /**
     * Loads a class from the classloader.
     *
     * @param name the class name to load.
     * @return the loaded class or null if the class cannot be loaded.
     */
    private fun loadClass(name: String): Class<*>? = try {
        classLoader.loadClass(name)
        // some imported modules are not packaged correctly and have references to classes not present.
        // this is usually not a problem as those references are probably dead code but the randomizer can pick them
        // nonetheless so we should handle these class loading failures graciously.
    } catch (e: ClassNotFoundException) {
        null
    } catch (e: NoClassDefFoundError) {
        null
    }

    /**
     * creates a [ClassModel] without verify if the classes are suitable for use in code generation.
     */
    private fun loadModel(kClass: Class<*>): ClassModel<*>? {
        val selectedType = kClass.kotlin
        try {
            return ClassModel<Any>(selectedType,
                    selectedType.constructors.first(),
                    selectedType.declaredFunctions)
        } catch (e: Exception) {
            println("Caught !")
            return null
        } catch (e: Error) {
            println("Caught !")
            return null
        }
    }
    /**
     * Returns true if the class is eligible to used for code generation. It must follows all the constraints
     * described in the class comments.
     */
    private fun isClassEligible(kClass: Class<*>?): ClassModel<*>? {
        try {
            // if the class loader that actually loaded the class is a parent class loader like the boot
            // classpath, do not use the class as it is probably a base JDK class version rather than
            // android specific one.
            if (kClass?.classLoader != classLoader) return null
            // avoid picking up kotlin.* as it is problematic when using java code generation.
            if (kClass.packageName.startsWith("kotlin")) return null
            if (!kClass.isInterface
                    && !kClass.isAnnotation) {
                val selectedType = kClass.kotlin
                val declaredFunctions = selectedType.declaredFunctions
                if (!selectedType.isAbstract
                        && isClassHierarchySafe(selectedType.java)
                        && selectedType.visibility == KVisibility.PUBLIC
                        && declaredFunctions.isNotEmpty()
                        && selectedType.isNotDeprecated()) {
                    val constructor = selectedType.findSuitableConstructor(mutableListOf())
                            ?: return null
                    val suitableMethodsToCall = findSuitableMethodsToCall(declaredFunctions)
                    if (suitableMethodsToCall.isEmpty()) return null
                    return ClassModel<Any>(selectedType, constructor, suitableMethodsToCall)
                }
            }
            return null
        // A number of exceptions can be thrown by kotlin reflection.
        // Some of these exceptions are warranted like ClassNotFoundException which is due to faulty packaging
        // of dependencies. However, some like the java.lang.reflect.* ones are linked to features not implemented
        // in the kotlin reflection part or bugs in its implementation.
        } catch (e: Exception) {
            return null
        } catch (e: Error) {
            return null
        }
    }

    private fun isClassHierarchySafe(type: Class<*>): Boolean {
        if (!type.interfaces.all { intf -> isClassHierarchySafe(intf)}) return false
        return isTypeSafe(type) && (type.superclass == null || isClassHierarchySafe(type.superclass))
    }

    private fun findSuitableMethodsToCall(methods: Collection<KFunction<*>>): List<KFunction<*>> =
            methods
                .filter {
                    it.isNotDeprecated()
                            && it.isPublic()
                            && it.allParametersCanBeInstantiated(mutableListOf<Class<*>>())
                            && it.parameters.all { parameter -> isTypeSafe(parameter.type.jvmErasure.java)}
                            && isTypeSafe(it.returnType.jvmErasure.java)
                            && (it.parameters.any { parameter -> parameter.kind == KParameter.Kind.INSTANCE })
                }

    private fun isTypeSafe(type: Class<*>): Boolean =
        type.classLoader == classLoader
                || type.isPrimitive
                || type.packageName == "java.lang"
                || type.packageName == "java.util"
}