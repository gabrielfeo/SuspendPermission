package com.gabrielfeo.suspend.permission.sample

import android.Manifest
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gabrielfeo.suspend.permission.assurer.PermissionsDeniedException
import com.gabrielfeo.suspend.permission.requester.requestPermissionsAsync

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launchWhenStarted {
            changeText("Will request permissions now")
            requestPermissions()
        }
    }

    private suspend fun requestPermissions() {
        val camera = Manifest.permission.CAMERA
        val mic = Manifest.permission.RECORD_AUDIO
        try {
            requestPermissionsAsync(arrayOf(camera, mic), 12553)
            changeText("Permissions granted")
        } catch (e: PermissionsDeniedException) {
            when (camera) {
                in e.permanentlyDenied -> changeText("I really can't work without that one")
                in e.currentlyDenied -> changeText("OK, let me explain")
                else -> changeText("Be aware your video will have no sound") // (denied mic permission)
            }
        }
    }

    private fun changeText(text: String) {
        findViewById<TextView>(R.id.text).text = text
    }

}
