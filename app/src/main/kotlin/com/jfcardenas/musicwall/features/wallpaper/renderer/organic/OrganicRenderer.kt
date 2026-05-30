package com.jfcardenas.musicwall.features.wallpaper.renderer.organic

import android.content.Context
import android.graphics.*
import com.jfcardenas.musicwall.features.wallpaper.renderer.RenderItem
import com.jfcardenas.musicwall.features.wallpaper.renderer.WallpaperRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    private val context:  Context,
    val layoutId: String,
) : WallpaperRenderer {

    override val id        = "organic/$layoutId"
    override val isPremium = false

    // Caché en memoria — un OrganicRenderer por layoutId
    private var cachedBackground: Bitmap?      = null
    private var cachedLayout:     OrganicLayout? = null

    // ── API pública ───────────────────────────────────────────────────────────

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap =
        withContext(Dispatchers.Default) {

            val layout     = getLayout()
            val background = getBackground(width, height)

            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)

            // 1 — Fondo
            if (background != null) {
                canvas.drawBitmap(background, 0f, 0f, null)
            } else {
                canvas.drawColor(parseColor(layout.lightingMap.ambientColor, Color.BLACK))
            }

            if (items.isEmpty()) {
                drawVignette(canvas, width, height)
                return@withContext result
            }

            // 2 — Portadas en slots
            layout.slots.forEachIndexed { idx, slot ->
                val item = items[idx % items.size]
                renderSlot(canvas, item.bitmap, slot, width, height, layout.lightingMap)
            }

            // 3 — Vignette de cohesión
            drawVignette(canvas, width, height)

            result
        }

    override fun release() {
        cachedBackground?.recycle()
        cachedBackground = null
        cachedLayout = null
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

    private fun getBackground(canvasW: Int, canvasH: Int): Bitmap? {
        cachedBackground?.let { return it }
        val exts = listOf(".jpg", ".jpeg", ".png", ".webp")
        for (ext in exts) {
            try {
                val raw = context.assets.open("organic/$layoutId$ext").use {
                    BitmapFactory.decodeStream(it)
                } ?: continue

                // Ajustar escala manteniendo relación de aspecto (fill)
                val scale = maxOf(canvasW.toFloat() / raw.width, canvasH.toFloat() / raw.height)
                val sw    = (raw.width  * scale).toInt()
                val sh    = (raw.height * scale).toInt()
                val scaled = Bitmap.createScaledBitmap(raw, sw, sh, true)

                // Centro y recorte al canvas
                val dx = (sw - canvasW) / 2
                val dy = (sh - canvasH) / 2
                val cropped = Bitmap.createBitmap(scaled, dx, dy, canvasW, canvasH)

                if (scaled !== raw) raw.recycle()
                if (cropped !== scaled) scaled.recycle()

                return cropped.also { cachedBackground = it }
            } catch (_: Exception) {}
        }
        return null
    }

    // ── Renderizado de un slot ────────────────────────────────────────────────

    private fun renderSlot(
        canvas:   Canvas,
        album:    Bitmap,
        slot:     OrganicSlot,
        cw:       Int,
        ch:       Int,
        lighting: OrganicLightingMap,
    ) {
        val pw = (slot.width  * cw).toInt().coerceAtLeast(4)
        val ph = (slot.height * ch).toInt().coerceAtLeast(4)
        val px = slot.x * cw
        val py = slot.y * ch
        val cx = px + pw / 2f
        val cy = py + ph / 2f

        // Escalar portada al slot (recortar si la relación de aspecto difiere)
        val scaled = scaleCropBitmap(album, pw, ph)

        canvas.save()
        canvas.rotate(slot.rotation, cx, cy)

        // Sombra suave
        drawSlotShadow(canvas, px, py, pw.toFloat(), ph.toFloat(),
                       shadowOffset(lighting.shadowDirection, pw.toFloat()),
                       cornerRadius = pw * 0.05f)

        // Portada con grading de luz
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

        val sw = (src.width  * scale).toInt()
        val sh = (src.height * scale).toInt()
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
