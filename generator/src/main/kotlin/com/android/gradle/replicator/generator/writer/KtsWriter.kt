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

import com.android.gradle.replicator.model.PluginType

class KtsWriter(
    private val newDsl: Boolean
): DslWriter(newDsl) {

    override val extension: String
        get() = "gradle.kts"

    override fun pluginInBlock(plugin: PluginType, version: String?, apply: Boolean) {
        writeIndent()
        if (plugin.kotlinId != null) {
            buffer.append("""kotlin("${plugin.kotlinId}")""")
        } else {
            buffer.append("""id("${plugin.id}")""")
        }
        version?.let {
            buffer.append(" version \"$version\"")
        }
        if (!apply) {
            buffer.append(" apply false")
        }
        buffer.append("\n")
    }

    override fun asString(value: String): String = """"$value""""

    override fun compileSdk(level: Int) {
        assign("compileSdk", level.toString())
    }

    override fun compileSdkPreview(level: String) {
        assign("compileSdkPreview", """"$level"""")
    }

    override fun url(uri: String) {
        writeIndent()
        buffer.append("url = uri(\"$uri\")\n")
    }

    override fun doMethodCall(methodName: String, withBlock: Boolean, vararg values: Any) {
        buffer.append("$methodName(")
        values.joinTo(buffer)
        buffer.append(")")
    }
}