plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
}

repositories {
    gradlePluginPortal()
    jcenter()
}

gradlePlugin {
    val extractor by plugins.creating {
        id = "agp-function-tests"
        implementationClass = "AgpFunctionalTestPlugin"
    }
}
