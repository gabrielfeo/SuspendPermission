# SuspendPermission

[![Release](https://jitpack.io/v/com.gabrielfeo/suspendpermission.svg)](https://jitpack.io/#com.gabrielfeo/suspendpermission)

A simple, idiomatic permissions API with Kotlin Coroutines.

## Setup

Declare [JitPack](https://jitpack.io/) as a repository and add a dependency on the library:

```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.gabrielfeo:suspendpermission:1.0.0'
}
```

[Gradle Kotlin DSL example](/sample/build.gradle.kts)

## Usage

`requestPermissionsAsync` can be called from any `Activity` or `Fragment` with a set of permissions and a request code, just like the platform's [requestPermissions](https://developer.android.com/reference/android/app/Activity.html#requestPermissions(java.lang.String[],%20int)):

```kotlin
suspend fun requestPermissionsForRecording() {
    val camera = Manifest.permission.CAMERA
    val mic = Manifest.permission.RECORD_AUDIO
    try {
        requestPermissionsAsync(arrayOf(camera, mic), 13949)) // suspend
    } catch (exception: PermissionsDeniedException) {
        when {
            camera in exception.permanentlyDeniedPermissions -> // respect the user's decision
            else -> // explain you really need access to the camera
        }
    }
}
```

Permissions will first be checked and the ones which aren't yet granted will be requested to the user, throwing a `PermissionsDeniedException` if any is denied. The exception specifies which were permanently denied so that it's easy to decide what to do.

`requestPermissionsAsync` is available as an extension function on `androidx.app.FragmentActivity` and `androidx.fragment.Fragment`.

## License

This project is licensed under the terms of the MIT license. See the [LICENSE](LICENSE.txt) file.
