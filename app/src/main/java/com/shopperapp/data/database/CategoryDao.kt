package com.shopperapp.data.database

import androidx.room.*
import com.shopperapp.data.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder")
    fun getAllActiveCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): Category?

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder")
    fun getSubCategories(parentId: String?): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("UPDATE categories SET isActive = :isActive WHERE id = :categoryId")
    suspend fun updateCategoryActiveStatus(categoryId: String, isActive: Boolean)
}