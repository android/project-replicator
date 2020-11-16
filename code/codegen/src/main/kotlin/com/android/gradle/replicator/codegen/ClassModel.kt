package com.android.gradle.replicator.codegen

import java.lang.RuntimeException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

abstract class AbstractTypeModel<T: Any> {
    abstract val type: KClass<out T>
    abstract val constructor: KFunction<T>
}

class PrimitiveModel<T: Any>(override val type: KClass<out T>): AbstractTypeModel<T>() {
    override val constructor: KFunction<T>
        get() {
            throw RuntimeException("No constructor for primitive types")
        }
}

class ClassModel<T: Any>(
        override val type: KClass<out T>,
        override val constructor: KFunction<T>,
        val callableMethods: Collection<KFunction<*>>): AbstractTypeModel<T>()

fun KClass<*>.toTypeModel(): AbstractTypeModel<*> {
        val constructor = findSuitableConstructor(mutableListOf())
        return if (constructor == null) PrimitiveModel(this)
        else ClassModel(this, constructor, listOf())
}

