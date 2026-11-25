package com.example.hepai.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import java.io.ByteArrayOutputStream

/** Sends the original grid, or a labeled seven-column sheet when configured. */
object TimetableUpload {
    fun jpeg(
        context: Context,
        uri: Uri,
        personId: String,
        maxSide: Int = 1600,
    ): ByteArray {
        val original =
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: error("无法读取这张图片")
        try {
            return if (personId == "member_b") memberBColumns(original) else scaleJpeg(original, maxSide)
        } finally {
            original.recycle()
        }
    }

    private fun scaleJpeg(
        original: Bitmap,
        maxSide: Int,
    ): ByteArray {
        val longest = maxOf(original.width, original.height)
        val scaled =
            if (longest <= maxSide) {
                original
            } else {
                val ratio = maxSide.toFloat() / longest
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * ratio).toInt().coerceAtLeast(1),
                    (original.height * ratio).toInt().coerceAtLeast(1),
                    true,
                )
            }
        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 88, output)
        if (scaled !== original) scaled.recycle()
        return output.toByteArray()
    }

    private fun memberBColumns(original: Bitmap): ByteArray {
        val w = original.width
        val h = original.height
        val top = (h * 0.18f).toInt()
        val bottom = (h * 0.99f).toInt()
        val bodyHeight = bottom - top
        val targetHeight = 1600
        val bounds =
            listOf(
                0.11f to 0.23f,
                0.23f to 0.35f,
                0.35f to 0.47f,
                0.47f to 0.59f,
                0.59f to 0.71f,
                0.71f to 0.83f,
                0.83f to 0.95f,
            )
        val labels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val rulerLeft = (w * 0.015f).toInt()
        val rulerRight = (w * 0.10f).toInt()
        val scale = targetHeight.toFloat() / bodyHeight

        val ruler = Bitmap.createBitmap(original, rulerLeft, top, rulerRight - rulerLeft, bodyHeight)
        val rulerScaled =
            Bitmap.createScaledBitmap(
                ruler,
                (ruler.width * scale).toInt().coerceAtLeast(1),
                targetHeight,
                true,
            )
        ruler.recycle()

        val days =
            bounds.map { (left, right) ->
                val crop =
                    Bitmap.createBitmap(
                        original,
                        (w * left).toInt(),
                        top,
                        ((w * right) - (w * left)).toInt().coerceAtLeast(1),
                        bodyHeight,
                    )
                val scaled =
                    Bitmap.createScaledBitmap(
                        crop,
                        (crop.width * scale).toInt().coerceAtLeast(1),
                        targetHeight,
                        true,
                    )
                crop.recycle()
                scaled
            }
        val cellWidth = days.maxOf { it.width }
        val sheet =
            Bitmap.createBitmap(
                rulerScaled.width + cellWidth * 7,
                targetHeight + 60,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(sheet)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(rulerScaled, 0f, 60f, null)
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
        labels.forEachIndexed { index, label ->
            val x = rulerScaled.width + index * cellWidth
            canvas.drawBitmap(days[index], x.toFloat(), 60f, null)
            canvas.drawText(label, x + 10f, 38f, paint)
        }
        val output = ByteArrayOutputStream()
        sheet.compress(Bitmap.CompressFormat.JPEG, 90, output)
        sheet.recycle()
        rulerScaled.recycle()
        days.forEach { it.recycle() }
        return output.toByteArray()
    }
}
