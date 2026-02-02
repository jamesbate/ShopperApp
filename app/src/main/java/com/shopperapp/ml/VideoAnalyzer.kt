package com.shopperapp.ml

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoAnalyzer @Inject constructor(
    private val mlKitAnalyzer: MLKitAnalyzer
) : ImageAnalysis.Analyzer {
    
    private val processingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    var onBarcodeDetected: ((MLKitAnalyzer.ScanResult) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onTextDetected: ((String) -> Unit)? = null
    
    private var lastAnalyzedTime = 0L
    private val ANALYSIS_INTERVAL_MS = 500L // Analyze every 500ms
    
    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        
        // Throttle analysis to avoid processing every frame
        if (currentTime - lastAnalyzedTime < ANALYSIS_INTERVAL_MS) {
            image.close()
            return
        }
        
        lastAnalyzedTime = currentTime
        
        processingScope.launch {
            try {
                // Convert ImageProxy to Bitmap for ML Kit analysis
                val bitmap = imageProxyToBitmap(image)
                
                // Analyze with ML Kit
                val result = mlKitAnalyzer.analyzeBitmap(bitmap)
                
                result?.let { scanResult ->
                    withContext(Dispatchers.Main) {
                        if (scanResult.barcode != null) {
                            onBarcodeDetected?.invoke(scanResult)
                            Timber.d("Barcode detected: ${scanResult.barcode}")
                        }
                        
                        if (scanResult.extractedText.isNotEmpty()) {
                            onTextDetected?.invoke(scanResult.extractedText)
                            Timber.d("Text detected: ${scanResult.extractedText}")
                        }
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to analyze image")
                withContext(Dispatchers.Main) {
                    onError?.invoke("Failed to analyze image: ${e.message}")
                }
            } finally {
                image.close()
            }
        }
    }
    
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        // Convert YUV_420_888 to RGB Bitmap
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        // Y
        yBuffer.get(nv21, 0, ySize)
        
        // VU (swapped)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
    
    fun release() {
        processingScope.cancel()
    }
}