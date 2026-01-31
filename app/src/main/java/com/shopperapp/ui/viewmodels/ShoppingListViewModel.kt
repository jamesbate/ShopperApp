package com.shopperapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopperapp.data.models.*
import com.shopperapp.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingItemRepository: ShoppingItemRepository,
    private val userRepository: UserRepository,
    private val itemMetadataRepository: ItemMetadataRepository,
    private val scanHistoryRepository: ScanHistoryRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()
    
    // User data
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Shopping items
    val shoppingItems: StateFlow<List<ShoppingItem>> = combine(
        _currentUser,
        _uiState.map { it.showCompletedItems }
    ) { user, showCompleted ->
        if (user != null && user.currentGroupId != null) {
            if (showCompleted) {
                shoppingItemRepository.getShoppingItemsForGroup(user.currentGroupId)
            } else {
                shoppingItemRepository.getActiveShoppingItemsForGroup(user.currentGroupId)
            }
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Search results
    val searchResults: StateFlow<List<ShoppingItem>> = _uiState
        .map { it.searchQuery }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isNotEmpty() && _currentUser.value?.currentGroupId != null) {
                shoppingItemRepository.searchItems(query, _currentUser.value!!.currentGroupId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Item suggestions for autocomplete
    val itemSuggestions: StateFlow<List<String>> = _currentUser
        .map { user ->
            if (user?.currentGroupId != null) {
                shoppingItemRepository.getItemNamesForGroup(user.currentGroupId)
            } else {
                flowOf(emptyList())
            }
        }
        .flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
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
    
    fun addShoppingItem(
        name: String,
        quantity: Int = 1,
        notes: String? = null,
        hyperlink: String? = null,
        barcode: String? = null
    ) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val groupId = user.currentGroupId ?: return@launch
                
                val item = ShoppingItem(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    quantity = quantity,
                    barcode = barcode,
                    groupId = groupId,
                    addedBy = user.id,
                    addedAt = System.currentTimeMillis(),
                    notes = notes,
                    hyperlink = hyperlink,
                    suggestedFromHistory = false
                )
                
                shoppingItemRepository.addShoppingItem(groupId, item)
                
                _uiState.value = _uiState.value.copy(
                    newItemName = "",
                    error = null
                )
                
                Timber.d("Added shopping item: $name")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to add shopping item")
                handleError("Failed to add item: ${e.message}")
            }
        }
    }
    
    fun updateShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val groupId = user.currentGroupId ?: return@launch
                
                shoppingItemRepository.updateShoppingItem(groupId, item)
                Timber.d("Updated shopping item: ${item.name}")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to update shopping item")
                handleError("Failed to update item: ${e.message}")
            }
        }
    }
    
    fun deleteShoppingItem(itemId: String) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val groupId = user.currentGroupId ?: return@launch
                
                shoppingItemRepository.deleteShoppingItem(groupId, itemId)
                Timber.d("Deleted shopping item: $itemId")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete shopping item")
                handleError("Failed to delete item: ${e.message}")
            }
        }
    }
    
    fun toggleItemCompletion(itemId: String, completed: Boolean) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val groupId = user.currentGroupId ?: return@launch
                
                if (completed) {
                    shoppingItemRepository.markItemCompleted(groupId, itemId, user.id)
                } else {
                    // Mark as incomplete - update the item directly
                    val item = shoppingItems.value.find { it.id == itemId }
                    item?.let {
                        val updatedItem = it.copy(
                            completed = false,
                            completedAt = null,
                            completedBy = null
                        )
                        shoppingItemRepository.updateShoppingItem(groupId, updatedItem)
                    }
                }
                
                Timber.d("Toggled item completion: $itemId -> $completed")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle item completion")
                handleError("Failed to update item: ${e.message}")
            }
        }
    }
    
    fun updateItemQuantity(itemId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val groupId = user.currentGroupId ?: return@launch
                
                val item = shoppingItems.value.find { it.id == itemId }
                item?.let {
                    val updatedItem = it.copy(quantity = quantity)
                    shoppingItemRepository.updateShoppingItem(groupId, updatedItem)
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to update item quantity")
                handleError("Failed to update quantity: ${e.message}")
            }
        }
    }
    
    fun onNewItemNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(newItemName = name)
        
        // Check if this item exists in history to mark as suggested
        if (name.isNotEmpty()) {
            viewModelScope.launch {
                val suggestions = itemSuggestions.value
                val isSuggested = suggestions.any { it.equals(name, ignoreCase = true) }
                
                _uiState.value = _uiState.value.copy(
                    suggestedFromHistory = isSuggested
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                suggestedFromHistory = false
            )
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = query.isNotEmpty()
        )
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            isSearching = false
        )
    }
    
    fun toggleShowCompletedItems() {
        _uiState.value = _uiState.value.copy(
            showCompletedItems = !_uiState.value.showCompletedItems
        )
    }
    
    fun showAddItemDialog() {
        _uiState.value = _uiState.value.copy(showAddItemDialog = true)
    }
    
    fun hideAddItemDialog() {
        _uiState.value = _uiState.value.copy(
            showAddItemDialog = false,
            newItemName = "",
            error = null
        )
    }
    
    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun handleError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
    }
    
    // Get active items count for badge
    fun getActiveItemsCount(): Int {
        return shoppingItems.value.count { !it.completed }
    }
    
    // Get completed items count
    fun getCompletedItemsCount(): Int {
        return shoppingItems.value.count { it.completed }
    }
    
    // Filter items by completion status
    fun getActiveItems(): List<ShoppingItem> {
        return shoppingItems.value.filter { !it.completed }
    }
    
    fun getCompletedItems(): List<ShoppingItem> {
        return shoppingItems.value.filter { it.completed }
    }
    
    // Get items added by current user
    fun getMyItems(): List<ShoppingItem> {
        return shoppingItems.value.filter { it.addedBy == _currentUser.value?.id }
    }
    
    data class ShoppingListUiState(
        val newItemName: String = "",
        val searchQuery: String = "",
        val isSearching: Boolean = false,
        val showCompletedItems: Boolean = false,
        val suggestedFromHistory: Boolean = false,
        val showAddItemDialog: Boolean = false,
        val error: String? = null
    )
}