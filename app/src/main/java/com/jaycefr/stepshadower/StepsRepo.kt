package com.jaycefr.stepshadower

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.fitness.FitnessLocal
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.request.LocalDataReadRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.resumeWithException

// Make sure to check for permissions before creating an instance.
@RequiresApi(Build.VERSION_CODES.Q)
class StepsRepo(private val appContext : Context){
    private val client = FitnessLocal.getLocalRecordingClient(appContext)

    fun ensureSubscription() {
        if (
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            client.subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener { Log.d("StepsRepo", "Subscription OK") }
                .addOnFailureListener { e -> Log.w("StepsRepo", "Subscription failed", e) }
        } else {
            Log.w("StepsRepo", "ACTIVITY_RECOGNITION permission NOT granted")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun todaySteps() : Int{
        val zone  = ZoneId.systemDefault()
        val start = LocalDate.now(zone).atStartOfDay(zone).toEpochSecond()
        val end   = Instant.now().epochSecond

        val request = LocalDataReadRequest.Builder()
            .read(LocalDataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(start, end, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return suspendCancellableCoroutine { cont ->
            client.readData(request)
                .addOnSuccessListener { r ->
                    val total = r.dataSets.sumOf { ds ->
                        ds.dataPoints.sumOf { dp ->
                            dp.getValue(dp.dataType.fields[0]).asInt()
                        }
                    }
                    cont.resume(total) {}
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }

    }

}