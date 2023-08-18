plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("com.gradle.plugin-publish") version "1.2.0"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

group = "dev.smithers"
version = "0.0.2"

gradlePlugin {
    plugins {
        create("the plugin 🥇🐘⭐") {
            id = "dev.smithers.envrc"
            displayName = "gradle-envrc"
            description = "Read environment variables directly from .envrc files"
            implementationClass = "dev.smithers.GradleEnvrcPlugin"
            tags.set(listOf("envrc", "direnv", "environment variables"))
        }
    }
    website = "https://github.com/jonsmithers/gradle-envrc"
    vcsUrl = "https://github.com/jonsmithers/gradle-envrc"
}
