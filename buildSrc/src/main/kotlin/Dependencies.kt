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