package com.jaycefr.stepshadower

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.fitness.FitnessLocal
import com.google.android.gms.fitness.data.LocalDataSet
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.request.LocalDataReadRequest
import com.jaycefr.stepshadower.ui.theme.StepShadowerTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    fun dumpDataSet(dataSet: LocalDataSet) {
        Log.i("MainActivity", "Data returned for Data type: ${dataSet.dataType.name}")
        for (dp in dataSet.dataPoints) {
            Log.i("MainActivity","Data point:")
            Log.i("MainActivity","\tType: ${dp.dataType.name}")
            Log.i("MainActivity","\tStart: ${dp.getStartTime(TimeUnit.HOURS)}")
            Log.i("MainActivity","\tEnd: ${dp.getEndTime(TimeUnit.HOURS)}")
            for (field in dp.dataType.fields) {
                Log.i("MainActivity","\tLocalField: ${field.name.toString()} LocalValue: ${dp.getValue(field)}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Ask for permission
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){
            isGranted ->
            if (isGranted){
                Log.d("MainActivity", "Permission granted");
            }
            else{
                Log.d("MainActivity", "Permission denied");
            }
        }
//        when{
//            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED -> {
//                Log.d("MainActivity", "Permission granted");
//                // Subscribe to steps data
//                val localRecordingClient = FitnessLocal.getLocalRecordingClient(this)
//                localRecordingClient.subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA)
//                    .addOnSuccessListener {
//                        Log.d("MainActivity", "Subscribed to steps data");
//                    }
//                    .addOnFailureListener {
//                        Log.w("MainActivity", "Failed to subscribe to steps data");
//                    }
//            }
//            else -> {
//                requestPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
//            }
//        }
        val repo = StepsRepo(this)
        val viewModel = StepViewModel(repo)
        lifecycleScope.launch {
            viewModel.refresh()
        }
        setContent {
            Column(
                modifier = Modifier.fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Greeting(name = "Jayce", modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(text = "Steps taken today = ${viewModel.steps.collectAsState().value}")
                Button(onClick = {lifecycleScope.launch { viewModel.refresh() }}) { Text("Refresh") }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StepShadowerTheme {
        Greeting("Android")
    }
}