package com.signalzero.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.core.Localization
import com.signalzero.app.ui.theme.EmergencyRed
import com.signalzero.app.ui.theme.EmergencyRedGlow
import kotlinx.coroutines.delay

@Composable
fun SosButton(
    language: AppLanguage,
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    // Pulsing animation for emergency standby
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Progress counter when holding
    LaunchedEffect(isPressed) {
        if (isPressed) {
            val stepTime = 15L
            val totalTime = 1500L
            val increments = totalTime / stepTime
            for (i in 1..increments) {
                if (!isPressed) break
                delay(stepTime)
                progress = i.toFloat() / increments.toFloat()
                if (i % 20 == 0L) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
            }
            if (isPressed && progress >= 0.99f) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onTrigger()
                progress = 0f
                isPressed = false
            }
        } else {
            progress = 0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(50),
        label = "progress"
    )

    Box(
        modifier = modifier
            .size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Radiant Glow
        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(pulseScale)
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    spotColor = EmergencyRed,
                    ambientColor = EmergencyRedGlow
                )
        )

        // Progress Arc Canvas
        Canvas(modifier = Modifier.size(200.dp)) {
            // Background track
            drawCircle(
                color = Color(0x33FF3B30),
                radius = size.minDimension / 2 - 8.dp.toPx(),
                style = Stroke(width = 6.dp.toPx())
            )

            // Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(EmergencyRed, Color(0xFFFF8A80), EmergencyRed)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Central Red Button
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF5252),
                            EmergencyRed,
                            Color(0xFFB71C1C)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Localization.get("sos", language),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Localization.get("hold_to_sos", language),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
