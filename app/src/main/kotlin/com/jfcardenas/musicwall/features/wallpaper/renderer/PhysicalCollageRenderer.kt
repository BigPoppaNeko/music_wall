package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.*
import kotlin.math.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PhysicalCollageRenderer — every album is a physical object.
 *
 * Randomly assigns one of 7 physical types per album (deterministic per album name):
 *   vinyl sleeve | CD jewel case | worn poster | polaroid |
 *   magazine clipping | sticker | torn paper print
 *
 * Every object casts a double-layered shadow and carries subtle physical
 * imperfections: bent corners, scratches, folds, tape, thumbtacks, worn edges.
 */
@Singleton
class PhysicalCollageRenderer @Inject constructor() : WallpaperRenderer {

    override val id        = "physical"
    override val isPremium = false

    private enum class PhysicalType {
        VINYL_SLEEVE, CD_JEWEL_CASE, WORN_POSTER, POLAROID,
        MAGAZINE_CLIPPING, STICKER, TORN_PAPER
    }

    private data class ItemLayout(
        val cx: Float, val cy: Float,
        val size: Int,
        val rot: Float,
        val type: PhysicalType
    )

    // ── Entry ─────────────────────────────────────────────────────────────────

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap {
        val sorted = sortByRelevance(items).take(20)
        val n      = sorted.size
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        if (n == 0) {
            canvas.drawColor(Color.rgb(20, 17, 14))
            return result
        }

        val seed = sorted.fold(0L) { acc, it -> acc * 31L + it.image.name.hashCode() }
        val rng  = java.util.Random(seed)

        drawBackground(canvas, width, height, rng)

        val layout = buildLayout(sorted, width, height, rng)
        // Draw back→front: index 0 = most-played = drawn last = on top
        for (i in n - 1 downTo 0) {
            drawPhysicalItem(canvas, sorted[i], layout[i])
        }

        drawVignette(canvas, width, height)
        return result
    }

    // ── Background: dark cork/linen board ────────────────────────────────────

    private fun drawBackground(canvas: Canvas, width: Int, height: Int, rng: java.util.Random) {
        canvas.drawColor(Color.rgb(22, 19, 16))

        // Cork-grain low-frequency noise
        val gw = width / 14 + 2
        val gh = height / 14 + 2
        val px = IntArray(gw * gh) {
            val v = (rng.nextGaussian() * 9).toInt().coerceIn(-28, 28)
            Color.rgb((38 + v).coerceIn(0, 255), (31 + v * 9 / 10).coerceIn(0, 255), (22 + v * 7 / 10).coerceIn(0, 255))
        }
        val grain = Bitmap.createBitmap(px, gw, gh, Bitmap.Config.ARGB_8888)
        canvas.drawBitmap(grain, null, RectF(0f, 0f, width.toFloat(), height.toFloat()),
            Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 185 })
        grain.recycle()

