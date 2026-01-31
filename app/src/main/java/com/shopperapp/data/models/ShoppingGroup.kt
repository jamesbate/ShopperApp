package com.shopperapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@Entity(tableName = "shopping_groups")
@IgnoreExtraProperties
data class ShoppingGroup(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val memberIds: List<String> = emptyList(),
    val isActive: Boolean = true
)