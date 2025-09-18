package com.jaycefr.EyeSpy.screens

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.jaycefr.EyeSpy.user.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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
    var emailToSend by remember { mutableStateOf("") }
    var numberOfFailedAttempts by remember { mutableStateOf(3) }
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope("https://www.googleapis.com/auth/gmail.send"))
        .build()
    val googleSignInClient = GoogleSignIn.getClient(activity, gso)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            val email = account?.email

            if (email != null && account != null) {
                val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)
                prefs.edit { putString("gmail_email", email) }
                prefs.edit { putString("name", account.displayName)}

                getAccessToken(context, account) { token ->
                    if (token != null) {
                        prefs.edit { putString("gmail_token", token) } // Save token
                        Toast.makeText(context, "Token acquired for $email", Toast.LENGTH_SHORT).show()
                        Log.d("AuthToken", "Access token: $token")
//                        sendEmail(token, email)
                        viewModel.insertUser(emailToSend, numberOfFailedAttempts)
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    } else {
                        Toast.makeText(context, "Failed to get access token", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.e("SignIn", "Sign-in failed", task.exception)
        }
    }

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

//                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Enter the email where we should send the intruder’s picture:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                EmailInputField(emailToSend) { emailToSend = it }
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
                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(text = "Sign in")
                }

//                Button(
//                    onClick = {
//                        viewModel.insertUser(email, numberOfFailedAttempts)
//                        navController.navigate("home"){
//                            popUpTo(0)
//                        }
//                    }
//                ){
//                    Text("Continue")
//                }
            }
        }
    }
}

fun getAccessToken(
    context: Context,
    account: GoogleSignInAccount,
    scope: String = "https://www.googleapis.com/auth/gmail.send",
    onTokenReceived: (String?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = GoogleAuthUtil.getToken(
                context,
                account.account!!,
                "oauth2:$scope"
            )
            withContext(Dispatchers.Main) {
                onTokenReceived(token)
            }
        } catch (e: Exception) {
            Log.e("Auth", "Failed to get token", e)
            withContext(Dispatchers.Main) {
                onTokenReceived(null)
            }
        }
    }
}

fun sendEmail(accessToken: String, fromEmail: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val to = "jaycejefferson.vicious@gmail.com"
            val subject = "Intruder Alert"
            val body = "There have been intrusions detected."

            val message = """
                From: $fromEmail
                To: $to
                Subject: $subject

                $body
            """.trimIndent()

            val encodedEmail = Base64.encodeToString(message.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)

            val json = JSONObject()
            json.put("raw", encodedEmail)

            val url = URL("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $accessToken")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            conn.outputStream.use { os ->
                val input = json.toString().toByteArray()
                os.write(input, 0, input.size)
            }

            val responseCode = conn.responseCode
            Log.d("GmailAPI", "Email send response: $responseCode")
        } catch (e: Exception) {
            Log.e("GmailAPI", "Failed to send email", e)
        }
    }
}



