package com.jaycefr.stepshadower

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jaycefr.stepshadower.step.Greeting
import com.jaycefr.stepshadower.step.StepViewModel
import com.jaycefr.stepshadower.step.StepsRepo
import kotlin.collections.emptyList

@Composable
fun PermissionNavHost(appContext: Context, navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = "camera") {
        composable("camera") {
            PermissionScreen(
                title = "Camera Access",
                description = "We use your camera to take a picture of intruders.",
                permission = Manifest.permission.CAMERA,
                imageRes = R.drawable.ic_launcher_background,
                nextRoute = "notifications",
                appContext = appContext,
                navController = navController
            )
        }
        composable("notifications") {
            PermissionScreen(
                title = "Notification Access",
                description = "We use notifications to alert you when someone tries to break in.",
                permission = Manifest.permission.POST_NOTIFICATIONS,
                imageRes = R.drawable.ic_launcher_background,
                nextRoute = "location",
                appContext = appContext,
                navController = navController
            )
        }
        composable("location") {
            PermissionScreen(
                title = "Location Access",
                description = "Location helps us tag where the unauthorized attempt occurred.",
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                imageRes = R.drawable.ic_launcher_background,
                nextRoute = "background_location",
                appContext = appContext,
                navController = navController
            )
        }
        composable("background_location") {
            PermissionScreen(
                title = "Background location Access",
                description = "Location helps us tag where the unauthorized attempt occurred.",
                permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                imageRes = R.drawable.ic_launcher_background,
                nextRoute = null,
                appContext = appContext,
                navController = navController
            )
        }
    }
}

@Composable
fun PermissionScreen(
    title: String,
    description: String,
    permission: String,
    imageRes: Int,
    nextRoute: String?,
    appContext: Context,
    navController: NavController
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            nextRoute?.let { navController.navigate(it) }
        } else {
            // Optional: show a dialog or allow retry
        }
    }

    val alreadyGranted = ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED

    LaunchedEffect(Unit) {
        if (alreadyGranted) {
            nextRoute?.let { navController.navigate(it) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(title, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text(description, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            launcher.launch(permission)
        }) {
            Text("Allow & Continue")
        }
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomePage(appContext: Context) {
    PermissionNavHost(appContext)
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
