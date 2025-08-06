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
import com.jaycefr.stepshadower.step.StepPage
import com.jaycefr.stepshadower.ui.theme.StepShadowerTheme

class MainActivity : ComponentActivity() {

    private lateinit var enableAdminReceiver: ActivityResultLauncher<Intent>

    private var shutDownReceiver : ShutDownReceiver? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        enableAdminReceiver = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            result ->
            if (result.resultCode == RESULT_OK){
                Log.d("MainActivity", "Admin enabled")
            }
            else{
                Log.d("MainActivity", "Admin not enabled")
            }
        }

        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, AdminReceiver::class.java)
        if (!dpm.isAdminActive(adminComponent)){
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "This app needs Device Admin Permission to monitor failed logins."
                )
            }
            enableAdminReceiver.launch(intent)
        }

        shutDownReceiver = ShutDownReceiver()
        val filter = IntentFilter(Intent.ACTION_SHUTDOWN)
        registerReceiver(shutDownReceiver, filter)
        setContent {
            HomePage(this.applicationContext)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StepShadowerTheme {
        HomePage(LocalContext.current)
    }
}