package com.shopperapp.data.database

import androidx.room.*
import com.shopperapp.data.models.ScanHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history WHERE userId = :userId ORDER BY scannedAt DESC")
    fun getScanHistoryForUser(userId: String): Flow<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE userId = :userId AND isOpened = 0 ORDER BY scannedAt DESC")
    fun getUnopenedItemsForUser(userId: String): Flow<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE userId = :userId AND expiryDate <= :currentDate AND isOpened = 0")
    fun getExpiringItemsForUser(userId: String, currentDate: String): Flow<List<ScanHistory>>

    @Query("SELECT * FROM scan_history WHERE barcode = :barcode ORDER BY scannedAt DESC")
    fun getScanHistoryForBarcode(barcode: String): Flow<List<ScanHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanHistory(scanHistory: ScanHistory)

    @Update
    suspend fun updateScanHistory(scanHistory: ScanHistory)

    @Delete
    suspend fun deleteScanHistory(scanHistory: ScanHistory)

    @Query("UPDATE scan_history SET isOpened = :isOpened, openedAt = :openedAt WHERE id = :scanId")
    suspend fun updateItemOpenStatus(scanId: String, isOpened: Boolean, openedAt: Long?)
}