        // Fine-grain overlay
        val fw = width / 4 + 2
        val fh = height / 4 + 2
        val fp = IntArray(fw * fh) {
            val v = (rng.nextGaussian() * 4).toInt().coerceIn(-14, 14)
            Color.argb(55, (128 + v).coerceIn(0, 255), (110 + v).coerceIn(0, 255), (85 + v).coerceIn(0, 255))
        }
        val fine = Bitmap.createBitmap(fp, fw, fh, Bitmap.Config.ARGB_8888)
        canvas.drawBitmap(fine, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
        fine.recycle()
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private fun buildLayout(items: List<RenderItem>, width: Int, height: Int, rng: java.util.Random): List<ItemLayout> {
        val n      = items.size
        val minDim = minOf(width, height).toFloat()
        val types  = PhysicalType.values()

        val nLarge  = (n / 4).coerceAtLeast(1).coerceAtMost(n)
        val nMedium = (n / 3).coerceAtLeast(1).coerceAtMost(n - nLarge)
        val nSmall  = (n - nLarge - nMedium).coerceAtLeast(0)

        val sizes = (
            List(nLarge)  { (minDim * (0.28f + rng.nextFloat() * 0.14f)).toInt() } +
            List(nMedium) { (minDim * (0.18f + rng.nextFloat() * 0.09f)).toInt() } +
            List(nSmall)  { (minDim * (0.11f + rng.nextFloat() * 0.07f)).toInt() }
        ).toMutableList().also { it.shuffle(rng) }

        val cols = 4
        val rows = ceil(n.toFloat() / cols).toInt().coerceAtLeast(1)
        val cw   = width.toFloat()  / cols
        val ch   = height.toFloat() / rows

        return items.mapIndexed { i, item ->
            val type = types[abs(item.image.name.hashCode()) % types.size]
            val rot  = when (type) {
                PhysicalType.CD_JEWEL_CASE      -> rng.nextFloat() * 10f - 5f
                PhysicalType.VINYL_SLEEVE       -> rng.nextFloat() * 14f - 7f
                PhysicalType.POLAROID           -> rng.nextFloat() * 18f - 9f
                PhysicalType.WORN_POSTER        -> rng.nextFloat() * 22f - 11f
                PhysicalType.MAGAZINE_CLIPPING  -> rng.nextFloat() * 26f - 13f
                PhysicalType.TORN_PAPER         -> rng.nextFloat() * 28f - 14f
                PhysicalType.STICKER            -> rng.nextFloat() * 32f - 16f
            }
            val col = i % cols
            val row = i / cols
            ItemLayout(
                cx   = (col + 0.5f) * cw + (rng.nextFloat() * 2f - 1f) * cw * 0.38f,
                cy   = (row + 0.5f) * ch + (rng.nextFloat() * 2f - 1f) * ch * 0.34f,
                size = sizes[i].coerceAtLeast(60),
                rot  = rot,
                type = type
            )
        }
    }

    // ── Dispatcher ────────────────────────────────────────────────────────────

    private fun drawPhysicalItem(canvas: Canvas, item: RenderItem, layout: ItemLayout) {
        val itemRng = java.util.Random(item.image.name.hashCode().toLong())
        val scaled  = Bitmap.createScaledBitmap(item.bitmap, layout.size, layout.size, true)

        val treated: Bitmap = when (layout.type) {
            PhysicalType.VINYL_SLEEVE       -> makeVinylSleeve(scaled, layout.size, itemRng)
            PhysicalType.CD_JEWEL_CASE      -> makeCdJewelCase(scaled, layout.size, itemRng)
            PhysicalType.WORN_POSTER        -> makeWornPoster(scaled, layout.size, itemRng)
            PhysicalType.POLAROID           -> makePolaroid(scaled, layout.size, item, itemRng)
            PhysicalType.MAGAZINE_CLIPPING  -> makeMagazineClipping(scaled, layout.size, itemRng)
            PhysicalType.STICKER            -> makeSticker(scaled, layout.size, itemRng)
            PhysicalType.TORN_PAPER         -> makeTornPaper(scaled, layout.size, itemRng)
        }
        scaled.recycle()

        val tw        = treated.width.toFloat()
        val th        = treated.height.toFloat()
        val x0        = layout.cx - tw / 2f
        val y0        = layout.cy - th / 2f
        val shadowOff = (layout.size * 0.038f).coerceAtLeast(4f)
        val blackMask = ColorMatrixColorFilter(ColorMatrix().apply { setScale(0f, 0f, 0f, 1f) })

        canvas.save()
        canvas.rotate(layout.rot, layout.cx, layout.cy)
        // Far shadow
        canvas.drawBitmap(treated, x0 + shadowOff * 1.9f, y0 + shadowOff * 1.9f,
            Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 62;  colorFilter = blackMask })
        // Near shadow
        canvas.drawBitmap(treated, x0 + shadowOff, y0 + shadowOff,
            Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 100; colorFilter = blackMask })
        // Object
        canvas.drawBitmap(treated, x0, y0, Paint(Paint.FILTER_BITMAP_FLAG))
        canvas.restore()

