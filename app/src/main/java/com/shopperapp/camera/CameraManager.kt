package com.shopperapp.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor() {
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var activeRecording: File? = null
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    
    private val _recordingError = MutableStateFlow<String?>(null)
    val recordingError: StateFlow<String?> = _recordingError
    
    private val _recordingComplete = MutableStateFlow<File?>(null)
    val recordingComplete: StateFlow<File?> = _recordingComplete
    
    suspend fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ): Boolean {
        return try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            
            setupCameraUseCases(context, lifecycleOwner, previewView)
            true
        } catch (e: Exception) {
            Timber.e(e, "Camera initialization failed")
            _recordingError.value = "Failed to initialize camera: ${e.message}"
            false
        }
    }
    
    private fun setupCameraUseCases(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val cameraProvider = cameraProvider ?: return
        
        // Preview use case
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
        
        // Video capture use case
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        
        // Image analysis for ML Kit processing
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        
        // Select back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            
            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Use case binding failed")
            _recordingError.value = "Failed to setup camera: ${e.message}"
        }
    }
    
    fun startRecording(context: Context): Boolean {
        val videoCapture = this.videoCapture ?: run {
            _recordingError.value = "Video capture not initialized"
            return false
        }
        
        if (_isRecording.value) {
            return true // Already recording
        }
        
        // Create output file
        val outputFile = createVideoOutputFile(context)
        activeRecording = outputFile
        
        val outputOptions = FileOutputOptions.Builder(outputFile).build()
        
        // Configure recording
        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled() // Enable audio recording
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        _isRecording.value = true
                        Timber.d("Recording started")
                    }
                    is VideoRecordEvent.Finalize -> {
                        _isRecording.value = false
                        if (recordEvent.hasError()) {
                            _recordingError.value = "Video recording failed: ${recordEvent.cause}"
                            Timber.e(recordEvent.cause, "Video recording failed")
                        } else {
                            _recordingComplete.value = outputFile
                            Timber.d("Recording completed successfully")
                        }
                        recording = null
                    }
                    else -> {
                        // Handle other events as needed
                    }
                }
            }
        
        return true
    }
    
    fun stopRecording() {
        recording?.stop()
    }
    
    private fun createVideoOutputFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = File(context.getExternalFilesDir(null), "ShopperApp/Videos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, "scan_video_$timestamp.mp4")
    }
    
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasAudioPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun getRequiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
    
    fun release() {
        cameraProvider?.unbindAll()
        camera = null
        videoCapture = null
        recording = null
    }
}