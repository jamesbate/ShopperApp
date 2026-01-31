package com.shopperapp.di

import com.shopperapp.data.repository.FirebaseAuthRepository
import com.shopperapp.data.repository.FirebaseRealtimeRepository
import com.shopperapp.data.repository.ShoppingItemRepository
import com.shopperapp.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFirebaseAuthRepository(
        firebaseAuthRepo: FirebaseAuthRepository
    ): FirebaseAuthRepository {
        return firebaseAuthRepo
    }

    @Provides
    @Singleton
    fun provideFirebaseRealtimeRepository(
        firebaseRealtimeRepo: FirebaseRealtimeRepository
    ): FirebaseRealtimeRepository {
        return firebaseRealtimeRepo
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuthRepo: FirebaseAuthRepository,
        firebaseRealtimeRepo: FirebaseRealtimeRepository
    ): UserRepository {
        return UserRepository(firebaseAuthRepo, firebaseRealtimeRepo)
    }

    @Provides
    @Singleton
    fun provideShoppingItemRepository(
        firebaseRealtimeRepo: FirebaseRealtimeRepository
    ): ShoppingItemRepository {
        return ShoppingItemRepository(firebaseRealtimeRepo)
    }
}