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

enum class PluginType(
    val id: String,
    val oldId: String = id,
    val kotlinId: String? = null,
    val isAndroid: Boolean = false,
    val isKotlin: Boolean = false,
    val isJava: Boolean = false,
    val useNewDsl: Boolean = true,
    val priority: Int = 0
) {
    JAVA_LIBRARY(
        id = "java-library",
        isJava = true
    ),
    JAVA(
        id = "java",
        isJava = true
    ),
    APPLICATION(
        id = "application",
        isJava = true
    ),
    KOTLIN_JVM(
        id = "org.jetbrains.kotlin.jvm",
        oldId = "kotlin",
        kotlinId = "jvm",
        isKotlin = true
    ),
    KOTLIN_ANDROID(
        id = "kotlin-android",
        isAndroid = true,
        isKotlin = true,
        useNewDsl = false,
        priority = 10
    ),
    KAPT(
        id = "kotlin-kapt",
        isKotlin = true,
        useNewDsl = false,
        priority = 11
    ),
    ANDROID_APP(
        id = "com.android.application",
        isAndroid = true,
        useNewDsl = false
    ),
    ANDROID_LIB(
        id = "com.android.library",
        isAndroid = true,
        useNewDsl = false
    ),
    ANDROID_TEST(
        id = "com.android.test",
        isAndroid = true,
        useNewDsl = false
    ),
    ANDROID_DYNAMIC_FEATURE(
        id = "com.android.dynamic-feature",
        isAndroid = true,
        useNewDsl = false
    ),
}
