package com.jaycefr.stepshadower

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat

class UnlockListenerService : Service() {

    private lateinit var receiver: BroadcastReceiver

    private var isRegistered = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_USER_PRESENT) {
                    if (isRegistered) {
                        unregisterReceiver(this)
                        isRegistered = false
                    }
                    launchCameraActivity()
                    stopSelf()
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(receiver, filter)
        isRegistered = true

        Log.d("UnlockListenerService", "Service started")

        startForeground(1, buildNotification("Waiting for unlock to take photo"))

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRegistered) {
            unregisterReceiver(receiver)
            isRegistered = false
        }
    }

    private fun launchCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(msg: String): Notification {
        val channelId = "fail_channel"
        val rm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Fail Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            rm.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Security Alert")
            .setContentText(msg)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
