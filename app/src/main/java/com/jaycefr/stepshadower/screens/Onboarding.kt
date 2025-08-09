package com.jaycefr.stepshadower.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*

@Composable
fun EmailInputField(
    email: String,
    onEmailChange: (String) -> Unit
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email Address") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,  // Make the text field circular
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.primary,
            disabledBorderColor = MaterialTheme.colorScheme.primary,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}


@Composable
fun FailedAttemptsStepper(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = { if (value > 1) onValueChange(value - 1) },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .size(40.dp)
        ) {
            Text("−", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        }

        Text(
            value.toString(),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.headlineSmall
        )

        IconButton(
            onClick = { if (value < 5) onValueChange(value + 1) },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .size(40.dp)
        ) {
            Text("+", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun OnboardingScreen() {

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.jaycefr.stepshadower.R.raw.onboarding))

    var email by remember { mutableStateOf("") }
    var numberOfFailedAttempts by remember { mutableStateOf(3) }

    // Show loading indicator while composition is null
    AnimatedVisibility(
        visible = (composition == null),
        enter = fadeIn(),
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
    }

    // Show Lottie animation with fade in when loaded
    AnimatedVisibility(
        visible = (composition != null),
        enter = fadeIn(animationSpec = tween(durationMillis = 600)),
        modifier = Modifier.fillMaxSize(),
        exit = fadeOut()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            Text(
                "Almost Ready to Come Aboard!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Section
            Text(
                "Enter the email where we should send the intruder’s picture:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))
            EmailInputField(email) { email = it }

            Spacer(modifier = Modifier.height(28.dp))

            // Attempts Section
            Text(
                "Choose the number of failed attempts before we take the picture:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))
            FailedAttemptsStepper(numberOfFailedAttempts) { numberOfFailedAttempts = it }
        }
    }
}


