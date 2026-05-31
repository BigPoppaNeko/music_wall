package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.*
import kotlin.math.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ManchesterWallRenderer — pared de habitación adolescente, generada completamente por código.
 *
 * Sin imagen externa. Sin JSON. Todo sale del historial de Last.fm del usuario:
 * los nombres de los artistas aparecen como stencils en la pared, y las portadas
 * de sus álbumes más escuchados llenan los marcos polaroid.
 *
 * Layers (bottom → top):
 *   1. Pared oscura con textura de yeso (noise multicapa)
 *   2. Glow de lámpara incandescente (esquina inferior izquierda)
 *   3. Stencils de artistas en zonas laterales y superior
 *   4. Grid 3×5 de marcos polaroid con portadas
 *   5. Cordón de fairy lights en la parte superior
 *   6. Viñeta final
 */
@Singleton
class ManchesterWallRenderer @Inject constructor() : WallpaperRenderer {

    override val id        = "scene_manchester"
    override val isPremium = false

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap {
        val sorted = sortByRelevance(items)
        val seed   = sorted.fold(0L) { acc, it -> acc * 31L + it.image.name.hashCode() }
        val rng    = java.util.Random(seed)

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        drawWall(canvas, width, height, rng)
        drawLampGlow(canvas, width, height)
        if (sorted.isNotEmpty()) drawArtistStencils(canvas, sorted, width, height, rng)
        drawPolaroidGrid(canvas, sorted, width, height, rng)
        drawFairyLights(canvas, width, height, rng)
        drawVignette(canvas, width, height)

        return result
    }

    // ── 1. Pared de yeso oscura ───────────────────────────────────────────────

