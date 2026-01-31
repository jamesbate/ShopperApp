package com.shopperapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.shopperapp.data.models.*

@Database(
    entities = [
        ShoppingItem::class,
        User::class,
        ShoppingGroup::class,
        ItemMetadata::class,
        ScanHistory::class,
        Category::class,
        PriceHistory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ShopperDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun userDao(): UserDao
    abstract fun shoppingGroupDao(): ShoppingGroupDao
    abstract fun itemMetadataDao(): ItemMetadataDao
    abstract fun scanHistoryDao(): ScanHistoryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun priceHistoryDao(): PriceHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: ShopperDatabase? = null

        fun getDatabase(context: Context): ShopperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopperDatabase::class.java,
                    "shopper_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Example migration for future schema changes
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add future migrations here
            }
        }
    }
}