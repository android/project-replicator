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
}