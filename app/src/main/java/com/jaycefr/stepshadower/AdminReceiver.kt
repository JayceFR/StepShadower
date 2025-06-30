package com.jaycefr.stepshadower

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.widget.Toast

class AdminReceiver : DeviceAdminReceiver(){
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Device admin enabled", Toast.LENGTH_SHORT).show()
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, "Device admin disabled", Toast.LENGTH_SHORT).show()
        super.onDisabled(context, intent)
    }

    override fun onPasswordFailed(
        context: Context,
        intent: Intent,
        user: UserHandle
    ) {
        Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show()
        super.onPasswordFailed(context, intent, user)
    }

    override fun onPasswordSucceeded(
        context: Context,
        intent: Intent,
        user: UserHandle
    ) {
        Toast.makeText(context, "Welcome boss", Toast.LENGTH_SHORT).show()
        super.onPasswordSucceeded(context, intent, user)
    }
}