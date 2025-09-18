package com.jaycefr.EyeSpy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeBodyPart
import java.io.File
import javax.activation.DataHandler
import javax.activation.FileDataSource
import com.google.android.gms.location.LocationServices
import com.google.api.client.auth.oauth.OAuthGetAccessToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LockWatchService : LifecycleService(){
    private lateinit var cameraExecutor: ExecutorService

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        cameraExecutor = Executors.newSingleThreadExecutor()
        startForeground(1, startForegroundServiceNotification())
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)
        captureIntruderPhoto()
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundServiceNotification() : Notification{
        val channelID = "eyespy_channel"
        val channel = NotificationChannel(channelID, "EyeSpy Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle("EyeSpy Active")
            .setContentText("Monitoring")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
        return notification
    }

    private fun captureIntruderPhoto(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                imageCapture
            )

            val photoFile = File(filesDir, "intruder_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.d("EyeSpy", "Photo Saved : ${photoFile.absolutePath}")

                        val prefs = applicationContext.getSharedPreferences("User", Context.MODE_PRIVATE)
                        val toEmail : String = prefs.getString("email", "").toString()


                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
                        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { location ->
                                    if (location != null) {
                                        val lat = location.latitude
                                        val lon = location.longitude
                                        val locationText = """
                                            Latitude : $lat 
                                            Longitude : $lon
                                            https://maps.google.com/?q=$lat,$lon
                                        """.trimIndent()
                                        val subject = "Intruder"
                                        val photoFile = File(photoFile.absolutePath)
                                        val body = "Intruder detected\n$locationText"
                                        getFreshAccessToken(applicationContext){ token ->
                                            if (token != null){
                                                sendEmailWithGmailApi(
                                                    context = applicationContext,
                                                    toEmail = toEmail,
                                                    subject = subject,
                                                    bodyText = body,
                                                    accessToken = token,
                                                    attachmentFile = photoFile
                                                )
                                            }
                                            else{
                                                Log.d("EyeSpy", "Token returned is null")
                                            }
                                        }
                                        stopSelf()
                                    }
                                    else{
                                        Log.d("EyeSpy", "Location not found")
                                        val subject = "Intruder"
                                        val body = "Location not found"
                                        val photoFile = File(photoFile.absolutePath)
                                        getFreshAccessToken(applicationContext){ token ->
                                            if (token != null){
                                                sendEmailWithGmailApi(
                                                    context = applicationContext,
                                                    toEmail = toEmail,
                                                    subject = subject,
                                                    bodyText = body,
                                                    accessToken = token,
                                                    attachmentFile = photoFile
                                                )
                                            }
                                            else{
                                                Log.d("EyeSpy", "Token returned is null")
                                            }
                                        }
                                        stopSelf()
                                    }
                                }
                        }
                        else{
                            Log.d("EyeSpy", "Location permission not granted")
                            stopSelf()
                        }

                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("EyeSpy", "Photo Capture failed", exception)
                        stopSelf()
                    }
                })
        }, ContextCompat.getMainExecutor(this))
    }


    fun getFreshAccessToken(
        context: Context,
        onTokenReceived: (String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (account != null) {
                    val scope = "oauth2:https://www.googleapis.com/auth/gmail.send"
                    val token = GoogleAuthUtil.getToken(context, account.account!!, scope)
                    withContext(Dispatchers.Main) {
                        onTokenReceived(token)
                    }
                } else {
                    Log.e("Token", "No signed-in account found")
                    withContext(Dispatchers.Main) {
                        onTokenReceived(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("Token", "Failed to get token", e)
                withContext(Dispatchers.Main) {
                    onTokenReceived(null)
                }
            }
        }
    }

    fun sendEmailWithGmailApi(
        context: Context,
        toEmail: String,
        subject: String,
        bodyText: String,
        accessToken: String,
        attachmentFile: File?
    ) {
        val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)
//        val accessToken = prefs.getString("gmail_token", null)
        val fromEmail = prefs.getString("gmail_email", null)

        if (accessToken.isNullOrEmpty() || fromEmail.isNullOrEmpty()) {
            Log.e("EmailAPI", "Missing access token or email")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create MIME message manually
                val boundary = "boundary_${System.currentTimeMillis()}"
                val sb = StringBuilder()
                sb.append("From: $fromEmail\r\n")
                sb.append("To: $toEmail\r\n")
                sb.append("Subject: $subject\r\n")
                sb.append("MIME-Version: 1.0\r\n")
                sb.append("Content-Type: multipart/mixed; boundary=\"$boundary\"\r\n")
                sb.append("\r\n")

                // Body part
                sb.append("--$boundary\r\n")
                sb.append("Content-Type: text/plain; charset=UTF-8\r\n\r\n")
                sb.append(bodyText).append("\r\n")

                // Attachment part
                if (attachmentFile != null && attachmentFile.exists()) {
                    val imageBytes = attachmentFile.readBytes()
                    val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)

                    sb.append("--$boundary\r\n")
                    sb.append("Content-Type: image/jpeg\r\n")
                    sb.append("Content-Transfer-Encoding: base64\r\n")
                    sb.append("Content-Disposition: attachment; filename=\"${attachmentFile.name}\"\r\n\r\n")
                    sb.append(base64Image).append("\r\n")
                }

                sb.append("--$boundary--")

                // Encode entire MIME message
                val rawMessage = android.util.Base64.encodeToString(
                    sb.toString().toByteArray(),
                    android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
                )

                // Create JSON payload
                val jsonBody = """
                {
                    "raw": "$rawMessage"
                }
            """.trimIndent()

                val url = java.net.URL("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $accessToken")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                conn.outputStream.use {
                    it.write(jsonBody.toByteArray())
                }

                val responseCode = conn.responseCode
                val responseMsg = conn.inputStream.bufferedReader().readText()
                Log.d("EmailAPI", "Response $responseCode: $responseMsg")
            } catch (e: Exception) {
                Log.e("EmailAPI", "Failed to send email", e)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}