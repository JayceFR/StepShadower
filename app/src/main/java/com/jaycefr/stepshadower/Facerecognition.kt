package com.jaycefr.stepshadower

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import android.graphics.BitmapFactory
import java.io.File

fun analyzeFaceSummary(photoFile: File, onResult: (String) -> Unit) {
    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
    val image = InputImage.fromBitmap(bitmap, 0)

    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .enableTracking()
        .build()

    val detector = FaceDetection.getClient(options)

    detector.process(image)
        .addOnSuccessListener { faces ->
            if (faces.isEmpty()) {
                onResult("No face detected in the photo.")
                return@addOnSuccessListener
            }

            val summaries = faces.mapIndexed { i, face ->
                val smileProb = face.smilingProbability ?: -1f
                val rightEyeProb = face.rightEyeOpenProbability ?: -1f
                val leftEyeProb = face.leftEyeOpenProbability ?: -1f

                buildString {
                    append("Face ${i+1}: ")
                    if (smileProb >= 0) {
                        append(if (smileProb > 0.6) "Smiling, " else "Not smiling, ")
                    }
                    if (rightEyeProb >= 0 && leftEyeProb >= 0) {
                        append(if (rightEyeProb > 0.5 && leftEyeProb > 0.5) "Eyes open, " else "Eyes closed/partially closed, ")
                    }
                }.trimEnd(',', ' ')
            }

            onResult(summaries.joinToString("\n"))
        }
        .addOnFailureListener { e ->
            onResult("Face analysis failed: ${e.message}")
        }
}
