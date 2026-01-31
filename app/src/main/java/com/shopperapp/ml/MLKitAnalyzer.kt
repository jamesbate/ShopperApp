package com.shopperapp.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.Image
import android.net.Uri
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MLKitAnalyzer @Inject constructor() {
    
    private val barcodeOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_QR_CODE
        )
        .build()
    
    private val barcodeScanner = BarcodeScanning.getClient(barcodeOptions)
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    // Scan results
    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    data class ScanResult(
        val barcode: String? = null,
        val barcodeFormat: String? = null,
        val extractedText: String = "",
        val expiryDate: String? = null,
        val confidence: Float = 0f,
        val boundingBox: Rect? = null,
        val rawText: String = ""
    )
    
    suspend fun analyzeImage(image: Image): ScanResult? {
        _isProcessing.value = true
        
        return try {
            val inputImage = InputImage.fromMediaImage(image, image.imageInfo.rotationDegrees)
            analyzeImageInternal(inputImage)
        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze image")
            null
        } finally {
            _isProcessing.value = false
        }
    }
    
    suspend fun analyzeBitmap(bitmap: Bitmap): ScanResult? {
        _isProcessing.value = true
        
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            analyzeImageInternal(inputImage)
        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze bitmap")
            null
        } finally {
            _isProcessing.value = false
        }
    }
    
    suspend fun analyzeImageFile(imageFile: File): ScanResult? {
        _isProcessing.value = true
        
        return try {
            val inputImage = InputImage.fromFilePath(imageFile.absolutePath)
            analyzeImageInternal(inputImage)
        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze image file")
            null
        } finally {
            _isProcessing.value = false
        }
    }
    
    private suspend fun analyzeImageInternal(inputImage: InputImage): ScanResult? {
        var barcodeResult: Barcode? = null
        var textResult: String = ""
        
        try {
            // Scan for barcodes
            val barcodes = barcodeScanner.process(inputImage).await()
            barcodeResult = barcodes.firstOrNull()
            
            // Extract text
            val text = textRecognizer.process(inputImage).await()
            textResult = text.text
            
        } catch (e: Exception) {
            Timber.e(e, "ML Kit processing failed")
        }
        
        return if (barcodeResult != null || textResult.isNotEmpty()) {
            val scanResult = createScanResult(barcodeResult, textResult)
            _scanResult.value = scanResult
            scanResult
        } else {
            null
        }
    }
    
    private fun createScanResult(barcode: Barcode?, extractedText: String): ScanResult {
        val barcodeValue = barcode?.rawValue
        val barcodeFormat = barcode?.format?.let { formatToString(it) }
        
        // Extract expiry date from text
        val expiryDate = extractExpiryDate(extractedText)
        
        return ScanResult(
            barcode = barcodeValue,
            barcodeFormat = barcodeFormat,
            extractedText = extractedText,
            expiryDate = expiryDate,
            confidence = calculateConfidence(barcode, extractedText),
            boundingBox = barcode?.boundingBox,
            rawText = extractedText
        )
    }
    
    private fun extractExpiryDate(text: String): String? {
        val patterns = listOf(
            // Various date formats
            Pattern.compile("(\\d{2}[/-]\\d{2}[/-]\\d{2,4})"), // MM/DD/YY or MM-DD-YY
            Pattern.compile("(\\d{4}[/-]\\d{2}[/-]\\d{2})"), // YYYY-MM-DD
            Pattern.compile("(EXP[\\s:]*(\\d{2}[/-]\\d{2}[/-]\\d{2,4}))", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(BEST[\\sBY|BEFORE][\\s:]*(\\d{2}[/-]\\d{2}[/-]\\d{2,4}))", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(USE[\\sBY][\\s:]*(\\d{2}[/-]\\d{2}[/-]\\d{2,4}))", Pattern.CASE_INSENSITIVE)
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                // Return the date part (group 1 for most patterns, group 2 for EXP patterns)
                val dateMatch = if (matcher.groupCount() > 1) matcher.group(2) else matcher.group(1)
                return normalizeDate(dateMatch)
            }
        }
        
        return null
    }
    
    private fun normalizeDate(dateString: String?): String? {
        if (dateString == null) return null
        
        return try {
            // Try to parse various date formats
            val formats = listOf(
                "MM/dd/yy",
                "MM-dd-yy", 
                "MM/dd/yyyy",
                "MM-dd-yyyy",
                "yyyy-MM-dd",
                "dd/MM/yy",
                "dd-MM-yy"
            )
            
            for (format in formats) {
                try {
                    val date = SimpleDateFormat(format, Locale.US).parse(dateString)
                    if (date != null) {
                        // Return in standard format
                        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
                    }
                } catch (e: Exception) {
                    // Continue to next format
                }
            }
            
            dateString
        } catch (e: Exception) {
            Timber.w(e, "Failed to normalize date: $dateString")
            dateString
        }
    }
    
    private fun calculateConfidence(barcode: Barcode?, extractedText: String): Float {
        var confidence = 0f
        
        // Barcode gives highest confidence
        if (barcode != null) {
            confidence += 0.8f
        }
        
        // Text with expiry date adds confidence
        if (extractedText.isNotEmpty() && extractExpiryDate(extractedText) != null) {
            confidence += 0.3f
        }
        
        // Just having text adds some confidence
        if (extractedText.isNotEmpty()) {
            confidence += 0.1f
        }
        
        return minOf(confidence, 1.0f)
    }
    
    private fun formatToString(format: Int): String {
        return when (format) {
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            Barcode.FORMAT_CODE_128 -> "CODE-128"
            Barcode.FORMAT_QR_CODE -> "QR_CODE"
            else -> "UNKNOWN"
        }
    }
    
    fun extractProductName(text: String): String? {
        // Simple product name extraction - this could be enhanced with ML models
        val lines = text.split('\n')
        
        // Look for lines that might be product names
        val productPatterns = listOf(
            Pattern.compile("([A-Z][a-z]+\\s+[A-Z][a-z]+\\s+[A-Z][a-z]+)"), // Three-word product names
            Pattern.compile("([A-Z][a-z]+\\s+[A-Z][a-z]+)"), // Two-word product names
            Pattern.compile("([A-Z][a-z]+)")
        )
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty() && trimmedLine.length > 2) {
                for (pattern in productPatterns) {
                    val matcher = pattern.matcher(trimmedLine)
                    if (matcher.find()) {
                        return matcher.group(1)
                    }
                }
            }
        }
        
        return null
    }
    
    fun clearResults() {
        _scanResult.value = null
    }
    
    fun release() {
        try {
            barcodeScanner.close()
            textRecognizer.close()
        } catch (e: Exception) {
            Timber.e(e, "Failed to release ML Kit resources")
        }
    }
}