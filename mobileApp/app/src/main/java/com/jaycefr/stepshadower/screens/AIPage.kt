package com.jaycefr.stepshadower.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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

    // dialog states
    var showThresholdDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }

    // dialog inputs
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
                    messages.add("You: $message")

                    val label = predictIntent(context, message)
                    messages.add("Bot: $label")
                    message = ""

                    when (label) {
                        "change_threshold" -> showThresholdDialog = true
                        "change_email" -> showEmailDialog = true
                        "enable_alerts" -> {
                            prefs.edit { putBoolean("activated", true) }
                            Toast.makeText(context, "✅ Alerts enabled", Toast.LENGTH_SHORT).show()
                            messages.add("Bot: Alerts enabled")
                        }
                        "disable_alerts" -> {
                            prefs.edit { putBoolean("activated", false) }
                            Toast.makeText(context, "🚫 Alerts disabled", Toast.LENGTH_SHORT).show()
                            messages.add("Bot: Alerts disabled")
                        }
                        else -> Toast.makeText(context, "Predicted: $label", Toast.LENGTH_SHORT).show()
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
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showThresholdDialog = false }) {
                    Text("Cancel")
                }
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
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        prefs.edit { putString("email", newEmail) }
                        Toast.makeText(context, "📧 Email updated to $newEmail", Toast.LENGTH_SHORT).show()
                        messages.add("Bot: Email updated")
                    } else {
                        Toast.makeText(context, "⚠️ Invalid email", Toast.LENGTH_SHORT).show()
                    }
                    newEmail = ""
                    showEmailDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showEmailDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

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
    tokens.take(maxLen).forEachIndexed { i, token ->
        seq[i] = vocab[token] ?: 0
    }

    val interpreter = Interpreter(loadModelFile(context, "intent_classifier.tflite"))

    val input = Array(1) { seq.map { it.toFloat() }.toFloatArray() }
    val output = Array(1) { FloatArray(labels.size) }
    interpreter.run(input, output)

    val probs = output[0]
    val maxIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
    return labels[maxIdx]
}
