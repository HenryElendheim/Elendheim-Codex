package com.elendheim.codex.ui.theme

import androidx.compose.ui.graphics.Color

// The Elendheim palette. Dark gray with a soft red accent, which already reads like
// a classified file. These are the base tokens the theme wires into Material 3.
val CodexBlack = Color(0xFF0E0E10)      // app background
val CodexSurface = Color(0xFF16161A)    // cards and bars
val CodexSurfaceHigh = Color(0xFF1E1E24) // raised rows and inputs
val CodexOutline = Color(0xFF2C2C34)    // hairline borders

val CodexRed = Color(0xFFC4383A)        // the accent, used sparingly
val CodexRedDim = Color(0xFF7E2A2B)     // pressed and muted accent

val CodexText = Color(0xFFE8E8EA)       // main text
val CodexTextMuted = Color(0xFF9A9AA2)  // labels and secondary text
val CodexTextFaint = Color(0xFF6A6A72)  // hints and disabled

// A light set is kept minimal because the app is dark first by design. It stays
// usable if the system is in light mode but the dark scheme is the intended look.
val LightBackground = Color(0xFFF3F3F5)
val LightSurface = Color(0xFFFFFFFF)
val LightText = Color(0xFF141416)
