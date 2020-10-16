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

package com.android.gradle.replicator.generator.writer

import java.io.File

abstract class DslWriter(
    private val newDsl: Boolean
) {

    companion object {
        private const val INDENT: String = "  "
    }

    private var file: File? = null
    private var indent: Int = 0
    protected val buffer = StringBuffer(50)

    fun newBuildFile(folder: File) {
        flush()
        indent = 0
        file = File(folder, "build.$extension")
    }

    fun newSettingsFile(folder: File) {
        flush()
        indent = 0
        file = File(folder, "settings.$extension")
    }

    abstract fun pluginInBlock(pluginId: String, version: String? = null, apply: Boolean = true)

    fun block(name: String, action: DslWriter.() -> Unit) {
        writeIndent()
        buffer.append("$name {\n")
        indent++
        action(this)
        indent--
        writeIndent()
        buffer.append("}\n")
    }

    abstract fun asString(value: String): String

    fun assign(propertyName: String, value: Any) {
        writeIndent()
        buffer.append("$propertyName = ${value}\n")
    }

    abstract fun compileSdk(level: Int)

    abstract fun compileSdkPreview(level: String)

    fun call(methodName: String, vararg values: Any) {
        writeIndent()
        doMethodCall(methodName = methodName, withBlock = false, values = *values)
        buffer.append("\n")
    }

    fun callWithBlock(methodName: String, vararg values: Any, action: DslWriter.() -> Unit) {
        writeIndent()
        doMethodCall(methodName = methodName, withBlock = true, values = *values)
        buffer.append(" {\n")
        indent++
        action(this)
        indent--
        writeIndent()
        buffer.append("}\n")
    }

    fun google() {
        writeIndent()
        buffer.append("google()\n")
    }

    fun jcenter() {
        writeIndent()
        buffer.append("jcenter()\n")
    }

    abstract fun url(uri: String)

    fun flush() {
        file?.let {
            it.appendText(buffer.toString())
            buffer.setLength(0)
        }
    }

    protected abstract val extension: String

    protected abstract fun doMethodCall(methodName: String, withBlock: Boolean, vararg values: Any)

    protected fun writeIndent() {
        for (i in 1..indent) {
            buffer.append(INDENT)
        }
    }
}