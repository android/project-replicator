plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
}

repositories {
    gradlePluginPortal()
    jcenter()
}

dependencies {
  compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
}

gradlePlugin {
    val agpFunctionalTest by plugins.creating {
        id = "agp-function-tests"
        implementationClass = "AgpFunctionalTestPlugin"
    }

    val defaultConfig by plugins.creating {
        id = "default-config"
        implementationClass = "DefaultConfigPlugin"
    }
}
