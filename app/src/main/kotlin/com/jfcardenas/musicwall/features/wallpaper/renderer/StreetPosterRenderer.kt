package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.*
import androidx.palette.graphics.Palette
import kotlin.math.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * StreetPosterRenderer — Muro Vivido.
 *
 * Layer order (bottom → top):
 *   1  Wall surface   — concrete with low/mid-frequency noise variation
 *   2  Archaeology    — ghost prints from previous generations
 *   3  Old remnants   — torn poster fragments, aged color patches
 *   4  Physical wear  — tape strips, water stains, paint drips, paper scraps
 *   5  Stencil text   — artist names painted on wall
 *   6  Current posters — heavily overlapping, back→front, with paper borders / torn edges
 */
@Singleton
class StreetPosterRenderer @Inject constructor() : WallpaperRenderer {

    override val id        = "street"
    override val isPremium = false

    private data class Placement(val cx: Float, val cy: Float, val size: Int, val rot: Float)

    // ── Entry point ───────────────────────────────────────────────────────────

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap {
        val sorted = sortByRelevance(items).take(24)
        val n = sorted.size
        if (n == 0) return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            .also { Canvas(it).drawColor(Color.rgb(21, 18, 15)) }

        val seed = sorted.fold(0L) { acc, it -> acc * 31L + it.image.name.hashCode() }

        // Two independent RNGs — layout computed first (density-aware positioning)
        val layoutRng = java.util.Random(seed)
        val rng       = java.util.Random(seed xor 0xF00DCAFEL)

        // Pre-extract palettes once to avoid repeated Palette.generate() calls
        val palettes   = sorted.map { Palette.from(thumb(it.bitmap)).generate() }
        val placements = buildLayout(n, width, height, layoutRng)

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        drawWallSurface(canvas, width, height, seed)
        drawArchaeology(canvas, sorted, rng, width, height)
        drawOldRemnants(canvas, sorted, palettes, rng, width, height)
        drawWear(canvas, rng, width, height)
        drawStencilText(canvas, sorted, palettes, rng, width, height)

        // Back → front so most-listened sits on top
        for (idx in n - 1 downTo 0) {
            drawPoster(canvas, sorted[idx], placements[idx], rng)
        }

        return result
    }

    // ── 1. Wall surface ───────────────────────────────────────────────────────

    private fun drawWallSurface(canvas: Canvas, width: Int, height: Int, seed: Long) {
        val rng = java.util.Random(seed)
        canvas.drawColor(Color.rgb(21, 18, 15))

        // Low-frequency variation — big humidity/grime blotches
        val lfW = width / 18 + 2
        val lfH = height / 18 + 2
        val lfPx = IntArray(lfW * lfH) {
            val v = (rng.nextGaussian() * 11).toInt().coerceIn(-42, 42)
            Color.rgb((21 + v).coerceIn(0, 255), (18 + v).coerceIn(0, 255), (15 + v).coerceIn(0, 255))
        }
        val lfBmp = Bitmap.createBitmap(lfPx, lfW, lfH, Bitmap.Config.ARGB_8888)
        canvas.drawBitmap(lfBmp, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
        lfBmp.recycle()

        // Mid-frequency variation — plaster / brick texture
        val mfW = width / 6 + 2
        val mfH = height / 6 + 2
        val mfPx = IntArray(mfW * mfH) {
            val v = (rng.nextGaussian() * 4).toInt().coerceIn(-18, 18)
            Color.argb(70,
                (128 + v).coerceIn(0, 255),
                (128 + v * 95 / 100).coerceIn(0, 255),
                (128 + v * 88 / 100).coerceIn(0, 255))
        }
        val mfBmp = Bitmap.createBitmap(mfPx, mfW, mfH, Bitmap.Config.ARGB_8888)
        canvas.drawBitmap(mfBmp, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 55 })
        mfBmp.recycle()
    }

    // ── 2. Archaeology ────────────────────────────────────────────────────────

