package com.jaycefr.stepshadower.screens

//@file:Suppress("MagicNumber")

//package com.example.lockorbits

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * A premium-looking lock with two tilted orbit rings and orbiting dots that
 * pass *behind* and *in front of* the lock to create a subtle 3D illusion.
 */
@Composable
fun LockWith3DOrbits(
    modifier: Modifier = Modifier,
    locked: Boolean = true,
    lockColor: Color = Color(0xFFF4F4F4), // light body for white bg
    accent: Color = Color(0xFF3B82F6),    // blue orbit
    accent2: Color = Color(0xFF8B5CF6),   // purple orbit
    lockSize: Dp = 260.dp
) {
    val infinite = rememberInfiniteTransition(label = "orbits")
    val t1 by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "theta1"
    )
    val t2 by infinite.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "theta2"
    )

    Box(
        modifier
            .size(lockSize)
            .background(Color.White) // white background
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val minDim = min(size.width, size.height)
            val cx = size.width / 2f
            val cy = size.height / 2f

            // Lock sizing
            val bodyW = minDim * 0.40f
            val bodyH = minDim * 0.38f
            val shackleOuterR = bodyW * 0.60f
            val shackleThickness = bodyW * 0.14f

            // Orbit ovals (semi-axes)
            val rx1 = minDim * 0.46f
            val ry1 = minDim * 0.18f
            val rx2 = minDim * 0.36f
            val ry2 = minDim * 0.12f

            // Tilt angles
            val tilt1 = -22f
            val tilt2 = 18f

            // --- BACK half of orbits ---
            drawOrbitHalf(cx, cy, rx1, ry1, tilt1, isFront = false, color = accent.copy(alpha = 0.4f), minDim)
            drawOrbitHalf(cx, cy, rx2, ry2, tilt2, isFront = false, color = accent2.copy(alpha = 0.4f), minDim)

            // Orbiting dots behind lock
            drawOrbitDotIfHalf(cx, cy, rx1, ry1, tilt1, t1, accent, isFront = false, minDim)
            drawOrbitDotIfHalf(cx, cy, rx2, ry2, tilt2, t2, accent2, isFront = false, minDim)

            val glowRadius = minDim * 0.55f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = glowRadius
                ),
                center = Offset(cx, cy),
                radius = glowRadius
            )

            // --- LOCK ---
            drawLock(cx, cy, bodyW, bodyH, shackleOuterR, shackleThickness, lockColor, locked)

            // --- FRONT half of orbits ---
            drawOrbitHalf(cx, cy, rx1, ry1, tilt1, isFront = true, color = accent, minDim)
            drawOrbitHalf(cx, cy, rx2, ry2, tilt2, isFront = true, color = accent2, minDim)

            // Orbiting dots in front
            drawOrbitDotIfHalf(cx, cy, rx1, ry1, tilt1, t1, accent, isFront = true, minDim)
            drawOrbitDotIfHalf(cx, cy, rx2, ry2, tilt2, t2, accent2, isFront = true, minDim)
        }
    }
}


private fun angleOnBackHalf(angleDeg: Float): Boolean {
    val a = ((angleDeg % 360f) + 360f) % 360f
    return a in 180f..360f
}

