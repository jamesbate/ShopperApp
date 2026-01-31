package com.shopperapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopperapp.ui.navigation.Screen
import com.shopperapp.ui.viewmodels.ShoppingListViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    navController: NavController,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shoppingItems by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val itemSuggestions by viewModel.itemSuggestions.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val displayItems = if (uiState.isSearching) searchResults else shoppingItems
    val activeItems = displayItems.filter { !it.completed }
    val completedItems = displayItems.filter { it.completed }
    
    // Show add item dialog
    if (uiState.showAddItemDialog) {
        AddItemDialog(
            itemName = uiState.newItemName,
            suggestions = itemSuggestions,
            isSuggested = uiState.suggestedFromHistory,
            onItemNameChanged = viewModel::onNewItemNameChanged,
            onAddItem = { name, notes, hyperlink ->
                viewModel.addShoppingItem(name, 1, notes, hyperlink)
                viewModel.hideAddItemDialog()
            },
            onDismiss = viewModel::hideAddItemDialog
        )
    }
    
    // Show error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text("OK")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Shopping List",
                    style = MaterialTheme.typography.titleLarge
                ) 
            },
            actions = {
                IconButton(onClick = { viewModel.toggleShowCompletedItems() }) {
                    Icon(
                        imageVector = if (uiState.showCompletedItems) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle completed items"
                    )
                }
                IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile"
                    )
                }
            }
        )
        
        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChanged = viewModel::onSearchQueryChanged,
            onClearSearch = viewModel::clearSearch,
            onAddItem = viewModel::showAddItemDialog
        )
        
        // Items List
        if (displayItems.isEmpty()) {
            EmptyState(
                isSearching = uiState.isSearching,
                onAddItem = viewModel::showAddItemDialog
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Active Items
                if (activeItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active (${activeItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(activeItems) { item ->
                        ShoppingItemCard(
                            item = item,
                            onToggleComplete = { viewModel.toggleItemCompletion(item.id, !item.completed) },
                            onUpdateQuantity = { viewModel.updateItemQuantity(item.id, it) },
                            onEdit = { /* TODO: Navigate to edit screen */ },
                            onDelete = { viewModel.deleteShoppingItem(item.id) }
                        )
                    }
                }
                
                // Completed Items
                if (completedItems.isNotEmpty() && uiState.showCompletedItems) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Completed (${completedItems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(completedItems) { item ->
                        ShoppingItemCard(
                            item = item,
                            onToggleComplete = { viewModel.toggleItemCompletion(item.id, !item.completed) },
                            onUpdateQuantity = { viewModel.updateItemQuantity(item.id, it) },
                            onEdit = { /* TODO: Navigate to edit screen */ },
                            onDelete = { viewModel.deleteShoppingItem(item.id) }
                        )
                    }
                }
            }
        }
        
        // Bottom Navigation Bar
        BottomNavigationBar(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onAddItem: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search or add item...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (query.isNotEmpty()) {
                        onAddItem()
                    }
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier.weight(1f)
        )
        
        FloatingActionButton(
            onClick = onAddItem,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Item"
            )
        }
    }
}

@Composable
private fun ShoppingItemCard(
    item: com.shopperapp.data.models.ShoppingItem,
    onToggleComplete: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggleComplete() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox and item info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = item.completed,
                    onCheckedChange = { onToggleComplete() }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (item.completed) TextDecoration.LineThrough else null,
                        color = if (item.completed) 
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (item.quantity > 1) {
                        Text(
                            text = "Quantity: ${item.quantity}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    if (item.notes != null) {
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Options menu
            IconButton(
                onClick = { showOptions = !showOptions }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options"
                )
            }
            
            DropdownMenu(
                expanded = showOptions,
                onDismissRequest = { showOptions = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    },
                    onClick = {
                        onEdit()
                        showOptions = false
                    }
                )
                
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    },
                    onClick = {
                        onDelete()
                        showOptions = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    isSearching: Boolean,
    onAddItem: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isSearching) "No items found" else "Your shopping list is empty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isSearching) "Try a different search term" else "Add your first item to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        if (!isSearching) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onAddItem) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add First Item")
            }
        }
    }
}

@Composable
private fun AddItemDialog(
    itemName: String,
    suggestions: List<String>,
    isSuggested: Boolean,
    onItemNameChanged: (String) -> Unit,
    onAddItem: (String, String?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var hyperlink by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column {
                // Item name with suggestions
                ExposedDropdownMenuBox(
                    expanded = showSuggestions,
                    onExpandedChange = { showSuggestions = it }
                ) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { 
                            onItemNameChanged(it)
                            showSuggestions = true
                        },
                        label = { Text("Item Name") },
                        trailingIcon = {
                            if (suggestions.isNotEmpty()) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSuggestions)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    if (suggestions.isNotEmpty() && (itemName.isNotEmpty() || showSuggestions)) {
                        val filteredSuggestions = suggestions.filter { 
                            it.contains(itemName, ignoreCase = true) 
                        }
                        
                        ExposedDropdownMenu(
                            expanded = showSuggestions && filteredSuggestions.isNotEmpty(),
                            onDismissRequest = { showSuggestions = false }
                        ) {
                            filteredSuggestions.take(5).forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        onItemNameChanged(suggestion)
                                        showSuggestions = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = hyperlink,
                    onValueChange = { hyperlink = it },
                    label = { Text("Link (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (isSuggested) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Item found in your history",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isNotEmpty()) {
                        onAddItem(itemName, notes.ifEmpty { null }, hyperlink.ifEmpty { null })
                    }
                },
                enabled = itemName.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { Text("List") },
            selected = true,
            onClick = { }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
            label = { Text("Scan") },
            selected = false,
            onClick = { navController.navigate(Screen.Scanner.route) }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            label = { Text("Finance") },
            selected = false,
            onClick = { navController.navigate(Screen.Finance.route) }
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            label = { Text("Expiry") },
            selected = false,
            onClick = { navController.navigate(Screen.Expiry.route) }
        )
    }
}