package com.jaycefr.stepshadower.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jaycefr.stepshadower.HomePage
import com.jaycefr.stepshadower.permissions.PermissionScreen
import com.jaycefr.stepshadower.permissions.buildRequiredPermissionList

@Composable
fun Navigation(appContext : Context){
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
                    navController = navController,
                    deviceAdmin = permission.deviceAdmin
                )
            }
        }

        // Final destination
        composable("home") {
            HomePage()
        }
    }
}