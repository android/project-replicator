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
plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    gradlePluginPortal()
    jcenter()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
    implementation(project(":code:codegen"))
}

// publish to maven local, it is where the init script is expecting it.
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.android.gradle.replicator"
            artifactId = "codegen-plugin"
            version = Versions.pluginVersion

            from(components["java"])
        }
    }
}

gradlePlugin {
    val agpFunctionalTest by plugins.creating {
        id = "com.android.gradle.replicator.codegen-plugin"
        version = Versions.pluginVersion
        implementationClass = "com.android.gradle.replicator.codegen.plugin.CodegenPlugin"
    }

    val javaLibraryCodegen by plugins.creating {
        id = "com.android.gradle.replicator.java-library-codegen-plugin"
        version = 0.1
        implementationClass = "com.android.gradle.replicator.codegen.plugin.JavaLibraryCodegenPlugin"
    }
}
// easier to run task for local publishing
val publishLocal by tasks.registering {
    dependsOn("publishMavenPublicationToMavenLocal")
}