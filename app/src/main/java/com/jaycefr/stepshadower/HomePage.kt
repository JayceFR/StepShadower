package com.jaycefr.stepshadower

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jaycefr.stepshadower.permissions.PermissionScreen
import com.jaycefr.stepshadower.permissions.buildRequiredPermissionList
import com.jaycefr.stepshadower.step.Greeting

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
