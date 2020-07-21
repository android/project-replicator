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

package com.android.gradle.replicator.model

import com.android.gradle.replicator.model.internal.DefaultDependenciesInfo
import com.android.gradle.replicator.model.internal.fixtures.project
import com.google.common.truth.Truth
import org.junit.Test

class ProjectInfoAnonymizerTest {

    @Test
    fun testSingleProject() {
        val project = project {
            subModule {
                path = ":foo"
            }
        }

        val anonymizedInfo = project.toAnonymized()
        val anonymizedProject = anonymizedInfo.projectInfo
        val mapping = anonymizedInfo.mapping

        Truth.assertThat(anonymizedProject.rootModule.path).isEqualTo(":")
        Truth.assertThat(anonymizedProject.subModules[0].path).isEqualTo(":module1")
        Truth.assertThat(mapping).containsExactly(":foo", ":module1")
    }

    @Test
    fun `test multi projects`() {
        val project = project {
            subModule {
                path = ":foo"
            }
            subModule {
                path = ":libs:bar"
            }
            subModule {
                path = ":libs"
            }
            subModule {
                path = ":libs:baz"
            }
            subModule {
                path = ":another_lib"
            }
            subModule {
                path = ":another_lib:baz"
            }
        }

        Truth.assertThat(project.toAnonymized().mapping).containsExactly(
            ":another_lib", ":module1",
            ":another_lib:baz", ":module1:module1",
            ":foo", ":module2",
            ":libs", ":module3",
            ":libs:bar", ":module3:module1",
            ":libs:baz", ":module3:module2"
        )
    }

    @Test
    fun `test dependency anonymization`() {
        // use this as a marker to always identify the right module.
        val pluginMarker = listOf(PluginType.ANDROID_APP)

        val project = project {
            subModule {
                path = ":foo"
            }
            subModule {
                path = ":bar"
                plugins = pluginMarker
                dependencies = listOf(
                    DefaultDependenciesInfo(
                        type = DependencyType.MODULE,
                        dependency = ":foo",
                        scope = "api"
                    )
                )
            }
        }

        val anonymizedProject = project.toAnonymized().projectInfo

        val barProject = anonymizedProject.subModules.singleOrNull { it.plugins === pluginMarker }
        Truth.assertWithMessage("bar project").that(barProject).isNotNull()
        barProject!!

        Truth.assertWithMessage("bar dependency on foo").that(barProject.dependencies).containsExactly(
            DefaultDependenciesInfo(
                type = DependencyType.MODULE,
                dependency = ":module2",
                scope = "api"
            )
        )
    }
}