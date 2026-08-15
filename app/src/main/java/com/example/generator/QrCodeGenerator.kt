package com.example.generator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.QrCenterLogo
import com.example.data.QrDotStyle
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.EnumMap

object QrCodeGenerator {

    /**
     * Generate BitMatrix from raw content with given error correction level.
     */
    fun generateBitMatrix(
        content: String,
        errorCorrection: String = "M"
    ): BitMatrix? {
        if (content.isBlank()) return null
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.MARGIN, 1)
                val ecLevel = when (errorCorrection.uppercase()) {
                    "L" -> ErrorCorrectionLevel.L
                    "Q" -> ErrorCorrectionLevel.Q
                    "H" -> ErrorCorrectionLevel.H
                    else -> ErrorCorrectionLevel.M
                }
                put(EncodeHintType.ERROR_CORRECTION, ecLevel)
            }
            QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Renders a BitMatrix to a stylized Android Bitmap.
     */
    fun renderBitmap(
        matrix: BitMatrix,
        sizePx: Int = 1024,
        fgColor: Int = android.graphics.Color.BLACK,
        bgColor: Int = android.graphics.Color.WHITE,
        dotStyle: QrDotStyle = QrDotStyle.SQUARE,
        centerLogo: QrCenterLogo = QrCenterLogo.NONE
    ): Bitmap {
        val matrixWidth = matrix.width
        val matrixHeight = matrix.height
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), bgPaint)

        val moduleSize = sizePx.toFloat() / matrixWidth
        val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.FILL
        }

        // Check if a center logo is requested to preserve center space
        val hasLogo = centerLogo != QrCenterLogo.NONE
        val logoReserveRatio = 0.22f
        val logoCenterBox = if (hasLogo) {
            val logoSize = sizePx * logoReserveRatio
            val left = (sizePx - logoSize) / 2f
            val top = (sizePx - logoSize) / 2f
            RectF(left, top, left + logoSize, top + logoSize)
        } else null

        // Finder pattern regions (7x7 blocks)
        fun isFinderPattern(x: Int, y: Int): Boolean {
            val isTopLeft = x < 7 && y < 7
            val isTopRight = x >= matrixWidth - 7 && y < 7
            val isBottomLeft = x < 7 && y >= matrixHeight - 7
            return isTopLeft || isTopRight || isBottomLeft
        }

        for (y in 0 until matrixHeight) {
            for (x in 0 until matrixWidth) {
                if (matrix.get(x, y)) {
                    val left = x * moduleSize
                    val top = y * moduleSize
                    val right = left + moduleSize
                    val bottom = top + moduleSize

                    // Skip drawing modules inside logo reserve zone
                    if (logoCenterBox != null) {
                        val moduleCenter = (left + right) / 2f to (top + bottom) / 2f
                        if (logoCenterBox.contains(moduleCenter.first, moduleCenter.second)) {
                            continue
                        }
                    }

                    if (isFinderPattern(x, y)) {
                        // Render finder patterns with high contrast crispness
                        if (dotStyle == QrDotStyle.ROUNDED || dotStyle == QrDotStyle.DOTS) {
                            val cornerRadius = moduleSize * 0.35f
                            canvas.drawRoundRect(
                                RectF(left, top, right, bottom),
                                cornerRadius,
                                cornerRadius,
                                fgPaint
                            )
                        } else {
                            canvas.drawRect(left, top, right, bottom, fgPaint)
                        }
                    } else {
                        when (dotStyle) {
                            QrDotStyle.SQUARE -> {
                                canvas.drawRect(left, top, right, bottom, fgPaint)
                            }
                            QrDotStyle.ROUNDED -> {
                                val radius = moduleSize * 0.45f
                                canvas.drawRoundRect(
                                    RectF(left + 0.5f, top + 0.5f, right - 0.5f, bottom - 0.5f),
                                    radius,
                                    radius,
                                    fgPaint
                                )
                            }
                            QrDotStyle.DOTS -> {
                                val cx = left + moduleSize / 2f
                                val cy = top + moduleSize / 2f
                                val radius = (moduleSize / 2f) * 0.88f
                                canvas.drawCircle(cx, cy, radius, fgPaint)
                            }
                        }
                    }
                }
            }
        }

        // Draw Center Logo badge if configured
        if (hasLogo && logoCenterBox != null) {
            drawCenterLogoBadge(canvas, logoCenterBox, centerLogo, fgColor, bgColor)
        }

        return bitmap
    }

    private fun drawCenterLogoBadge(
        canvas: Canvas,
        box: RectF,
        centerLogo: QrCenterLogo,
        fgColor: Int,
        bgColor: Int
    ) {
        val badgePadding = box.width() * 0.08f
        val badgeRect = RectF(
            box.left - badgePadding,
            box.top - badgePadding,
            box.right + badgePadding,
            box.bottom + badgePadding
        )

        // Badge Background (Solid background with smooth rounded corners)
        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val cornerRadius = badgeRect.width() * 0.28f
        canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, badgeBgPaint)

        // Badge Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.STROKE
            strokeWidth = badgeRect.width() * 0.04f
        }
        canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, borderPaint)

        // Icon Paint
        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.FILL
        }
        val strokeIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.STROKE
            strokeWidth = box.width() * 0.09f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val cx = box.centerX()
        val cy = box.centerY()
        val r = box.width() * 0.35f

        when (centerLogo) {
            QrCenterLogo.LINK -> {
                // Draw link chain glyph
                val path = Path()
                path.moveTo(cx - r * 0.6f, cy + r * 0.2f)
                path.lineTo(cx + r * 0.6f, cy - r * 0.2f)
                canvas.drawPath(path, strokeIconPaint)
                canvas.drawCircle(cx - r * 0.5f, cy + r * 0.3f, r * 0.25f, strokeIconPaint)
                canvas.drawCircle(cx + r * 0.5f, cy - r * 0.3f, r * 0.25f, strokeIconPaint)
            }
            QrCenterLogo.WIFI -> {
                // Draw Wifi waves
                val arcPaint = Paint(strokeIconPaint).apply { style = Paint.Style.STROKE }
                canvas.drawCircle(cx, cy + r * 0.5f, r * 0.12f, iconPaint)
                canvas.drawArc(
                    RectF(cx - r * 0.45f, cy - r * 0.1f, cx + r * 0.45f, cy + r * 0.8f),
                    200f, 140f, false, arcPaint
                )
                canvas.drawArc(
                    RectF(cx - r * 0.85f, cy - r * 0.5f, cx + r * 0.85f, cy + r * 1.2f),
                    200f, 140f, false, arcPaint
                )
            }
            QrCenterLogo.PHONE -> {
                // Draw Phone handset glyph
                val path = Path().apply {
                    moveTo(cx - r * 0.5f, cy - r * 0.5f)
                    quadTo(cx - r * 0.8f, cy, cx - r * 0.2f, cy + r * 0.6f)
                    lineTo(cx + r * 0.1f, cy + r * 0.3f)
                    lineTo(cx - r * 0.1f, cy + r * 0.1f)
                    lineTo(cx + r * 0.1f, cy - r * 0.1f)
                    lineTo(cx - r * 0.1f, cy - r * 0.3f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
            QrCenterLogo.EMAIL -> {
                // Draw Email Envelope glyph
                val envRect = RectF(cx - r * 0.7f, cy - r * 0.45f, cx + r * 0.7f, cy + r * 0.45f)
                canvas.drawRoundRect(envRect, r * 0.15f, r * 0.15f, strokeIconPaint)
                val flap = Path().apply {
                    moveTo(envRect.left, envRect.top)
                    lineTo(cx, cy + r * 0.1f)
                    lineTo(envRect.right, envRect.top)
                }
                canvas.drawPath(flap, strokeIconPaint)
            }
            QrCenterLogo.STAR -> {
                // Draw 5-point star
                val starPath = Path()
                val points = 5
                val outerR = r * 0.85f
                val innerR = outerR * 0.45f
                for (i in 0 until points * 2) {
                    val currentR = if (i % 2 == 0) outerR else innerR
                    val angle = Math.toRadians((i * 180.0 / points - 90)).toFloat()
                    val px = (cx + currentR * Math.cos(angle.toDouble())).toFloat()
                    val py = (cy + currentR * Math.sin(angle.toDouble())).toFloat()
                    if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
                }
                starPath.close()
                canvas.drawPath(starPath, iconPaint)
            }
            QrCenterLogo.HEART -> {
                // Draw Heart
                val heartPath = Path().apply {
                    moveTo(cx, cy + r * 0.6f)
                    cubicTo(cx - r * 0.9f, cy, cx - r * 0.7f, cy - r * 0.7f, cx, cy - r * 0.2f)
                    cubicTo(cx + r * 0.7f, cy - r * 0.7f, cx + r * 0.9f, cy, cx, cy + r * 0.6f)
                    close()
                }
                canvas.drawPath(heartPath, iconPaint)
            }
            QrCenterLogo.SHIELD -> {
                // Draw Shield glyph
                val shieldPath = Path().apply {
                    moveTo(cx, cy - r * 0.7f)
                    lineTo(cx + r * 0.65f, cy - r * 0.4f)
                    lineTo(cx + r * 0.65f, cy + r * 0.1f)
                    quadTo(cx + r * 0.4f, cy + r * 0.7f, cx, cy + r * 0.8f)
                    quadTo(cx - r * 0.4f, cy + r * 0.7f, cx - r * 0.65f, cy + r * 0.1f)
                    lineTo(cx - r * 0.65f, cy - r * 0.4f)
                    close()
                }
                canvas.drawPath(shieldPath, strokeIconPaint)
            }
            QrCenterLogo.INFO -> {
                canvas.drawCircle(cx, cy, r * 0.75f, strokeIconPaint)
                canvas.drawCircle(cx, cy - r * 0.3f, r * 0.1f, iconPaint)
                val linePaint = Paint(strokeIconPaint).apply { strokeWidth = box.width() * 0.08f }
                canvas.drawLine(cx, cy - r * 0.05f, cx, cy + r * 0.4f, linePaint)
            }
            QrCenterLogo.NONE -> {}
        }
    }

    /**
     * Generates an SVG string representation of the QR code.
     */
    fun generateSvg(
        matrix: BitMatrix,
        fgColorHex: String = "#000000",
        bgColorHex: String = "#FFFFFF",
        dotStyle: QrDotStyle = QrDotStyle.SQUARE
    ): String {
        val width = matrix.width
        val height = matrix.height
        val sb = StringBuilder()

        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 $width $height" width="1024" height="1024" shape-rendering="crispEdges">
  <rect width="100%" height="100%" fill="$bgColorHex"/>
""")

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (matrix.get(x, y)) {
                    when (dotStyle) {
                        QrDotStyle.SQUARE -> {
                            sb.append("""  <rect x="$x" y="$y" width="1" height="1" fill="$fgColorHex"/>""").append("\n")
                        }
                        QrDotStyle.ROUNDED -> {
                            sb.append("""  <rect x="$x" y="$y" width="1" height="1" rx="0.3" ry="0.3" fill="$fgColorHex"/>""").append("\n")
                        }
                        QrDotStyle.DOTS -> {
                            val cx = x + 0.5f
                            val cy = y + 0.5f
                            sb.append("""  <circle cx="$cx" cy="$cy" r="0.45" fill="$fgColorHex"/>""").append("\n")
                        }
                    }
                }
            }
        }
        sb.append("</svg>")
        return sb.toString()
    }

    /**
     * Save Bitmap to Android Gallery / MediaStore.
     */
    fun saveBitmapToDevice(
        context: Context,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        title: String = "QR_Code"
    ): Result<Uri> {
        return try {
            val extension = if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"
            val mimeType = if (format == Bitmap.CompressFormat.JPEG) "image/jpeg" else "image/png"
            val filename = "${title}_${System.currentTimeMillis()}.$extension"

            val outputStream: OutputStream?
            val imageUri: Uri?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRMaker")
                }
                val resolver = context.contentResolver
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(imagesDir, "QRMaker").apply { mkdirs() }
                val imageFile = File(appDir, filename)
                outputStream = FileOutputStream(imageFile)
                imageUri = Uri.fromFile(imageFile)
            }

            outputStream?.use { out ->
                val quality = if (format == Bitmap.CompressFormat.JPEG) 95 else 100
                bitmap.compress(format, quality, out)
            } ?: return Result.failure(Exception("Failed to open output stream"))

            Result.success(imageUri ?: Uri.EMPTY)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Save SVG string to Downloads.
     */
    fun saveSvgToDevice(
        context: Context,
        svgString: String,
        title: String = "QR_Code"
    ): Result<Uri> {
        return try {
            val filename = "${title}_${System.currentTimeMillis()}.svg"
            val outputStream: OutputStream?
            val fileUri: Uri?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/svg+xml")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/QRMaker")
                }
                val resolver = context.contentResolver
                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = fileUri?.let { resolver.openOutputStream(it) }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, "QRMaker").apply { mkdirs() }
                val file = File(appDir, filename)
                outputStream = FileOutputStream(file)
                fileUri = Uri.fromFile(file)
            }

            outputStream?.use { out ->
                out.write(svgString.toByteArray(Charsets.UTF_8))
            } ?: return Result.failure(Exception("Failed to open output stream"))

            Result.success(fileUri ?: Uri.EMPTY)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Share QR Code Bitmap via Android Native Share Sheet.
     */
    fun shareQrCode(
        context: Context,
        bitmap: Bitmap,
        textMessage: String = "Here is my QR Code created with QR Maker"
    ) {
        try {
            val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(cachePath, "qr_code_share.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, textMessage)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share QR Code")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
