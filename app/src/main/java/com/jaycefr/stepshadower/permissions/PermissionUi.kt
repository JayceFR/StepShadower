package com.jaycefr.stepshadower.permissions


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jaycefr.stepshadower.AdminReceiver
import com.jaycefr.stepshadower.R
import kotlinx.coroutines.delay

@Composable
fun PermissionScreen(
    title: String,
    description: String,
    permission: String?,
    imageRes: Int,
    nextRoute: String?,
    appContext: Context,
    navController: NavController,
    deviceAdmin : Boolean = false,
) {
    var showAnimation by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showAnimation = true
        }
    }

    val enableAdminReceiver = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == RESULT_OK){
            Log.d("MainActivity", "Admin enabled")
        }
        else{
            Log.d("MainActivity", "Admin not enabled")
        }
    }

    if (showAnimation) {
        ShowCheckAnimation()
        LaunchedEffect(Unit) {
            delay(1500)
            nextRoute?.let {
                navController.navigate(it) {
                    popUpTo(0)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(imageRes))

            val isDeclinedPermanently = remember { mutableStateOf(false) }

            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(32.dp))


            val context = LocalContext.current

            val lifecycleOwner = LocalLifecycleOwner.current
            val activity = context as? Activity

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        if (deviceAdmin){
                            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                            val adminComponent = ComponentName(context, AdminReceiver::class.java)
                            if (dpm.isAdminActive(adminComponent)){
                                showAnimation = true
                            }
                        }
                        else{
                            val permissionGranted = ContextCompat.checkSelfPermission(
                                context,
                                permission!!
                            ) == PackageManager.PERMISSION_GRANTED
                            if (permissionGranted) {
                                showAnimation = true
                            }

                            isDeclinedPermanently.value = activity?.let {
                                !ActivityCompat.shouldShowRequestPermissionRationale(it, permission) &&
                                        ContextCompat.checkSelfPermission(it, permission) != PackageManager.PERMISSION_GRANTED
                            } ?: false
                        }
                    }
                }

                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            if (isDeclinedPermanently.value){
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Permission permanently denied.\nPlease enable it manually in settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Open Settings")
                    }
                }
            } else {
                Button(onClick = {
                    if (deviceAdmin){
                        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                        val adminComponent = ComponentName(context, AdminReceiver::class.java)
                        if (dpm.isAdminActive(adminComponent)) {
                            showAnimation = true
                        } else{
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                putExtra(
                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "This app needs Device Admin Permission to monitor failed logins."
                                )
                            }
                            enableAdminReceiver.launch(intent)
                        }
                    }
                    else{
                        if (ContextCompat.checkSelfPermission(context, permission!!) == PackageManager.PERMISSION_GRANTED) {
                            showAnimation = true
                        } else {
                            launcher.launch(permission)
                        }
                    }
                }) {
                    Text("Allow & Continue")
                }
            }
        }
    }
}

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