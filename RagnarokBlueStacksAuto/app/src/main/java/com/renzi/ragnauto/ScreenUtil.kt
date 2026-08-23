package com.renzi.ragnauto

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

object ScreenUtil {
    // Average absolute RGB difference on a small scaled sample.
    fun similarity(a: Bitmap, b: Bitmap): Double {
        val w = minOf(a.width, b.width)
        val h = minOf(a.height, b.height)
        if (w <= 0 || h <= 0) return 0.0

        val stepX = maxOf(1, w / 32)
        val stepY = maxOf(1, h / 16)

        var total = 0L
        var count = 0L
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                val ca = a.getPixel(x, y)
                val cb = b.getPixel(x, y)
                total += abs(Color.red(ca) - Color.red(cb))
                total += abs(Color.green(ca) - Color.green(cb))
                total += abs(Color.blue(ca) - Color.blue(cb))
                count += 3
                x += stepX
            }
            y += stepY
        }
        val avg = total.toDouble() / count.toDouble()
        return (1.0 - (avg / 255.0)).coerceIn(0.0, 1.0)
    }

    fun cropRelative(src: Bitmap, left: Double, top: Double, right: Double, bottom: Double): Bitmap {
        val x = (src.width * left).toInt().coerceIn(0, src.width - 1)
        val y = (src.height * top).toInt().coerceIn(0, src.height - 1)
        val r = (src.width * right).toInt().coerceIn(x + 1, src.width)
        val b = (src.height * bottom).toInt().coerceIn(y + 1, src.height)
        return Bitmap.createBitmap(src, x, y, r - x, b - y)
    }

    fun barFillPercent(
        src: Bitmap,
        left: Double, top: Double, right: Double, bottom: Double,
        mode: String
    ): Int {
        val roi = cropRelative(src, left, top, right, bottom)
        var matched = 0
        var total = 0
        val step = maxOf(1, roi.width / 100)
        val y = roi.height / 2
        var x = 0
        while (x < roi.width) {
            val c = roi.getPixel(x, y)
            val r = Color.red(c); val g = Color.green(c); val b = Color.blue(c)
            val ok = when(mode) {
                "hp" -> g > 120 && g > r * 1.15 && g > b * 1.05
                "sp" -> b > 120 && b > r * 1.15
                else -> false
            }
            if (ok) matched++
            total++
            x += step
        }
        return if (total == 0) 100 else ((matched.toDouble() / total) * 100).toInt()
    }
}
