package com.jaycefr.EyeSpy.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit

@Composable
fun SettingsPage() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)

    // Load saved values
    var email by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var attempts by remember { mutableStateOf(prefs.getInt("attempts", 3)) }
    var isActive by remember { mutableStateOf(prefs.getBoolean("activated", true)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "⚙️ Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- Activation Toggle ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("App Status", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (isActive) "Currently Active" else "Currently Inactive",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Email field ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Alert Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Attempts selector ---
        Text("Number of Attempts", style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            FilledTonalButton(
                onClick = { if (attempts > 1) attempts-- },
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("-") }

            Text(
                text = attempts.toString(),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            FilledTonalButton(
                onClick = { attempts++ },
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("+") }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Save button ---
        Button(
            onClick = {
                prefs.edit {
                    putString("email", email)
                    putInt("attempts", attempts)
                    putBoolean("activated", isActive)
                }
                Toast.makeText(context, "✅ Settings saved", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Save Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Clear intruder data button ---
        OutlinedButton(
            onClick = {
                val deleted = clearIntruderPhotos(context)
                Toast.makeText(
                    context,
                    if (deleted) "🗑️ All intruder photos deleted" else "No photos found",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Clear All Intruder Data")
        }
    }
}

// Deletes all intruder photos from the app's filesDir
fun clearIntruderPhotos(context: Context): Boolean {
    val dir = context.filesDir
    val intruderFiles = dir.listFiles { file ->
        file.name.startsWith("intruder_") && file.name.endsWith(".jpg")
    } ?: return false

    intruderFiles.forEach { it.delete() }
    return intruderFiles.isNotEmpty()
}
