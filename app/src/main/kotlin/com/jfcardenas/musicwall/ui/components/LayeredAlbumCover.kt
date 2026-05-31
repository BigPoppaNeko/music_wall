package com.jfcardenas.musicwall.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jfcardenas.musicwall.ui.theme.*

enum class AlbumFrameStyle(val label: String) {
    NONE("Sin marco"),
    METAL("Metal"),
    NEON("Neón"),
    VINTAGE("Vintage"),
}

/**
 * Capas de Vinilo — sistema de 3 capas para el arte de álbum:
 *   Capa 0: sombra drop-shadow via Modifier.shadow()
 *   Capa 1: imagen de portada (AsyncImage)
 *   Capa 2: marco decorativo Canvas según [frameStyle]
 */
@Composable
fun LayeredAlbumCover(
    imageUrl: String?,
    frameStyle: AlbumFrameStyle = AlbumFrameStyle.NONE,
    cornerRadius: Dp = 10.dp,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        // Capa 0: sombra
        modifier = modifier.shadow(elevation = 10.dp, shape = shape),
    ) {
        // Capa 1: portada
        if (imageUrl != null) {
            AsyncImage(
                model              = imageUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF2D1B69), Color(0xFF1A0A2E)))),
                contentAlignment = Alignment.Center,
            ) {
                Text("♫", color = Purple, fontSize = 28.sp)
            }
        }

        // Capa 2: marco
        if (frameStyle != AlbumFrameStyle.NONE) {
            Canvas(Modifier.fillMaxSize()) {
                val cr = cornerRadius.toPx()
                when (frameStyle) {
                    AlbumFrameStyle.METAL   -> drawFrameMetal(cr)
                    AlbumFrameStyle.NEON    -> drawFrameNeon(cr)
                    AlbumFrameStyle.VINTAGE -> drawFrameVintage(cr)
                    AlbumFrameStyle.NONE    -> {}
                }
            }
        }
    }
}

// ── Selector de marcos ────────────────────────────────────────────────────────

@Composable
fun FrameSelector(
    selected: AlbumFrameStyle,
    imageUrl: String?,
    onSelect: (AlbumFrameStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        AlbumFrameStyle.values().forEach { style ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onSelect(style) }
                    .padding(vertical = 4.dp),
            ) {
                LayeredAlbumCover(
                    imageUrl     = imageUrl,
                    frameStyle   = style,
                    cornerRadius = 8.dp,
                    modifier     = Modifier
                        .size(52.dp)
                        .then(
                            if (selected == style)
                                Modifier.border(2.dp, Purple, RoundedCornerShape(8.dp))
                            else Modifier,
                        ),
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text       = style.label,
                    fontSize   = 9.sp,
                    color      = if (selected == style) TextPrimary else TextMuted,
                    fontWeight = if (selected == style) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign  = TextAlign.Center,
                )
            }
        }
    }
}

// ── Marco 1: Metal — Cuero & Tachuelas ────────────────────────────────────────

private fun DrawScope.drawFrameMetal(cr: Float) {
    val borderW = 5.5.dp.toPx()

    // Borde cuero oscuro
    drawRoundRect(
        color        = Color(0xFF4E342E),
        topLeft      = Offset.Zero,
        size         = size,
        cornerRadius = CornerRadius(cr),
        style        = Stroke(borderW),
    )
    // Línea interior metálica
    drawRoundRect(
        color        = Color(0xFF8D6E63).copy(alpha = 0.65f),
        topLeft      = Offset(1.5.dp.toPx(), 1.5.dp.toPx()),
        size         = Size(size.width - 3.dp.toPx(), size.height - 3.dp.toPx()),
        cornerRadius = CornerRadius((cr - 1.5.dp.toPx()).coerceAtLeast(0f)),
        style        = Stroke(1.dp.toPx()),
    )

    // Tachuelas de metal
    val studR   = 2.7.dp.toPx()
    val edge    = 2.8.dp.toPx()
    val spacing = 15.dp.toPx()
    val y1      = edge + studR
    val y2      = size.height - edge - studR
    val x1      = edge + studR
    val x2      = size.width - edge - studR

    var x = spacing
    while (x < size.width - spacing * 0.5f) {
        listOf(y1, y2).forEach { y ->
            drawCircle(Color(0xFFCFD8DC), studR, Offset(x, y))
            drawCircle(Color(0xFF78909C).copy(alpha = 0.65f), studR * 0.4f, Offset(x, y))
        }
        x += spacing
    }
    var y = spacing
    while (y < size.height - spacing * 0.5f) {
        listOf(x1, x2).forEach { cx ->
            drawCircle(Color(0xFFCFD8DC), studR, Offset(cx, y))
            drawCircle(Color(0xFF78909C).copy(alpha = 0.65f), studR * 0.4f, Offset(cx, y))
        }
        y += spacing
    }
}

