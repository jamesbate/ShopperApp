package com.shopperapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.datetime.LocalDate

@Entity(tableName = "shopping_items")
@IgnoreExtraProperties
data class ShoppingItem(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val barcode: String? = null,
    val categoryId: String? = null,
    val groupId: String = "",
    val addedBy: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val completedBy: String? = null,
    val notes: String? = null,
    val hyperlink: String? = null,
    val suggestedFromHistory: Boolean = false
)