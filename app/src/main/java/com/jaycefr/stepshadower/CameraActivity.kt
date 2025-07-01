package com.jaycefr.stepshadower

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        window.setBackgroundDrawableResource(android.R.color.transparent)

        startCapture()

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2000)
    }

    private fun startCapture(){
        Log.d("CameraActivity", "Starting camera capture...")
    }
}