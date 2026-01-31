package com.shopperapp.data.repository

import com.shopperapp.data.database.ScanHistoryDao
import com.shopperapp.data.models.ScanHistory
import com.shopperapp.data.repository.FirebaseRealtimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanHistoryRepository @Inject constructor(
    private val scanHistoryDao: ScanHistoryDao,
    private val firebaseRealtimeRepo: FirebaseRealtimeRepository
) {

    fun getScanHistoryForUser(userId: String): Flow<List<ScanHistory>> {
        return scanHistoryDao.getScanHistoryForUser(userId)
    }

    fun getUnopenedItemsForUser(userId: String): Flow<List<ScanHistory>> {
        return scanHistoryDao.getUnopenedItemsForUser(userId)
    }

    fun getExpiringItemsForUser(userId: String, currentDate: String): Flow<List<ScanHistory>> {
        return scanHistoryDao.getExpiringItemsForUser(userId, currentDate)
    }

    fun getScanHistoryForBarcode(barcode: String): Flow<List<ScanHistory>> {
        return scanHistoryDao.getScanHistoryForBarcode(barcode)
    }

    suspend fun addScanHistory(scanHistory: ScanHistory): Result<Unit> {
        return try {
            // Add to both local and remote
            scanHistoryDao.insertScanHistory(scanHistory)
            firebaseRealtimeRepo.addScanHistory(scanHistory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateScanHistory(scanHistory: ScanHistory): Result<Unit> {
        return try {
            scanHistoryDao.updateScanHistory(scanHistory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteScanHistory(scanHistory: ScanHistory): Result<Unit> {
        return try {
            scanHistoryDao.deleteScanHistory(scanHistory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItemOpenStatus(scanId: String, isOpened: Boolean, openedAt: Long?): Result<Unit> {
        return try {
            scanHistoryDao.updateItemOpenStatus(scanId, isOpened, openedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}