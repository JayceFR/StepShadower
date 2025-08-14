package com.jaycefr.stepshadower.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jaycefr.stepshadower.R
import com.jaycefr.stepshadower.step.Greeting
import kotlin.math.max
import kotlin.math.min

@Composable
fun HomePage() {
    MaterialTheme {
        SmoothFadingRippleLockScreen()
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

@Composable
fun SmoothFadingRippleLockScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val totalDuration = 1800f
        val ringCount = 2

        val infiniteTransition = rememberInfiniteTransition(label = "infinite")
        val time by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = totalDuration,
            animationSpec = infiniteRepeatable(
                animation = tween(totalDuration.toInt(), easing = LinearEasing)
            ),
            label = "time"
        )

        for (i in 0 until ringCount) {
            val offset = (totalDuration / ringCount) * i
            val localTime = (time + offset) % totalDuration
            val progress = localTime / totalDuration

            RippleRingSmooth(progress, Color(0xFF4CAF50))
        }

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Lock Icon",
            modifier = Modifier.size(100.dp)
        )
    }
}

@Composable
fun RippleRingSmooth(progress: Float, color: Color) {
    val scale = 1f + progress * 1.5f

    // Fade in first 20%, fade out last 30%
    val fadeIn = min(1f, progress / 0.2f)
    val fadeOut = min(1f, (1f - progress) / 0.3f)
    val alpha = max(0f, fadeIn * fadeOut)

    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .alpha(alpha)
            .background(color = color.copy(alpha = 0.4f), shape = CircleShape)
    )
}