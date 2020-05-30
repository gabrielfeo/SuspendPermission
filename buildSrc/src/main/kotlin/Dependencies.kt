object Kotlin {
    const val Version = "1.3.72"
    private fun module(name: String) = "org.jetbrains.kotlin:kotlin-$name:$Version"

    val StdLib = module("stdlib-jdk8")

    object Testing {
        val Jvm = module("test")
        val JUnit5 = module("test-junit5")
    }

    object Coroutines {
        const val Version = "1.3.7"
        private fun module(name: String) = "org.jetbrains.kotlinx:kotlinx-$name:$Version"

        val Android = module("coroutines-android")
        val Testing = module("coroutines-test")
    }
}

object AndroidX {
    const val Core = "androidx.core:core-ktx:1.3.0"
    const val Fragment = "androidx.fragment:fragment-ktx:1.2.4"
    const val AppCompat = "androidx.appcompat:appcompat:1.1.0"

    object Lifecycle {
        const val Version = "2.2.0"
        const val Runtime = "androidx.lifecycle:lifecycle-runtime-ktx:$Version"
        const val Compiler = "androidx.lifecycle:lifecycle-compiler:$Version"
    }
}

object JUnit {
    const val Version = "5.6.2"
    const val Jupiter = "org.junit.jupiter:junit-jupiter:$Version"
}