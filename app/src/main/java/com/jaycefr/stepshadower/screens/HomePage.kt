package com.jaycefr.stepshadower.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomePage() {
    MaterialTheme {
        LockWith3DOrbits(
            modifier = Modifier.fillMaxSize(),
            lockSize = 320.dp,
            locked = true
        )
    }
}