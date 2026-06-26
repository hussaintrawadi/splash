package com.splash.water.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

/** A full-screen confetti burst. Render it (e.g. inside a Box) while a celebration is active. */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier) {
    val colors = listOf(0xFF1FA2FF, 0xFF12D8FA, 0xFF38EF7D, 0xFFFFD166, 0xFFFF6B9D, 0xFF06D6A0)
        .map { it.toInt() }
    val party = Party(
        speed = 0f,
        maxSpeed = 32f,
        damping = 0.9f,
        spread = 360,
        colors = colors,
        emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(140),
        position = Position.Relative(0.5, 0.32),
    )
    KonfettiView(modifier = modifier.fillMaxSize(), parties = listOf(party))
}