private fun pointOnEllipse(
    angleDeg: Float,
    center: Offset,
    rx: Float,
    ry: Float,
    rotationDeg: Float
): Offset {
    val a = Math.toRadians(angleDeg.toDouble())
    val x = rx * cos(a).toFloat()
    val y = ry * sin(a).toFloat()
    val r = Math.toRadians(rotationDeg.toDouble())
    val xr = x * cos(r).toFloat() - y * sin(r).toFloat()
    val yr = x * sin(r).toFloat() + y * cos(r).toFloat()
    return Offset(center.x + xr, center.y + yr)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrbitHalf(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    tilt: Float,
    isFront: Boolean,
    color: Color,
    minDim: Float
) {
    val oval = Rect(cx - rx, cy - ry, cx + rx, cy + ry)
    withTransform({ rotate(tilt, pivot = Offset(cx, cy)) }) {
        val start = if (isFront) 0f else 180f
        val sweep = 180f
        drawArc(
            color = color,
            startAngle = start,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = min(minDim * 0.01f, 6f), cap = StrokeCap.Round),
            topLeft = Offset(oval.left, oval.top),
            size = oval.size
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrbitDotIfHalf(
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    tilt: Float,
    angleDeg: Float,
    color: Color,
    isFront: Boolean,
    minDim: Float
) {
    val onBack = angleOnBackHalf(angleDeg)
    if ((isFront && !onBack) || (!isFront && onBack)) {
        val p = pointOnEllipse(angleDeg, Offset(cx, cy), rx, ry, tilt)
        val r = min(minDim * 0.016f, 8f)
        val glow = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.45f), Color.Transparent),
            center = p,
            radius = r * 3.2f
        )
        drawCircle(brush = glow, radius = r * 3.2f, center = p)
        drawCircle(color = Color.White.copy(alpha = 0.9f), radius = r * 0.60f, center = p)
        drawCircle(color = color, radius = r, center = p, style = Stroke(width = r * 0.65f))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLock(
    cx: Float,
    cy: Float,
    bodyW: Float,
    bodyH: Float,
    shackleOuterR: Float,
    shackleThickness: Float,
    lockColor: Color,
    locked: Boolean
) {
    val bodyRect = Rect(
        left = cx - bodyW / 2f,
        top = cy - bodyH / 2f + shackleOuterR * 0.25f,
        right = cx + bodyW / 2f,
        bottom = cy + bodyH / 2f + shackleOuterR * 0.25f
    )

    val bodyPath = Path().apply {
        val r = bodyW * 0.16f
        addRoundRect(androidx.compose.ui.geometry.RoundRect(bodyRect, r, r))
    }

    val bodyBrush = Brush.verticalGradient(
        listOf(lockColor.copy(alpha = 0.95f), lockColor.copy(alpha = 0.8f))
    )
    drawPath(bodyPath, brush = bodyBrush)

    drawPath(
        bodyPath,
        color = Color.Black.copy(alpha = 0.08f),
        style = Stroke(width = bodyW * 0.03f)
    )

    val outer = Rect(
        cx - shackleOuterR,
        cy - shackleOuterR - bodyH * 0.10f,
        cx + shackleOuterR,
        cy + shackleOuterR - bodyH * 0.10f
    )
    val strokeW = shackleThickness

    if (locked) {
        // closed shackle
        drawArc(
            color = Color.DarkGray,
            startAngle = 210f,
            sweepAngle = 120f,
            useCenter = false,
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
            topLeft = Offset(outer.left, outer.top),
            size = outer.size
        )
    } else {
        // unlocked shackle: rotate slightly left + lifted
        withTransform({
            translate(left = -bodyW * 0.25f, top = -bodyH * 0.2f)
            rotate(degrees = -25f, pivot = Offset(cx, cy))
        }) {
            drawArc(
                color = Color.DarkGray,
                startAngle = 210f,
                sweepAngle = 120f,
                useCenter = false,
                style = Stroke(width = strokeW, cap = StrokeCap.Round),
                topLeft = Offset(outer.left, outer.top),
                size = outer.size
            )
        }
    }

    // keyhole (always)
    val keyholeY = bodyRect.top + bodyH * 0.42f
    val keyholeR = bodyW * 0.075f
    drawCircle(color = Color.Black.copy(alpha = 0.15f), radius = keyholeR * 1.15f, center = Offset(cx, keyholeY + keyholeR * 0.1f))
    drawCircle(color = Color.Black, radius = keyholeR, center = Offset(cx, keyholeY))
    val stemH = bodyH * 0.20f
    val stemW = keyholeR * 0.9f
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(cx - stemW / 2f, keyholeY),
        size = androidx.compose.ui.geometry.Size(stemW, stemH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(stemW / 2f, stemW / 2f)
    )
}


@Preview
@Composable
private fun LockWith3DOrbitsPreview() {
    MaterialTheme {
        Surface(color = Color(0xFF0b1220)) {
            LockWith3DOrbits(
                modifier = Modifier.fillMaxSize(),
                lockSize = 320.dp,
                locked = true
            )
        }
    }
}

