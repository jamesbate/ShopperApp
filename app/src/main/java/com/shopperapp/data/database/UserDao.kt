package com.shopperapp.data.database

import androidx.room.*
import com.shopperapp.data.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE currentGroupId = :groupId")
    fun getUsersInGroup(groupId: String): Flow<List<User>>

    @Query("SELECT * FROM users WHERE isOnline = 1")
    fun getOnlineUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET currentGroupId = :groupId WHERE id = :userId")
    suspend fun updateUserGroup(userId: String, groupId: String?)

    @Query("UPDATE users SET isOnline = :isOnline, lastActiveAt = :lastActiveAt WHERE id = :userId")
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean, lastActiveAt: Long)
}