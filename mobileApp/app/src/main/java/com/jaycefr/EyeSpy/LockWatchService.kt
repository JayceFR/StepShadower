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
        val channelID = "lockwatch_channel"
        val channel = NotificationChannel(channelID, "Lockwatch Service", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle("Lockwatch Active")
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
                        Log.d("Lockwatch", "Photo Saved : ${photoFile.absolutePath}")

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
                                        // Send the email here
                                        val gmailUser = "jaycejefferson.vicious@gmail.com"
                                        val appPassword = "oeml jwrb ngdd gsfd"
//                                        val toEmail = "jaycejefferson31@gmail.com"
                                        val subject = "Intruder"
                                        val photoFile = File(photoFile.absolutePath)
                                        analyzeFaceSummary(photoFile){
                                            faceSummary ->
                                            val body = """
                                                Intruder detected 
                                                
                                                $faceSummary
                                                
                                                $locationText
                                            """.trimIndent()
                                            sendEmail(gmailUser, appPassword, toEmail, subject, body, photoFile)
                                        }
                                        stopSelf()
                                    }
                                    else{
                                        Log.d("Lockwatch", "Location not found")
                                        // Send the email here
                                        val gmailUser = "jaycejefferson.vicious@gmail.com"
                                        val appPassword = "oeml jwrb ngdd gsfd"
//                                        val toEmail = "jaycejefferson31@gmail.com"
                                        val subject = "Intruder"
                                        val body = "Location not found"
                                        val photoFile = File(photoFile.absolutePath)
                                        sendEmail(gmailUser, appPassword, toEmail, subject, body, photoFile)
                                        stopSelf()
                                    }
                                }
                        }
                        else{
                            Log.d("Lockwatch", "Location permission not granted")
                            stopSelf()
                        }

                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("Lockwatch", "Photo Capture failed", exception)
                        stopSelf()
                    }
                })
        }, ContextCompat.getMainExecutor(this))
    }

    fun sendEmail(
        userEmail: String,
        userPassword: String,
        toEmail: String,
        subject: String,
        bodyText: String,
        attachmentFile: File? = null
    ) {
        Thread {
            try {
                val props = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(userEmail, userPassword)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(userEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                    setSubject(subject)

                    if (attachmentFile != null && attachmentFile.exists()) {
                        val multipart = MimeMultipart()

                        // Body part
                        val textPart = MimeBodyPart().apply {
                            setText(bodyText)
                        }
                        multipart.addBodyPart(textPart)

                        // Attachment part
                        val attachmentPart = MimeBodyPart().apply {
                            dataHandler = DataHandler(FileDataSource(attachmentFile))
                            fileName = attachmentFile.name
                        }
                        multipart.addBodyPart(attachmentPart)

                        setContent(multipart)
                    } else {
                        setText(bodyText)
                    }
                }
                Transport.send(message)
                println("Email sent successfully")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}