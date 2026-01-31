package com.shopperapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.datetime.LocalDate

@Entity(tableName = "scan_history")
@IgnoreExtraProperties
data class ScanHistory(
    @PrimaryKey
    val id: String = "",
    val userId: String = "",
    val barcode: String? = null,
    val itemName: String = "",
    val price: Double? = null,
    val scannedAt: Long = System.currentTimeMillis(),
    val expiryDate: String? = null,
    val isOpened: Boolean = false,
    val openedAt: Long? = null,
    val categoryId: String? = null,
    val quantity: Int = 1,
    val storeName: String? = null,
    val location: String? = null
)