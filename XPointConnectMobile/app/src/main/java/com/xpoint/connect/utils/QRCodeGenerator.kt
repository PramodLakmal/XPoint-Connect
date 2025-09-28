/**
 * QRCodeGenerator.kt
 *
 * Purpose: QR code generation and management utility for XPoint Connect bookings Author: XPoint
 * Connect Development Team Date: September 28, 2025
 *
 * Description: This utility class handles QR code generation for approved bookings, including
 * bitmap creation, base64 encoding, and validation functionality. It uses the ZXing library for QR
 * code generation and provides methods for creating secure, time-limited QR codes for booking
 * check-ins.
 */
package com.xpoint.connect.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.io.ByteArrayOutputStream

class QRCodeGenerator {

    companion object {
        private const val TAG = "QRCodeGenerator"
        private const val DEFAULT_QR_SIZE = 512
        private const val QR_CODE_MARGIN = 2
    }

    /**
     * Generates a QR code bitmap from the given data
     *
     * @param data The data to encode in the QR code
     * @param size The size of the QR code (width and height in pixels)
     * @return Bitmap of the generated QR code or null if generation fails
     */
    fun generateQRCode(data: String, size: Int = DEFAULT_QR_SIZE): Bitmap? {
        try {
            val writer = MultiFormatWriter()
            val hints = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = QR_CODE_MARGIN
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)

            return createBitmapFromBitMatrix(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /** Converts a BitMatrix to a Bitmap */
    private fun createBitmapFromBitMatrix(bitMatrix: BitMatrix): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    /**
     * Converts a bitmap to Base64 string for storage or transmission
     *
     * @param bitmap The bitmap to convert
     * @param format The compression format (default PNG)
     * @param quality The compression quality (0-100, ignored for PNG)
     * @return Base64 encoded string of the bitmap
     */
    fun bitmapToBase64(
            bitmap: Bitmap,
            format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
            quality: Int = 100
    ): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Converts a Base64 string back to a bitmap
     *
     * @param base64String The Base64 encoded bitmap string
     * @return Decoded bitmap or null if decoding fails
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a colored QR code with custom foreground and background colors
     *
     * @param data The data to encode
     * @param size The size of the QR code
     * @param foregroundColor The color of the QR code pattern
     * @param backgroundColor The background color
     * @return Colored QR code bitmap or null if generation fails
     */
    fun generateColoredQRCode(
            data: String,
            size: Int = DEFAULT_QR_SIZE,
            foregroundColor: Int = Color.BLACK,
            backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        try {
            val writer = MultiFormatWriter()
            val hints = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = QR_CODE_MARGIN
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)

            return createColoredBitmapFromBitMatrix(bitMatrix, foregroundColor, backgroundColor)
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /** Creates a colored bitmap from a BitMatrix */
    private fun createColoredBitmapFromBitMatrix(
            bitMatrix: BitMatrix,
            foregroundColor: Int,
            backgroundColor: Int
    ): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
            }
        }

        return bitmap
    }

    /**
     * Validates if a string can be encoded as a QR code
     *
     * @param data The data to validate
     * @return True if the data can be encoded, false otherwise
     */
    fun canEncodeData(data: String): Boolean {
        return try {
            val writer = MultiFormatWriter()
            writer.encode(data, BarcodeFormat.QR_CODE, 1, 1)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the maximum data capacity for a QR code
     *
     * @return Maximum number of characters that can be encoded
     */
    fun getMaxDataCapacity(): Int {
        // QR code can hold up to ~4,296 alphanumeric characters in the largest version
        return 4296
    }

    /**
     * Calculates the optimal size for a QR code based on data length
     *
     * @param dataLength The length of the data to encode
     * @return Recommended QR code size in pixels
     */
    fun getOptimalSize(dataLength: Int): Int {
        return when {
            dataLength <= 100 -> 256
            dataLength <= 500 -> 384
            dataLength <= 1000 -> 512
            dataLength <= 2000 -> 768
            else -> 1024
        }
    }
}
