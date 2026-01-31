package com.shopperapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopperapp.data.models.*
import com.shopperapp.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val priceHistoryRepository: PriceHistoryRepository,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val userRepository: UserRepository,
    private val shoppingItemRepository: ShoppingItemRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _uiState.asStateFlow()
    
    // User data
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Price history for current user
    val userPriceHistory: StateFlow<List<PriceHistory>> = _currentUser
        .map { user ->
            if (user != null) {
                priceHistoryRepository.getPriceHistoryForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Scan history for current user
    val userScanHistory: StateFlow<List<ScanHistory>> = _currentUser
        .map { user ->
            if (user != null) {
                scanHistoryRepository.getScanHistoryForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Spending analytics
    val spendingAnalytics: StateFlow<SpendingAnalytics> = userScanHistory
        .map { history ->
            calculateSpendingAnalytics(history)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingAnalytics())
    
    init {
        observeCurrentUser()
    }
    
    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.authStateFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }
    
    fun addPriceEntry(
        barcode: String,
        price: Double,
        storeName: String? = null,
        isOnSale: Boolean = false,
        salePrice: Double? = null
    ) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                
                val priceHistory = PriceHistory(
                    id = UUID.randomUUID().toString(),
                    barcode = barcode,
                    price = price,
                    storeName = storeName,
                    userId = user.id,
                    recordedAt = System.currentTimeMillis(),
                    isOnSale = isOnSale,
                    salePrice = salePrice
                )
                
                priceHistoryRepository.addPriceHistory(priceHistory)
                
                _uiState.value = _uiState.value.copy(
                    priceAdded = true,
                    error = null
                )
                
                Timber.d("Added price entry: $price for barcode $barcode")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to add price entry")
                handleError("Failed to add price: ${e.message}")
            }
        }
    }
    
    fun calculateGroupBalance(groupId: String) {
        viewModelScope.launch {
            try {
                // Get all users in the group
                val users = userRepository.getUsersInGroup(groupId).first()
                
                // Calculate total spending per user
                val userSpending = mutableMapOf<String, Double>()
                users.forEach { user ->
                    val userHistory = userScanHistory.value.filter { it.userId == user.id }
                    userSpending[user.id] = userHistory.sumOf { it.price ?: 0.0 }
                }
                
                // Calculate group balance
                val totalSpent = userSpending.values.sum()
                val averageSpent = if (users.isNotEmpty()) totalSpent / users.size else 0.0
                
                val balances = users.map { user ->
                    UserBalance(
                        userId = user.id,
                        userName = user.displayName,
                        spent = userSpending[user.id] ?: 0.0,
                        balance = userSpending[user.id] ?: 0.0 - averageSpent
                    )
                }
                
                _uiState.value = _uiState.value.copy(
                    groupBalances = balances,
                    totalGroupSpent = totalSpent
                )
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to calculate group balance")
                handleError("Failed to calculate balance: ${e.message}")
            }
        }
    }
    
    fun getSpendingByCategory(): Map<String, Double> {
        val history = userScanHistory.value
        return history
            .groupBy { it.categoryId ?: "Uncategorized" }
            .mapValues { (_, items) ->
                items.sumOf { it.price ?: 0.0 }
            }
    }
    
    fun getSpendingByStore(): Map<String, Double> {
        val history = userScanHistory.value
        return history
            .groupBy { it.storeName ?: "Unknown" }
            .mapValues { (_, items) ->
                items.sumOf { it.price ?: 0.0 }
            }
    }
    
    fun getMonthlySpending(): Map<String, Double> {
        val history = userScanHistory.value
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        
        return history
            .groupBy { item ->
                dateFormat.format(Date(item.scannedAt))
            }
            .mapValues { (_, items) ->
                items.sumOf { it.price ?: 0.0 }
            }
            .toSortedMap()
    }
    
    fun getLatestPricesForBarcode(barcode: String): Flow<List<PriceHistory>> {
        return priceHistoryRepository.getPriceHistoryForBarcode(barcode)
    }
    
    fun getAveragePriceForBarcode(barcode: String): Double? {
        return userPriceHistory.value
            .filter { it.barcode == barcode }
            .takeLast(10) // Last 10 entries
            .map { it.price }
            .average()
            .takeIf { !it.isNaN() }
    }
    
    fun dismissDialogs() {
        _uiState.value = _uiState.value.copy(
            priceAdded = false,
            error = null
        )
    }
    
    private fun handleError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }
    
    private fun calculateSpendingAnalytics(history: List<ScanHistory>): SpendingAnalytics {
        val totalSpent = history.sumOf { it.price ?: 0.0 }
        val itemsCount = history.size
        val averagePrice = if (itemsCount > 0) totalSpent / itemsCount else 0.0
        
        // Most expensive item
        val mostExpensive = history.maxByOrNull { it.price ?: 0.0 }
        
        // Most frequent store
        val mostFrequentStore = history
            .groupBy { it.storeName ?: "Unknown" }
            .maxByOrNull { it.value.size }
            ?.key
        
        // Spending by category
        val spendingByCategory = history
            .groupBy { it.categoryId ?: "Uncategorized" }
            .mapValues { (_, items) ->
                items.sumOf { it.price ?: 0.0 }
            }
        
        return SpendingAnalytics(
            totalSpent = totalSpent,
            itemsCount = itemsCount,
            averagePrice = averagePrice,
            mostExpensiveItem = mostExpensive,
            mostFrequentStore = mostFrequentStore,
            spendingByCategory = spendingByCategory
        )
    }
    
    data class FinanceUiState(
        val priceAdded: Boolean = false,
        val groupBalances: List<UserBalance> = emptyList(),
        val totalGroupSpent: Double = 0.0,
        val error: String? = null
    )
    
    data class SpendingAnalytics(
        val totalSpent: Double = 0.0,
        val itemsCount: Int = 0,
        val averagePrice: Double = 0.0,
        val mostExpensiveItem: ScanHistory? = null,
        val mostFrequentStore: String? = null,
        val spendingByCategory: Map<String, Double> = emptyMap()
    )
    
    data class UserBalance(
        val userId: String,
        val userName: String,
        val spent: Double,
        val balance: Double
    )
}