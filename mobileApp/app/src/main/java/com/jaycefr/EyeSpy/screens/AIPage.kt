package com.jaycefr.EyeSpy.screens

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.jaycefr.EyeSpy.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import com.jaycefr.EyeSpy.R



@Composable
fun AIPage() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)

    var message by remember { mutableStateOf("") }
    var messages = remember { mutableStateListOf<Pair<String, Boolean>>() } // Pair(msg, isUser)

    // Friendly welcome
    LaunchedEffect(Unit) {
        messages.add(
            "Hi there! 😄 I'm your personal assistant. I can help you change your email 📧, adjust thresholds 🔢, enable/disable alerts ✅🚫, or just chat with you. Ask me anything!" to false
        )
    }

    // Dialog states
    var showThresholdDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showClearData by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }

    // Dialog inputs
    var newThreshold by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { (msg, isUser) ->
                ChatBubble(msg, isUser)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("Ask me something...") },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    errorContainerColor = Color.White
                )

            )
            IconButton(
                onClick = {
                    if (message.isNotBlank()) {
                        val currentMessage = message
                        messages.add(currentMessage to true)
                        message = ""

                        val label = predictIntent(context, currentMessage)

                        val extractedEmail = extractEmail(currentMessage, prefs)
                            ?: prefs.getString("email", "")!!
                        val extractedThreshold = extractThreshold(currentMessage, prefs)
                            ?: prefs.getInt("attempts", 3)

                        when (label) {
                            "change_threshold" -> {
                                messages.add("Got it! Let's adjust your threshold 🔢." to false)
                                newThreshold = extractedThreshold.toString()
                                showThresholdDialog = true
                            }
                            "change_email" -> {
                                messages.add("Sure! Let's update your email 📧." to false)
                                newEmail = extractedEmail
                                showEmailDialog = true
                            }
                            "enable_alerts" -> {
                                prefs.edit { putBoolean("activated", true) }
                                messages.add("✅ Alerts are now enabled. Stay safe!" to false)
                            }
                            "disable_alerts" -> {
                                prefs.edit { putBoolean("activated", false) }
                                messages.add("🚫 Alerts are now disabled." to false)
                            }
                            "clear_intruder_data" -> {
                                showClearData = true
                                messages.add("Ok lets delete all the intruders data." to false)
                            }
                            "delete_account" -> {
                                showDeleteAccount = true
                                messages.add("Ok lets delete your account." to false)
                            }
                            "other" -> {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val aiReply = getAIResponse(currentMessage)
                                    withContext(Dispatchers.Main) {
                                        messages.add(aiReply to false)
                                    }
                                }
                            }
                        }
                    }
                }
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showClearData){
        AlertDialog(
            onDismissRequest = { showClearData = false },
            title = { Text("Clear intruder data") },
            text = {
                Text("Are you sure you want to clear all intruder data?")
            },
            confirmButton = {
                Button(onClick = {
                    clearIntruderPhotos(context)
                    showClearData = false
                    messages.add("I have cleared all intruder data successfully" to false)
                }) { Text("Confirm") }
            },
            dismissButton = {
                Button(onClick = { showClearData = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteAccount){
        AlertDialog(
            onDismissRequest = { showDeleteAccount = false },
            title = { Text("Delete my account") },
            text = {
                Text("Are you sure you want to delete your account?")
            },
            confirmButton = {
                Button(onClick = {
                    deleteAccount(context)
                    showDeleteAccount = false
                    messages.add("I have deleted your account successfully" to false)
                }) { Text("Confirm") }
            },
            dismissButton = {
                Button(onClick = { showDeleteAccount = false }) { Text("Cancel") }
            }
        )
    }

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
                        messages.add("I have updated your threshold to $thresholdInt successfully" to false)
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
                        messages.add("I have updated your email to $newEmail successfully" to false)
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

@Composable
fun ChatBubble(message: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.chatbot),
                contentDescription = "Bot",
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 4.dp)
                    .background(Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isUser) MaterialTheme.colorScheme.primary else Color.White,
            shadowElevation = 2.dp,
            tonalElevation = 2.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message,
                color = if (isUser) Color.White else Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}


// ---------- Helper functions ----------
fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelPath)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
}

fun predictIntent(context: Context, text: String, minConfidence: Float = 0.6f): String {
    val labels = listOf("change_email", "change_threshold", "disable_alerts", "enable_alerts", "clear_intruder_data", "delete_account", "other")
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
    val maxProb = probs[maxIdx]

    // 👇 Fallback to "other" if confidence too low
    return if (maxProb >= minConfidence) labels[maxIdx] else "other"
}

fun extractEmail(text: String, prefs: android.content.SharedPreferences): String? {
    val matcher = Patterns.EMAIL_ADDRESS.matcher(text)
    return if (matcher.find()) matcher.group() else null
}

fun extractThreshold(text: String, prefs: android.content.SharedPreferences): Int? {
    val numberRegex = Regex("\\d+")
    val match = numberRegex.find(text)
    return match?.value?.toIntOrNull()
}

// ---------- OpenRouter API call ----------
suspend fun getAIResponse(prompt: String): String {
    val apiKey = BuildConfig.API_KEY
    val url = "https://openrouter.ai/api/v1/chat/completions"
    val body = """
        {
          "model": "openai/gpt-oss-120b:free", 
          "messages": [
            {"role": "system", "content": "You are a friendly and helpful AI assistant for a mobile security app. You help the user manage emails, alert thresholds, and security alerts. Always respond clearly and kindly, even if the question is outside security."},
            {"role": "user", "content": "$prompt"}
          ],
          "max_tokens": 200
        }
    """.trimIndent()

    val client = OkHttpClient()
    val requestBody = body.toRequestBody("application/json; charset=utf-8".toMediaType())
    val request = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer $apiKey")
        .post(requestBody)
        .build()

    val response = client.newCall(request).execute()
    val jsonString = response.body?.string() ?: return "⚠️ No response from server"

    val json = JSONObject(jsonString)

    // Handle API errors gracefully
    if (json.has("error")) {
        val errorMsg = json.getJSONObject("error").optString("message", "Unknown error")
        return "⚠️ API Error: $errorMsg"
    }

    if (!json.has("choices")) {
        return "⚠️ Unexpected API response: $jsonString"
    }

    val aiText = json
        .getJSONArray("choices")
        .getJSONObject(0)
        .getJSONObject("message")
        .getString("content")

    return aiText.trim()
}

