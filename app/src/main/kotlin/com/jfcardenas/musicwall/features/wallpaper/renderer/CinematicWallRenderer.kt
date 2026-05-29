package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.*
import androidx.palette.graphics.Palette
import kotlin.math.cos
import kotlin.math.sin
import javax.inject.Inject

/**
 * Cinematic renderer: layered overlapping composition with glow, depth and atmosphere.
 * Inspired by dark ambient / progressive art aesthetics — no rigid grid, each piece
 * feels like generative art born from the user's listening history.
 *
 * Layers (bottom → top):
 *   0  Deep dark base
 *   1  Ambient color wash   (all bitmaps blurred at 1/8 res → upscaled)
 *   2  Secondary glows      (radial light halos before images)
 *   3  Hero glow            (large dramatic halo)
 *   4  Secondary images     (orbital, faded edges, subtle rotation)
 *   5  Hero image           (dominant, sharp centre, slight blur at rim)
 *   6  Vignette + edge grads
 *   7  Color grade          (dark indigo tint for cohesion)
 */
class CinematicWallRenderer @Inject constructor() : WallpaperRenderer {

    override val id = "cinematic_wall"
    override val isPremium = false

    companion object {
        private const val MAX_VISIBLE = 10
        // Golden angle in radians — produces non-repeating phyllotaxis distribution
        private val GOLDEN_ANGLE = Math.toRadians(137.508)
    }

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        if (items.isEmpty()) {
            canvas.drawColor(Color.parseColor("#050508"))
            return result
        }

        val sorted    = sortByRelevance(items).take(MAX_VISIBLE)
        val hero      = sorted[0]
        val secondary = sorted.drop(1)
        val palettes  = sorted.map { Palette.from(scaledForPalette(it.bitmap)).generate() }

        val w      = width.toFloat()
        val h      = height.toFloat()
        val heroCx = w * 0.5f
        val heroCy = h * 0.52f          // slightly below vertical centre

        // ── Layer 0: Deep dark base ────────────────────────────────────────────
        canvas.drawColor(Color.parseColor("#050508"))

        // ── Layer 1: Ambient color wash ────────────────────────────────────────
        drawAmbientWash(canvas, sorted, width, height)

        // ── Layer 2: Secondary glows (behind their images) ─────────────────────
        secondary.forEachIndexed { i, _ ->
            val (cx, cy) = secondaryPosition(i, secondary.size, w, h)
            val sz       = secondarySize(i, secondary.size, w)
            drawGlow(canvas, cx, cy, sz * 0.9f, palettes.getOrNull(i + 1), alpha = 45)
        }

        // ── Layer 3: Hero glow (large and dramatic) ────────────────────────────
        drawGlow(canvas, heroCx, heroCy, w * 0.72f, palettes[0], alpha = 95)

        // ── Layer 4: Secondary images (orbital, fading edges) ─────────────────
        secondary.forEachIndexed { i, item ->
            val (cx, cy) = secondaryPosition(i, secondary.size, w, h)
            val sz       = secondarySize(i, secondary.size, w)
            val rotation = ((i * 53) % 40 - 20).toFloat()   // ‑20° … +20°, deterministic
            val opacity  = (0.65f - 0.04f * i).coerceAtLeast(0.32f)
            drawFadedImage(canvas, item.bitmap, cx, cy, sz, rotation, opacity, fadeStart = 0.52f)
        }

        // ── Layer 5: Hero image (dominant, minimal fade) ───────────────────────
        drawFadedImage(canvas, hero.bitmap, heroCx, heroCy, w * 0.74f, 0f, 0.92f, fadeStart = 0.76f)

        // ── Layer 6: Cinematic finishers ───────────────────────────────────────
        drawVignette(canvas, width, height)
        drawEdgeGradients(canvas, width, height)
        drawColorGrade(canvas, width, height)