    private fun drawWall(canvas: Canvas, w: Int, h: Int, rng: java.util.Random) {
        canvas.drawColor(Color.rgb(18, 12, 7))

        // Manchas grandes de humedad (baja frecuencia)
        val lfW = w / 18 + 2
        val lfH = h / 18 + 2
        val lfPx = IntArray(lfW * lfH) {
            val v = (rng.nextGaussian() * 13).toInt().coerceIn(-38, 38)
            Color.rgb(
                (22 + v).coerceIn(0, 255),
                (15 + v * 8 / 10).coerceIn(0, 255),
                (8  + v * 6 / 10).coerceIn(0, 255)
            )
        }
        val lfBmp = Bitmap.createBitmap(lfPx, lfW, lfH, Bitmap.Config.ARGB_8888)
        canvas.drawBitmap(lfBmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()),
            Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 200 })
        lfBmp.recycle()

        // Grano fino de yeso (media frecuencia)
        val mfW = w / 5 + 2
        val mfH = h / 5 + 2
        val mfPx = IntArray(mfW * mfH) {
            val v = (rng.nextGaussian() * 5).toInt().coerceIn(-16, 16)
            Color.argb(
                60,
                (115 + v).coerceIn(0, 255),
                (82  + v * 8 / 10).coerceIn(0, 255),
                (50  + v * 6 / 10).coerceIn(0, 255)
            )
        }
        val mfBmp = Bitmap.createBitmap(mfPx, mfW, mfH, Bitmap.Config.ARGB_8888)
        canvas.drawBitmap(mfBmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()),
            Paint(Paint.FILTER_BITMAP_FLAG))
        mfBmp.recycle()
    }

    // ── 2. Glow de lámpara incandescente ──────────────────────────────────────

    private fun drawLampGlow(canvas: Canvas, w: Int, h: Int) {
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply {
            shader = RadialGradient(
                w * 0.07f, h * 0.90f, w * 1.05f,
                intArrayOf(
                    Color.argb(115, 200, 118, 38),
                    Color.argb(52,  155, 82, 18),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.30f, 1f),
                Shader.TileMode.CLAMP
            )
        })
        // Halo tenue desde el cordón de luces (parte superior, centro)
        canvas.drawRect(0f, 0f, w.toFloat(), h * 0.22f, Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, h * 0.22f,
                intArrayOf(Color.argb(52, 210, 165, 75), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
        })
    }

    // ── 3. Stencils de artistas ───────────────────────────────────────────────

    private fun drawArtistStencils(
        canvas: Canvas, items: List<RenderItem>,
        w: Int, h: Int, rng: java.util.Random
    ) {
        val artists = items.map { it.image.artistName }
            .filter { it.isNotBlank() }
            .distinct()
            .take(14)
            .toMutableList()
            .also { it.shuffle(rng) }

        if (artists.isEmpty()) return

        val mL = w * 0.12f; val mR = w * 0.12f
        val mT = h * 0.08f; val mB = h * 0.20f

        data class Zone(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val preferVertical: Boolean)
        val zones = listOf(
            Zone(0f,       mT,       mL * 0.88f, h - mB,      true),   // izquierda
            Zone(w - mR * 0.88f, mT, w.toFloat(), h - mB,     true),   // derecha
            Zone(mL,       mT * 0.1f, w - mR,    mT * 0.88f,  false),  // arriba
            Zone(mL,       h - mB * 0.88f, w - mR, h.toFloat(), false), // abajo
        )

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        for ((i, artist) in artists.withIndex()) {
            val zone  = zones[i % zones.size]
            val size  = 26f + rng.nextFloat() * 42f
            val alpha = 20 + rng.nextInt(32)
            val angle = if (zone.preferVertical) {
                if (rng.nextBoolean()) 90f else -90f
            } else {
                rng.nextFloat() * 10f - 5f
            }

            paint.textSize = size
            paint.color = when (rng.nextInt(5)) {
                0    -> Color.argb(alpha, 228, 218, 192)
                1    -> Color.argb(alpha, 188, 152, 108)
                2    -> Color.argb(alpha, 175, 48, 38)
                3    -> Color.argb(alpha, 155, 148, 128)
                else -> Color.argb(alpha, 210, 205, 185)
            }

            val text  = artist.uppercase().take(16)
            val textW = paint.measureText(text)
            val zoneW = (zone.x2 - zone.x1).coerceAtLeast(1f)
            val zoneH = (zone.y2 - zone.y1 - size).coerceAtLeast(0f)

            val cx = zone.x1 + rng.nextFloat() * zoneW
            val cy = zone.y1 + size + rng.nextFloat() * zoneH

            canvas.save()
            canvas.rotate(angle, cx, cy)
            canvas.drawText(text, cx - textW / 2f, cy, paint)
            canvas.restore()
        }
    }

    // ── 4. Grid 3×5 de polaroids ──────────────────────────────────────────────

    private fun drawPolaroidGrid(
        canvas: Canvas, items: List<RenderItem>,
        w: Int, h: Int, rng: java.util.Random
    ) {
        if (items.isEmpty()) return

        val cols = 3; val rows = 5
        val mL   = w * 0.12f;  val mR   = w * 0.12f
        val mT   = h * 0.08f;  val mB   = h * 0.20f
        val gapH = w * 0.018f; val gapV = h * 0.018f

        val frameW = (w - mL - mR - gapH * (cols - 1)) / cols
        val frameH = (h - mT - mB - gapV * (rows - 1)) / rows

        var slot = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val item = items[slot % items.size]
                val x = mL + col * (frameW + gapH)
                val y = mT + row * (frameH + gapV)
                drawPolaroid(canvas, item, x, y, frameW, frameH, rng)
                slot++
            }
        }
    }

    private fun drawPolaroid(
        canvas: Canvas, item: RenderItem,
        x: Float, y: Float, fw: Float, fh: Float,
        rng: java.util.Random
    ) {
        val borderS  = (fw * 0.050f).coerceAtLeast(3f)
        val captionH = (fh * 0.190f).coerceAtLeast(16f)
        val photoW   = fw - borderS * 2
        val photoH   = fh - borderS - captionH
        val photoX   = x + borderS
        val photoY   = y + borderS

        val rot = rng.nextFloat() * 4f - 2f
        val cx  = x + fw / 2f
        val cy  = y + fh / 2f

        canvas.save()
        canvas.rotate(rot, cx, cy)

        // Sombra
        canvas.drawRect(
            x + fw * 0.045f, y + fh * 0.045f,
            x + fw + fw * 0.045f, y + fh + fh * 0.045f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color      = Color.argb(88, 0, 0, 0)
                maskFilter = BlurMaskFilter(fw * 0.055f, BlurMaskFilter.Blur.NORMAL)
            }
        )

        // Cuerpo del polaroid (papel fotográfico)
        val paperTint = rng.nextInt(10) - 4
        canvas.drawRect(x, y, x + fw, y + fh, Paint().apply {
            color = Color.rgb(
                (240 + paperTint).coerceIn(220, 255),
                (234 + paperTint).coerceIn(215, 252),
                (218 + paperTint).coerceIn(198, 242)
            )
        })

        // Portada recortada con tinte de lámpara
        if (photoW >= 2 && photoH >= 2) {
            val scaled = scaleCrop(item.bitmap, photoW.toInt(), photoH.toInt())
            canvas.drawBitmap(
                scaled, photoX, photoY,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = lampColorFilter
                }
            )
            // Gloss diagonal sobre la foto
            canvas.drawRect(photoX, photoY, photoX + photoW, photoY + photoH, Paint().apply {
                shader = LinearGradient(
                    photoX, photoY,
                    photoX + photoW * 0.55f, photoY + photoH * 0.55f,
                    intArrayOf(Color.argb(25, 255, 255, 255), Color.TRANSPARENT),
                    floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
                )
            })
            scaled.recycle()
        }

        // Caption: nombre del álbum en la zona blanca inferior
        val caption = item.image.name.take(22)
        if (caption.isNotEmpty()) {
            val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = (captionH * 0.36f).coerceAtLeast(10f)
                color    = Color.argb(138, 25, 16, 8)
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
            val tx = x + (fw - tp.measureText(caption)) / 2f
            val ty = photoY + photoH + captionH * 0.60f
            canvas.drawText(caption, tx.coerceAtLeast(x + 3f), ty, tp)
        }

        // Esquinas desgastadas
        drawWornCorners(canvas, x, y, fw, fh, rng)

        // Chincheta (38% de probabilidad)
        if (rng.nextFloat() < 0.38f) drawThumbtack(canvas, x, y, fw, rng)

        canvas.restore()
    }

    // ── 5. Fairy lights ───────────────────────────────────────────────────────

    private fun drawFairyLights(canvas: Canvas, w: Int, h: Int, rng: java.util.Random) {
        val yBase   = h * 0.038f
        val amp     = h * 0.011f
        val spacing = w * 0.052f
        val count   = (w / spacing).toInt() + 2

        // Cordón
        val cordPath = Path()
        for (i in 0..count) {
            val fx = i * spacing
            val fy = (yBase + sin(i * 0.85) * amp).toFloat()
            if (i == 0) cordPath.moveTo(fx, fy) else cordPath.lineTo(fx, fy)
        }
        canvas.drawPath(cordPath, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style       = Paint.Style.STROKE
            strokeWidth = 1.5f
            color       = Color.argb(110, 55, 38, 18)
        })

        // Bombillas
        for (i in 0 until count) {
            val fx    = (i + 0.5f) * spacing + (rng.nextFloat() - 0.5f) * spacing * 0.14f
            val fy    = (yBase + sin(i * 0.85) * amp).toFloat()
            val warm  = rng.nextFloat() < 0.82f
            val bulbC = if (warm) Color.argb(255, 252, 215, 95) else Color.argb(255, 195, 195, 252)
            val r     = Color.red(bulbC); val g = Color.green(bulbC); val b = Color.blue(bulbC)

            // Halo suave
            canvas.drawCircle(fx, fy, spacing * 0.26f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    fx, fy, spacing * 0.26f,
                    intArrayOf(Color.argb(55, r, g, b), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP
                )
            })
            // Cuerpo del bulbo
            canvas.drawCircle(fx, fy, spacing * 0.062f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bulbC
            })
        }
    }

    // ── 6. Viñeta ─────────────────────────────────────────────────────────────

    private fun drawVignette(canvas: Canvas, w: Int, h: Int) {
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply {
            shader = RadialGradient(
                w / 2f, h / 2f, maxOf(w, h) * 0.74f,
                intArrayOf(Color.TRANSPARENT, Color.argb(205, 0, 0, 0)),
                floatArrayOf(0.28f, 1f),
                Shader.TileMode.CLAMP
            )
        })
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private val lampColorFilter: ColorFilter = ColorMatrixColorFilter(
        ColorMatrix(floatArrayOf(
            1.07f, 0f,    0f,    0f,  7f,
            0f,    0.97f, 0f,    0f,  2f,
            0f,    0f,    0.89f, 0f, -5f,
            0f,    0f,    0f,    1f,  0f
        ))
    )

    private fun drawWornCorners(canvas: Canvas, x: Float, y: Float, fw: Float, fh: Float, rng: java.util.Random) {
        val sz = fw * 0.088f
        val corners = listOf(x to y, x + fw to y, x to y + fh, x + fw to y + fh)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(65, 10, 6, 2) }
        for ((cx, cy) in corners) {
            if (rng.nextFloat() < 0.42f) continue
            val jit = sz * (0.5f + rng.nextFloat() * 0.7f)
            val dx  = if (cx < x + fw / 2) jit else -jit
            val dy  = if (cy < y + fh / 2) jit else -jit
            canvas.drawPath(Path().apply {
                moveTo(cx, cy); lineTo(cx + dx, cy); lineTo(cx, cy + dy); close()
            }, paint)
        }
    }

    private fun drawThumbtack(canvas: Canvas, x: Float, y: Float, fw: Float, rng: java.util.Random) {
        val edge = fw * 0.09f
        val candidates = listOf(
            x + edge to y + edge,
            x + fw - edge to y + edge
        )
        val (tcx, tcy) = candidates[rng.nextInt(candidates.size)]
        val r = fw * 0.030f

        canvas.drawCircle(tcx + r * 0.4f, tcy + r * 0.4f, r,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(55, 0, 0, 0) })
        canvas.drawCircle(tcx, tcy, r,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(152, 144, 134) })
        canvas.drawCircle(tcx - r * 0.28f, tcy - r * 0.28f, r * 0.36f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(185, 238, 235, 228) })
    }

    private fun scaleCrop(src: Bitmap, tw: Int, th: Int): Bitmap {
        val tws = tw.coerceAtLeast(2); val ths = th.coerceAtLeast(2)
        val scale   = maxOf(tws.toFloat() / src.width, ths.toFloat() / src.height)
        val sw      = (src.width  * scale).toInt().coerceAtLeast(tws)
        val sh      = (src.height * scale).toInt().coerceAtLeast(ths)
        val scaled  = Bitmap.createScaledBitmap(src, sw, sh, true)
        val dx      = ((sw - tws) / 2).coerceAtLeast(0)
        val dy      = ((sh - ths) / 2).coerceAtLeast(0)
        val cropped = Bitmap.createBitmap(scaled, dx, dy, tws, ths)
        if (scaled !== src && scaled !== cropped) scaled.recycle()
        return cropped
    }

    private fun sortByRelevance(items: List<RenderItem>): List<RenderItem> =
        if (items.any { it.image.playcount > 0 }) items.sortedByDescending { it.image.playcount }
        else items.sortedBy { it.image.rank }
}
