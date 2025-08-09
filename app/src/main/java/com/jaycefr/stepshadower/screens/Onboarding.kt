package com.jaycefr.stepshadower.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jaycefr.stepshadower.step.Greeting

@Composable
fun OnboardingScreen(){
    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Greeting(name = "Onboarding Jayce", modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}