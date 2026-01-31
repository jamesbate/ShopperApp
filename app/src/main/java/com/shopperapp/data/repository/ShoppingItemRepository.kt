package com.shopperapp.data.repository

import com.shopperapp.data.database.ShoppingItemDao
import com.shopperapp.data.models.ShoppingItem
import com.shopperapp.data.repository.FirebaseRealtimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingItemRepository @Inject constructor(
    private val firebaseRealtimeRepo: FirebaseRealtimeRepository,
    private val shoppingItemDao: ShoppingItemDao
) {

    fun getShoppingItemsForGroup(groupId: String): Flow<List<ShoppingItem>> {
        // Combine local and remote data sources
        val localItems = shoppingItemDao.getShoppingItemsForGroup(groupId)
        val remoteItems = firebaseRealtimeRepo.getShoppingItemsForGroup(groupId)
        
        return combine(localItems, remoteItems) { local, remote ->
            // Sync remote items to local and return the latest
            syncRemoteToLocal(remote)
            remote
        }
    }

    fun getActiveShoppingItemsForGroup(groupId: String): Flow<List<ShoppingItem>> {
        return shoppingItemDao.getActiveShoppingItemsForGroup(groupId)
    }

    suspend fun addShoppingItem(groupId: String, item: ShoppingItem): Result<Unit> {
        return try {
            // Add to both local and remote
            shoppingItemDao.insertItem(item)
            firebaseRealtimeRepo.addShoppingItem(groupId, item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShoppingItem(groupId: String, item: ShoppingItem): Result<Unit> {
        return try {
            // Update both local and remote
            shoppingItemDao.updateItem(item)
            firebaseRealtimeRepo.updateShoppingItem(groupId, item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteShoppingItem(groupId: String, itemId: String): Result<Unit> {
        return try {
            // Delete from both local and remote
            shoppingItemDao.deleteItemById(itemId)
            firebaseRealtimeRepo.deleteShoppingItem(groupId, itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markItemCompleted(groupId: String, itemId: String, userId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            shoppingItemDao.updateItemCompletion(itemId, true, currentTime, userId)
            // Also update in remote
            firebaseRealtimeRepo.updateShoppingItem(groupId, 
                ShoppingItem(id = itemId, completed = true, completedAt = currentTime, completedBy = userId)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun searchItems(query: String, groupId: String): Flow<List<ShoppingItem>> {
        return shoppingItemDao.searchItems(query, groupId)
    }

    fun getItemNamesForGroup(groupId: String): Flow<List<String>> {
        return shoppingItemDao.getItemNamesForGroup(groupId)
    }

    private suspend fun syncRemoteToLocal(remoteItems: List<ShoppingItem>) {
        // Sync remote items to local database
        remoteItems.forEach { item ->
            shoppingItemDao.insertItem(item)
        }
    }
}