package com.jfcardenas.musicwall.features.wallpaper.renderer

import android.graphics.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 12 imágenes encajadas como piezas de rompecabezas en una grilla 3×4.
 * Cada borde interno tiene una pestaña (tab) o encaje (hole) semicircular;
 * los bordes adyacentes son siempre opuestos para que las piezas encajen.
 */
@Singleton
class PuzzleRenderer @Inject constructor() : WallpaperRenderer {

    override val id        = "puzzle"
    override val isPremium = false

    companion object {
        private const val COLS = 3
        private const val ROWS = 4

        // hTabs[r][c] = true → la pestaña del borde entre fila r y r+1 apunta ABAJO
        private val H_TABS = arrayOf(
            booleanArrayOf(true,  false, true),
            booleanArrayOf(false, true,  false),
            booleanArrayOf(true,  false, true),
        )
        // vTabs[r][c] = true → la pestaña del borde entre col c y c+1 apunta a la DERECHA
        private val V_TABS = arrayOf(
            booleanArrayOf(true,  false),
            booleanArrayOf(false, true),
            booleanArrayOf(true,  false),
            booleanArrayOf(false, true),
        )
    }

    override suspend fun render(items: List<RenderItem>, width: Int, height: Int): Bitmap {
        val out    = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        canvas.drawColor(Color.rgb(10, 10, 10))

        val sorted = sortByRelevance(items).take(12)
        if (sorted.isEmpty()) return out

        val cw    = width.toFloat()  / COLS
        val ch    = height.toFloat() / ROWS
        val tabR  = minOf(cw, ch) * 0.13f

        val imgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color       = Color.argb(90, 0, 0, 0)
            maskFilter  = BlurMaskFilter(tabR * 0.7f, BlurMaskFilter.Blur.NORMAL)
            style       = Paint.Style.FILL
        }

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color       = Color.argb(60, 0, 0, 0)
            strokeWidth = 1.8f
            style       = Paint.Style.STROKE
        }

        sorted.forEachIndexed { idx, ri ->
            val col = idx % COLS
            val row = idx / COLS

            val left   = col * cw
            val top    = row * ch
            val right  = left + cw
            val bottom = top  + ch

            // Dirección de cada borde desde la perspectiva de esta pieza
            val topTab    = if (row == 0)       null else !H_TABS[row - 1][col]
            val bottomTab = if (row == ROWS - 1) null else H_TABS[row][col]
            val leftTab   = if (col == 0)       null else !V_TABS[row][col - 1]
            val rightTab  = if (col == COLS - 1) null else V_TABS[row][col]

            val path = buildPiecePath(left, top, right, bottom, topTab, rightTab, bottomTab, leftTab, tabR)

            // Sombra
            canvas.drawPath(path, shadowPaint)

            // Arte del álbum recortado a la pieza
            canvas.save()
            canvas.clipPath(path)
            canvas.drawBitmap(ri.bitmap,
                Rect(0, 0, ri.bitmap.width, ri.bitmap.height),
                RectF(left, top, right, bottom),
                imgPaint)
            // Gradiente sutil para dar profundidad
            canvas.drawRect(left, top, right, bottom, Paint().apply {
                shader = LinearGradient(0f, top, 0f, bottom,
                    intArrayOf(Color.argb(30, 255, 255, 255), Color.argb(55, 0, 0, 0)),
                    floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
            })
            canvas.restore()

            // Borde de corte
            canvas.drawPath(path, borderPaint)
        }

        return out
    }

    /**
     * Construye el Path de una pieza trazando en sentido horario.
     * null = borde plano | true = pestaña apunta AFUERA | false = encaje (dips ADENTRO)
     *
     * Borde superior (izq→der):   true=arriba / false=abajo
     * Borde derecho  (arr→ abj):  true=derecha / false=izquierda
     * Borde inferior (der→izq):   true=abajo / false=arriba
     * Borde izquierdo(abj→arr):   true=izquierda / false=derecha
     */
    private fun buildPiecePath(
        left: Float, top: Float, right: Float, bottom: Float,
        topTab: Boolean?, rightTab: Boolean?, bottomTab: Boolean?, leftTab: Boolean?,
        r: Float,
    ): Path {
        val path = Path()
        val midX = (left + right)   / 2f
        val midY = (top  + bottom)  / 2f
        path.moveTo(left, top)

        // ── Borde superior: izq → der ────────────────────────────────────────
        if (topTab == null) {
            path.lineTo(right, top)
        } else {
            val oval = RectF(midX - r, top - r, midX + r, top + r)
            path.lineTo(midX - r, top)
            path.arcTo(oval, 180f, if (topTab) -180f else 180f, false)
            path.lineTo(right, top)
        }

        // ── Borde derecho: arr → abj ─────────────────────────────────────────
        if (rightTab == null) {
            path.lineTo(right, bottom)
        } else {
            val oval = RectF(right - r, midY - r, right + r, midY + r)
            path.lineTo(right, midY - r)
            path.arcTo(oval, 270f, if (rightTab) 180f else -180f, false)
            path.lineTo(right, bottom)
        }

        // ── Borde inferior: der → izq ────────────────────────────────────────
        if (bottomTab == null) {
            path.lineTo(left, bottom)
        } else {
            val oval = RectF(midX - r, bottom - r, midX + r, bottom + r)
            path.lineTo(midX + r, bottom)
            path.arcTo(oval, 0f, if (bottomTab) 180f else -180f, false)
            path.lineTo(left, bottom)
        }

        // ── Borde izquierdo: abj → arr ───────────────────────────────────────
        if (leftTab == null) {
            path.lineTo(left, top)
        } else {
            val oval = RectF(left - r, midY - r, left + r, midY + r)
            path.lineTo(left, midY + r)
            path.arcTo(oval, 90f, if (leftTab) 180f else -180f, false)
            path.lineTo(left, top)
        }

        path.close()
        return path
    }

    private fun sortByRelevance(items: List<RenderItem>): List<RenderItem> =
        if (items.any { it.image.playcount > 0 }) items.sortedByDescending { it.image.playcount }
        else items.sortedBy { it.image.rank }
}
