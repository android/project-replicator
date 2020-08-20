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

package com.android.gradle.replicator.collectors

import com.android.build.api.dsl.ApplicationBuildFeatures
import com.android.build.api.dsl.DynamicFeatureBuildFeatures
import com.android.build.api.dsl.LibraryBuildFeatures
import com.android.build.api.dsl.TestBuildFeatures
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.plugins.BasePlugin
import com.android.gradle.replicator.model.internal.DefaultAndroidInfo
import com.android.gradle.replicator.model.internal.DefaultBuildFeaturesInfo
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import javax.inject.Inject

/**
 * A collector that can look at a project and collect Android information.
 *
 * In the future there may be more than one depending on AGP APIs
 */
interface AndroidCollector {
    fun collectInfo(project: Project) : AndroidInfoInputs?
}

/**
 * Basic implementation of collector to start with.
 */
class DefaultAndroidCollector : AndroidCollector {
    private lateinit var project: Project
    override fun collectInfo(project: Project): AndroidInfoInputs? {
        // load the BasePlugin by reflection in case it does not exist so that we dont fail with ClassNotFoundException
        return project.plugins.withType(BasePlugin::class.java).firstOrNull()?.let {
            this.project = project

            // if we are here there is indeed an Android plugin applied
            val baseExtension = project.extensions.getByName("android") as BaseExtension

            val buildFeatures = getBuildFeatures(baseExtension)

            val inputs = project.objects.newInstance(
                AndroidInfoInputs::class.java,
                baseExtension.compileSdkVersion ?: "",
                baseExtension.defaultConfig.minSdkVersion?.apiLevel ?: 0, //FIXME
                baseExtension.defaultConfig.targetSdkVersion?.apiLevel ?: 0, //FIXME
                buildFeatures
            )

            // FIXME when we get to provide flavor/build type info, then we can augment this.
            val dirs = baseExtension.sourceSets.findByName("main")?.java?.srcDirs
            dirs?.let {
                inputs.javaFolders.from(dirs)
            }

            inputs
        }
    }

    @Suppress("UnstableApiUsage")
    private fun getBuildFeatures(extension: BaseExtension): BuildFeaturesInput =
            project.objects.newInstance(BuildFeaturesInput::class.java).also { buildFeaturesInput ->
                try {
                    val buildFeatures = extension.buildFeatures

                    buildFeaturesInput.aidl.lenientSet { buildFeatures.aidl }
                    buildFeaturesInput.buildConfig.lenientSet { buildFeatures.buildConfig }
                    buildFeaturesInput.compose.lenientSet { buildFeatures.compose }

                    buildFeaturesInput.prefab.lenientSet { buildFeatures.prefab }
                    buildFeaturesInput.renderScript.lenientSet { buildFeatures.renderScript }
                    buildFeaturesInput.resValues.lenientSet { buildFeatures.resValues }
                    buildFeaturesInput.shaders.lenientSet { buildFeatures.shaders }
                    buildFeaturesInput.viewBinding.lenientSet { buildFeatures.viewBinding }

                    when (buildFeatures) {
                        is ApplicationBuildFeatures -> {
                            buildFeaturesInput.dataBinding.lenientSet { buildFeatures.dataBinding }
                            buildFeaturesInput.mlModelBinding.lenientSet { buildFeatures.mlModelBinding }
                        }
                        is LibraryBuildFeatures -> {
                            buildFeaturesInput.androidResources.lenientSet { buildFeatures.androidResources }
                            buildFeaturesInput.dataBinding.lenientSet { buildFeatures.dataBinding }
                            buildFeaturesInput.mlModelBinding.lenientSet { buildFeatures.mlModelBinding }
                            buildFeaturesInput.prefabPublishing.lenientSet { buildFeatures.prefabPublishing }
                        }
                        is DynamicFeatureBuildFeatures -> {
                            buildFeaturesInput.dataBinding.lenientSet { buildFeatures.dataBinding }
                            buildFeaturesInput.mlModelBinding.lenientSet { buildFeatures.mlModelBinding }
                        }
                        is TestBuildFeatures -> {
                        }
                    }

                } catch (e: Throwable) {
                    // older AGP don't have buildFeatures
                }
            }
}

private fun <T> Property<T>.lenientSet(access: () -> T?) = try {
    set(access())
} catch (e: Throwable) {
    // do nothing
}


abstract class AndroidInfoInputs @Inject constructor(
    @get:Input
    val compileSdkVersion: String,
    @get:Input
    val minSdkVersion: Int,
    @get:Input
    val targetSdkVersion: Int,
    @get:Nested
    val buildFeatures: BuildFeaturesInput
) {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val javaFolders: ConfigurableFileCollection

    fun toInfo(): DefaultAndroidInfo {
        return DefaultAndroidInfo(compileSdkVersion, minSdkVersion, targetSdkVersion, buildFeatures.toInfo())
    }
}

abstract class BuildFeaturesInput {

    @get:Input
    @get:Optional
    abstract val aidl: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val androidResources: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val buildConfig: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val compose: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val dataBinding: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val mlModelBinding: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val prefab: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val prefabPublishing: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val renderScript: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val resValues: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val shaders: Property<Boolean>
    @get:Input
    @get:Optional
    abstract val viewBinding: Property<Boolean>

    fun toInfo(): DefaultBuildFeaturesInfo =
            DefaultBuildFeaturesInfo(
                aidl.orNull,
                androidResources.orNull,
                buildConfig.orNull,
                compose.orNull,
                dataBinding.orNull,
                mlModelBinding.orNull,
                prefab.orNull,
                prefabPublishing.orNull,
                renderScript.orNull,
                resValues.orNull,
                shaders.orNull,
                viewBinding.orNull
            )
}