        treated.recycle()
    }

    // ── 1. Vinyl sleeve ───────────────────────────────────────────────────────

    private fun makeVinylSleeve(src: Bitmap, sz: Int, rng: java.util.Random): Bitmap {
        val border = (sz * 0.045f).toInt().coerceAtLeast(4)
        val total  = sz + border * 2
        val out    = Bitmap.createBitmap(total, total, Bitmap.Config.ARGB_8888)
        val c      = Canvas(out)

        val v = rng.nextInt(18)
        c.drawColor(Color.rgb((212 + v).coerceIn(0, 255), (200 + v * 9 / 10).coerceIn(0, 255), (174 + v * 8 / 10).coerceIn(0, 255)))

        // Cardboard texture
        grain(c, total, total, rng, grainAlpha = 50, r = 100, g = 80, b = 55, variance = 7f)

        c.drawBitmap(src, border.toFloat(), border.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))

        // Left spine shadow
        c.drawRect(0f, 0f, border * 0.75f, total.toFloat(), Paint().apply {
            color = Color.argb(58, 25, 16, 8)
        })
        // Right open-edge hint
        c.drawRect((total - border * 0.5f), 0f, total.toFloat(), total.toFloat(), Paint().apply {
            color = Color.argb(40, 55, 40, 25)
        })

        addWornCorners(c, total, total, rng, 0.6f)
        addScratches(c, total, total, rng, 3 + rng.nextInt(4))
        if (rng.nextFloat() < 0.28f) addTapeStrip(c, total, total, rng)

        return out
    }

    // ── 2. CD jewel case ──────────────────────────────────────────────────────

    private fun makeCdJewelCase(src: Bitmap, sz: Int, rng: java.util.Random): Bitmap {
        val border = (sz * 0.028f).toInt().coerceAtLeast(2)
        val total  = sz + border * 2
        val out    = Bitmap.createBitmap(total, total, Bitmap.Config.ARGB_8888)
        val c      = Canvas(out)

        c.drawColor(Color.rgb(24, 24, 28))

        c.drawBitmap(src, border.toFloat(), border.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))

        // Top-left highlight ridges (plastic molding)
        c.drawLine(0f, 0f, total.toFloat(), 0f, Paint().apply {
            color = Color.argb(78, 255, 255, 255); strokeWidth = border * 0.55f
        })
        c.drawLine(0f, 0f, 0f, total.toFloat(), Paint().apply {
            color = Color.argb(58, 255, 255, 255); strokeWidth = border * 0.4f
        })
        // Bottom-right shadow ridges
        c.drawLine(0f, total.toFloat(), total.toFloat(), total.toFloat(), Paint().apply {
            color = Color.argb(55, 0, 0, 0); strokeWidth = border * 0.5f
        })
        c.drawLine(total.toFloat(), 0f, total.toFloat(), total.toFloat(), Paint().apply {
            color = Color.argb(40, 0, 0, 0); strokeWidth = border * 0.4f
        })

        // Plastic sheen diagonal
        c.drawRect(0f, 0f, total.toFloat(), total.toFloat(), Paint().apply {
            shader = LinearGradient(
                0f, 0f, total * 0.65f, total * 0.65f,
                intArrayOf(Color.argb(42, 255, 255, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
            )
        })

        addScratches(c, total, total, rng, 6 + rng.nextInt(7), alpha = 44)

        return out
    }

    // ── 3. Worn poster print ──────────────────────────────────────────────────

    private fun makeWornPoster(src: Bitmap, sz: Int, rng: java.util.Random): Bitmap {
        val border = (sz * 0.075f).toInt().coerceAtLeast(6)
        val total  = sz + border * 2
        val out    = Bitmap.createBitmap(total, total, Bitmap.Config.ARGB_8888)
        val c      = Canvas(out)

        val age = 0.3f + rng.nextFloat() * 0.7f
        c.drawColor(Color.rgb(
            (238 - age * 22).toInt().coerceIn(0, 255),
            (222 - age * 28).toInt().coerceIn(0, 255),
            (188 - age * 32).toInt().coerceIn(0, 255)
        ))

        grain(c, total, total, rng, grainAlpha = 35, r = 90, g = 70, b = 45, variance = 5f)

        val agedMatrix = ColorMatrix().apply {
            setSaturation(0.78f - age * 0.22f)
            postConcat(ColorMatrix(floatArrayOf(
                1f, 0f, 0f, 0f, 10f,
                0f, 1f, 0f, 0f,  4f,
                0f, 0f, 1f, 0f, -6f,
                0f, 0f, 0f, 1f,  0f
            )))
        }
        c.drawBitmap(src, border.toFloat(), border.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(agedMatrix)
        })

        addWornCorners(c, total, total, rng, 0.7f)
        if (rng.nextFloat() < 0.65f) addFoldLine(c, total, total, rng, vertical = false)
        if (rng.nextFloat() < 0.32f) addFoldLine(c, total, total, rng, vertical = true)
        if (rng.nextFloat() < 0.45f) addTapeStrip(c, total, total, rng)
        if (rng.nextFloat() < 0.32f) addThumbtack(c, total, total, rng)

        return out
    }

    // ── 4. Polaroid ───────────────────────────────────────────────────────────

    private fun makePolaroid(src: Bitmap, sz: Int, item: RenderItem, rng: java.util.Random): Bitmap {
        val side   = (sz * 0.055f).toInt().coerceAtLeast(4)
        val bottom = (sz * 0.24f ).toInt().coerceAtLeast(14)
        val tw     = sz + side * 2
        val th     = sz + side + bottom
        val out    = Bitmap.createBitmap(tw, th, Bitmap.Config.ARGB_8888)
        val c      = Canvas(out)

        val wb = rng.nextInt(8)
        c.drawColor(Color.rgb((248 + wb).coerceIn(0, 255), (245 + wb).coerceIn(0, 255), (238 + wb).coerceIn(0, 255)))

        grain(c, tw, th, rng, grainAlpha = 18, r = 80, g = 70, b = 55, variance = 3f)

        c.drawBitmap(src, side.toFloat(), side.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))

        // Gloss sheen over photo area
        c.drawRect(side.toFloat(), side.toFloat(), (side + sz).toFloat(), (side + sz).toFloat(), Paint().apply {
            shader = LinearGradient(
                side.toFloat(), side.toFloat(),
                (side + sz * 0.55f).toFloat(), (side + sz * 0.55f).toFloat(),
                intArrayOf(Color.argb(30, 255, 255, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
            )
        })

        // Handwritten label in bottom margin
        val text = item.image.name.take(24)
        if (text.isNotEmpty()) {
            val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = (bottom * 0.32f).coerceAtLeast(8f)
                color    = Color.argb(118, 28, 18, 45)
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
            val tx = ((tw - tp.measureText(text)) / 2f).coerceAtLeast(4f)
            val ty = side + sz + bottom * 0.56f
            c.drawText(text, tx, ty, tp)
        }

        addWornCorners(c, tw, th, rng, 0.35f)
        if (rng.nextFloat() < 0.22f) addThumbtack(c, tw, th, rng)

        return out
    }

    // ── 5. Magazine clipping ──────────────────────────────────────────────────

    private fun makeMagazineClipping(src: Bitmap, sz: Int, rng: java.util.Random): Bitmap {
        val margin = (sz * 0.06f).toInt().coerceAtLeast(4)
        val total  = sz + margin * 2
        val out    = Bitmap.createBitmap(total, total, Bitmap.Config.ARGB_8888)
        val c      = Canvas(out)

        c.drawColor(Color.rgb(232 + rng.nextInt(12), 229 + rng.nextInt(10), 220 + rng.nextInt(10)))

        grain(c, total, total, rng, grainAlpha = 40, r = 65, g = 60, b = 52, variance = 5f)

        // Slight CMYK print cast
        val printMatrix = ColorMatrix(floatArrayOf(
            0.92f, 0f, 0f, 0f, 0f,
            0f, 0.96f, 0f, 0f, 0f,
            0f, 0f, 1.06f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        c.drawBitmap(src, margin.toFloat(), margin.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(printMatrix)
        })

        applyScissorEdge(c, total, total, rng)
        if (rng.nextFloat() < 0.42f) addFoldLine(c, total, total, rng, vertical = rng.nextBoolean())

        return out
    }

    // ── 6. Sticker ────────────────────────────────────────────────────────────

    private fun makeSticker(src: Bitmap, sz: Int, rng: java.util.Random): Bitmap {
        val outline = (sz * 0.042f).toInt().coerceAtLeast(3)
        val total   = sz + outline * 2
        val corner  = total * 0.13f
        val out     = Bitmap.createBitmap(total, total, Bitmap.Config.ARGB_8888)
        val c       = Canvas(out)

        val stickerBg = when (rng.nextInt(4)) {
            0    -> Color.WHITE
            1    -> Color.rgb(255, 254, 220)
            2    -> Color.rgb(218, 234, 255)
            else -> Color.rgb(255, 244, 234)
        }
        c.drawRoundRect(
            RectF(0f, 0f, total.toFloat(), total.toFloat()), corner, corner,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = stickerBg }
        )

        // Art clipped to slightly-smaller rounded rect
        val artPath = Path().apply {
            addRoundRect(
                RectF(outline.toFloat(), outline.toFloat(), (total - outline).toFloat(), (total - outline).toFloat()),
                corner * 0.65f, corner * 0.65f, Path.Direction.CW
            )
        }
        c.save()
        c.clipPath(artPath)
        c.drawBitmap(src, outline.toFloat(), outline.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
        c.restore()

        // Gloss sheen
        c.drawRoundRect(RectF(0f, 0f, total.toFloat(), total.toFloat()), corner, corner, Paint().apply {
            shader = LinearGradient(
                0f, 0f, total * 0.55f, total * 0.55f,
                intArrayOf(Color.argb(46, 255, 255, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
            )
        })

        if (rng.nextFloat() < 0.58f) addStickerPeel(c, total, rng)

        return out
    }

    // ── 7. Torn paper print ───────────────────────────────────────────────────

    private fun makeTornPaper(src: Bitmap, sz: Int, rng: java.util.Random): Bitmap {
        val margin = (sz * 0.09f).toInt().coerceAtLeast(6)
        val total  = sz + margin * 2
        val out    = Bitmap.createBitmap(total, total, Bitmap.Config.ARGB_8888)
        val c      = Canvas(out)

        // Clip to torn outline before filling
        val tornPath = buildTornPath(total, rng)
        c.save()
        c.clipPath(tornPath)

        c.drawColor(Color.rgb(
            (240 + rng.nextInt(12)).coerceIn(0, 255),
            (234 + rng.nextInt(12)).coerceIn(0, 255),
            (220 + rng.nextInt(14)).coerceIn(0, 255)
        ))

        grain(c, total, total, rng, grainAlpha = 35, r = 80, g = 65, b = 45, variance = 5f)

        c.drawBitmap(src, margin.toFloat(), margin.toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
        c.restore()

        // Fiber trails at torn boundary (drawn outside clip)
        addTornFibers(c, total, rng)

        return out
    }

    private fun buildTornPath(sz: Int, rng: java.util.Random): Path {
        val tear  = (sz * 0.065f).toInt().coerceAtLeast(2)
        val steps = 14
        fun j() = (rng.nextInt(tear * 2 + 1) - tear).toFloat()
        return Path().apply {
            moveTo(j(), j())
            for (i in 1..steps) lineTo(sz.toFloat() * i / steps + j(), j())
            for (i in 1..steps) lineTo(sz.toFloat() + j(), sz.toFloat() * i / steps + j())
            for (i in steps downTo 0) lineTo(sz.toFloat() * i / steps + j(), sz.toFloat() + j())
            for (i in steps downTo 1) lineTo(j(), sz.toFloat() * i / steps + j())
            close()
        }
    }

    // ── Imperfection helpers ──────────────────────────────────────────────────

    private fun grain(
        c: Canvas, w: Int, h: Int, rng: java.util.Random,
        grainAlpha: Int, r: Int, g: Int, b: Int, variance: Float
    ) {
        val gw = w / 6 + 2
        val gh = h / 6 + 2
        val px = IntArray(gw * gh) {
            val v = (rng.nextGaussian() * variance).toInt().coerceIn(-20, 20)
            Color.argb(grainAlpha, (r + v).coerceIn(0, 255), (g + v).coerceIn(0, 255), (b + v).coerceIn(0, 255))
        }
        val bmp = Bitmap.createBitmap(px, gw, gh, Bitmap.Config.ARGB_8888)
        c.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
        bmp.recycle()
    }

    private fun addWornCorners(c: Canvas, w: Int, h: Int, rng: java.util.Random, intensity: Float) {
        val sz = (minOf(w, h) * 0.10f * intensity).coerceAtLeast(1f)
        val p  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb((60 * intensity).toInt().coerceIn(1, 255), 15, 10, 5)
        }
        listOf(0f to 0f, w.toFloat() to 0f, 0f to h.toFloat(), w.toFloat() to h.toFloat())
            .forEach { (cx, cy) ->
                if (rng.nextFloat() < 0.35f) return@forEach
                val jitter = sz * (0.4f + rng.nextFloat() * 0.8f)
                c.drawPath(Path().apply {
                    moveTo(cx, cy)
                    lineTo(cx + if (cx < w / 2) jitter else -jitter, cy)
                    lineTo(cx, cy + if (cy < h / 2) jitter else -jitter)
                    close()
                }, p)
            }
    }

    private fun addScratches(c: Canvas, w: Int, h: Int, rng: java.util.Random, count: Int, alpha: Int = 35) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        repeat(count) {
            val x1  = rng.nextFloat() * w
            val y1  = rng.nextFloat() * h
            val len = w * 0.04f + rng.nextFloat() * w * 0.20f
            val ang = rng.nextFloat() * Math.PI.toFloat()
            p.strokeWidth = 0.5f + rng.nextFloat() * 1f
            p.color = Color.argb(alpha + rng.nextInt(18), 210, 210, 210)
            c.drawLine(x1, y1, x1 + cos(ang) * len, y1 + sin(ang) * len, p)
        }
    }

    private fun addFoldLine(c: Canvas, w: Int, h: Int, rng: java.util.Random, vertical: Boolean) {
        val pos    = if (vertical) w * (0.28f + rng.nextFloat() * 0.44f) else h * (0.28f + rng.nextFloat() * 0.44f)
        val jitter = rng.nextInt(4) - 2f
        c.drawLine(
            if (vertical) pos else 0f,
            if (vertical) 0f else pos,
            if (vertical) pos + jitter else w.toFloat(),
            if (vertical) h.toFloat() else pos + jitter,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.argb(32 + rng.nextInt(18), 0, 0, 0)
                strokeWidth = 2f + rng.nextFloat() * 2f
            }
        )
        c.drawLine(
            if (vertical) pos + 1.5f else 0f,
            if (vertical) 0f else pos + 1.5f,
            if (vertical) pos + 1.5f + jitter else w.toFloat(),
            if (vertical) h.toFloat() else pos + 1.5f + jitter,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = Color.argb(22 + rng.nextInt(14), 255, 255, 255)
                strokeWidth = 1.5f
            }
        )
    }

    private fun addTapeStrip(c: Canvas, w: Int, h: Int, rng: java.util.Random) {
        val tw  = w * 0.14f + rng.nextFloat() * w * 0.18f
        val th  = 5f + rng.nextInt(6)
        val rot = rng.nextFloat() * 10f - 5f
        val cx  = w * 0.2f + rng.nextFloat() * w * 0.6f
        val cy  = when (rng.nextInt(4)) {
            0    -> th * 1.4f
            1    -> h - th * 1.4f
            else -> h * 0.2f + rng.nextFloat() * h * 0.6f
        }
        c.save()
        c.rotate(rot, cx, cy)
        c.drawRect(cx - tw / 2f, cy - th / 2f, cx + tw / 2f, cy + th / 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(52 + rng.nextInt(28), 230, 210, 138)
        })
        c.drawLine(cx - tw / 2f, cy - th / 2f, cx + tw / 2f, cy - th / 2f, Paint().apply {
            color = Color.argb(28, 255, 255, 190); strokeWidth = 1f
        })
        c.restore()
    }

    private fun addThumbtack(c: Canvas, w: Int, h: Int, rng: java.util.Random) {
        val margin = minOf(w, h) * 0.08f
        val cx = when (rng.nextInt(4)) {
            0, 1 -> margin + rng.nextFloat() * (w - margin * 2)
            2    -> margin
            else -> w - margin
        }
        val cy = when (rng.nextInt(4)) {
            0    -> margin
            1    -> h - margin
            else -> margin + rng.nextFloat() * (h - margin * 2)
        }
        val r = minOf(w, h) * 0.026f
        c.drawCircle(cx + r * 0.5f, cy + r * 0.5f, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(55, 0, 0, 0)
        })
        c.drawCircle(cx, cy, r, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(158, 152, 144)
        })
        c.drawCircle(cx - r * 0.3f, cy - r * 0.3f, r * 0.38f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(188, 240, 238, 235)
        })
    }

    private fun applyScissorEdge(c: Canvas, w: Int, h: Int, rng: java.util.Random) {
        val sides = mutableListOf(0, 1, 2, 3).also { it.shuffle(rng) }.take(1 + rng.nextInt(2))
        val jagg  = (minOf(w, h) * 0.016f).coerceAtLeast(1f)
        val steps = 22
        val p     = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color       = Color.argb(55, 195, 188, 178)
            style       = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        sides.forEach { side ->
            val path = Path()
            for (i in 0..steps) {
                val t   = i.toFloat() / steps
                val off = rng.nextFloat() * jagg * 2 - jagg
                val x   = if (side < 2) w * t else (if (side == 2) off else w + off)
                val y   = if (side < 2) (if (side == 0) off else h + off) else h * t
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            c.drawPath(path, p)
        }
    }

    private fun addStickerPeel(c: Canvas, sz: Int, rng: java.util.Random) {
        val peelSz = sz * 0.11f + rng.nextFloat() * sz * 0.09f
        val corner = rng.nextInt(4)
        val px     = if (corner % 2 == 0) 0f else sz.toFloat()
        val py     = if (corner < 2) 0f else sz.toFloat()
        val dx     = if (px == 0f) peelSz else -peelSz
        val dy     = if (py == 0f) peelSz else -peelSz

        val path = Path().apply {
            moveTo(px, py); lineTo(px + dx, py); lineTo(px, py + dy); close()
        }
        c.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color      = Color.argb(36, 0, 0, 0)
            maskFilter = BlurMaskFilter(peelSz * 0.35f, BlurMaskFilter.Blur.NORMAL)
        })
        c.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(238, 234, 226) })
        c.drawLine(px + dx, py, px, py + dy, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(52, 155, 145, 130); strokeWidth = 1f
        })
    }

    private fun addTornFibers(c: Canvas, sz: Int, rng: java.util.Random) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 0.8f }
        repeat(10 + rng.nextInt(10)) {
            val side = rng.nextInt(4)
            val pos  = rng.nextFloat() * sz
            val len  = sz * 0.018f + rng.nextFloat() * sz * 0.038f
            val jit  = (rng.nextInt(5) - 2).toFloat()
            p.color  = Color.argb(28 + rng.nextInt(38), 195, 178, 148)
            when (side) {
                0    -> c.drawLine(pos, 0f,         pos + jit, len,      p)
                1    -> c.drawLine(pos, sz.toFloat(), pos + jit, sz - len, p)
                2    -> c.drawLine(0f,  pos,        len,       pos + jit, p)
                else -> c.drawLine(sz.toFloat(), pos, sz - len, pos + jit, p)
            }
        }
    }

    // ── Vignette ──────────────────────────────────────────────────────────────

    private fun drawVignette(canvas: Canvas, width: Int, height: Int) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
            shader = RadialGradient(
                width / 2f, height / 2f, maxOf(width, height) * 0.68f,
                intArrayOf(Color.TRANSPARENT, Color.argb(85, 0, 0, 0)),
                floatArrayOf(0.48f, 1f), Shader.TileMode.CLAMP
            )
        })
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private fun sortByRelevance(items: List<RenderItem>): List<RenderItem> =
        if (items.any { it.image.playcount > 0 }) items.sortedByDescending { it.image.playcount }
        else items.sortedBy { it.image.rank }
}
