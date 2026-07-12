package com.elendheim.codex.codex.io

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

// Turns a picked image into a small base64 string and back. Images are downscaled
// and JPEG compressed first, so one sketch does not bloat the export file. The
// base64 lives on the entity, so it travels inside the single export like everything
// else.
object ImageCodec {

    // Load a chosen image, shrink it and return it as base64, or null on any failure.
    fun fromUri(
        resolver: ContentResolver,
        uri: Uri,
        maxDim: Int = 1024,
        quality: Int = 80
    ): String? = runCatching {
        // First pass reads only the size so we can pick a sensible sample rate.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val largest = maxOf(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)

        // Second pass decodes at roughly the target size to keep memory low.
        val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize(largest, maxDim) }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: return null

        // A final exact scale so the long edge is at most maxDim.
        val scaled = scaleDown(decoded, maxDim)
        val bytes = ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    }.getOrNull()

    // Decode a stored base64 image back into a bitmap for display, or null if empty
    // or malformed.
    fun toBitmap(base64: String): Bitmap? {
        if (base64.isBlank()) return null
        return runCatching {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    // Largest power of two that keeps the image at or above the target, cheap to
    // decode and good enough before the exact scale below.
    private fun sampleSize(largest: Int, maxDim: Int): Int {
        var sample = 1
        while (largest / (sample * 2) >= maxDim) sample *= 2
        return sample
    }

    // Scale a bitmap so its longest edge is at most maxDim, keeping the aspect ratio.
    private fun scaleDown(src: Bitmap, maxDim: Int): Bitmap {
        val longest = maxOf(src.width, src.height)
        if (longest <= maxDim) return src
        val ratio = maxDim.toFloat() / longest
        val w = (src.width * ratio).toInt().coerceAtLeast(1)
        val h = (src.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, w, h, true)
    }
}
