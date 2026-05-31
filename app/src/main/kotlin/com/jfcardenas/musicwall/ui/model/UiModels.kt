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

data class WallItem(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val gradientStart: Color = Color(0xFF1A0A2E),
    val gradientEnd: Color   = Color(0xFF0A0A18),
)

data class Quote(
    val text: String,
    val author: String,
)

val STYLE_OPTIONS = listOf(
    StyleOption(
        id            = "scene_woodstock",
        name          = "Woodstock",
        tagline       = "3 días de paz y música.\nTus portadas en el cartel.",
        gradientStart = Color(0xFF2A1A08),
        gradientEnd   = Color(0xFF120C04),
    ),
)

// Experimentos pendientes de pulir — se moverán a STYLE_OPTIONS cuando estén listos
val STYLE_OPTIONS_INACTIVE = listOf(
    StyleOption(
        id            = "scene_bano_bar_lima",
        name          = "Baño Bar Lima",
        tagline       = "Tus portadas colgadas\nen las paredes de un bar de Lima",
        gradientStart = Color(0xFF2A1406),
        gradientEnd   = Color(0xFF0E0704),
    ),
    StyleOption(
        id            = "scene_manchester",
        name          = "Manchester Wall",
        tagline       = "Tu música grabada en la pared.\nSolo código, cero imágenes.",
        gradientStart = Color(0xFF1A0C07),
        gradientEnd   = Color(0xFF0A0604),
    ),
    StyleOption(
        id            = "scene_britrock",
        name          = "Rocknrolla",
        tagline       = "Tu música en la pared\nde un adolescente en Manchester",
        gradientStart = Color(0xFF1A0A04),
        gradientEnd   = Color(0xFF0A0602),
    ),
    StyleOption(
        id            = "mosaic",
        name          = "Mosaico",
        tagline       = "25 portadas fusionadas,\ncapas y desenfoque",
        gradientStart = Color(0xFF1A0A2E),
        gradientEnd   = Color(0xFF0A0A18),
    ),
    StyleOption(
        id            = "puzzle",
        name          = "Rompecabezas",
        tagline       = "12 portadas encajadas,\ncada pieza en su lugar",
        gradientStart = Color(0xFF0D1A0D),
        gradientEnd   = Color(0xFF080D08),
    ),
    StyleOption(
        id            = "psychedelic",
        name          = "Psicodélico",
        tagline       = "Grilla 3×5, tonos alterados,\nla esencia sin filtro",
        gradientStart = Color(0xFF2A0A2A),
        gradientEnd   = Color(0xFF0A0514),
    ),
)

// Agrega aquí tus imágenes generadas con IA.
// imageUrl puede ser una URL remota (HTTPS) o null para mostrar el gradiente placeholder.
val CURATED_WALLS: List<WallItem> = listOf(
    // WallItem(id = "wall_01", title = "Nombre", description = "Descripción", imageUrl = "https://…"),
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
