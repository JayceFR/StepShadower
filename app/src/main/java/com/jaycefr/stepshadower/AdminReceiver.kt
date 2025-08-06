package com.jaycefr.stepshadower

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.core.app.NotificationCompat
import androidx.core.content.edit

class AdminReceiver : DeviceAdminReceiver(){
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Device admin enabled", Toast.LENGTH_SHORT).show()
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Device admin disabled", Toast.LENGTH_SHORT).show()
        super.onDisabled(context, intent)
    }

    fun showNotification(context : Context, msg : String){
        val channelId = "fail_channel"
        val rm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel =
                NotificationChannel(channelId, "Fail Channel", NotificationManager.IMPORTANCE_HIGH)
            rm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Security alert")
            .setContentText(msg)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        rm.notify(42, notification)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPasswordFailed(
        context: Context,
        intent: Intent,
        user: UserHandle
    ) {
//        Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show()
        val prefs = context.getSharedPreferences("failed_attempts", Context.MODE_PRIVATE)
        val failedCount = prefs.getInt("count", 0) + 1
        prefs.edit { putInt("count", failedCount) }

        if (failedCount >= 3){
            Toast.makeText(context, "3 failed attempts detected", Toast.LENGTH_SHORT).show()
            Log.d("AdminReceiver", "3 failed attempts detected")
            showNotification(context, "3 failed attempts detected")

//            val unlockEvent = Intent(context, UnlockListenerService::class.java)
//            context.startForegroundService(unlockEvent)

//            val camIntent = Intent(context, CameraActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            context.startActivity(camIntent)

            val serviceIntent = Intent(context, LockWatchService::class.java)
            context.startForegroundService(serviceIntent)


            prefs.edit { putInt("count", 0) }
        }

        super.onPasswordFailed(context, intent, user)
    }

    override fun onPasswordSucceeded(
        context: Context,
        intent: Intent,
        user: UserHandle
    ) {
//        Toast.makeText(context, "Welcome boss", Toast.LENGTH_SHORT).show()
        val prefs = context.getSharedPreferences("failed_attempts", Context.MODE_PRIVATE)
        prefs.edit { putInt("count", 0) }
        super.onPasswordSucceeded(context, intent, user)
    }
}