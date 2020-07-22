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

package com.android.gradle.replicator.model.internal.fixtures

import com.android.gradle.replicator.model.BuildFeaturesInfo
import com.android.gradle.replicator.model.internal.DefaultBuildFeaturesInfo

class BuildFeaturesBuilder {
    var aidl: Boolean? = null
    var androidResources: Boolean? = null
    var buildConfig: Boolean? = null
    var compose: Boolean? = null
    var dataBinding: Boolean? = null
    var mlModelBinding: Boolean? = null
    var prefab: Boolean? = null
    var prefabPublishing: Boolean? = null
    var renderScript: Boolean? = null
    var resValues: Boolean? = null
    var shaders: Boolean? = null
    var viewBinding: Boolean? = null

    fun toInfo(): BuildFeaturesInfo = DefaultBuildFeaturesInfo(
        aidl,
        androidResources,
        buildConfig,
        compose,
        dataBinding,
        mlModelBinding,
        prefab,
        prefabPublishing,
        renderScript,
        resValues,
        shaders,
        viewBinding
    )
}