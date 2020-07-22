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

import com.android.gradle.replicator.model.AndroidInfo
import com.android.gradle.replicator.model.internal.DefaultAndroidInfo

class AndroidBuilder {
    var compileSdkVersion: String = ""
    var minSdkVersion: Int = 0
    var targetSdkVersion: Int = 0
    var buildFeatures: BuildFeaturesBuilder = BuildFeaturesBuilder()

    fun buildFeatures(action: BuildFeaturesBuilder.() -> Unit) {
        action(buildFeatures)
    }

    fun toInfo(): AndroidInfo = DefaultAndroidInfo(compileSdkVersion, minSdkVersion, targetSdkVersion, buildFeatures.toInfo())
}