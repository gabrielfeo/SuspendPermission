plugins {
    id("com.android.library")
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
    google()
    mavenCentral()
}

dependencies {
    implementation(Kotlin.StdLib)
    api(Kotlin.Coroutines.Android)

    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.fragment:fragment-ktx:1.2.4")
    implementation("androidx.appcompat:appcompat:1.1.0")

    testImplementation(Kotlin.Testing.Jvm)
    testImplementation(Kotlin.Testing.JUnit5)
    testImplementation(Kotlin.Coroutines.Testing)
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.mockito:mockito-core:3.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}