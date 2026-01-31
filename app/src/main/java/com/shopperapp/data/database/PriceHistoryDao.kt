package com.shopperapp.data.database

import androidx.room.*
import com.shopperapp.data.models.PriceHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Query("SELECT * FROM price_history WHERE barcode = :barcode ORDER BY recordedAt DESC")
    fun getPriceHistoryForBarcode(barcode: String): Flow<List<PriceHistory>>

    @Query("SELECT * FROM price_history WHERE userId = :userId ORDER BY recordedAt DESC")
    fun getPriceHistoryForUser(userId: String): Flow<List<PriceHistory>>

    @Query("SELECT * FROM price_history WHERE storeName = :storeName ORDER BY recordedAt DESC")
    fun getPriceHistoryForStore(storeName: String): Flow<List<PriceHistory>>

    @Query("SELECT price FROM price_history WHERE barcode = :barcode ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestPriceForBarcode(barcode: String): Double?

    @Query("SELECT AVG(price) FROM price_history WHERE barcode = :barcode AND recordedAt >= :sinceTimestamp")
    suspend fun getAveragePriceSince(barcode: String, sinceTimestamp: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(priceHistory: PriceHistory)

    @Update
    suspend fun updatePriceHistory(priceHistory: PriceHistory)

    @Delete
    suspend fun deletePriceHistory(priceHistory: PriceHistory)
}