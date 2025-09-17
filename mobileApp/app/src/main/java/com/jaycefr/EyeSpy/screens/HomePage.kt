package com.jaycefr.EyeSpy.screens

import android.app.Activity
import android.content.Context
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.jaycefr.EyeSpy.R
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun HomePage() {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    val photos by remember { mutableStateOf(getIntruderPhotos(context)) }
    val count = photos.size

    // Pretend you save this when the app first arms
    val armedSince = remember { System.currentTimeMillis() - 86400000L } // 1 day ago
    val trialDaysLeft = remember { 7 } // Example value

    val RC_SIGN_IN = 1001

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

                // ✅ Get OAuth token
                getAccessToken(context, account) { token ->
                    if (token != null) {
                        prefs.edit { putString("gmail_token", token) } // Save token
                        Toast.makeText(context, "Token acquired for $email", Toast.LENGTH_SHORT).show()
                        Log.d("AuthToken", "Access token: $token")
                        sendEmail(token, email)
                    } else {
                        Toast.makeText(context, "Failed to get access token", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Log.e("SignIn", "Sign-in failed", task.exception)
        }
    }


    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
        ) {
            LockWith3DOrbits(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                lockSize = 320.dp,
                locked = true
            )

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


            // First row of stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Intruders detected",
                    value = count.toString(),
                    iconRes = R.drawable.intruder2,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Trial left",
                    value = "$trialDaysLeft days",
                    iconRes = R.drawable.hourglass,
                    modifier = Modifier.weight(1f)
                )
            }


            // Photo list below stats
            if (photos.isNotEmpty()) {
                Text(
                    text = "Intruders",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(photos) { file ->
                        IntruderPhotoRow(file)
                    }
                }
            } else {
                Text(
                    text = "No intruder photos yet",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

}

@Composable
fun StatCard(
    title: String,
    value: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
            .height(140.dp) // uniform height for all cards
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon at the top
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(42.dp)
            )

            // Value big in the middle
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium, // big text
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Title at the bottom
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}



@Composable
fun IntruderPhotoRow(file: File) {
    val bitmap = remember(file) { BitmapFactory.decodeFile(file.absolutePath) }
    val timestamp = extractTimestamp(file.name)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Intruder photo",
                modifier = Modifier
                    .size(100.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = "Captured at:", style = MaterialTheme.typography.labelSmall)
            Text(
                text = timestamp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun getIntruderPhotos(context: Context): List<File> {
    val dir = context.filesDir
    return dir.listFiles { file ->
        file.name.startsWith("intruder_") && file.name.endsWith(".jpg")
    }?.sortedByDescending { it.lastModified() } ?: emptyList()
}

fun extractTimestamp(fileName: String): String {
    return try {
        val millis = fileName
            .removePrefix("intruder_")
            .removeSuffix(".jpg")
            .toLong()
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        "Unknown time"
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


