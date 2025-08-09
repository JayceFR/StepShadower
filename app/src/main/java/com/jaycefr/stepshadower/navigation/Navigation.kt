package com.jaycefr.stepshadower.navigation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jaycefr.stepshadower.screens.HomePage
import com.jaycefr.stepshadower.screens.OnboardingScreen
import com.jaycefr.stepshadower.user.UserRepo
import com.jaycefr.stepshadower.user.UserViewModel
import com.jaycefr.stepshadower.permissions.PermissionScreen
import com.jaycefr.stepshadower.permissions.buildRequiredPermissionList

@Composable
fun Navigation(appContext : Context){
    val navController = rememberNavController()

    val repo : UserRepo = remember { UserRepo(appContext) }
    val userViewModel : UserViewModel = remember { UserViewModel(repo) }

    val onboard by userViewModel.toOnboard.collectAsState()

    if (onboard == null){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
        return
    }

    val needsPermissions by remember {
        derivedStateOf {
            val onboard = userViewModel.toOnboard.value
            buildRequiredPermissionList(appContext, if (onboard == true) "onboard" else "home")
        }
    }

    val startPos = remember {
        derivedStateOf {
            if (needsPermissions.isNotEmpty()){
                needsPermissions[0].routeName
            }
            else if (onboard == true){
                "onboard"
            }
            else{
                "home"
            }
        }
    }

    Log.d("Navigation", "startPos: ${startPos.value}")

    NavHost(
        navController = navController,
        startDestination = startPos.value
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

        composable("onboard"){
            OnboardingScreen()
        }

        // Final destination
        composable("home") {
            HomePage()
        }
    }
}