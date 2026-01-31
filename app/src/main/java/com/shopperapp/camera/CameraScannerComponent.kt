package com.shopperapp.camera

import android.Manifest
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScannerComponent(
    cameraManager: CameraManager,
    onVideoRecorded: (java.io.File) -> Unit,
    onError: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Camera permissions
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    val isRecording by cameraManager.isRecording.collectAsState()
    val recordingError by cameraManager.recordingError.collectAsState()
    val recordingComplete by cameraManager.recordingComplete.collectAsState()
    
    // Handle recording completion
    LaunchedEffect(recordingComplete) {
        recordingComplete?.let { file ->
            onVideoRecorded(file)
        }
    }
    
    // Handle recording errors
    LaunchedEffect(recordingError) {
        recordingError?.let { error ->
            onError(error)
        }
    }
    
    if (!cameraPermissionState.status.isGranted) {
        PermissionRequestScreen(
            permission = cameraPermissionState,
            permissionText = "Camera permission is required to scan items",
            rationaleText = "ShopperApp needs access to your camera to scan barcodes and capture product information"
        )
    } else if (!audioPermissionState.status.isGranted) {
        PermissionRequestScreen(
            permission = audioPermissionState,
            permissionText = "Audio permission is required for recording",
            rationaleText = "Audio recording helps with the scanning process and improves accuracy"
        )
    } else {
        CameraView(
            cameraManager = cameraManager,
            lifecycleOwner = lifecycleOwner,
            isRecording = isRecording
        )
    }
}

@Composable
private fun PermissionRequestScreen(
    permission: androidx.compose.ui.graphics.vector.ImageVector,
    permissionText: String,
    rationaleText: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Permission Required",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = permissionText,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (permission.status.shouldShowRationale) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rationaleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permission.launchPermissionRequest() }
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
private fun CameraView(
    cameraManager: CameraManager,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    isRecording: Boolean
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    
    // Initialize camera
    LaunchedEffect(Unit) {
        cameraManager.initializeCamera(context, lifecycleOwner, previewView)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
            // Recording indicator
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color = MaterialTheme.colorScheme.onError, shape = MaterialTheme.shapes.small)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REC",
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
    }
}

// Fix the permission state parameter type
}