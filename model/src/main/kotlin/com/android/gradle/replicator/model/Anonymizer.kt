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

package com.android.gradle.replicator.model

import com.android.gradle.replicator.model.internal.DefaultDependenciesInfo
import com.android.gradle.replicator.model.internal.DefaultModuleInfo
import com.android.gradle.replicator.model.internal.DefaultProjectInfo

data class AnonymizedProjectInfo(
    val projectInfo: DefaultProjectInfo,
    val mapping: Map<String, String>
)

fun ProjectInfo.toAnonymized(): AnonymizedProjectInfo {
    val rootNode = PathNode(":").also {
        it.setModuleInfo(rootModule)
    }

    for (module in subModules) {
        val node = rootNode.findNode(module.path)
        node.setModuleInfo(module)
    }

    val mapping = mutableMapOf<String, String>()
    rootNode.anonymize(mapping)

    val newModules = mutableListOf<ModuleInfo>()
    val newRootModule = rootNode.toNewModules(newModules, mapping)

    return AnonymizedProjectInfo(
        DefaultProjectInfo(
            gradleVersion = this.gradleVersion,
            agpVersion = this.agpVersion,
            kotlinVersion = this.kotlinVersion,
            rootModule = newRootModule,
            subModules = newModules,
            gradleProperties = gradleProperties
        ),
        mapping
    )
}

/**
 * Represents a gradle path project, e.g. :foo:bar.
 *
 * This contains the oldname "bar", and the new anonymized name.
 * This also contains the ModuleInfo for :foo:bar, and the list of children
 * sub-project (:foo:bar:p1, :foo:bar:p2) etc...
 */

private class PathNode(
        /**
         * The  leaf name pre-anonymizatio
         */
        val oldName: String
) {
    /**
     * The full path pre-anonymizatio
     */
    lateinit var oldPath: String
    lateinit var newPath: String
    lateinit var newName: String
    private lateinit var module: ModuleInfo

    fun setModuleInfo(module: ModuleInfo) {
        if (this::module.isInitialized) {
            throw RuntimeException("module for '$oldName' already set")
        }
        this.module = module
        this.oldPath = module.path
    }

    val subNodes = mutableListOf<PathNode>()

    /**
     * for a given project path and a current node, returns a
     * [PathNode] that represents this path, creating it and the
     * intermediates nodes along the way
     */
    internal fun findNode(projectPath: String): PathNode {
        val list = projectPath.split(":")
        // list will contain an empty segment due to the first ":"
        val segments = list.subList(1, list.size)

        return findNode(segments, 0)
    }

    /**
     * for a given project path and a current node, returns a
     * [PathNode] that represents this path, creating it and the
     * intermediates nodes along the way
     */
    private fun findNode(
            segments: List<String>,
            index: Int
    ): PathNode {
        val value = segments[index]

        // get the matching node or create/add it otherwise
        val node = subNodes.singleOrNull { it.oldName == value } ?:
        PathNode(value).also { subNodes.add(it) }

        if (index == segments.size - 1) {
            return node
        }

        return node.findNode(segments, index + 1)
    }

    internal fun anonymize(mapping: MutableMap<String, String>) {
        // root module does not need anonymization
        newName = ":"
        newPath = ":"

        // handle sub-nodes
        anonymizeSubNodes(PathBuilder(), mapping)
    }

    private fun anonymizeSubNodes(
        pathBuilder: PathBuilder,
        mapping: MutableMap<String, String>
    ) {
        subNodes.sortBy { it.oldName }

        val count = subNodes.size
        val digitCount = count.toString().length
        val formatter = "%0${digitCount}d"

        for (index in 1..count) {
            val node = subNodes[index - 1]
            node.newName = "module${formatter.format(index)}"

            pathBuilder.use {
                pathBuilder.appendSegment(node.newName)
                node.newPath = pathBuilder.toString()
                mapping[node.oldPath] = node.newPath
                node.anonymizeSubNodes(pathBuilder, mapping)
            }
        }
    }

    internal fun toNewModules(modules: MutableList<ModuleInfo>, mapping: MutableMap<String, String>): ModuleInfo {
        // convert current Node
        val dependencies = module.dependencies
        val newModuleInfo = DefaultModuleInfo(
            path = newPath,
            plugins = module.plugins,
            javaSources = module.javaSources,
            kotlinSources = module.kotlinSources,
            dependencies = dependencies.map {
                if (it.type == DependencyType.EXTERNAL_LIBRARY) {
                    it
                } else {
                    DefaultDependenciesInfo(
                        type = it.type,
                        dependency = mapping[it.dependency]
                                ?: throw RuntimeException("could not find anonymized module name for ${it.dependency}"),
                        scope = it.scope
                    )
                }
            },
            android = module.android
        )

        // convert the sub-nodes and add them to the list
        for (subNode in subNodes) {
            modules.add(subNode.toNewModules(modules, mapping))
        }

        // return the current node, without adding it (it'll be handled by the caller)
        return newModuleInfo
    }
}

/**
 * a PathBuilder that allows one to build a path and backtrack to the previous
 * segment
 */
private class PathBuilder {
    private val buffer = StringBuilder()

    fun use(action: PathBuilder.() -> Unit) {
        val len = buffer.length
        action(this)
        buffer.setLength(len)
    }

    fun appendSegment(name: String) {
        buffer.append(':').append(name)
    }

    override fun toString(): String {
        return buffer.toString()
    }
}