// ── Marco 2: Neon — Luces de concierto ────────────────────────────────────────

private fun DrawScope.drawFrameNeon(cr: Float) {
    // Capas de glow: más ancho = más difuso; más estrecho = más brillante
    listOf(
        Triple(Color(0xFF7B00D4), 14.dp.toPx(), 0.07f),
        Triple(Color(0xFF9B00FF),  9.dp.toPx(), 0.14f),
        Triple(Color(0xFFD500F9),  5.dp.toPx(), 0.28f),
        Triple(Color(0xFFE040FB),  2.5.dp.toPx(), 0.62f),
        Triple(Color.White,        1.dp.toPx(),   0.86f),
    ).forEach { (color, w, a) ->
        drawRoundRect(
            color        = color.copy(alpha = a),
            topLeft      = Offset.Zero,
            size         = size,
            cornerRadius = CornerRadius(cr),
            style        = Stroke(w),
        )
    }

    // Focos en las esquinas (stage lights)
    val d = 8.dp.toPx()
    listOf(
        Offset(d, d),
        Offset(size.width - d, d),
        Offset(size.width - d, size.height - d),
        Offset(d, size.height - d),
    ).forEach { c ->
        drawCircle(Color(0xFF9B00FF).copy(alpha = 0.45f), 7.dp.toPx(), c)
        drawCircle(Color.White.copy(alpha = 0.78f), 2.dp.toPx(), c)
    }
}

// ── Marco 3: Vintage — Vinilo de los 70s ──────────────────────────────────────

private fun DrawScope.drawFrameVintage(cr: Float) {
    // Borde exterior marrón oscuro
    drawRoundRect(
        color        = Color(0xFF3E2723),
        topLeft      = Offset.Zero,
        size         = size,
        cornerRadius = CornerRadius(cr),
        style        = Stroke(6.dp.toPx()),
    )
    // Línea interior dorada
    drawRoundRect(
        color        = Color(0xFFD4A017).copy(alpha = 0.76f),
        topLeft      = Offset(2.dp.toPx(), 2.dp.toPx()),
        size         = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
        cornerRadius = CornerRadius((cr - 2.dp.toPx()).coerceAtLeast(0f)),
        style        = Stroke(1.5.dp.toPx()),
    )

    // Perforaciones de tira de film en las esquinas
    val pW  = 4.5.dp.toPx()
    val pH  = 6.5.dp.toPx()
    val m   = 1.2.dp.toPx()
    val gap = 3.5.dp.toPx()
    val pcr = CornerRadius(1.2.dp.toPx())
    val dark  = Color(0xFF3E2723)
    val glint = Color(0xFFD4A017).copy(alpha = 0.42f)

    listOf(
        // Esquina TL – par vertical
        Offset(m + 1.dp.toPx(), m + 2.dp.toPx()),
        Offset(m + 1.dp.toPx(), m + 2.dp.toPx() + pH + gap),
        // Esquina TR
        Offset(size.width - m - 1.dp.toPx() - pW, m + 2.dp.toPx()),
        Offset(size.width - m - 1.dp.toPx() - pW, m + 2.dp.toPx() + pH + gap),
        // Esquina BL
        Offset(m + 1.dp.toPx(), size.height - m - 2.dp.toPx() - pH * 2 - gap),
        Offset(m + 1.dp.toPx(), size.height - m - 2.dp.toPx() - pH),
        // Esquina BR
        Offset(size.width - m - 1.dp.toPx() - pW, size.height - m - 2.dp.toPx() - pH * 2 - gap),
        Offset(size.width - m - 1.dp.toPx() - pW, size.height - m - 2.dp.toPx() - pH),
    ).forEach { tl ->
        drawRoundRect(dark,  tl, Size(pW, pH), pcr)
        drawRoundRect(glint, Offset(tl.x + 0.5.dp.toPx(), tl.y + 0.5.dp.toPx()), Size(pW - 1.dp.toPx(), pH - 1.dp.toPx()), pcr)
    }
}
