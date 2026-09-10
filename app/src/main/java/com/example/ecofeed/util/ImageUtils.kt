package com.example.ecofeed.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * ImageUtils optimized for gallery and camera photo uploads.
 * Scales down and compresses to under 100KB JPEG Base64.
 */
object ImageUtils {
    fun compressUriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            // 1. Calculate target dimensions (Max 400px while maintaining aspect ratio)
            val maxDimension = 400
            val width = originalBitmap.width
            val height = originalBitmap.height
            val ratio = width.toFloat() / height.toFloat()

            val targetWidth: Int
            val targetHeight: Int
            if (width > height) {
                targetWidth = maxDimension
                targetHeight = (maxDimension / ratio).toInt()
            } else {
                targetHeight = maxDimension
                targetWidth = (maxDimension * ratio).toInt()
            }

            // 2. Scale bitmap to target size
            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            
            // 3. Iteratively compress until under 100KB
            val outputStream = ByteArrayOutputStream()
            var quality = 70
            
            var compressedBase64: String? = null
            while (compressedBase64 == null) {
                outputStream.reset()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val bytes = outputStream.toByteArray()
                
                // 100KB = 102400 bytes
                if (bytes.size <= 102400 || quality <= 30) {
                    // Encode to Base64 with mandatory Data URI prefix
                    val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    compressedBase64 = "data:image/jpeg;base64,$base64String"
                }
                
                quality -= 10
            }
            compressedBase64
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
