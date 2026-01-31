package com.shopperapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@Entity(tableName = "categories")
@IgnoreExtraProperties
data class Category(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val parentId: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)