package com.shopperapp.data.repository

import com.shopperapp.data.database.ItemMetadataDao
import com.shopperapp.data.models.ItemMetadata
import com.shopperapp.data.repository.FirebaseRealtimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemMetadataRepository @Inject constructor(
    private val itemMetadataDao: ItemMetadataDao,
    private val firebaseRealtimeRepo: FirebaseRealtimeRepository
) {

    fun getMetadataByBarcode(barcode: String): Flow<ItemMetadata?> {
        return firebaseRealtimeRepo.getItemMetadata(barcode)
    }

    fun searchItemsByName(query: String): Flow<List<ItemMetadata>> {
        return itemMetadataDao.searchItemsByName(query)
    }

    fun getItemsByCategory(categoryId: String): Flow<List<ItemMetadata>> {
        return itemMetadataDao.getItemsByCategory(categoryId)
    }

    fun getMostScannedItems(limit: Int = 10): Flow<List<ItemMetadata>> {
        return itemMetadataDao.getMostScannedItems(limit)
    }

    suspend fun updateItemMetadata(metadata: ItemMetadata): Result<Unit> {
        return try {
            // Update both local and remote
            itemMetadataDao.updateMetadata(metadata)
            firebaseRealtimeRepo.updateItemMetadata(metadata)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createItemMetadata(metadata: ItemMetadata): Result<Unit> {
        return try {
            // Add to both local and remote
            itemMetadataDao.insertMetadata(metadata)
            firebaseRealtimeRepo.updateItemMetadata(metadata)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementScanCount(barcode: String): Result<Unit> {
        return try {
            val timestamp = System.currentTimeMillis()
            itemMetadataDao.incrementScanCount(barcode, timestamp)
            
            // Also update in remote
            val metadata = getMetadataByBarcode(barcode).firstOrNull()
            metadata?.let {
                val updatedMetadata = it.copy(scanCount = it.scanCount + 1, lastUpdated = timestamp)
                firebaseRealtimeRepo.updateItemMetadata(updatedMetadata)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}