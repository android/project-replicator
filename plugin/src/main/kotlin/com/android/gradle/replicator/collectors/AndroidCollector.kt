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

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.plugins.BasePlugin
import com.android.gradle.replicator.model.internal.DefaultAndroidInfo
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
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
    override fun collectInfo(project: Project): AndroidInfoInputs? {
        // load the BasePlugin by reflection in case it does not exist so that we dont fail with ClassNotFoundException
        return project.plugins.withType(BasePlugin::class.java).firstOrNull()?.let {
            // if we are here there is indeed an Android plugin applied

            val baseExtension = project.extensions.getByName("android") as BaseExtension

            val inputs = project.objects.newInstance(
                AndroidInfoInputs::class.java,
                baseExtension.compileSdkVersion ?: "",
                baseExtension.defaultConfig.minSdkVersion?.apiLevel ?: 0, //FIXME
                baseExtension.defaultConfig.targetSdkVersion?.apiLevel ?: 0 //FIXME
            )

            // FIXME when we get to provide flavor/build type info, then we can augment this.
            val dirs = baseExtension.sourceSets.findByName("main")?.java?.srcDirs
            dirs?.let {
                inputs.javaFolders.from(dirs)
            }

            inputs
        }
    }
}

abstract class AndroidInfoInputs @Inject constructor(
    @get:Input
    val compileSdkVersion: String,
    @get:Input
    val minSdkVersion: Int,
    @get:Input
    val targetSdkVersion: Int
) {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val javaFolders: ConfigurableFileCollection

    fun toInfo(): DefaultAndroidInfo {
        return DefaultAndroidInfo(compileSdkVersion, minSdkVersion, targetSdkVersion)
    }
}