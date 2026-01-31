package com.shopperapp.data.repository

import com.shopperapp.data.database.PriceHistoryDao
import com.shopperapp.data.models.PriceHistory
import com.shopperapp.data.repository.FirebaseRealtimeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceHistoryRepository @Inject constructor(
    private val priceHistoryDao: PriceHistoryDao,
    private val firebaseRealtimeRepo: FirebaseRealtimeRepository
) {

    fun getPriceHistoryForBarcode(barcode: String): Flow<List<PriceHistory>> {
        return priceHistoryDao.getPriceHistoryForBarcode(barcode)
    }

    fun getPriceHistoryForUser(userId: String): Flow<List<PriceHistory>> {
        return priceHistoryDao.getPriceHistoryForUser(userId)
    }

    fun getPriceHistoryForStore(storeName: String): Flow<List<PriceHistory>> {
        return priceHistoryDao.getPriceHistoryForStore(storeName)
    }

    suspend fun getLatestPriceForBarcode(barcode: String): Double? {
        return priceHistoryDao.getLatestPriceForBarcode(barcode)
    }

    suspend fun getAveragePriceSince(barcode: String, sinceTimestamp: Long): Double? {
        return priceHistoryDao.getAveragePriceSince(barcode, sinceTimestamp)
    }

    suspend fun addPriceHistory(priceHistory: PriceHistory): Result<Unit> {
        return try {
            // Add to both local and remote
            priceHistoryDao.insertPriceHistory(priceHistory)
            firebaseRealtimeRepo.addPriceHistory(priceHistory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePriceHistory(priceHistory: PriceHistory): Result<Unit> {
        return try {
            priceHistoryDao.updatePriceHistory(priceHistory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePriceHistory(priceHistory: PriceHistory): Result<Unit> {
        return try {
            priceHistoryDao.deletePriceHistory(priceHistory)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}