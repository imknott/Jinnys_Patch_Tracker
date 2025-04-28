package com.example.jinnyspatchtracker.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.jinnyspatchtracker.R
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun SplashScreen(onSplashFinished: (() -> Unit)? = null) {
    // Automatically trigger callback after 6 seconds
    LaunchedEffect(Unit) {
        delay(100000L)
        onSplashFinished?.invoke()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F7E6)),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.turtle),
                contentDescription = "Turtle Logo",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit
            )
            CircularText("Jinny's Patch Tracker")
        }
    }
}


@Composable
fun CircularText(text: String) {
    val radius = 140.dp
    val angleStep = 360f / text.length
    val animatedVisibleCount = remember { Animatable(0f) }

    // Animate drawing characters one by one
    LaunchedEffect(Unit) {
        animatedVisibleCount.animateTo(
            targetValue = text.length.toFloat(),
            animationSpec = tween(durationMillis = 4000)
        )
    }

    // Pulsing effect
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val density = LocalDensity.current

    Box(
        modifier = Modifier.size(radius * 2),
        contentAlignment = Alignment.Center
    ) {
        val visibleCount = animatedVisibleCount.value.toInt()

        // Access pixel value inside Composable scope
        with(density) {
            val centerOffset = radius.toPx()

            for (i in 0 until visibleCount) {
                val angleDegrees = i * angleStep - 90f
                val angleRad = Math.toRadians(angleDegrees.toDouble())

                val x = cos(angleRad) * centerOffset
                val y = sin(angleRad) * centerOffset

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = x.toInt(),
                                y = y.toInt()
                            )
                        }
                        .graphicsLayer(
                            scaleX = pulseScale,
                            scaleY = pulseScale
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text[i].toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C5F2D),
                        modifier = Modifier.rotate(angleDegrees.toFloat() + 90f)
                    )
                }
            }
        }
    }
}


