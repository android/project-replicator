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
            version = "0.1"

            from(components["java"])
        }
    }
}

gradlePlugin {
    val agpFunctionalTest by plugins.creating {
        id = "com.android.gradle.replicator.codegen-plugin"
        version = 0.1
        implementationClass = "com.android.gradle.replicator.codegen.plugin.CodegenPlugin"
    }
}