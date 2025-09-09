package com.jaycefr.EyeSpy.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.jaycefr.EyeSpy.user.UserViewModel

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
fun OnboardingScreen(
    viewModel : UserViewModel,
    navController : NavController
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.jaycefr.EyeSpy.R.raw.onboarding))
    var email by remember { mutableStateOf("") }
    var numberOfFailedAttempts by remember { mutableStateOf(3) }

    Crossfade(targetState = composition != null, modifier = Modifier.fillMaxSize().padding(24.dp)) { isLoaded ->
        if (!isLoaded) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            // Content with animation
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(220.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    "Almost Ready to Come Aboard!",
                    style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Enter the email where we should send the intruder’s picture:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                EmailInputField(email) { email = it }
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    "Choose the number of failed attempts before we take the picture:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                FailedAttemptsStepper(numberOfFailedAttempts) { numberOfFailedAttempts = it }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        viewModel.insertUser(email, numberOfFailedAttempts)
                        navController.navigate("home"){
                            popUpTo(0)
                        }
                    }
                ){
                    Text("Continue")
                }
            }
        }
    }
}



