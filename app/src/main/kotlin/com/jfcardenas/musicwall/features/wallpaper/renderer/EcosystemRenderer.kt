package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.*
import androidx.palette.graphics.Palette
import kotlin.math.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * EcosystemRenderer — generative art born from the listener's musical identity.
 *
 * Album art is NEVER shown as a grid. Each cover is dissolved into raw material:
 * colour fields, curl-noise flow lines, scattered light, and hidden geometry.
 * The result feels like a lucid dream — every viewing reveals new details.
 *
 * Rendering layers (bottom → top):
 *   0  Deep space base (#030306)
 *   1  Colour landscape   — palette blobs fused into an abstract colour field
 *   2  Dissolved hero     — three blur levels: aura / haze / ghost
 *   3  Flow lines         — curl-noise vector field, ~700 streamlines
 *   4  Sacred geometry    — Flower of Life + Golden Spiral, ≤18 % opacity
 *   5  Particle field     — 280 glowing specks distributed across the surface
 *   6  Atmospheric glows  — large soft radial gradients per album colour
 *   7  Vignette + colour grade
 */
@Singleton
class EcosystemRenderer @Inject constructor() : WallpaperRenderer {

    override val id        = "ecosystem"
    override val isPremium = false

    companion object {
        private const val MAX_ITEMS  = 12
        private const val FLOW_LINES = 700
        private const val FLOW_STEPS = 90
        private const val NOISE_EPS  = 0.01f
        private val GOLDEN_ANGLE     = Math.toRadians(137.508).toFloat()
        // Logarithmic growth factor for a golden spiral: b = ln(φ) / (π/2)
        private val GOLDEN_B         = ln(1.618034f) / (PI.toFloat() / 2f)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entry point
    // ─────────────────────────────────────────────────────────────────────────

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap {
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        if (items.isEmpty()) {
            canvas.drawColor(Color.parseColor("#030306"))
            return result
        }

        val sorted   = sortByRelevance(items).take(MAX_ITEMS)
        val palettes = sorted.map { Palette.from(scaledForPalette(it.bitmap)).generate() }

        // Two independent seeded RNGs → deterministic but uncorrelated passes
        val seed  = sorted.fold(0L) { acc, it -> acc * 31L + it.image.name.hashCode() }
        val rng1  = java.util.Random(seed)
        val rng2  = java.util.Random(seed * 6364136223846793005L + 1442695040888963407L)
        val noiseOx = rng1.nextFloat() * 200f
        val noiseOy = rng1.nextFloat() * 200f

        // ── 0: Deep space base ─────────────────────────────────────────────
        canvas.drawColor(Color.parseColor("#030306"))

        // ── 1: Colour landscape ────────────────────────────────────────────
        val colorField = buildColorField(sorted, palettes, width / 4, height / 4)
        canvas.drawBitmap(
            colorField, null, RectF(0f, 0f, width.toFloat(), height.toFloat()),
            Paint(Paint.FILTER_BITMAP_FLAG).apply { alpha = 210 }
        )
        // Crush blacks — deepen the void between colour clouds
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
            color = Color.argb(155, 2, 2, 8)
        })

        // ── 2: Dissolved hero ──────────────────────────────────────────────
        drawDissolvedHero(canvas, sorted[0].bitmap, width, height)

        // ── 3: Flow lines ──────────────────────────────────────────────────
        drawFlowLines(canvas, colorField, palettes, rng1, noiseOx, noiseOy, width, height)
        colorField.recycle()

        // ── 4: Sacred geometry ─────────────────────────────────────────────
        drawSacredGeometry(canvas, palettes[0], sorted.size, width, height)

        // ── 5: Particles ───────────────────────────────────────────────────
        drawParticles(canvas, palettes, rng2, width, height)

        // ── 6: Atmospheric glows ───────────────────────────────────────────
        drawColorGlows(canvas, palettes, width, height)

        // ── 7: Vignette + colour grade ─────────────────────────────────────
        drawVignette(canvas, width, height)
        drawColorGrade(canvas, palettes[0], width, height)

        return result
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 1 — Colour landscape
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Paints soft radial colour blobs from every album's palette at 1/4
     * resolution.  When upscaled to full screen the bilinear filter creates a
     * natural atmospheric smear with no visible album shapes.
     */
    private fun buildColorField(
        items: List<RenderItem>, palettes: List<Palette>,
        w: Int, h: Int
    ): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c   = Canvas(bmp)
        c.drawColor(Color.parseColor("#030306"))

        items.forEachIndexed { i, _ ->
            val pal     = palettes.getOrNull(i) ?: return@forEachIndexed
            val dom     = pal.getDominantColor(Color.DKGRAY)
            val viv     = pal.getVibrantColor(dom)
            val mut     = pal.getMutedColor(dom)
            val progress = i.toFloat() / items.size.coerceAtLeast(1)
            val cx       = w * 0.5f + cos(i * GOLDEN_ANGLE) * w * 0.40f * progress
            val cy       = h * 0.5f + sin(i * GOLDEN_ANGLE) * h * 0.40f * progress
            val radius   = w * (0.65f - 0.30f * progress)
            val alpha    = (115 - i * 9).coerceAtLeast(28)

            c.drawRect(cx - radius, cy - radius, cx + radius, cy + radius,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        cx, cy, radius,
                        intArrayOf(
                            Color.argb(alpha,     Color.red(viv), Color.green(viv), Color.blue(viv)),
                            Color.argb(alpha / 2, Color.red(mut), Color.green(mut), Color.blue(mut)),
                            Color.argb(alpha / 5, Color.red(dom), Color.green(dom), Color.blue(dom)),
                            Color.TRANSPARENT
                        ),
                        floatArrayOf(0f, 0.28f, 0.58f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
            )
        }
        return bmp
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 2 — Dissolved hero
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * The top album/artist is rendered as three overlapping blur layers:
     * a wide diffuse aura, a medium haze, and a crisp but near-invisible ghost.
     * Recognisable in colour; dissolved in form.
     */
    private fun drawDissolvedHero(canvas: Canvas, bitmap: Bitmap, width: Int, height: Int) {
        val heroSz = width * 0.76f
        val cx     = width  * 0.50f
        val cy     = height * 0.50f

        // Aura: 1/10 res → maximum blur, widest spread
        val aW     = (heroSz / 10f).toInt().coerceAtLeast(1)
        val aSmall = Bitmap.createScaledBitmap(bitmap, aW, aW, false)
        val aLarge = Bitmap.createScaledBitmap(aSmall, (heroSz * 1.45f).toInt(), (heroSz * 1.45f).toInt(), true)
        aSmall.recycle()
        drawFadedImage(canvas, aLarge, cx, cy, heroSz * 1.45f, fadeStart = 0.40f, opacity = 0.38f)
        aLarge.recycle()

        // Haze: 1/5 res → moderate softness
        val hW     = (heroSz / 5f).toInt().coerceAtLeast(1)
        val hSmall = Bitmap.createScaledBitmap(bitmap, hW, hW, false)
        val hLarge = Bitmap.createScaledBitmap(hSmall, heroSz.toInt(), heroSz.toInt(), true)
        hSmall.recycle()
        drawFadedImage(canvas, hLarge, cx, cy, heroSz, fadeStart = 0.58f, opacity = 0.28f)
        hLarge.recycle()

        // Ghost: original quality, barely visible — fine detail at very low opacity
        drawFadedImage(canvas, bitmap, cx, cy, heroSz * 0.60f, fadeStart = 0.68f, opacity = 0.13f)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 3 — Flow lines (curl-noise vector field)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Traces [FLOW_LINES] streamlines through a two-octave curl-noise field.
     * 1/3 of lines are coloured with vibrant palette hues (energy streaks);
     * 2/3 sample the colour landscape for a soft atmospheric flow.
     *
     * A reusable FloatArray avoids 63 000 Pair allocations in the inner loop.
     */
    private fun drawFlowLines(
        canvas: Canvas,
        colorField: Bitmap,
        palettes: List<Palette>,
        rng: java.util.Random,
        noiseOx: Float, noiseOy: Float,
        width: Int, height: Int
    ) {
        val noiseScale = 2.9f / width.toFloat()
        val stepLen    = width * 0.0023f
        val curlOut    = FloatArray(2)       // reused each step — zero heap pressure

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style      = Paint.Style.STROKE
            strokeCap  = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val path = Path()

        repeat(FLOW_LINES) {
            val sx        = rng.nextFloat() * width
            val sy        = rng.nextFloat() * height
            val isEnergy  = rng.nextFloat() < 0.33f
            val lineColor = if (isEnergy) {
                val pal = palettes[rng.nextInt(palettes.size)]
                pal.getVibrantColor(pal.getDominantColor(Color.WHITE))
            } else {
                sampleField(colorField, sx, sy, width, height)
            }
            val alpha     = if (isEnergy) 45 + rng.nextInt(40) else 22 + rng.nextInt(35)
            val stroke    = if (isEnergy) 0.6f + rng.nextFloat() * 0.8f
                            else          0.3f + rng.nextFloat() * 0.7f

            paint.color       = Color.argb(alpha,
                Color.red(lineColor), Color.green(lineColor), Color.blue(lineColor))
            paint.strokeWidth = stroke

            path.reset()
            var x = sx
            var y = sy
            path.moveTo(x, y)

            for (step in 0 until FLOW_STEPS) {
                curlAt(x * noiseScale + noiseOx, y * noiseScale + noiseOy, curlOut)
                x += curlOut[0] * stepLen
                y += curlOut[1] * stepLen
                if (x < -width  * 0.12f || x > width  * 1.12f ||
                    y < -height * 0.12f || y > height * 1.12f) break
                path.lineTo(x, y)
            }
            canvas.drawPath(path, paint)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 4 — Sacred geometry
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawSacredGeometry(
        canvas: Canvas, palette: Palette, numItems: Int, width: Int, height: Int
    ) {
        val vibrant = palette.getVibrantColor(Color.WHITE)
        val muted   = palette.getMutedColor(vibrant)
        val cx      = width  * 0.50f
        val cy      = height * 0.50f

        val folPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style       = Paint.Style.STROKE
            strokeWidth = 0.55f
            color       = Color.argb(16,
                Color.red(vibrant), Color.green(vibrant), Color.blue(vibrant))
        }
        drawFlowerOfLife(canvas, cx, cy, width * 0.095f, folPaint)

        drawGoldenSpiral(canvas,
            cx + width * 0.07f, cy - height * 0.04f,
            width * 0.37f, vibrant, alpha = 18)

        if (numItems >= 5) {
            drawGoldenSpiral(canvas,
                cx - width * 0.18f, cy + height * 0.13f,
                width * 0.20f, muted, alpha = 12)
        }
    }

    private fun drawFlowerOfLife(canvas: Canvas, cx: Float, cy: Float, r: Float, paint: Paint) {
        canvas.drawCircle(cx, cy, r, paint)
        for (i in 0 until 6) {
            val a = i * PI.toFloat() / 3f
            canvas.drawCircle(cx + cos(a) * r, cy + sin(a) * r, r, paint)
        }
        canvas.drawCircle(cx, cy, r * 2f, paint)  // outer bounding circle
    }

    private fun drawGoldenSpiral(
        canvas: Canvas, cx: Float, cy: Float, maxR: Float, color: Int, alpha: Int
    ) {
        val path  = Path()
        var theta = 0f
        var first = true
        while (theta < 6f * PI.toFloat()) {
            val r  = 2f * exp(GOLDEN_B * theta)
            if (r > maxR) break
            val px = cx + r * cos(theta)
            val py = cy + r * sin(theta)
            if (first) { path.moveTo(px, py); first = false } else path.lineTo(px, py)
            theta += 0.04f
        }
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style       = Paint.Style.STROKE
            strokeWidth = 0.75f
            this.color  = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        })
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 5 — Particle field
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawParticles(
        canvas: Canvas, palettes: List<Palette>,
        rng: java.util.Random, width: Int, height: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(280) { i ->
            val x   = rng.nextFloat() * width
            val y   = rng.nextFloat() * height
            val pal = palettes[i % palettes.size]
            val dom = pal.getDominantColor(Color.WHITE)
            val viv = pal.getVibrantColor(dom)
            val col = if (rng.nextBoolean()) viv else dom
            val sz  = 0.7f + rng.nextFloat() * 3.3f
            val a   = 50 + rng.nextInt(115)

            if (sz > 2.2f) {
                paint.color = Color.argb(a / 6,
                    Color.red(col), Color.green(col), Color.blue(col))
                canvas.drawCircle(x, y, sz * 3.2f, paint)
            }
            paint.color = Color.argb(a, Color.red(col), Color.green(col), Color.blue(col))
            canvas.drawCircle(x, y, sz, paint)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 6 — Atmospheric colour glows
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawColorGlows(canvas: Canvas, palettes: List<Palette>, width: Int, height: Int) {
        palettes.take(7).forEachIndexed { i, pal ->
            val dom    = pal.getDominantColor(Color.DKGRAY)
            val viv    = pal.getVibrantColor(dom)
            val p      = i.toFloat() / 7f
            val cx     = width  * 0.5f + cos(i * GOLDEN_ANGLE) * width  * 0.27f * p
            val cy     = height * 0.5f + sin(i * GOLDEN_ANGLE) * height * 0.27f * p
            val radius = width  * (0.50f - 0.22f * p)
            val alpha  = (68 - i * 9).coerceAtLeast(16)

            canvas.drawRect(cx - radius, cy - radius, cx + radius, cy + radius,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        cx, cy, radius,
                        intArrayOf(
                            Color.argb(alpha,     Color.red(viv), Color.green(viv), Color.blue(viv)),
                            Color.argb(alpha / 3, Color.red(dom), Color.green(dom), Color.blue(dom)),
                            Color.TRANSPARENT
                        ),
                        floatArrayOf(0f, 0.38f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer 7 — Vignette + colour grade
    // ─────────────────────────────────────────────────────────────────────────

    private fun drawVignette(canvas: Canvas, width: Int, height: Int) {
        val w = width.toFloat()
        val h = height.toFloat()

        canvas.drawRect(0f, 0f, w, h, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w / 2f, h / 2f, maxOf(width, height) * 0.68f,
                intArrayOf(Color.TRANSPARENT, Color.argb(200, 0, 0, 5)),
                floatArrayOf(0.28f, 1f),
                Shader.TileMode.CLAMP
            )
        })
        canvas.drawRect(0f, 0f, w, h * 0.28f, Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h * 0.28f,
                intArrayOf(Color.argb(175, 0, 0, 5), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP)
        })
        canvas.drawRect(0f, h * 0.72f, w, h, Paint().apply {
            shader = LinearGradient(0f, h * 0.72f, 0f, h,
                intArrayOf(Color.TRANSPARENT, Color.argb(200, 0, 0, 5)),
                null, Shader.TileMode.CLAMP)
        })
    }

    /**
     * Applies a subtle tint derived from the hero's dominant colour plus a
     * fixed dark-indigo bias, unifying the palette across all generated art.
     */
    private fun drawColorGrade(canvas: Canvas, palette: Palette, width: Int, height: Int) {
        val dom = palette.getDominantColor(Color.DKGRAY)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply {
            color = Color.argb(30,
                (Color.red(dom)   / 8 + 5).coerceAtMost(255),
                (Color.green(dom) / 8 + 1).coerceAtMost(255),
                (Color.blue(dom)  / 8 + 32).coerceAtMost(255)
            )
        })
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared drawing primitive
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders [bitmap] into an offscreen ARGB buffer, then applies a radial
     * DST_IN mask — opaque at centre, transparent beyond [fadeStart] × radius —
     * producing a circular dissolve when composited onto [canvas].
     */
    private fun drawFadedImage(
        canvas: Canvas, bitmap: Bitmap,
        cx: Float, cy: Float, size: Float,
        fadeStart: Float, opacity: Float
    ) {
        val sz   = size.toInt().coerceAtLeast(2)
        val half = size / 2f

        val off  = Bitmap.createBitmap(sz, sz, Bitmap.Config.ARGB_8888)
        val offC = Canvas(off)

        offC.drawBitmap(bitmap, null, RectF(0f, 0f, sz.toFloat(), sz.toFloat()),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

        // DST_IN retains destination alpha proportional to source alpha.
        // BLACK (α=255) at centre → preserve; TRANSPARENT (α=0) at edge → erase.
        offC.drawRect(0f, 0f, sz.toFloat(), sz.toFloat(), Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            shader   = RadialGradient(
                sz / 2f, sz / 2f, sz / 2f,
                intArrayOf(Color.BLACK, Color.BLACK, Color.TRANSPARENT),
                floatArrayOf(0f, fadeStart, 1f),
                Shader.TileMode.CLAMP
            )
        })

        canvas.drawBitmap(off, cx - half, cy - half,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = (opacity * 255f).toInt() })
        off.recycle()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Math — curl-noise vector field
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Writes the 2-D curl of a two-octave value-noise field into [out].
     *   out[0] = vx = -∂N/∂y
     *   out[1] = vy =  ∂N/∂x
     * Curl is divergence-free, so streamlines never converge to a sink.
     */
    private fun curlAt(x: Float, y: Float, out: FloatArray) {
        fun n(nx: Float, ny: Float) =
            valueNoise(nx, ny) * 0.65f +
            valueNoise(nx * 2.7f + 13.1f, ny * 2.7f + 7.5f) * 0.35f

        out[0] = -(n(x, y + NOISE_EPS) - n(x, y - NOISE_EPS)) / (2f * NOISE_EPS)
        out[1] =  (n(x + NOISE_EPS, y) - n(x - NOISE_EPS, y)) / (2f * NOISE_EPS)
    }

    /** Smooth 2-D value noise via bilinear interpolation of a hashed lattice. */
    private fun valueNoise(x: Float, y: Float): Float {
        val xi = x.toInt()
        val yi = y.toInt()
        val xf = x - xi
        val yf = y - yi
        val xt = xf * xf * (3f - 2f * xf)   // smoothstep
        val yt = yf * yf * (3f - 2f * yf)
        return lerp(
            lerp(hash2(xi,     yi),     hash2(xi + 1, yi    ), xt),
            lerp(hash2(xi,     yi + 1), hash2(xi + 1, yi + 1), xt),
            yt
        )
    }

    /** Integer lattice hash → float in [0, 1]. PCG-inspired bit scramble. */
    private fun hash2(xi: Int, yi: Int): Float {
        var n = xi * 374761393 + yi * 668265263
        n = (n xor (n ushr 13)) * 1274126177
        return ((n xor (n ushr 16)) and 0x7FFFFFFF).toFloat() / 2147483647f
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    // ─────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────

    private fun sampleField(bmp: Bitmap, x: Float, y: Float, w: Int, h: Int): Int {
        val fx = (x / w * bmp.width).toInt().coerceIn(0, bmp.width  - 1)
        val fy = (y / h * bmp.height).toInt().coerceIn(0, bmp.height - 1)
        return bmp.getPixel(fx, fy)
    }

    private fun sortByRelevance(items: List<RenderItem>): List<RenderItem> =
        if (items.any { it.image.playcount > 0 }) items.sortedByDescending { it.image.playcount }
        else items.sortedBy { it.image.rank }

    private fun scaledForPalette(bitmap: Bitmap): Bitmap =
        Bitmap.createScaledBitmap(bitmap, 64, 64, false)
}
