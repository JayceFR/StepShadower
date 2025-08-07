package com.jaycefr.stepshadower

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jaycefr.stepshadower.step.Greeting
import com.jaycefr.stepshadower.step.StepViewModel
import com.jaycefr.stepshadower.step.StepsRepo
import kotlinx.coroutines.delay
import kotlin.collections.emptyList

@Composable
fun ShowCheckAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.checkmark))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1
    )
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(150.dp)
        )
    }
}




fun getRequiredPermissions() : List<String>{
    return buildList {
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }
}

fun areAllPermissionsGranted(context: Context, permissions: List<String>) : Boolean{
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

fun isPermissionPermanentlyDeclined(context : Context, permission: String): Boolean {
//    val context = LocalContext.current
    val activity = context as? Activity
    return activity?.let {
        !ActivityCompat.shouldShowRequestPermissionRationale(it, permission) &&
                ContextCompat.checkSelfPermission(it, permission) != PackageManager.PERMISSION_GRANTED
    } ?: false
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomePage(appContext: Context) {
    val navController = rememberNavController()

    val needsPermissions by remember {
        derivedStateOf {
            !areAllPermissionsGranted(appContext, getRequiredPermissions())
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (needsPermissions) "camera" else "home"
    ) {
        // Permissions flow screens
        composable("camera") {
            PermissionScreen(
                title = "Camera Access",
                description = "We use your camera to take a picture of intruders.",
                permission = Manifest.permission.CAMERA,
                imageRes = R.raw.camera,
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
                imageRes = R.raw.notification,
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
                imageRes = R.raw.location,
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
                imageRes = R.raw.background_loc,
                nextRoute = "home",
                appContext = appContext,
                navController = navController
            )
        }

        // Final destination
        composable("home") {
            Greeting()
        }
    }
}

@Composable
fun Greeting(){
    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Greeting(name = "Jayce", modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
