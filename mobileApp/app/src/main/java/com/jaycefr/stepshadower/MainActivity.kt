package com.jaycefr.stepshadower

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.jaycefr.stepshadower.navigation.Navigation
import com.jaycefr.stepshadower.step.StepPage
import com.jaycefr.stepshadower.ui.theme.StepShadowerTheme

class MainActivity : ComponentActivity() {

    private lateinit var enableAdminReceiver: ActivityResultLauncher<Intent>

    private var shutDownReceiver : ShutDownReceiver? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        shutDownReceiver = ShutDownReceiver()
        val filter = IntentFilter(Intent.ACTION_SHUTDOWN)
        registerReceiver(shutDownReceiver, filter)
        setContent {
            Navigation(applicationContext)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StepShadowerTheme {
        Navigation(LocalContext.current)
    }
}