    private fun drawArchaeology(canvas: Canvas, items: List<RenderItem>, rng: java.util.Random, width: Int, height: Int) {
        // Near-grayscale ghost fragments — previous generations of posters
        val ghostMatrix = ColorMatrix().apply {
            setSaturation(0f)
            postConcat(ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, 12f,   // warm tint: boost red
                0f, 1f, 0f, 0f,  0f,
                0f, 0f, 1f, 0f, -8f,   // reduce blue
                0f, 0f, 0f, 1f,  0f
            )))
        }
        val ghostPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(ghostMatrix)
        }

        for (item in items.take(min(8, items.size))) {
            val sz = (width * 0.30f + rng.nextFloat() * width * 0.42f).toInt().coerceAtLeast(2)
            val cx = rng.nextFloat() * width
            val cy = rng.nextFloat() * height
            val rot = rng.nextFloat() * 60f - 30f
            ghostPaint.alpha = 8 + rng.nextInt(15)

            val scaled = Bitmap.createScaledBitmap(item.bitmap, sz, sz, true)
            canvas.save()
            canvas.rotate(rot, cx, cy)
            canvas.drawBitmap(scaled, cx - sz / 2f, cy - sz / 2f, ghostPaint)
            canvas.restore()
            scaled.recycle()
        }

        // Huge artist typography — barely legible, like old paint
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val shuffled = items.toMutableList().also { it.shuffle(rng) }
        for (item in shuffled.take(5)) {
            if (item.image.artistName.isEmpty()) continue
            val textSize = 130f + rng.nextInt(110)
            val alpha    = 10 + rng.nextInt(18)
            val angle    = listOf(0f, 0f, 90f, -90f)[rng.nextInt(4)]
            val cx       = rng.nextFloat() * width
            val cy       = rng.nextFloat() * height
            val text     = item.image.artistName.uppercase()

            textPaint.textSize = textSize
            textPaint.color    = Color.argb(alpha, 215, 205, 185)

            canvas.save()
            canvas.rotate(angle, cx, cy)
            canvas.drawText(text, cx - textPaint.measureText(text) / 2f, cy, textPaint)
            canvas.restore()
        }
    }

    // ── 3. Old remnants ───────────────────────────────────────────────────────

    private fun drawOldRemnants(
        canvas: Canvas, items: List<RenderItem>, palettes: List<Palette>,
        rng: java.util.Random, width: Int, height: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val fragMatrix = ColorMatrix().apply {
            setSaturation(0.18f)
            postConcat(ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, 16f,
                0f, 1f, 0f, 0f,  0f,
                0f, 0f, 1f, 0f,-12f,
                0f, 0f, 0f, 1f,  0f
            )))
        }
        val fragFilter = ColorMatrixColorFilter(fragMatrix)

        repeat(16) { i ->
            val item    = items[i % items.size]
            val muted   = palettes[i % palettes.size].getMutedColor(Color.GRAY)

            if (rng.nextFloat() < 0.50f) {
                // Aged solid color patch
                val rw    = (width  * 0.18f + rng.nextFloat() * width  * 0.44f).toInt()
                val rh    = (height * 0.06f + rng.nextFloat() * height * 0.18f).toInt()
                val cx    = rng.nextFloat() * width
                val cy    = rng.nextFloat() * height
                val rot   = rng.nextFloat() * 24f - 12f
                paint.color = Color.argb(
                    28 + rng.nextInt(34),
                    Color.red(muted), Color.green(muted), Color.blue(muted)
                )
                canvas.save()
                canvas.rotate(rot, cx, cy)
                canvas.drawRect(cx - rw / 2f, cy - rh / 2f, cx + rw / 2f, cy + rh / 2f, paint)
                canvas.restore()
            } else {
                // Desaturated faded image fragment
                val sz    = (width * 0.12f + rng.nextFloat() * width * 0.26f).toInt().coerceAtLeast(2)
                val cx    = rng.nextFloat() * width
                val cy    = rng.nextFloat() * height
                val rot   = rng.nextFloat() * 30f - 15f
                val fPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = fragFilter
                    alpha = 25 + rng.nextInt(30)
                }
                val scaled = Bitmap.createScaledBitmap(item.bitmap, sz, sz, true)
                canvas.save()
                canvas.rotate(rot, cx, cy)
                canvas.drawBitmap(scaled, cx - sz / 2f, cy - sz / 2f, fPaint)
                canvas.restore()
                scaled.recycle()
            }
        }
    }

    // ── 4. Physical wear ──────────────────────────────────────────────────────

    private fun drawWear(canvas: Canvas, rng: java.util.Random, width: Int, height: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Tape strips — yellowed, semi-transparent
        repeat(4 + rng.nextInt(6)) {
            val tw  = width  * 0.06f + rng.nextFloat() * width  * 0.16f
            val th  = 5f + rng.nextInt(8)
            val cx  = rng.nextFloat() * width
            val cy  = rng.nextFloat() * height
            val rot = rng.nextFloat() * 16f - 8f
            paint.color = Color.argb(
                28 + rng.nextInt(30),
                228 + rng.nextInt(20), 208 + rng.nextInt(22), 135 + rng.nextInt(40)
            )
            canvas.save()
            canvas.rotate(rot, cx, cy)
            canvas.drawRect(cx - tw / 2f, cy - th / 2f, cx + tw / 2f, cy + th / 2f, paint)
            canvas.restore()
        }

        // Water stains — dark organic ellipses
        repeat(3 + rng.nextInt(5)) {
            val cx = rng.nextFloat() * width
            val cy = rng.nextFloat() * height
            val rx = 20f + rng.nextInt(80)
            val ry = 15f + rng.nextInt(50)
            paint.color = Color.argb(8 + rng.nextInt(17), 30, 25, 18)
            canvas.drawOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), paint)
        }

        // Paint drips — thin vertical streaks
        val dripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        repeat(2 + rng.nextInt(4)) {
            val x    = width  * 0.05f + rng.nextFloat() * width  * 0.90f
            val yTop = rng.nextFloat() * height * 0.4f
            val len  = height * 0.07f + rng.nextFloat() * height * 0.21f
            dripPaint.color       = Color.argb(
                10 + rng.nextInt(22),
                175 + rng.nextInt(55), 165 + rng.nextInt(55), 135 + rng.nextInt(50)
            )
            dripPaint.strokeWidth = 1f + rng.nextInt(3)
            canvas.drawLine(x, yTop, x + rng.nextInt(11) - 5f, yTop + len, dripPaint)
        }

        // Paper scraps — tiny torn fragments
        repeat(6 + rng.nextInt(9)) {
            val sw  = 18f + rng.nextInt(67)
            val sh  = 7f  + rng.nextInt(22)
            val cx  = rng.nextFloat() * width
            val cy  = rng.nextFloat() * height
            val rot = rng.nextFloat() * 50f - 25f
            paint.color = if (rng.nextBoolean())
                Color.argb(22 + rng.nextInt(30), 220 + rng.nextInt(22), 210 + rng.nextInt(22), 185 + rng.nextInt(25))
            else
                Color.argb(12 + rng.nextInt(20), 60 + rng.nextInt(30), 50 + rng.nextInt(30), 40 + rng.nextInt(28))
            canvas.save()
            canvas.rotate(rot, cx, cy)
            canvas.drawRect(cx - sw / 2f, cy - sh / 2f, cx + sw / 2f, cy + sh / 2f, paint)
            canvas.restore()
        }
    }

    // ── 5. Stencil text ───────────────────────────────────────────────────────

    private fun drawStencilText(
        canvas: Canvas, items: List<RenderItem>, palettes: List<Palette>,
        rng: java.util.Random, width: Int, height: Int
    ) {
        val texts = items.filter { it.image.artistName.isNotEmpty() }
            .map { it.image.artistName.uppercase() }
            .toMutableList().also { it.shuffle(rng) }
            .take(6)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        for (text in texts) {
            val display = text.take(18)
            val size    = 52f + rng.nextInt(45)
            val angle   = when (rng.nextInt(5)) { 0, 1, 2 -> 0f; 3 -> rng.nextFloat() * 8f - 4f; else -> 90f }
            val cx      = width  * 0.04f + rng.nextFloat() * width  * 0.92f
            val cy      = height * 0.05f + rng.nextFloat() * height * 0.90f
            val alpha   = 55 + rng.nextInt(55)

            paint.textSize = size
            paint.color = when (rng.nextInt(3)) {
                0    -> Color.argb(alpha, 240, 235, 220)
                1    -> Color.argb(alpha, 220, 200, 150)
                else -> {
                    val m = palettes[rng.nextInt(palettes.size)].getMutedColor(Color.GRAY)
                    Color.argb(alpha, Color.red(m), Color.green(m), Color.blue(m))
                }
            }

            canvas.save()
            canvas.rotate(angle, cx, cy)
            canvas.drawText(display, cx - paint.measureText(display) / 2f, cy, paint)
            canvas.restore()
        }
    }

    // ── 6. Layout ─────────────────────────────────────────────────────────────

    private fun buildLayout(n: Int, width: Int, height: Int, rng: java.util.Random): List<Placement> {
        val cols   = 4
        val cw     = width.toFloat()  / cols
        val ch     = height.toFloat() / ceil(n.toFloat() / cols).toInt()
        val minDim = minOf(width, height).toFloat()

        val nLarge  = maxOf(3, n / 5).coerceAtMost(n)
        val nMedium = maxOf(6, n * 2 / 5).coerceAtMost(n - nLarge)
        val nSmall  = maxOf(0, n - nLarge - nMedium)

        val sizes = (
            List(nLarge)  { (minDim * (0.33f + rng.nextFloat() * 0.11f)).toInt() } +
            List(nMedium) { (minDim * (0.24f + rng.nextFloat() * 0.08f)).toInt() } +
            List(nSmall)  { (minDim * (0.14f + rng.nextFloat() * 0.08f)).toInt() }
        ).toMutableList().also { it.shuffle(rng) }

        return List(n) { i ->
            val col = i % cols
            val row = i / cols
            Placement(
                cx  = (col + 0.5f) * cw + (rng.nextFloat() * 2f - 1f) * cw * 0.42f,
                cy  = (row + 0.5f) * ch + (rng.nextFloat() * 2f - 1f) * ch * 0.38f,
                size = sizes[i].coerceAtLeast(8),
                rot  = rng.nextFloat() * 40f - 20f
            )
        }
    }

    // ── 6. Poster drawing ─────────────────────────────────────────────────────

    private fun drawPoster(canvas: Canvas, item: RenderItem, p: Placement, rng: java.util.Random) {
        var sz  = p.size
        var bmp = Bitmap.createScaledBitmap(item.bitmap, sz, sz, true)

        when {
            rng.nextFloat() < 0.40f -> {
                // Paper border (printed poster)
                val border   = maxOf(4, sz / 40)
                val framedSz = sz + border * 2
                val framed   = Bitmap.createBitmap(framedSz, framedSz, Bitmap.Config.ARGB_8888)
                val fc       = Canvas(framed)
                fc.drawColor(if (rng.nextBoolean()) Color.rgb(242, 238, 228) else Color.WHITE)
                fc.drawBitmap(bmp, border.toFloat(), border.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
                if (rng.nextFloat() < 0.50f) addLabel(fc, item, framedSz, border, rng)
                bmp.recycle()
                bmp = framed
                sz  = framedSz
            }
            rng.nextFloat() < 0.30f -> {
                // Torn edges
                bmp = applyTornMask(bmp, sz, rng)
            }
        }

        val shadowOff = maxOf(sz / 20, 6).toFloat()
        val blackMask = ColorMatrixColorFilter(ColorMatrix().apply { setScale(0f, 0f, 0f, 1f) })

        canvas.save()
        canvas.rotate(p.rot, p.cx, p.cy)
        val x0 = p.cx - sz / 2f
        val y0 = p.cy - sz / 2f
        canvas.drawBitmap(bmp, x0 + shadowOff * 1.8f, y0 + shadowOff * 1.8f,
            Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 80;  colorFilter = blackMask })
        canvas.drawBitmap(bmp, x0 + shadowOff, y0 + shadowOff,
            Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 110; colorFilter = blackMask })
        canvas.drawBitmap(bmp, x0, y0, Paint(Paint.FILTER_BITMAP_FLAG))
        canvas.restore()
        bmp.recycle()
    }

    private fun addLabel(fc: Canvas, item: RenderItem, framedSz: Int, border: Int, rng: java.util.Random) {
        val labelH = maxOf(20, border * 3)
        val text   = item.image.artistName.ifEmpty { item.image.name }.uppercase().take(20)
        val bg     = if (rng.nextFloat() < 0.6f) Color.argb(240, 15, 12, 10) else Color.argb(240, 40, 35, 28)
        fc.drawRect(0f, (framedSz - labelH).toFloat(), framedSz.toFloat(), framedSz.toFloat(),
            Paint().apply { color = bg })
        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color    = Color.argb(230, 240, 235, 220)
            textSize = maxOf(10f, labelH - 6f)
        }
        val tx = ((framedSz - tp.measureText(text)) / 2f).coerceAtLeast(4f)
        val ty = framedSz - labelH + (labelH + tp.textSize) / 2f - 4f
        fc.drawText(text, tx, ty, tp)
    }

    private fun applyTornMask(bmp: Bitmap, sz: Int, rng: java.util.Random): Bitmap {
        val result = Bitmap.createBitmap(sz, sz, Bitmap.Config.ARGB_8888)
        val c      = Canvas(result)
        val tear   = (sz * 0.055f).toInt().coerceAtLeast(1)
        val steps  = 10
        val path   = Path()

        fun j() = rng.nextInt(tear * 2 + 1) - tear

        path.moveTo(j().toFloat(), j().toFloat())
        for (i in 1..steps) path.lineTo((sz * i / steps + j()).toFloat(), j().toFloat())
        for (i in 1..steps) path.lineTo((sz - j()).toFloat(), (sz * i / steps + j()).toFloat())
        for (i in steps downTo 0) path.lineTo((sz * i / steps + j()).toFloat(), (sz - j()).toFloat())
        for (i in steps downTo 1) path.lineTo(j().toFloat(), (sz * i / steps + j()).toFloat())
        path.close()

        c.clipPath(path)
        c.drawBitmap(bmp, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
        bmp.recycle()
        return result
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private fun sortByRelevance(items: List<RenderItem>): List<RenderItem> =
        if (items.any { it.image.playcount > 0 }) items.sortedByDescending { it.image.playcount }
        else items.sortedBy { it.image.rank }

    private fun thumb(bmp: Bitmap): Bitmap = Bitmap.createScaledBitmap(bmp, 32, 32, false)
}