        return result
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer implementations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Tiles all bitmaps at 1/4 screen resolution, then upscales to full screen.
     * Bilinear filtering during upscale creates a dreamy smeared background.
     */
    private fun drawAmbientWash(
        canvas: Canvas, items: List<RenderItem>, width: Int, height: Int
    ) {
        val cols  = 3
        val rows  = 2
        val smallW = width / 4
        val smallH = height / 4

        val wash = Bitmap.createBitmap(smallW, smallH, Bitmap.Config.ARGB_8888)
        val wc   = Canvas(wash)
        val tw   = smallW.toFloat() / cols
        val th   = smallH.toFloat() / rows

        items.take(cols * rows).forEachIndexed { i, item ->
            val col = i % cols
            val row = i / cols
            wc.drawBitmap(
                item.bitmap, null,
                RectF(col * tw, row * th, (col + 1) * tw, (row + 1) * th),
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
        }

        canvas.drawBitmap(
            wash, null, RectF(0f, 0f, width.toFloat(), height.toFloat()),
            Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 85 }
        )
        wash.recycle()

        // Darken strongly to keep blacks deep
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
            color = Color.argb(185, 2, 2, 6)
        })
    }

    /**
     * Draws a soft radial glow centred at (cx, cy) using the palette's
     * vibrant / dominant colour.  Drawn as a rect so the gradient shader
     * handles the falloff; no hard circle edge.
     */
    private fun drawGlow(
        canvas: Canvas, cx: Float, cy: Float, radius: Float,
        palette: Palette?, alpha: Int
    ) {
        val dominant = palette?.getDominantColor(Color.DKGRAY) ?: Color.DKGRAY
        val vivid    = palette?.getVibrantColor(dominant) ?: dominant

        canvas.drawRect(
            cx - radius, cy - radius, cx + radius, cy + radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    cx, cy, radius,
                    intArrayOf(
                        Color.argb(alpha,     Color.red(vivid),    Color.green(vivid),    Color.blue(vivid)),
                        Color.argb(alpha / 2, Color.red(dominant), Color.green(dominant), Color.blue(dominant)),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.35f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
        )
    }

    /**
     * Draws [bitmap] as a circle-faded image on the canvas.
     *
     * Technique: render onto a square offscreen ARGB bitmap, then apply a
     * RadialGradient mask via DST_IN (keeps destination where source is opaque).
     * The gradient is fully opaque from 0 to [fadeStart] of the radius, then
     * fades to transparent, producing a soft dissolving edge.
     */
    private fun drawFadedImage(
        canvas: Canvas, bitmap: Bitmap,
        cx: Float, cy: Float, size: Float,
        rotationDeg: Float, opacity: Float,
        fadeStart: Float
    ) {
        val sz   = size.toInt().coerceAtLeast(2)
        val half = size / 2f

        val offBmp = Bitmap.createBitmap(sz, sz, Bitmap.Config.ARGB_8888)
        val offC   = Canvas(offBmp)

        // Draw the source image, scaled to fill the square buffer
        offC.drawBitmap(
            bitmap, null, RectF(0f, 0f, sz.toFloat(), sz.toFloat()),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )

        // Apply circular dissolve mask:
        // DST_IN retains destination pixels proportional to source alpha.
        // Gradient: BLACK (alpha=255) at centre → TRANSPARENT (alpha=0) at edge.
        offC.drawRect(0f, 0f, sz.toFloat(), sz.toFloat(), Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            shader   = RadialGradient(
                sz / 2f, sz / 2f, sz / 2f,
                intArrayOf(Color.BLACK, Color.BLACK, Color.TRANSPARENT),
                floatArrayOf(0f, fadeStart, 1f),
                Shader.TileMode.CLAMP
            )
        })

        canvas.save()
        if (rotationDeg != 0f) canvas.rotate(rotationDeg, cx, cy)
        canvas.drawBitmap(
            offBmp, cx - half, cy - half,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = (opacity * 255f).toInt() }
        )
        canvas.restore()

        offBmp.recycle()
    }

    // ── Cinematic overlay passes ──────────────────────────────────────────────

    private fun drawVignette(canvas: Canvas, width: Int, height: Int) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width / 2f, height / 2f, maxOf(width, height) * 0.68f,
                intArrayOf(Color.TRANSPARENT, Color.argb(195, 0, 0, 4)),
                floatArrayOf(0.32f, 1f),
                Shader.TileMode.CLAMP
            )
        })
    }

    private fun drawEdgeGradients(canvas: Canvas, width: Int, height: Int) {
        val w   = width.toFloat()
        val h   = height.toFloat()
        val top = h * 0.32f
        val bot = h * 0.68f

        canvas.drawRect(0f, 0f, w, top, Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, top,
                intArrayOf(Color.argb(175, 0, 0, 4), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP)
        })
        canvas.drawRect(0f, bot, w, h, Paint().apply {
            shader = LinearGradient(0f, bot, 0f, h,
                intArrayOf(Color.TRANSPARENT, Color.argb(200, 0, 0, 4)),
                null, Shader.TileMode.CLAMP)
        })
    }

    /** Subtle dark indigo tint for visual cohesion across all album palettes. */
    private fun drawColorGrade(canvas: Canvas, width: Int, height: Int) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
            color = Color.argb(35, 8, 2, 45)
        })
    }

    // ── Positioning helpers ───────────────────────────────────────────────────

    /**
     * Places secondary images using golden-angle phyllotaxis around the hero.
     * Produces an organic non-repeating distribution that avoids obvious symmetry.
     */
    private fun secondaryPosition(
        i: Int, total: Int, w: Float, h: Float
    ): Pair<Float, Float> {
        val heroCx   = w * 0.5f
        val heroCy   = h * 0.52f
        val angle    = (i * GOLDEN_ANGLE).toFloat()
        val progress = (i + 1f) / (total.coerceAtLeast(1) + 1f)
        val minR     = w * 0.20f
        val maxR     = w * 0.57f
        val radius   = minR + (maxR - minR) * progress
        return (heroCx + cos(angle) * radius) to (heroCy + sin(angle) * radius * 1.22f)
    }

    /** Items with lower index (higher relevance) receive a larger tile. */
    private fun secondarySize(i: Int, total: Int, w: Float): Float {
        val progress = i.toFloat() / total.coerceAtLeast(1)
        return w * (0.45f - 0.22f * progress)   // 0.45w → 0.23w
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private fun sortByRelevance(items: List<RenderItem>): List<RenderItem> =
        if (items.any { it.image.playcount > 0 }) items.sortedByDescending { it.image.playcount }
        else items.sortedBy { it.image.rank }

    private fun scaledForPalette(bitmap: Bitmap): Bitmap =
        Bitmap.createScaledBitmap(bitmap, 64, 64, false)
}
