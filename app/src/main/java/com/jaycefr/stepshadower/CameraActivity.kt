package com.jaycefr.stepshadower

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import java.io.File

class CameraActivity : ComponentActivity() {

    private lateinit var outputDirectory : File

    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

//        window.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(R.layout.activity_camera)

        outputDirectory = getOutputDirectory()

        startCapture()

//        Handler(Looper.getMainLooper()).postDelayed({
//            finish()
//        }, 2000)
    }

    private fun getOutputDirectory() : File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, "captured").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun startCapture(){
        Log.d("CameraActivity", "Starting camera capture...")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build()
                .also {
                    it.surfaceProvider = findViewById<PreviewView>(R.id.previewView).surfaceProvider
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(windowManager.defaultDisplay.rotation)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                Log.d("CameraActivity", "Camera started")

                val previewView = findViewById<PreviewView>(R.id.previewView)
                previewView.postDelayed({
                    takePhoto()
                }, 500)

            } catch (e : Exception){
                Log.e("CameraActivity", "Use case binding failed", e)
            }


        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val photoFile = File(
            outputDirectory,
            java.text.SimpleDateFormat(FILENAME_FORMAT, java.util.Locale.UK)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    finish()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo saved: ${photoFile.absolutePath}")
                    Toast.makeText(baseContext, "Photo saved", Toast.LENGTH_SHORT).show()
                    finish()
                }

            }
        )
    }

    companion object {
        private const val TAG = "CameraActivity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}