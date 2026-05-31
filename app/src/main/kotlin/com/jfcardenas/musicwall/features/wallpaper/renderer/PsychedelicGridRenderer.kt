package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.*
import androidx.palette.graphics.Palette
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Grilla 3×5 de 15 portadas. A cada celda se le aplica una rotación de tono
 * única (HSL), saturación elevada y un halo de color en el tono dominante
 * desplazado — como si un artista hubiera reinterpretado cada carátula
 * bajo un estado alterado, sin perder su esencia visual.
 */
@Singleton
class PsychedelicGridRenderer @Inject constructor() : WallpaperRenderer {

    override val id        = "psychedelic"
    override val isPremium = false

    companion object {
        private const val COLS = 3
        private const val ROWS = 5
    }

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap {
        val out    = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        canvas.drawColor(Color.BLACK)

        val sorted = sortByRelevance(items).take(15)
        if (sorted.isEmpty()) return out

        val cw = width.toFloat()  / COLS
        val ch = height.toFloat() / ROWS

        sorted.forEachIndexed { idx, ri ->
            val col    = idx % COLS
            val row    = idx / COLS
            val left   = col * cw
            val top    = row * ch
            val dst    = RectF(left, top, left + cw, top + ch)

            // Rotación de tono única por celda; saturación y contraste elevados
            val hue     = (idx * 24f) % 360f
            val matrix  = buildColorMatrix(hue, saturation = 2.2f, contrast = 1.18f)
            val imgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(matrix)
            }

            canvas.drawBitmap(
                ri.bitmap,
                Rect(0, 0, ri.bitmap.width, ri.bitmap.height),
                dst,
                imgPaint,
            )

            // Halo de color en el tono del álbum desplazado 180° → contraste psicodélico
            val palette = Palette.from(
                Bitmap.createScaledBitmap(ri.bitmap, 48, 48, false)
            ).generate()
            val base      = palette.getVibrantColor(palette.getDominantColor(Color.MAGENTA))
            val glowColor = shiftHue(base, 180f)
            canvas.drawRect(dst, Paint().apply {
                shader = RadialGradient(
                    left + cw / 2f, top + ch / 2f,
                    maxOf(cw, ch) * 0.65f,
                    intArrayOf(
                        Color.argb(55, Color.red(glowColor), Color.green(glowColor), Color.blue(glowColor)),
                        Color.TRANSPARENT,
                    ),
                    floatArrayOf(0f, 1f),
                    Shader.TileMode.CLAMP,
                )
            })

            // Borde de neón delgado en el tono primario de la celda
            val neon = hslToRgb(hue, 1f, 0.55f)
            canvas.drawRect(dst, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color       = Color.argb(90, Color.red(neon), Color.green(neon), Color.blue(neon))
                strokeWidth = 1.5f
                style       = Paint.Style.STROKE
            })
        }

        // Viñeta suave global
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
            shader = RadialGradient(
                width / 2f, height / 2f,
                maxOf(width, height) * 0.72f,
                intArrayOf(Color.TRANSPARENT, Color.argb(120, 0, 0, 0)),
                floatArrayOf(0.45f, 1f), Shader.TileMode.CLAMP,
            )
        })

        return out
    }

    // ── Matriz de color ────────────────────────────────────────────────────────

    private fun buildColorMatrix(hueDeg: Float, saturation: Float, contrast: Float): ColorMatrix {
        val hueM = hueRotationMatrix(hueDeg)
        val satM = ColorMatrix().also { it.setSaturation(saturation) }
        val shift = 128f * (1f - contrast)
        val conM  = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, shift,
            0f, contrast, 0f, 0f, shift,
            0f, 0f, contrast, 0f, shift,
            0f, 0f, 0f, 1f, 0f,
        ))
        val result = ColorMatrix()
        result.setConcat(hueM, satM)
        result.postConcat(conM)
        return result
    }

    /** Rotación HSL real alrededor del eje gris (pesos ITU-R BT.709). */
    private fun hueRotationMatrix(deg: Float): ColorMatrix {
        val rad = Math.toRadians(deg.toDouble())
        val cos = cos(rad).toFloat()
        val sin = sin(rad).toFloat()
        val lr = 0.213f; val lg = 0.715f; val lb = 0.072f
        return ColorMatrix(floatArrayOf(
            lr + cos*(1f-lr) + sin*(-lr),       lg + cos*(-lg)     + sin*(-lg),      lb + cos*(-lb)     + sin*(1f-lb),   0f, 0f,
            lr + cos*(-lr)   + sin*(0.143f),    lg + cos*(1f-lg)   + sin*(0.140f),   lb + cos*(-lb)     + sin*(-0.283f), 0f, 0f,
            lr + cos*(-lr)   + sin*(-(1f-lr)),  lg + cos*(-lg)     + sin*(lg),       lb + cos*(1f-lb)   + sin*(lb),      0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        ))
    }

    // ── Helpers de color ──────────────────────────────────────────────────────

    /** Desplaza el tono de un color ARGB en [degrees] grados. */
    private fun shiftHue(argb: Int, degrees: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(argb, hsv)
        hsv[0] = (hsv[0] + degrees) % 360f
        return Color.HSVToColor(hsv)
    }

    /** HSL (h∈[0,360], s∈[0,1], l∈[0,1]) → ARGB opaco. */
    private fun hslToRgb(h: Float, s: Float, l: Float): Int {
        val c  = (1f - abs(2f * l - 1f)) * s
        val x  = c * (1f - abs((h / 60f) % 2f - 1f))
        val m  = l - c / 2f
        val (r, g, b) = when {
            h < 60f  -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else     -> Triple(c, 0f, x)
        }
        return Color.rgb(((r + m) * 255).toInt(), ((g + m) * 255).toInt(), ((b + m) * 255).toInt())
    }

    private fun sortByRelevance(items: List<RenderItem>): List<RenderItem> =
        if (items.any { it.image.playcount > 0 }) items.sortedByDescending { it.image.playcount }
        else items.sortedBy { it.image.rank }
}
