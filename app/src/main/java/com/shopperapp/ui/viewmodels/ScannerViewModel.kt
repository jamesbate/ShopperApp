package com.shopperapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopperapp.camera.CameraManager
import com.shopperapp.data.models.*
import com.shopperapp.data.repository.*
import com.shopperapp.ml.MLKitAnalyzer
import com.shopperapp.ml.VideoAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val cameraManager: CameraManager,
    private val mlKitAnalyzer: MLKitAnalyzer,
    private val shoppingItemRepository: ShoppingItemRepository,
    private val itemMetadataRepository: ItemMetadataRepository,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    // UI States
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()
    
    // Camera states
    val isRecording = cameraManager.isRecording
    val recordingError = cameraManager.recordingError
    val recordingComplete = cameraManager.recordingComplete
    
    // ML Kit states
    val scanResult = mlKitAnalyzer.scanResult
    val isProcessing = mlKitAnalyzer.isProcessing
    
    // User data
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Video analyzer for real-time scanning
    private lateinit var videoAnalyzer: VideoAnalyzer
    
    init {
        initializeVideoAnalyzer()
        observeCurrentUser()
    }
    
    private fun initializeVideoAnalyzer() {
        videoAnalyzer = VideoAnalyzer(mlKitAnalyzer)
        videoAnalyzer.onBarcodeDetected = { result ->
            handleBarcodeDetected(result)
        }
        videoAnalyzer.onTextDetected = { text ->
            handleTextDetected(text)
        }
        videoAnalyzer.onError = { error ->
            handleError(error)
        }
    }
    
    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.authStateFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }
    
    fun startRecording(context: android.content.Context) {
        if (cameraManager.hasCameraPermission(context) && cameraManager.hasAudioPermission(context)) {
            cameraManager.startRecording(context)
            _uiState.value = _uiState.value.copy(isRecording = true)
        } else {
            _uiState.value = _uiState.value.copy(
                error = "Camera and microphone permissions are required"
            )
        }
    }
    
    fun stopRecording() {
        cameraManager.stopRecording()
        _uiState.value = _uiState.value.copy(isRecording = false)
    }
    
    private fun handleBarcodeDetected(result: MLKitAnalyzer.ScanResult) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                scanInProgress = true,
                lastScanResult = result
            )
            
            try {
                // Check if we have metadata for this barcode
                result.barcode?.let { barcode ->
                    val existingMetadata = itemMetadataRepository.getMetadataByBarcode(barcode).firstOrNull()
                    
                    if (existingMetadata != null) {
                        // Found existing item metadata
                        _uiState.value = _uiState.value.copy(
                            itemSuggestion = existingMetadata,
                            scanInProgress = false,
                            showPriceDialog = true
                        )
                    } else {
                        // New item - need to create metadata
                        _uiState.value = _uiState.value.copy(
                            scanInProgress = false,
                            showNewItemDialog = true
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to handle barcode detection")
                handleError("Failed to process barcode: ${e.message}")
            }
        }
    }
    
    private fun handleTextDetected(text: String) {
        // Try to extract product information from text
        val productName = mlKitAnalyzer.extractProductName(text)
        val expiryDate = mlKitAnalyzer.extractExpiryDate(text)
        
        _uiState.value = _uiState.value.copy(
            detectedText = text,
            suggestedProductName = productName,
            suggestedExpiryDate = expiryDate
        )
    }
    
    fun createShoppingItem(
        name: String,
        price: Double,
        barcode: String? = null,
        expiryDate: String? = null,
        categoryId: String? = null
    ) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val groupId = user.currentGroupId ?: return@launch
                
                // Create shopping item
                val item = ShoppingItem(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    quantity = 1,
                    barcode = barcode,
                    categoryId = categoryId,
                    groupId = groupId,
                    addedBy = user.id
                )
                
                // Add to shopping list
                shoppingItemRepository.addShoppingItem(groupId, item)
                
                // Create or update item metadata
                barcode?.let { code ->
                    val metadata = ItemMetadata(
                        barcode = code,
                        itemName = name,
                        typicalPrice = price,
                        categoryId = categoryId,
                        brand = extractBrand(name),
                        lastUpdated = System.currentTimeMillis()
                    )
                    itemMetadataRepository.updateItemMetadata(metadata)
                }
                
                // Add to scan history
                val scanHistory = ScanHistory(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    barcode = barcode,
                    itemName = name,
                    price = price,
                    expiryDate = expiryDate,
                    categoryId = categoryId,
                    quantity = 1
                )
                scanHistoryRepository.addScanHistory(scanHistory)
                
                _uiState.value = _uiState.value.copy(
                    itemAdded = true,
                    error = null
                )
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to create shopping item")
                handleError("Failed to add item: ${e.message}")
            }
        }
    }
    
    fun updateItemPrice(barcode: String, price: Double, storeName: String? = null) {
        viewModelScope.launch {
            try {
                // Update item metadata
                val metadata = itemMetadataRepository.getMetadataByBarcode(barcode).firstOrNull()
                if (metadata != null) {
                    val updatedMetadata = metadata.copy(
                        typicalPrice = price,
                        lastUpdated = System.currentTimeMillis()
                    )
                    itemMetadataRepository.updateItemMetadata(updatedMetadata)
                }
                
                // Add to price history
                val priceHistory = PriceHistory(
                    id = UUID.randomUUID().toString(),
                    barcode = barcode,
                    price = price,
                    storeName = storeName,
                    userId = _currentUser.value?.id ?: "",
                    recordedAt = System.currentTimeMillis()
                )
                priceHistoryRepository.addPriceHistory(priceHistory)
                
                _uiState.value = _uiState.value.copy(
                    priceUpdated = true,
                    error = null
                )
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to update item price")
                handleError("Failed to update price: ${e.message}")
            }
        }
    }
    
    fun dismissDialogs() {
        _uiState.value = _uiState.value.copy(
            showPriceDialog = false,
            showNewItemDialog = false,
            itemAdded = false,
            priceUpdated = false,
            error = null
        )
    }
    
    fun processVideoFile(videoFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(scanInProgress = true)
            
            try {
                // For now, simulate processing by analyzing a single frame
                // In a real implementation, you would extract frames from video
                // and analyze each one with ML Kit
                delay(2000) // Simulate processing time
                
                // Simulate finding a barcode
                _uiState.value = _uiState.value.copy(
                    scanInProgress = false,
                    lastScanResult = MLKitAnalyzer.ScanResult(
                        barcode = "1234567890123",
                        barcodeFormat = "EAN-13",
                        extractedText = "Sample Product",
                        confidence = 0.95f
                    )
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    scanInProgress = false,
                    error = "Failed to process video: ${e.message}"
                )
            }
        }
    }
    
    private fun handleError(error: String) {
        _uiState.value = _uiState.value.copy(
            scanInProgress = false,
            error = error
        )
    }
    
    private fun extractBrand(productName: String): String? {
        // Simple brand extraction - could be enhanced with ML models
        val commonBrands = listOf(
            "Nestle", "Kellogg's", "Coca-Cola", "Pepsi", "Heinz", "Campbell's",
            "General Mills", "Kraft", "Unilever", "Procter & Gamble"
        )
        
        for (brand in commonBrands) {
            if (productName.contains(brand, ignoreCase = true)) {
                return brand
            }
        }
        
        // Try first word if it's capitalized properly
        val words = productName.split(" ")
        if (words.isNotEmpty() && words[0].isNotEmpty() && words[0][0].isUpperCase()) {
            return words[0]
        }
        
        return null
    }
    
    data class ScannerUiState(
        val isRecording: Boolean = false,
        val scanInProgress: Boolean = false,
        val lastScanResult: MLKitAnalyzer.ScanResult? = null,
        val detectedText: String = "",
        val suggestedProductName: String? = null,
        val suggestedExpiryDate: String? = null,
        val itemSuggestion: ItemMetadata? = null,
        val showPriceDialog: Boolean = false,
        val showNewItemDialog: Boolean = false,
        val itemAdded: Boolean = false,
        val priceUpdated: Boolean = false,
        val error: String? = null
    )
    
    override fun onCleared() {
        super.onCleared()
        cameraManager.release()
        mlKitAnalyzer.release()
        if (::videoAnalyzer.isInitialized) {
            videoAnalyzer.release()
        }
    }
}