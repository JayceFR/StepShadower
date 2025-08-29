package com.jaycefr.stepshadower.screens

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@Composable
fun AIPage() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)

    var message by remember { mutableStateOf("") }
    var messages = remember { mutableStateListOf<String>() }

    // Add a friendly welcome at start
    LaunchedEffect(Unit) {
        messages.add(
            "Bot: Hi there! 😄 I'm your personal assistant. I can help you change your email 📧, adjust thresholds 🔢, enable/disable alerts ✅🚫, or just chat with you. Ask me anything!"
        )
    }

    // Dialog states
    var showThresholdDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }

    // Dialog inputs
    var newThreshold by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Chat log
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                Text(text = msg, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask me something...") }
            )
            Button(onClick = {
                if (message.isNotBlank()) {

                    val currentMessage = message
                    val label = predictIntent(context, currentMessage)

                    // Extract before clearing
                    val extractedEmail = extractEmail(currentMessage, prefs)
                        ?: prefs.getString("email", "")!!
                    val extractedThreshold = extractThreshold(currentMessage, prefs)
                        ?: prefs.getInt("attempts", 3)

                    messages.add("You: $currentMessage")
                    val friendlyReply = when (label) {
                        "change_email" -> "Sure! Let's update your email 📧. Please check the box below."
                        "change_threshold" -> "Got it! Let's adjust your threshold 🔢. You can edit it below."
                        "enable_alerts" -> "All set! ✅ Alerts are now enabled. Stay safe!"
                        "disable_alerts" -> "No worries! 🚫 Alerts are now disabled. I'll stay quiet for now."
                        "other" -> "Hey there! 😄 I’m here to help. Ask me anything or just say hi!"
                        else -> "Hmm… I understood '$label', but let's keep going! 😊"
                    }
                    messages.add("Bot: $friendlyReply")
                    message = "" // clear input

                    when (label) {
                        "change_threshold" -> {
                            newThreshold = extractedThreshold.toString()
                            showThresholdDialog = true
                        }
                        "change_email" -> {
                            newEmail = extractedEmail
                            showEmailDialog = true
                        }
                        "enable_alerts" -> prefs.edit { putBoolean("activated", true) }
                        "disable_alerts" -> prefs.edit { putBoolean("activated", false) }
                    }
                }
            }) {
                Text("Send")
            }
        }
    }

    // ---------- Dialogs ----------
    if (showThresholdDialog) {
        AlertDialog(
            onDismissRequest = { showThresholdDialog = false },
            title = { Text("Change Threshold") },
            text = {
                TextField(
                    value = newThreshold,
                    onValueChange = { newThreshold = it },
                    placeholder = { Text("Enter new threshold") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val thresholdInt = newThreshold.toIntOrNull()
                    if (thresholdInt != null) {
                        prefs.edit { putInt("attempts", thresholdInt) }
                        Toast.makeText(context, "🔢 Threshold set to $thresholdInt", Toast.LENGTH_SHORT).show()
                        messages.add("Bot: Threshold updated to $thresholdInt")
                    } else {
                        Toast.makeText(context, "⚠️ Invalid number", Toast.LENGTH_SHORT).show()
                    }
                    newThreshold = ""
                    showThresholdDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { showThresholdDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Change Email") },
            text = {
                TextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    placeholder = { Text("Enter new email") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        prefs.edit { putString("email", newEmail) }
                        Toast.makeText(context, "📧 Email updated to $newEmail", Toast.LENGTH_SHORT).show()
                        messages.add("Bot: Email updated")
                    } else {
                        Toast.makeText(context, "⚠️ Invalid email", Toast.LENGTH_SHORT).show()
                    }
                    newEmail = ""
                    showEmailDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { showEmailDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ---------- Helper functions ----------
fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelPath)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
}

fun predictIntent(context: Context, text: String): String {
    val labels = listOf("change_email", "change_threshold", "disable_alerts", "enable_alerts", "other")

    val vocab = mutableMapOf<String, Int>()
    context.assets.open("vocab.txt").bufferedReader().useLines { lines ->
        var idx = 1
        lines.forEach { word -> vocab[word] = idx++ }
    }

    val maxLen = 20
    val tokens = text.lowercase().replace(Regex("[^\\w\\s]"), "").split(" ")
    val seq = IntArray(maxLen) { 0 }
    tokens.take(maxLen).forEachIndexed { i, token -> seq[i] = vocab[token] ?: 0 }

    val interpreter = Interpreter(loadModelFile(context, "intent_classifier.tflite"))
    val input = Array(1) { seq.map { it.toFloat() }.toFloatArray() }
    val output = Array(1) { FloatArray(labels.size) }
    interpreter.run(input, output)

    val probs = output[0]
    val maxIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
    return labels[maxIdx]
}

fun extractEmail(text: String, prefs: android.content.SharedPreferences): String? {
    val matcher = android.util.Patterns.EMAIL_ADDRESS.matcher(text)
    return if (matcher.find()) matcher.group() else null
}

fun extractThreshold(text: String, prefs: android.content.SharedPreferences): Int? {
    val numberRegex = Regex("\\d+")
    val match = numberRegex.find(text)
    return match?.value?.toIntOrNull()
}

