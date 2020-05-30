plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.github.dcendents.android-maven")
}

group = "com.gabrielfeo"

android {

    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }

}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(Kotlin.StdLib)

    api(Kotlin.Coroutines.Android)
    with(AndroidX) {
        api(Core)
        api(AppCompat)
        api(Fragment)
    }

    testImplementation(Kotlin.Testing.Jvm)
    testImplementation(Kotlin.Testing.JUnit5)
    testImplementation(Kotlin.Coroutines.Testing)
    testImplementation(JUnit.Jupiter)
}

tasks.withType<Test> {
    useJUnitPlatform()
}