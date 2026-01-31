package com.shopperapp.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.shopperapp.data.models.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items WHERE groupId = :groupId ORDER BY addedAt DESC")
    fun getShoppingItemsForGroup(groupId: String): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE groupId = :groupId AND completed = 0 ORDER BY addedAt DESC")
    fun getActiveShoppingItemsForGroup(groupId: String): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE id = :itemId")
    suspend fun getItemById(itemId: String): ShoppingItem?

    @Query("SELECT * FROM shopping_items WHERE name LIKE '%' || :query || '%' AND groupId = :groupId ORDER BY addedAt DESC")
    fun searchItems(query: String, groupId: String): Flow<List<ShoppingItem>>

    @Query("SELECT DISTINCT name FROM shopping_items WHERE groupId = :groupId ORDER BY name")
    fun getItemNamesForGroup(groupId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingItem>)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)

    @Query("DELETE FROM shopping_items WHERE groupId = :groupId")
    suspend fun deleteItemsForGroup(groupId: String)

    @Query("UPDATE shopping_items SET completed = :completed, completedAt = :completedAt, completedBy = :completedBy WHERE id = :itemId")
    suspend fun updateItemCompletion(itemId: String, completed: Boolean, completedAt: Long?, completedBy: String?)
}