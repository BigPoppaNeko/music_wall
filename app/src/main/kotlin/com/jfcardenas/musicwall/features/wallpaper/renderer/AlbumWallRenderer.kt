package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.*
import androidx.palette.graphics.Palette
import javax.inject.Inject

class AlbumWallRenderer @Inject constructor() : WallpaperRenderer {

    override val id = "album_wall"
    override val isPremium = false

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        if (items.isEmpty()) {
            canvas.drawColor(Color.parseColor("#0C0C0C"))
            return result
        }

        val scale    = width / 1080f
        val gap      = 14f * scale
        val corner   = 18f * scale
        val halfG    = gap / 2f
        val glowBlur = 22f * scale
        val glowDy   = 5f * scale

        val sorted   = sortByRelevance(items)
        val palettes = sorted.map { Palette.from(scaledForPalette(it.bitmap)).generate() }

        // ── Background: hero's dark color tinted toward black ─────────────────
        val fallbackBg = Color.parseColor("#0C0C0C")
        val heroMuted  = palettes[0].getDarkMutedColor(fallbackBg)
        canvas.drawColor(blendWithBlack(heroMuted, 0.35f))

        // Ambient radial wash from hero's dominant color behind the hero tile
        val heroDominant = palettes[0].getDominantColor(fallbackBg)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
            shader = RadialGradient(
                width / 2f, height * 0.19f,
                width * 0.65f,
                intArrayOf(
                    Color.argb(55, Color.red(heroDominant), Color.green(heroDominant), Color.blue(heroDominant)),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        })

        // ── Layout ────────────────────────────────────────────────────────────
        val heroRect   = RectF(gap, gap, width - gap, height * 0.38f - halfG)
        val mosaicRect = RectF(gap, height * 0.38f + halfG, width - gap, height - gap)
        val mosaic     = if (sorted.size > 1)
            treemap(buildWeighted(sorted.drop(1)), mosaicRect, halfG)
        else emptyList()

        // ── Colored glows (drawn before tiles so they bleed behind) ───────────
        drawGlow(canvas, heroRect,   palettes[0],          corner, glowBlur * 1.5f, glowDy, alpha = 85)
        mosaic.forEachIndexed { i, (rect, _) ->
            if (rect.isUsable())
                drawGlow(canvas, rect, palettes.getOrNull(i + 1), corner, glowBlur, glowDy, alpha = 65)
        }

        // ── Tiles ─────────────────────────────────────────────────────────────
        drawTile(canvas, sorted[0].bitmap, heroRect, corner, gradientAlpha = 40)
        mosaic.forEach { (rect, bmp) ->
            if (rect.isUsable()) drawTile(canvas, bmp, rect, corner, gradientAlpha = 95)
        }

        // ── Vignette ──────────────────────────────────────────────────────────
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
            shader = RadialGradient(
                width / 2f, height / 2f, maxOf(width, height) * 0.72f,
                intArrayOf(Color.TRANSPARENT, Color.argb(60, 0, 0, 0)),
                floatArrayOf(0.45f, 1f), Shader.TileMode.CLAMP
            )
        })

        return result
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    private fun drawGlow(
        canvas: Canvas,
        rect: RectF,
        palette: Palette?,
        corner: Float,
        blurRadius: Float,
        dy: Float,
        alpha: Int
    ) {
        val base = palette?.getVibrantColor(
            palette.getDominantColor(Color.DKGRAY)
        ) ?: Color.DKGRAY

        canvas.drawRoundRect(
            rect.shifted(0f, dy), corner, corner,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(alpha, Color.red(base), Color.green(base), Color.blue(base))
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }
        )
    }

    private fun drawTile(canvas: Canvas, bitmap: Bitmap, rect: RectF, corner: Float, gradientAlpha: Int) {
        val path = Path().apply { addRoundRect(rect, corner, corner, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(path)
        canvas.drawBitmap(
            bitmap, Rect(0, 0, bitmap.width, bitmap.height), rect,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
        canvas.drawRect(rect, Paint().apply {
            shader = LinearGradient(
                0f, rect.top, 0f, rect.bottom,
                intArrayOf(Color.TRANSPARENT, Color.argb(gradientAlpha, 0, 0, 0)),
                floatArrayOf(0.3f, 1f), Shader.TileMode.CLAMP
            )
        })
        canvas.restore()
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private fun treemap(
        items: List<Pair<Float, Bitmap>>,
        rect: RectF,
        halfGap: Float
    ): List<Pair<RectF, Bitmap>> {
        if (items.isEmpty()) return emptyList()
        if (items.size == 1) return listOf(rect to items[0].second)

        val total = items.sumOf { it.first.toDouble() }.toFloat()
        var acc = 0f
        var split = items.size - 1
        for (i in items.indices) {
            acc += items[i].first
            if (acc / total >= 0.5f) { split = (i + 1).coerceIn(1, items.size - 1); break }
        }

        val ratio = (items.take(split).sumOf { it.first.toDouble() }.toFloat() / total)
            .coerceIn(0.15f, 0.85f)

        val (r1, r2) = if (rect.width() >= rect.height()) {
            val mid = rect.left + rect.width() * ratio
            RectF(rect.left, rect.top, mid - halfGap, rect.bottom) to
            RectF(mid + halfGap, rect.top, rect.right, rect.bottom)
        } else {
            val mid = rect.top + rect.height() * ratio
            RectF(rect.left, rect.top, rect.right, mid - halfGap) to
            RectF(rect.left, mid + halfGap, rect.right, rect.bottom)
        }

        return treemap(items.take(split), r1, halfGap) + treemap(items.drop(split), r2, halfGap)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun sortByRelevance(items: List<RenderItem>): List<RenderItem> =
        if (items.any { it.image.playcount > 0 }) items.sortedByDescending { it.image.playcount }
        else items.sortedBy { it.image.rank }

    private fun buildWeighted(items: List<RenderItem>): List<Pair<Float, Bitmap>> {
        val raw = if (items.any { it.image.playcount > 0 })
            items.map { it.image.playcount.toFloat() }
        else
            items.indices.map { i -> items.size.toFloat() / (i + 1) }
        val floor = raw.average().toFloat() * 0.2f
        return items.mapIndexed { i, ri -> maxOf(raw[i], floor) to ri.bitmap }
    }

    private fun scaledForPalette(bitmap: Bitmap): Bitmap =
        Bitmap.createScaledBitmap(bitmap, 64, 64, false)

    private fun blendWithBlack(color: Int, fraction: Float): Int {
        val f = fraction.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(color) * f).toInt(),
            (Color.green(color) * f).toInt(),
            (Color.blue(color) * f).toInt()
        )
    }

    private fun RectF.shifted(dx: Float, dy: Float) = RectF(left + dx, top + dy, right + dx, bottom + dy)
    private fun RectF.isUsable() = width() >= 40f && height() >= 40f
}
