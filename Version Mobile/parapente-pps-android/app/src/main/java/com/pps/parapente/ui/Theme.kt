package com.pps.parapente.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Même palette bleu / blanc / orange que la version PC
val Bleu = Color(0xFF1B3A5C)
val Orange = Color(0xFFD9480F)
val Fond = Color(0xFFF4F6F8)
val Rouge = Color(0xFFC0392B)
val Vert = Color(0xFF27AE60)
val Gris = Color(0xFF7F8C8D)

@Composable
fun PpsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Bleu, onPrimary = Color.White,
            secondary = Orange, onSecondary = Color.White,
            background = Fond, surface = Color.White, error = Rouge
        ),
        content = content
    )
}
