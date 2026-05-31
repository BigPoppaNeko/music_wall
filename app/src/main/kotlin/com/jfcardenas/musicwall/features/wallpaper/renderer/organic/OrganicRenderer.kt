package com.jfcardenas.musicwall.features.wallpaper.renderer.organic

import android.content.Context
import android.graphics.*
import com.jfcardenas.musicwall.features.wallpaper.renderer.RenderItem
import com.jfcardenas.musicwall.features.wallpaper.renderer.WallpaperRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Random

/**
 * OrganicRenderer — integra portadas reales en escenas generadas por IA.
 *
 * Flujo de construcción:
 *   1. El script Python detect_layout.py procesa la imagen de fondo y produce
 *      un layout.json con la posición, rotación y zona de iluminación de cada slot.
 *   2. Ambos archivos (imagen + JSON) van en  assets/organic/<layoutId>.(jpg|json)
 *   3. Este renderer los carga, dibuja el fondo y coloca cada portada con:
 *        • Escalado/recorte al tamaño del slot
 *        • Rotación exacta del slot
 *        • Color grading: empuja los colores de la portada hacia el color de luz ambiental
 *        • Sombra suave basada en la dirección global de luz de la escena
 *        • Bordes suavizados (rounded corners)
 *        • Vignette final para cohesión
 *
 * No modifica ningún renderer existente ni la factory de wallpapers.
 */
