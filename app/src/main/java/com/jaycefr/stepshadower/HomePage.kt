package com.jaycefr.stepshadower

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jaycefr.stepshadower.step.Greeting
import com.jaycefr.stepshadower.step.StepViewModel
import com.jaycefr.stepshadower.step.StepsRepo
import kotlin.collections.emptyList

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomePage(appContext: Context) {
    val permissionStates = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (permission, granted) ->
            permissionStates[permission] = granted
        }
    }

    // Dynamically compute permissions to request
    val permissionsToRequest = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.FOREGROUND_SERVICE_CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            add(Manifest.permission.FOREGROUND_SERVICE_CAMERA)
        }

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // DO NOT include ACCESS_BACKGROUND_LOCATION here
    }.toMutableList()

    val backgroundLocationGranted = ContextCompat.checkSelfPermission(
        appContext,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val allMainPermissionsGranted = permissionsToRequest.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (allMainPermissionsGranted && backgroundLocationGranted) {
            Text(
                text = "All necessary permissions are granted!",
                color = Color.Green
            )
        } else {
            Text(
                text = "To use the app fully, we need access to:",
                modifier = Modifier.padding(bottom = 8.dp)
            )

//            if (!backgroundLocationGranted){
//                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//            }
            PermissionExplanationList(
                permissionsToRequest.plus(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                Log.d("HomePage", "Requesting permissions: $permissionsToRequest")
                if (permissionsToRequest.isNotEmpty()) {
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                } else if (!backgroundLocationGranted) {
                    // Request background location separately
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                }
            }) {
                Text("Enable Permissions")
            }
        }
    }
}


@Composable
fun PermissionExplanationList(permissions: List<String>) {
    Column {
        permissions.forEach { permission ->
            val reason = when (permission) {
                Manifest.permission.CAMERA -> "Camera – to capture intruders"
                Manifest.permission.POST_NOTIFICATIONS -> "Notifications – to alert you instantly"
                Manifest.permission.FOREGROUND_SERVICE_CAMERA -> "Foreground camera – to run in the background"
                Manifest.permission.ACCESS_FINE_LOCATION -> "Location – for location tagging"
                Manifest.permission.ACCESS_BACKGROUND_LOCATION -> "Background location – to track without opening the app"
                else -> "Permission required"
            }
            Text("• $reason", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
