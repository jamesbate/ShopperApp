package com.shopperapp.data.database

import androidx.room.*
import com.shopperapp.data.models.ItemMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemMetadataDao {
    @Query("SELECT * FROM item_metadata WHERE barcode = :barcode")
    suspend fun getMetadataByBarcode(barcode: String): ItemMetadata?

    @Query("SELECT * FROM item_metadata WHERE itemName LIKE '%' || :query || '%' ORDER BY scanCount DESC")
    fun searchItemsByName(query: String): Flow<List<ItemMetadata>>

    @Query("SELECT * FROM item_metadata WHERE categoryId = :categoryId ORDER BY itemName")
    fun getItemsByCategory(categoryId: String): Flow<List<ItemMetadata>>

    @Query("SELECT * FROM item_metadata ORDER BY scanCount DESC LIMIT :limit")
    fun getMostScannedItems(limit: Int = 10): Flow<List<ItemMetadata>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: ItemMetadata)

    @Update
    suspend fun updateMetadata(metadata: ItemMetadata)

    @Delete
    suspend fun deleteMetadata(metadata: ItemMetadata)

    @Query("UPDATE item_metadata SET scanCount = scanCount + 1, lastUpdated = :timestamp WHERE barcode = :barcode")
    suspend fun incrementScanCount(barcode: String, timestamp: Long)
}