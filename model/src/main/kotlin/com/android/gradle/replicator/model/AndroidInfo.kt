/*
 * Copyright (C) 2018 The Android Open Source Project
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

/**
 * Information specific to Android modules.
 */
interface AndroidInfo {
    val compileSdkVersion: String
    val minSdkVersion: Int
    val targetSdkVersion: Int
    val buildFeatures: BuildFeaturesInfo

    // TODO add support for build types, flavors, dynamic-features

    // TODO implement the items below if useful
    //    val activityCount: Int
    //    val hasLaunchActivity: Boolean
    //    val resources: ResourceInfo

}

interface BuildFeaturesInfo {
    val aidl: Boolean?
    val buildConfig: Boolean?
    val androidResources: Boolean?
    val compose: Boolean?
    val dataBinding: Boolean?
    val mlModelBinding: Boolean?
    val prefab: Boolean?
    val prefabPublishing: Boolean?
    val renderScript: Boolean?
    val resValues: Boolean?
    val shaders: Boolean?
    val viewBinding: Boolean?
}

/**
 * Information about the amount of resources in the module
 */
/*
interface ResourceInfo {
    val stringCount: Int
    val imageCount: Int
    val layoutCount: Int
}
*/
