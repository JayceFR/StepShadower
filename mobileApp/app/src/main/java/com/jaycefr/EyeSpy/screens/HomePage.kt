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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
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

    var photos by remember { mutableStateOf(getIntruderPhotos(context)) }

    // 🔁 Refresh photos every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            photos = getIntruderPhotos(context)
            kotlinx.coroutines.delay(5000)
        }
    }

    val count = photos.size
    val trialDaysLeft = remember { 7 } // Example value

    val name = remember {
        mutableStateOf(
            context.getSharedPreferences("User", Context.MODE_PRIVATE).getString("name", "")
        )
    }

    // ⏰ Greeting based on time
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Hello"
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp), // a bit more breathing room
            verticalArrangement = Arrangement.Top
        ) {
            // Greeting section
            Text(
                text = "$greeting, ${name.value}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Lock animation
            LockWith3DOrbits(
                modifier = Modifier
                    .fillMaxWidth(),
                lockSize = 260.dp,
                locked = true
            )

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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

            // Photos
            if (photos.isNotEmpty()) {
                Text(
                    text = "📸 Intruder photos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
//                    verticalArrangement = Arrangement.spacedBy(12.dp),
//                    modifier = Modifier.fillMaxSize()
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos) { file ->
                        IntruderPhotoRow(file)
                    }
                }
            } else {
                Text(
                    text = "No intruder photos yet 👌",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 24.dp)
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


