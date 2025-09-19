package com.jaycefr.EyeSpy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.edit

class AdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Device admin enabled", Toast.LENGTH_SHORT).show()
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Device admin disabled", Toast.LENGTH_SHORT).show()
        super.onDisabled(context, intent)
    }

    private fun showNotification(context: Context, msg: String) {
        val channelId = "fail_channel"
        val rm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        val masterPrefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)

        // ✅ Check if activated
        val isActive = masterPrefs.getBoolean("activated", false)
        if (!isActive) {
            Log.d("AdminReceiver", "Feature is deactivated, ignoring failed attempt")
            return
        }

        // Continue only if active
        val prefs = context.getSharedPreferences("failed_attempts", Context.MODE_PRIVATE)
        val failedCount = prefs.getInt("count", 0) + 1
        prefs.edit { putInt("count", failedCount) }

        val allowedFailAttempts = masterPrefs.getInt("numberOfFailedAttempts", 3)
        Log.d("AdminReceiver", "Allowed failed attempts : $allowedFailAttempts")

        if (failedCount >= allowedFailAttempts) {
//            Toast.makeText(context, "$allowedFailAttempts failed attempts detected", Toast.LENGTH_SHORT).show()
            Log.d("AdminReceiver", "$allowedFailAttempts failed attempts detected")
            showNotification(context, "$allowedFailAttempts failed attempts detected")

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
        // ✅ Reset only if active
        val masterPrefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)
        val isActive = masterPrefs.getBoolean("activated", false)
        if (!isActive) {
            Log.d("AdminReceiver", "Feature is deactivated, ignoring password success")
            return
        }

        val prefs = context.getSharedPreferences("failed_attempts", Context.MODE_PRIVATE)
        prefs.edit { putInt("count", 0) }
        super.onPasswordSucceeded(context, intent, user)
    }
}
