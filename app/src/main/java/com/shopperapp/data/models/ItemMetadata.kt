package com.shopperapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.datetime.LocalDate

@Entity(tableName = "item_metadata")
@IgnoreExtraProperties
data class ItemMetadata(
    @PrimaryKey
    val barcode: String = "",
    val itemName: String = "",
    val typicalPrice: Double? = null,
    val categoryId: String? = null,
    val typicalExpiryDays: Int? = null,
    val brand: String? = null,
    val manufacturer: String? = null,
    val weight: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val scanCount: Int = 1,
    val imageUrl: String? = null
)