package com.jaycefr.EyeSpy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class ShutDownReceiver : BroadcastReceiver(){

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(p0: Context?, p1: Intent?) {
        if (Intent.ACTION_SHUTDOWN == p1?.action){
            Log.d("ShutDownReceiver", "Shutting down")

            val serviceIntent = Intent(p0, LockWatchService::class.java)
            p0?.startForegroundService(serviceIntent)
        }
    }

}