class OrganicRenderer(
    private val context:     Context,
    val layoutId:            String,
    private val postProcess: Boolean = false,
) : WallpaperRenderer {

    override val id        = "organic/$layoutId"
    override val isPremium = false

    // Caché en memoria — un OrganicRenderer por layoutId
    private var cachedBackground: Bitmap?        = null
    private var cachedLayout:     OrganicLayout? = null

    // Transform del background: imagen → canvas (fill+center-crop)
    private var srcW  = 0
    private var srcH  = 0
    private var bgScl = 1f
    private var bgDx  = 0
    private var bgDy  = 0

    // ── API pública ───────────────────────────────────────────────────────────

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap =
        withContext(Dispatchers.Default) {

            val layout     = getLayout()
            val background = getBackground()

            val imgW = (background?.width  ?: width).coerceAtLeast(1)
            val imgH = (background?.height ?: height).coerceAtLeast(1)

            val result = Bitmap.createBitmap(imgW, imgH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)

            // 1 — Portadas detrás: el PNG tiene huecos transparentes donde se verán
            if (items.isNotEmpty()) {
                layout.slots.forEachIndexed { idx, slot ->
                    val item = items[idx % items.size]
                    renderSlotBehind(canvas, item.bitmap, slot, imgW, imgH, layout.lightingMap)
                }
            }

            // 2 — PNG de escena encima: sus marcos opacos cubren todo excepto los huecos
            if (background != null) {
                canvas.drawBitmap(background, 0f, 0f, null)
            } else {
                canvas.drawColor(parseColor(layout.lightingMap.ambientColor, Color.BLACK))
            }

            // 3 — Post-processing global o vignette básico
            if (postProcess) {
                drawAmbientOcclusion(canvas, layout, imgW, imgH)
                drawColorGrading(canvas, imgW, imgH)
                drawDirectionalLight(canvas, imgW, imgH)
                drawSoftVignette(canvas, imgW, imgH)
                drawFilmTexture(canvas, imgW, imgH)
            } else {
                drawVignette(canvas, imgW, imgH)
            }

            result
        }

    override fun release() {
        cachedBackground?.recycle()
        cachedBackground = null
        cachedLayout = null
        srcW = 0
    }

    // ── Carga de assets ───────────────────────────────────────────────────────

    private fun getLayout(): OrganicLayout {
        cachedLayout?.let { return it }
        return try {
            val json = context.assets.open("organic/$layoutId.json")
                .bufferedReader().readText()
            OrganicLayout.fromJson(json).also { cachedLayout = it }
        } catch (e: Exception) {
            OrganicLayout(id = layoutId)
        }
    }

    private fun getBackground(): Bitmap? {
        cachedBackground?.let { return it }
        val exts = listOf(".jpg", ".jpeg", ".png", ".webp")
        for (ext in exts) {
            try {
                val bmp = context.assets.open("organic/$layoutId$ext").use {
                    BitmapFactory.decodeStream(it)
                } ?: continue

                // Sin escala ni recorte: los slots se posicionan en el espacio nativo del asset
                srcW  = bmp.width
                srcH  = bmp.height
                bgScl = 1f
                bgDx  = 0
                bgDy  = 0

                return bmp.also { cachedBackground = it }
            } catch (_: Exception) {}
        }
        return null
    }

    // ── Renderizado de un slot ────────────────────────────────────────────────

    /** Coloca la portada exactamente en el hueco del marco; el PNG de escena ya aporta
     *  el tratamiento visual principal. Con postProcess activo añade contact shadow. */
    private fun renderSlotBehind(
        canvas:   Canvas,
        album:    Bitmap,
        slot:     OrganicSlot,
        cw:       Int,
        ch:       Int,
        lighting: OrganicLightingMap,
    ) {
        val pw = if (srcW > 0) (slot.width  * srcW).toInt().coerceAtLeast(4)
                 else          (slot.width  * cw  ).toInt().coerceAtLeast(4)
        val ph = if (srcH > 0) (slot.height * srcH).toInt().coerceAtLeast(4)
                 else          (slot.height * ch  ).toInt().coerceAtLeast(4)
        val px = if (srcW > 0) slot.x * srcW else slot.x * cw
        val py = if (srcH > 0) slot.y * srcH else slot.y * ch

        val scaled = scaleCropBitmap(album, pw, ph)
        val paint  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = buildLightingFilter(slot.lightingZone, strength = 0.18f)
        }
        canvas.save()
        canvas.rotate(slot.rotation, px + pw / 2f, py + ph / 2f)
        if (postProcess) drawContactShadow(canvas, px, py, pw.toFloat(), ph.toFloat())
        canvas.drawBitmap(scaled, px, py, paint)
        canvas.restore()
        if (scaled !== album) scaled.recycle()
    }

    private fun renderSlot(
        canvas:   Canvas,
        album:    Bitmap,
        slot:     OrganicSlot,
        cw:       Int,
        ch:       Int,
        lighting: OrganicLightingMap,
    ) {
        val pw: Int; val ph: Int; val px: Float; val py: Float
        if (srcW > 0) {
            val sW = srcW * bgScl
            val sH = srcH * bgScl
            pw = (slot.width  * sW).toInt().coerceAtLeast(4)
            ph = (slot.height * sH).toInt().coerceAtLeast(4)
            px = slot.x * sW - bgDx
            py = slot.y * sH - bgDy
        } else {
            pw = (slot.width  * cw).toInt().coerceAtLeast(4)
            ph = (slot.height * ch).toInt().coerceAtLeast(4)
            px = slot.x * cw
            py = slot.y * ch
        }
        val cx = px + pw / 2f
        val cy = py + ph / 2f

        val scaled = scaleCropBitmap(album, pw, ph)

        canvas.save()
        canvas.rotate(slot.rotation, cx, cy)

        drawSlotShadow(canvas, px, py, pw.toFloat(), ph.toFloat(),
                       shadowOffset(lighting.shadowDirection, pw.toFloat()),
                       cornerRadius = pw * 0.05f)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = buildLightingFilter(slot.lightingZone, strength = 0.28f)
        }
        drawRoundedBitmap(canvas, scaled, px, py, pw.toFloat(), ph.toFloat(),
                          cornerRadius = pw * 0.05f, paint = paint)

        canvas.restore()

        if (scaled !== album) scaled.recycle()
    }

    // ── Efectos de iluminación ────────────────────────────────────────────────

    /**
     * Color matrix que empuja los colores de la portada hacia el color ambiental
     * de esa zona de la escena. Strength 0.0 = sin efecto, 1.0 = color puro.
     */
    private fun buildLightingFilter(zoneRgb: List<Int>, strength: Float): ColorFilter {
        val r = (zoneRgb.getOrNull(0) ?: 128) / 255f
        val g = (zoneRgb.getOrNull(1) ?: 128) / 255f
        val b = (zoneRgb.getOrNull(2) ?: 128) / 255f

        val s  = strength.coerceIn(0f, 1f)
        val rs = 1f - s + s * r * 1.25f   // empuja hacia warm/cool
        val gs = 1f - s + s * g * 1.25f
        val bs = 1f - s + s * b * 1.25f

        val cm = ColorMatrix(floatArrayOf(
            rs,  0f,  0f,  0f, 0f,
            0f,  gs,  0f,  0f, 0f,
            0f,  0f,  bs,  0f, 0f,
            0f,  0f,  0f, 0.94f, 0f,   // ligera transparencia
        ))
        return ColorMatrixColorFilter(cm)
    }

    /** Dirección del desplazamiento de sombra según la fuente de luz. */
    private fun shadowOffset(direction: String, slotPx: Float): Pair<Float, Float> {
        val d = slotPx * 0.055f
        return when (direction) {
            "top-left"     -> -d to -d
            "top"          ->  0f to -d
            "top-right"    ->  d to -d
            "left"         -> -d to  0f
            "right"        ->  d to  0f
            "bottom-left"  -> -d to  d
            "bottom"       ->  0f to  d
            "bottom-right" ->  d to  d
            else           ->  d to  d
        }
    }

    // ── Post-processing global (Woodstock) ────────────────────────────────────

    /** Contact shadow: blur 15px, offset Y 4px, 25% opacidad. */
    private fun drawContactShadow(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(64, 0, 0, 0)
            maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRect(x, y + 4f, x + w, y + h + 4f, paint)
    }

    /** Ambient occlusion: sombra interior en bordes de cada slot, 7% intensidad. */
    private fun drawAmbientOcclusion(canvas: Canvas, layout: OrganicLayout, imgW: Int, imgH: Int) {
        val alpha = 18  // ~7% de 255
        layout.slots.forEach { slot ->
            val pw  = if (srcW > 0) slot.width  * srcW else slot.width  * imgW
            val ph  = if (srcH > 0) slot.height * srcH else slot.height * imgH
            val px  = if (srcW > 0) slot.x * srcW      else slot.x * imgW
            val py  = if (srcH > 0) slot.y * srcH      else slot.y * imgH
            val cx  = px + pw / 2f
            val cy  = py + ph / 2f
            val ewx = pw * 0.12f
            val ewy = ph * 0.12f
            val black = Color.argb(alpha, 0, 0, 0)

            canvas.save()
            canvas.rotate(slot.rotation, cx, cy)
            canvas.clipRect(px, py, px + pw, py + ph)

            Paint(Paint.ANTI_ALIAS_FLAG).let { p ->
                p.shader = LinearGradient(px, 0f, px + ewx, 0f, black, Color.TRANSPARENT, Shader.TileMode.CLAMP)
                canvas.drawRect(px, py, px + ewx, py + ph, p)
            }
            Paint(Paint.ANTI_ALIAS_FLAG).let { p ->
                p.shader = LinearGradient(px + pw, 0f, px + pw - ewx, 0f, black, Color.TRANSPARENT, Shader.TileMode.CLAMP)
                canvas.drawRect(px + pw - ewx, py, px + pw, py + ph, p)
            }
            Paint(Paint.ANTI_ALIAS_FLAG).let { p ->
                p.shader = LinearGradient(0f, py, 0f, py + ewy, black, Color.TRANSPARENT, Shader.TileMode.CLAMP)
                canvas.drawRect(px, py, px + pw, py + ewy, p)
            }
            Paint(Paint.ANTI_ALIAS_FLAG).let { p ->
                p.shader = LinearGradient(0f, py + ph, 0f, py + ph - ewy, black, Color.TRANSPARENT, Shader.TileMode.CLAMP)
                canvas.drawRect(px, py + ph - ewy, px + pw, py + ph, p)
            }

            canvas.restore()
        }
    }

    /** Color grading: overlay sépia cálido ~12% para unificar fondo y portadas. */
    private fun drawColorGrading(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint().apply { color = Color.argb(30, 180, 130, 70) }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    }

    /** Luz direccional cálida desde arriba-izquierda; sombra en la parte inferior. */
    private fun drawDirectionalLight(canvas: Canvas, w: Int, h: Int) {
        val wf = w.toFloat()
        val hf = h.toFloat()
        // Brillo superior cálido
        Paint(Paint.ANTI_ALIAS_FLAG).let { p ->
            p.shader = LinearGradient(0f, 0f, 0f, hf * 0.4f, Color.argb(40, 255, 200, 120), Color.TRANSPARENT, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, wf, hf, p)
        }
        // Sombra inferior
        Paint(Paint.ANTI_ALIAS_FLAG).let { p ->
            p.shader = LinearGradient(0f, hf, 0f, hf * 0.6f, Color.argb(50, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, wf, hf, p)
        }
        // Realce lateral izquierdo cálido
        Paint(Paint.ANTI_ALIAS_FLAG).let { p ->
            p.shader = LinearGradient(0f, 0f, wf * 0.3f, 0f, Color.argb(20, 255, 210, 140), Color.TRANSPARENT, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, wf, hf, p)
        }
    }

    /** Soft vignette: bordes -20%, centro sin cambios. */
    private fun drawSoftVignette(canvas: Canvas, w: Int, h: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                w / 2f, h / 2f, maxOf(w, h) * 0.75f,
                intArrayOf(Color.TRANSPARENT, Color.argb(51, 0, 0, 0)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    }

    /** Film texture: grain + polvo + arañazos sutiles sobre toda la composición. */
    private fun drawFilmTexture(canvas: Canvas, w: Int, h: Int) {
        val rng = Random(42L)

        // Grain: bitmap de ruido de 128×128 tileado
        val gs = 128
        val grainBmp = Bitmap.createBitmap(gs, gs, Bitmap.Config.ARGB_8888)
        val grainPx = IntArray(gs * gs) {
            val v = (128 + rng.nextInt(60) - 30).coerceIn(0, 255)
            Color.argb(35, v, v, v)
        }
        grainBmp.setPixels(grainPx, 0, gs, 0, 0, gs, gs)
        val grainPaint = Paint()
        var ty = 0
        while (ty < h) { var tx = 0; while (tx < w) { canvas.drawBitmap(grainBmp, tx.toFloat(), ty.toFloat(), grainPaint); tx += gs }; ty += gs }
        grainBmp.recycle()

        // Polvo: puntos blancos pequeños
        val dustPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(55, 255, 255, 255) }
        repeat(12) {
            canvas.drawCircle(rng.nextInt(w).toFloat(), rng.nextInt(h).toFloat(), 1f + rng.nextFloat() * 2f, dustPaint)
        }

        // Arañazos: líneas verticales tenues
        val scratchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(38, 255, 255, 255)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        repeat(3) {
            val sx = rng.nextInt(w).toFloat()
            val startY = h * 0.1f + rng.nextFloat() * h * 0.4f
            canvas.drawLine(sx, startY, sx, startY + h * 0.1f + rng.nextFloat() * h * 0.25f, scratchPaint)
        }
    }

    // ── Dibujo de primitivas ──────────────────────────────────────────────────

    private fun drawSlotShadow(
        canvas:       Canvas,
        x:            Float,
        y:            Float,
        w:            Float,
        h:            Float,
        offset:       Pair<Float, Float>,
        cornerRadius: Float,
    ) {
        val (ox, oy) = offset
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 0, 0, 0)
            maskFilter = BlurMaskFilter(w * 0.06f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
            RectF(x + ox, y + oy, x + w + ox, y + h + oy),
            cornerRadius, cornerRadius, paint,
        )
    }

    private fun drawRoundedBitmap(
        canvas:       Canvas,
        bmp:          Bitmap,
        x:            Float,
        y:            Float,
        w:            Float,
        h:            Float,
        cornerRadius: Float,
        paint:        Paint,
    ) {
        val saved = canvas.save()
        canvas.clipPath(Path().apply {
            addRoundRect(RectF(x, y, x + w, y + h), cornerRadius, cornerRadius, Path.Direction.CW)
        })
        canvas.drawBitmap(bmp, x, y, paint)
        canvas.restoreToCount(saved)
    }

    private fun drawVignette(canvas: Canvas, w: Int, h: Int) {
        val cx = w / 2f
        val cy = h / 2f
        val r  = maxOf(w, h) * 0.72f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, r,
                intArrayOf(Color.TRANSPARENT, Color.argb(170, 0, 0, 0)),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /** Escala y recorta (center-crop) una portada al tamaño exacto del slot. */
    private fun scaleCropBitmap(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val scaleX = targetW.toFloat() / src.width
        val scaleY = targetH.toFloat() / src.height
        val scale  = maxOf(scaleX, scaleY)

        // +1 para absorber errores de redondeo float que harían sw/sh < target
        val sw = (src.width  * scale).toInt().coerceAtLeast(targetW)
        val sh = (src.height * scale).toInt().coerceAtLeast(targetH)
        val dx = (sw - targetW) / 2
        val dy = (sh - targetH) / 2

        val scaled  = Bitmap.createScaledBitmap(src, sw, sh, true)
        val cropped = Bitmap.createBitmap(scaled, dx.coerceAtLeast(0),
                                                  dy.coerceAtLeast(0), targetW, targetH)
        if (scaled !== src && scaled !== cropped) scaled.recycle()
        return cropped
    }

    private fun parseColor(hex: String, fallback: Int): Int = try {
        Color.parseColor(hex)
    } catch (_: Exception) {
        fallback
    }
}
