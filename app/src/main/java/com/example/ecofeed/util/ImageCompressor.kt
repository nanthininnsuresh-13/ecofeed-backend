package com.example.ecofeed.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * ImageCompressor optimized for Google Photos content URIs.
 * Scales down to max 500px and converts to JPEG Base64.
 */
object ImageCompressor {
    fun compressUriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            val maxSize = 500
            val width = originalBitmap.width
            val height = originalBitmap.height
            val ratio = width.toFloat() / height.toFloat()

            val targetWidth: Int
            val targetHeight: Int
            if (width > height) {
                targetWidth = if (width > maxSize) maxSize else width
                targetHeight = (targetWidth / ratio).toInt()
            } else {
                targetHeight = if (height > maxSize) maxSize else height
                targetWidth = (targetHeight * ratio).toInt()
            }

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            
            val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
