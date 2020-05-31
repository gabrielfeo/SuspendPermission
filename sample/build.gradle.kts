plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

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
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.gabrielfeo:suspendpermission:1.0.0")
    implementation(Kotlin.StdLib)
    with(AndroidX) {
        implementation(Core)
        implementation(AppCompat)
        implementation(Fragment)
        with(AndroidX.Lifecycle) {
            implementation(Runtime)
            kapt(Compiler)
        }
    }
}