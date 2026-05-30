package com.jfcardenas.musicwall.ui.model

import androidx.compose.ui.graphics.Color
import com.jfcardenas.musicwall.ui.theme.Purple

data class StyleOption(
    val id: String,
    val name: String,
    val tagline: String,
    val gradientStart: Color,
    val gradientEnd: Color,
)

data class Quote(
    val text: String,
    val author: String,
)

val STYLE_OPTIONS = listOf(
    StyleOption(
        id = "street",
        name = "Street Poster",
        tagline = "Caótico, urbano,\ncon textura real",
        gradientStart = Color(0xFF5C2A0A),
        gradientEnd   = Color(0xFF1A0A04),
    ),
    StyleOption(
        id = "cinematic",
        name = "Cinematic",
        tagline = "Oscuro, capas,\nluz y sombra",
        gradientStart = Color(0xFF0A0A3A),
        gradientEnd   = Color(0xFF05051A),
    ),
    StyleOption(
        id = "album",
        name = "Album Wall",
        tagline = "Mosaico de portadas,\njerarquía visual",
        gradientStart = Color(0xFF1A1A22),
        gradientEnd   = Color(0xFF0A0A0E),
    ),
    StyleOption(
        id = "ecosystem",
        name = "Ecosystem",
        tagline = "Arte generativo,\ngeometría y flujo",
        gradientStart = Color(0xFF0A1A3A),
        gradientEnd   = Color(0xFF1A0A2E),
    ),
    StyleOption(
        id = "physical",
        name = "Physical",
        tagline = "Objetos reales,\ntextura, imperfección",
        gradientStart = Color(0xFF2A1C0A),
        gradientEnd   = Color(0xFF100A04),
    ),
)

val LOADING_QUOTES = listOf(
    Quote(
        text = "\"La música es el arte más directo,\nentra por el oído y va al corazón.\"",
        author = "MAGDALENA MARTÍNEZ"
    ),
    Quote(
        text = "\"No busques encontrarte en la música.\nPermite que la música te encuentre a ti.\"",
        author = "RUMI"
    ),
    Quote(
        text = "\"Sin música, la vida sería un error.\"",
        author = "NIETZSCHE"
    ),
    Quote(
        text = "\"Donde termina el lenguaje,\ncomienza la música.\"",
        author = "E.T.A. HOFFMANN"
    ),
    Quote(
        text = "\"Hay una grieta en todo.\nAsí es como entra la luz.\"",
        author = "LEONARD COHEN"
    ),
    Quote(
        text = "\"El alma se tiñe del color\nde sus pensamientos.\"",
        author = "MARCO AURELIO"
    ),
)
