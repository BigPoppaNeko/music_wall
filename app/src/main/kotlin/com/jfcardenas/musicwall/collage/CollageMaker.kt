package com.jfcardenas.musicwall.collage

import android.graphics.*
import kotlin.random.Random

class CollageMaker(
    private val wallpaperWidth: Int,
    private val wallpaperHeight: Int,
    private val tileSize: Int = 240
) {
    private val tilePaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        maskFilter = BlurMaskFilter(50f, BlurMaskFilter.Blur.NORMAL)
    }

    fun createCollage(tiles: List<Bitmap>): Bitmap {
        val collage = Bitmap.createBitmap(wallpaperWidth, wallpaperHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(collage)

        val rowTiles = 1 + ((wallpaperWidth - tileSize / 2) / tileSize)
        val colTiles = 1 + ((wallpaperHeight - tileSize / 2) / tileSize)
        val imagesToDraw = maxOf(rowTiles * colTiles * 2, tiles.size)
        val ring = RingList(tiles, imagesToDraw)

        // Cubrir la pantalla con un grid de tiles
        outer@ for (col in 0 until rowTiles) {
            for (row in 0 until colTiles) {
                val img = ring.next() ?: break@outer
                canvas.drawTile(img, col * tileSize, row * tileSize)
            }
        }

        // Tiles adicionales en posiciones aleatorias para mayor densidad
        while (ring.hasNext()) {
            val img = ring.next() ?: break
            if (wallpaperWidth > tileSize && wallpaperHeight > tileSize) {
                val x = Random.nextInt(wallpaperWidth - tileSize)
                val y = Random.nextInt(wallpaperHeight - tileSize)
                canvas.drawTile(img, x, y)
            }
        }

        return darken(collage, 0.3f)
    }

    private fun Canvas.drawTile(bitmap: Bitmap, x: Int, y: Int) {
        drawBitmap(
            bitmap,
            Rect(0, 0, bitmap.width, bitmap.height),
            RectF(x.toFloat(), y.toFloat(), (x + tileSize).toFloat(), (y + tileSize).toFloat()),
            tilePaint
        )
    }

    private fun darken(src: Bitmap, factor: Float): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, null)
        val overlay = Paint().apply {
            color = Color.argb((factor * 255).toInt(), 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, src.width.toFloat(), src.height.toFloat(), overlay)
        return result
    }

    private class RingList<T>(private val items: List<T>, private val total: Int) {
        private var index = 0
        private var count = 0

        fun hasNext() = count < total && items.isNotEmpty()

        fun next(): T? {
            if (!hasNext()) return null
            val item = items[index % items.size]
            index++
            count++
            return item
        }
    }
}
