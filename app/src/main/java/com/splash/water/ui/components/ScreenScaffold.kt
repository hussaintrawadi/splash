package com.splash.water.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.splash.water.ui.theme.LocalAppGradient

/** Shared screen background with a playful vertical gradient and a title header. */
@Composable
fun GradientScreen(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val gradient = LocalAppGradient.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradient.top, gradient.bottom))),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}
