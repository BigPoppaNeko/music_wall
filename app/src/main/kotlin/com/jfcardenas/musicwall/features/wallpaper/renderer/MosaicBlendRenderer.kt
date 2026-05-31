package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.*
import androidx.palette.graphics.Palette
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * 25 imágenes superpuestas en 3 capas de opacidad y desenfoque creciente.
 * El desenfoque gaussiano simulado (downscale + upscale) hace que los bordes
 * se mezclen entre sí, dando la sensación de fusión.
 *
 * Layer 0 — atmósfera: 25 imgs, blur máximo, alpha bajo  → fondo difuso
 * Layer 1 — mid:       15 imgs, blur medio, alpha medio  → cuerpo
 * Layer 2 — hero:      8 imgs,  blur suave, alpha alto   → foco radial
 */
@Singleton
class MosaicBlendRenderer @Inject constructor() : WallpaperRenderer {

    override val id        = "mosaic"
    override val isPremium = false

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap {
        val out    = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val w = width.toFloat(); val h = height.toFloat()

        val sorted = sortByRelevance(items)
        if (sorted.isEmpty()) { canvas.drawColor(Color.BLACK); return out }

        // ── Fondo: gradiente de los colores oscuros de los 3 más escuchados ──
        val pals = sorted.take(3).map { Palette.from(thumb(it.bitmap)).generate() }
        val bg0  = shade(pals[0].getDarkMutedColor(Color.rgb(8, 8, 16)), 0.28f)
        val bg1  = shade(pals.getOrNull(2)?.getDarkMutedColor(bg0) ?: bg0, 0.22f)
        canvas.drawPaint(Paint().apply {
            shader = LinearGradient(0f, 0f, w, h, bg0, bg1, Shader.TileMode.CLAMP)
        })

        val p = Paint(Paint.FILTER_BITMAP_FLAG)

        // ── Capa 0: atmósfera (5×5 grid, muy borroso, alpha 65) ──────────────
        sorted.forEachIndexed { i, ri ->
            val cw = w / 5f; val ch = h / 5f
            val x  = (i % 5) * cw - cw * 0.1f
            val y  = (i / 5) * ch - ch * 0.1f
            blit(canvas, ri.bitmap, RectF(x, y, x + cw * 1.22f, y + ch * 1.22f), 0.05f, 65, p)
        }

        // ── Capa 1: mid (3 col × 5 row, offset en filas pares, blur medio) ──
        sorted.take(15).forEachIndexed { i, ri ->
            val cw  = w / 3f; val ch = h / 5f
            val col = i % 3; val row = i / 3
            val dx  = if (row % 2 == 1) cw * 0.18f else 0f
            val x   = col * cw + dx - cw * 0.12f
            val y   = row * ch - ch * 0.12f
            blit(canvas, ri.bitmap, RectF(x, y, x + cw * 1.28f, y + ch * 1.28f),
                 0.18f, (118 + i * 3).coerceAtMost(160), p)
        }

        // ── Capa 2: hero (radial centrado en 8 imágenes, casi nítido) ────────
        val twoPi = (2.0 * PI).toFloat()
        sorted.take(8).forEachIndexed { i, ri ->
            val angle = if (i == 0) 0f else (i - 1) * twoPi / 7f - PI.toFloat() / 2f
            val radius = if (i == 0) 0f else w * 0.23f
            val cx = w * 0.5f  + cos(angle) * radius
            val cy = h * 0.43f + sin(angle) * radius * 1.52f
            val sz = w * (if (i == 0) 0.45f else (0.31f - i * 0.018f).coerceAtLeast(0.15f))
            blit(canvas, ri.bitmap,
                 RectF(cx - sz / 2f, cy - sz / 2f, cx + sz / 2f, cy + sz / 2f),
                 if (i < 2) 0.58f else 0.42f,
                 (218 - i * 18).coerceAtLeast(80), p)
        }

        // ── Viñeta ───────────────────────────────────────────────────────────
        canvas.drawRect(0f, 0f, w, h, Paint().apply {
            shader = RadialGradient(w / 2f, h / 2f, maxOf(w, h) * 0.68f,
                intArrayOf(Color.TRANSPARENT, Color.argb(215, 0, 0, 0)),
                floatArrayOf(0.22f, 1f), Shader.TileMode.CLAMP)
        })

        return out
    }

    // Dibuja bitmap escalado a [blurScale] y pintado en dst → efecto blur suave
    private fun blit(canvas: Canvas, src: Bitmap, dst: RectF, blurScale: Float, alpha: Int, p: Paint) {
        val bw    = (src.width  * blurScale).toInt().coerceAtLeast(2)
        val bh    = (src.height * blurScale).toInt().coerceAtLeast(2)
        val small = Bitmap.createScaledBitmap(src, bw, bh, true)
        p.alpha   = alpha.coerceIn(0, 255)
        canvas.drawBitmap(small, Rect(0, 0, bw, bh), dst, p)
        small.recycle()
    }

    private fun sortByRelevance(items: List<RenderItem>): List<RenderItem> =
        if (items.any { it.image.playcount > 0 }) items.sortedByDescending { it.image.playcount }
        else items.sortedBy { it.image.rank }

    private fun thumb(b: Bitmap): Bitmap = Bitmap.createScaledBitmap(b, 64, 64, false)

    private fun shade(color: Int, f: Float): Int = Color.rgb(
        (Color.red(color)   * f).toInt().coerceIn(0, 255),
        (Color.green(color) * f).toInt().coerceIn(0, 255),
        (Color.blue(color)  * f).toInt().coerceIn(0, 255),
    )
}
