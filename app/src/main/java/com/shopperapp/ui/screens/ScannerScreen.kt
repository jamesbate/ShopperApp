package com.shopperapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopperapp.camera.CameraScannerComponent
import com.shopperapp.ml.MLKitAnalyzer
import com.shopperapp.ui.navigation.Screen
import com.shopperapp.ui.viewmodels.ScannerViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingError by viewModel.recordingError.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    
    // Handle recording completion
    LaunchedEffect(recordingError, uiState.itemAdded, uiState.priceUpdated) {
        recordingError?.let { error ->
            // Show error snackbar or dialog
        }
        
        if (uiState.itemAdded) {
            // Navigate back to shopping list after successful scan
            navController.navigate(Screen.ShoppingList.route) {
                popUpTo(Screen.Scanner.route) { inclusive = true }
            }
            viewModel.dismissDialogs()
        }
        
        if (uiState.priceUpdated) {
            viewModel.dismissDialogs()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        CameraScannerComponent(
            cameraManager = viewModel.cameraManager,
            onVideoRecorded = { videoFile ->
                // Process video file for analysis
                viewModel.processVideoFile(videoFile)
            },
            onError = { error ->
                // Handle camera error
            }
        )
        
        // UI Overlay
        ScannerOverlay(
            uiState = uiState,
            isRecording = isRecording,
            isProcessing = isProcessing,
            scanResult = scanResult,
            onStartRecording = { viewModel.startRecording(context) },
            onStopRecording = { viewModel.stopRecording() },
            onDismissDialogs = { viewModel.dismissDialogs() },
            onCreateItem = { name, price, barcode, expiry ->
                viewModel.createShoppingItem(name, price, barcode, expiry)
            },
            onUpdatePrice = { barcode, price, store ->
                viewModel.updateItemPrice(barcode, price, store)
            },
            onNavigateBack = { 
                navController.popBackStack() 
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScannerOverlay(
    uiState: ScannerViewModel.ScannerUiState,
    isRecording: Boolean,
    isProcessing: Boolean,
    scanResult: MLKitAnalyzer.ScanResult?,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onDismissDialogs: () -> Unit,
    onCreateItem: (String, Double, String?, String?) -> Unit,
    onUpdatePrice: (String, Double, String?) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Top bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Text(
                text = "Item Scanner",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            }
        }
    }
    
    // Scanning instructions
    if (!isRecording && scanResult == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Scan Item",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Position the item in front of the camera\nRotate the item slowly for best results",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = onStartRecording,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoCall,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Recording")
                    }
                }
            }
        }
    }
    
    // Recording controls
    if (isRecording) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel button
                FloatingActionButton(
                    onClick = { onStopRecording() },
                    containerColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color.White
                    )
                }
                
                // Stop recording button
                FloatingActionButton(
                    onClick = { onStopRecording() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, shape = CircleShape)
                    )
                }
            }
        }
    }
    
    // Scan result dialog
    if (scanResult != null && !uiState.scanInProgress) {
        ScanResultDialog(
            scanResult = scanResult,
            itemSuggestion = uiState.itemSuggestion,
            onDismiss = onDismissDialogs,
            onCreateItem = onCreateItem,
            onUpdatePrice = onUpdatePrice
        )
    }
    
    // Error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = onDismissDialogs,
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onDismissDialogs) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ScanResultDialog(
    scanResult: MLKitAnalyzer.ScanResult,
    itemSuggestion: com.shopperapp.data.models.ItemMetadata?,
    onDismiss: () -> Unit,
    onCreateItem: (String, Double, String?, String?) -> Unit,
    onUpdatePrice: (String, Double, String?) -> Unit
) {
    var itemName by remember { mutableStateOf(itemSuggestion?.itemName ?: scanResult.extractedText) }
    var price by remember { mutableStateOf(itemSuggestion?.typicalPrice?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (scanResult.barcode != null) "Item Detected" else "Text Detected") 
        },
        text = {
            Column {
                if (scanResult.barcode != null) {
                    Text("Barcode: ${scanResult.barcode}")
                    Text("Format: ${scanResult.barcodeFormat}")
                }
                
                if (scanResult.expiryDate != null) {
                    Text("Expiry Date: ${scanResult.expiryDate}")
                }
                
                if (scanResult.extractedText.isNotEmpty()) {
                    Text("Detected Text: ${scanResult.extractedText}")
                }
                
                if (itemSuggestion != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Item found in database!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Suggested Price: $${itemSuggestion.typicalPrice}")
                }
                
                // Input fields for new item
                if (itemSuggestion == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (itemSuggestion != null) {
                // Existing item - add to shopping list
                Button(
                    onClick = {
                        val priceValue = itemSuggestion.typicalPrice ?: 0.0
                        onCreateItem(itemSuggestion.itemName, priceValue, scanResult.barcode, scanResult.expiryDate)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add to Shopping List")
                }
            } else {
                // New item - create it
                Button(
                    onClick = {
                        if (itemName.isNotEmpty()) {
                            val priceValue = price.toDoubleOrNull() ?: 0.0
                            onCreateItem(itemName, priceValue, scanResult.barcode, scanResult.expiryDate)
                        }
                    },
                    enabled = itemName.isNotEmpty()
                ) {
                    Text("Add to Shopping List")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}