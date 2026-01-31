package com.shopperapp.data.repository

import com.shopperapp.data.database.UserDao
import com.shopperapp.data.models.User
import com.shopperapp.data.repository.FirebaseAuthRepository
import com.shopperapp.data.repository.FirebaseRealtimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuthRepo: FirebaseAuthRepository,
    private val firebaseRealtimeRepo: FirebaseRealtimeRepository,
    private val userDao: UserDao
) {
    
    val currentUser: User?
        get() = firebaseAuthRepo.currentUser?.toUser()

    val isUserLoggedIn: Boolean
        get() = firebaseAuthRepo.isUserLoggedIn

    fun authStateFlow() = firebaseAuthRepo.authStateFlow().map { firebaseUser ->
        firebaseUser?.toUser()
    }

    suspend fun signUp(email: String, password: String, displayName: String): Result<User> {
        return firebaseAuthRepo.signUp(email, password, displayName).mapCatching { firebaseUser ->
            val user = firebaseUser.toUser()
            // Save user profile to both local and remote
            userDao.insertUser(user)
            firebaseRealtimeRepo.updateUserProfile(user)
            user
        }
    }

    suspend fun signIn(email: String, password: String): Result<User> {
        return firebaseAuthRepo.signIn(email, password).mapCatching { firebaseUser ->
            val user = firebaseUser.toUser()
            // Update local database with latest user info
            userDao.insertUser(user)
            user
        }
    }

    suspend fun signOut(): Result<Unit> {
        return firebaseAuthRepo.signOut()
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            // Update both local and remote
            userDao.updateUser(user)
            firebaseRealtimeRepo.updateUserProfile(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserProfile(userId: String): Flow<User?> {
        return firebaseRealtimeRepo.getUserProfile(userId)
    }

    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        firebaseRealtimeRepo.updateUserOnlineStatus(userId, isOnline)
    }

    fun getUsersInGroup(groupId: String): Flow<List<User>> {
        return userDao.getUsersInGroup(groupId)
    }
}