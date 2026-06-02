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
        text = "\"Sin música, la vida sería un error.\"",
        author = "FRIEDRICH NIETZSCHE"
    ),
    Quote(
        text = "\"La música es un ejercicio oculto de metafísica\nen el que el espíritu no sabe que está filosofando.\"",
        author = "ARTHUR SCHOPENHAUER"
    ),
    Quote(
        text = "\"La música expresa la esencia\nmás íntima del mundo.\"",
        author = "ARTHUR SCHOPENHAUER"
    ),
    Quote(
        text = "\"La música es una revelación más alta\nque toda sabiduría y filosofía.\"",
        author = "LUDWIG VAN BEETHOVEN"
    ),
    Quote(
        text = "\"La música es el espacio\nentre las notas.\"",
        author = "CLAUDE DEBUSSY"
    ),
    Quote(
        text = "\"La música es incapaz de expresar\nnada por sí misma.\"",
        author = "ÍGOR STRAVINSKI"
    ),
    Quote(
        text = "\"La música puede nombrar lo innombrable\ny comunicar lo desconocido.\"",
        author = "LEONARD BERNSTEIN"
    ),
    Quote(
        text = "\"La música expresa aquello que no puede decirse\ny sobre lo que es imposible permanecer en silencio.\"",
        author = "VÍCTOR HUGO"
    ),
    Quote(
        text = "\"La música es la taquigrafía\nde la emoción.\"",
        author = "LEV TOLSTÓI"
    ),
    Quote(
        text = "\"La música da alma al universo, alas a la mente,\nvuelo a la imaginación y vida a todo.\"",
        author = "PLATÓN"
    ),
    Quote(
        text = "\"Si quieres conocer el estado de una nación,\nescucha su música.\"",
        author = "CONFUCIO"
    ),
    Quote(
        text = "\"La arquitectura es música congelada.\"",
        author = "JOHANN WOLFGANG VON GOETHE"
    ),
    Quote(
        text = "\"Donde termina el poder de las palabras\ncomienza el de la música.\"",
        author = "RICHARD WAGNER"
    ),
    Quote(
        text = "\"La música crea orden\na partir del caos.\"",
        author = "YEHUDI MENUHIN"
    ),
    Quote(
        text = "\"Todo lo que hacemos\nes música.\"",
        author = "JOHN CAGE"
    ),
    Quote(
        text = "\"La música no es complicada;\nlas personas son complicadas.\"",
        author = "THELONIOUS MONK"
    ),
    Quote(
        text = "\"La música es una herramienta\npara dar forma al tiempo.\"",
        author = "BJÖRK"
    ),
    Quote(
        text = "\"Hablar sobre música es como\nbailar sobre arquitectura.\"",
        author = "FRANK ZAPPA"
    ),
    Quote(
        text = "\"No toques lo que está ahí;\ntoca lo que no está ahí.\"",
        author = "MILES DAVIS"
    ),
    Quote(
        text = "\"La música es todo aquello que uno escucha\ncon la intención de escuchar música.\"",
        author = "BRIAN ENO"
    ),
)
