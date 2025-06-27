package com.jaycefr.stepshadower

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.TimeUnit
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
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
class StepsRepo(appContext : Context){
    private val client = FitnessLocal.getLocalRecordingClient(appContext)

    init {
        // check for permissions and subscribe
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("StepsRepo", "No permission granted")
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        client.subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
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