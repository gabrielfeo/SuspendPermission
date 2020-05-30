group = "com.gabrielfeo.suspend.permission"
version = "0.0.1"

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.5.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.Version}")
    }
}

subprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}