package com.example.focuslock.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun FadeInAnimation(
    visible: Boolean = true,
    initialAlpha: Float = 0f,
    targetAlpha: Float = 1f,
    durationMillis: Int = 300,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            initialAlpha = initialAlpha,
            animationSpec = tween(durationMillis = durationMillis)
        ),
        exit = fadeOut(
            targetAlpha = initialAlpha,
            animationSpec = tween(durationMillis = durationMillis)
        ),
        content = content
    )
}

@Composable
fun SlideInAnimation(
    visible: Boolean = true,
    durationMillis: Int = 300,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = durationMillis)
        ) + fadeIn(
            initialAlpha = 0f,
            animationSpec = tween(durationMillis = durationMillis)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = durationMillis)
        ) + fadeOut(
            targetAlpha = 0f,
            animationSpec = tween(durationMillis = durationMillis)
        ),
        content = content
    )
}

@Composable
fun PulseAnimation(
    pulseFraction: Float = 0.1f,
    durationMillis: Int = 1000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + pulseFraction,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

@Composable
fun RotateAnimation(
    durationMillis: Int = 2000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing)
        )
    )
    
    Box(
        modifier = Modifier.graphicsLayer {
            rotationZ = rotation
        }
    ) {
        content()
    }
}
