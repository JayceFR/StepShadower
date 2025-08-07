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
import com.jaycefr.stepshadower.permissions.PermissionScreen
import com.jaycefr.stepshadower.permissions.buildRequiredPermissionList
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

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HomePage(appContext: Context) {
    val navController = rememberNavController()

    val needsPermissions by remember {
        derivedStateOf {
            buildRequiredPermissionList(appContext, "home")
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (needsPermissions.isNotEmpty()) needsPermissions[0].routeName else "home"
    ) {
        // Permissions flow screens
        for (permission in needsPermissions) {
            composable(permission.routeName) {
                PermissionScreen(
                    title = permission.title,
                    description = permission.description,
                    permission = permission.permission,
                    imageRes = permission.imageRes,
                    nextRoute = permission.nextRoute,
                    appContext = appContext,
                    navController = navController
                )
            }
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
