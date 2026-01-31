package com.shopperapp.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.shopperapp.data.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideShopperDatabase(@ApplicationContext context: Context): ShopperDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ShopperDatabase::class.java,
            "shopper_database"
        ).build()
    }

    @Provides
    fun provideShoppingItemDao(database: ShopperDatabase): ShoppingItemDao {
        return database.shoppingItemDao()
    }

    @Provides
    fun provideUserDao(database: ShopperDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideShoppingGroupDao(database: ShopperDatabase): ShoppingGroupDao {
        return database.shoppingGroupDao()
    }

    @Provides
    fun provideItemMetadataDao(database: ShopperDatabase): ItemMetadataDao {
        return database.itemMetadataDao()
    }

    @Provides
    fun provideScanHistoryDao(database: ShopperDatabase): ScanHistoryDao {
        return database.scanHistoryDao()
    }

    @Provides
    fun provideCategoryDao(database: ShopperDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun providePriceHistoryDao(database: ShopperDatabase): PriceHistoryDao {
        return database.priceHistoryDao()
    }
}