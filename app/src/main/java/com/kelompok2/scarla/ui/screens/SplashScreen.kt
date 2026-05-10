package com.kelompok2.scarla.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kelompok2.scarla.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    val logoAlpha = remember { Animatable(0f) }
    val maskotScale = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {

        maskotScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )

        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )

        delay(1000)

        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.maskot_splash),
                contentDescription = "Maskot",
                modifier = Modifier
                    .size(180.dp)
                    .scale(maskotScale.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.font_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(40.dp)
                    .alpha(logoAlpha.value)
            )
        }
    }
}