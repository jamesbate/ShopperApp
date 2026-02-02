package com.shopperapp.data.database

import androidx.room.*
import com.shopperapp.data.models.ShoppingGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingGroupDao {
    @Query("SELECT * FROM shopping_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): ShoppingGroup?

    @Query("SELECT * FROM shopping_groups WHERE createdBy = :userId OR memberIds LIKE ',' || :userId || ',' OR memberIds LIKE :userId || ',' OR memberIds LIKE ',' || :userId")
    fun getGroupsForUser(userId: String): Flow<List<ShoppingGroup>>

    @Query("SELECT * FROM shopping_groups WHERE createdBy = :userId")
    fun getGroupsCreatedByUser(userId: String): Flow<List<ShoppingGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ShoppingGroup)

    @Update
    suspend fun updateGroup(group: ShoppingGroup)

    @Delete
    suspend fun deleteGroup(group: ShoppingGroup)

    @Query("DELETE FROM shopping_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: String)

    @Query("UPDATE shopping_groups SET memberIds = :memberIds WHERE id = :groupId")
    suspend fun updateGroupMembers(groupId: String, memberIds: List<String>)
}