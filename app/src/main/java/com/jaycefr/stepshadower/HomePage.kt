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

@Composable
fun HomePage() {
    Greeting()
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
