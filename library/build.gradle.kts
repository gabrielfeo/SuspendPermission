plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {

    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(23)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(kotlin("coroutines-android"))

    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.fragment:fragment-ktx:1.2.4")
    implementation("androidx.appcompat:appcompat:1.1.0")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("coroutines-test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.mockito:mockito-core:3.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}