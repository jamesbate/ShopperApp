package com.shopperapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@Entity(tableName = "price_history")
@IgnoreExtraProperties
data class PriceHistory(
    @PrimaryKey
    val id: String = "",
    val barcode: String = "",
    val price: Double,
    val storeName: String? = null,
    val userId: String = "",
    val recordedAt: Long = System.currentTimeMillis(),
    val isOnSale: Boolean = false,
    val salePrice: Double? = null,
    val location: String? = null
)