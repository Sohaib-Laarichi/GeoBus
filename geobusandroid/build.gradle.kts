// Top-level build file
val kotlinVersion = "1.7.10"
val navVersion = "2.5.3"

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")
    }
}

// Rendre les versions disponibles pour tous les modules
extra["kotlin_version"] = kotlinVersion
extra["nav_version"] = navVersion
extra["maps_version"] = "18.1.0"

// Tâche clean
tasks.register("clean") {
    doLast {
        delete(rootProject.buildDir)
    }
}
