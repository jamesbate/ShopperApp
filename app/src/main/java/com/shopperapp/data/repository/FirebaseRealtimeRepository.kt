package com.shopperapp.data.repository

import com.google.firebase.database.*
import com.shopperapp.data.models.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRealtimeRepository @Inject constructor(
    private val database: FirebaseDatabase
) {
    private val reference = database.reference

    // Shopping List operations
    fun getShoppingItemsForGroup(groupId: String): Flow<List<ShoppingItem>> = callbackFlow {
        val itemsRef = reference.child("groups").child(groupId).child("shopping_items")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(ShoppingItem::class.java) }
                trySend(items)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        itemsRef.addValueEventListener(listener)
        awaitClose { itemsRef.removeEventListener(listener) }
    }

    suspend fun addShoppingItem(groupId: String, item: ShoppingItem): Result<Unit> {
        return try {
            reference.child("groups").child(groupId).child("shopping_items")
                .child(item.id).setValue(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShoppingItem(groupId: String, item: ShoppingItem): Result<Unit> {
        return try {
            reference.child("groups").child(groupId).child("shopping_items")
                .child(item.id).setValue(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteShoppingItem(groupId: String, itemId: String): Result<Unit> {
        return try {
            reference.child("groups").child(groupId).child("shopping_items")
                .child(itemId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User operations
    fun getUserProfile(userId: String): Flow<User?> = callbackFlow {
        val userRef = reference.child("users").child(userId)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                trySend(user)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        userRef.addValueEventListener(listener)
        awaitClose { userRef.removeEventListener(listener) }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            reference.child("users").child(user.id).setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Group operations
    fun getGroup(groupId: String): Flow<ShoppingGroup?> = callbackFlow {
        val groupRef = reference.child("groups").child(groupId)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(ShoppingGroup::class.java)
                trySend(group)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        groupRef.addValueEventListener(listener)
        awaitClose { groupRef.removeEventListener(listener) }
    }

    suspend fun createGroup(group: ShoppingGroup): Result<Unit> {
        return try {
            reference.child("groups").child(group.id).setValue(group).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Item Metadata operations
    fun getItemMetadata(barcode: String): Flow<ItemMetadata?> = callbackFlow {
        val metadataRef = reference.child("item_metadata").child(barcode)
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val metadata = snapshot.getValue(ItemMetadata::class.java)
                trySend(metadata)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        metadataRef.addValueEventListener(listener)
        awaitClose { metadataRef.removeEventListener(listener) }
    }

    suspend fun updateItemMetadata(metadata: ItemMetadata): Result<Unit> {
        return try {
            reference.child("item_metadata").child(metadata.barcode).setValue(metadata).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Scan History operations
    suspend fun addScanHistory(scanHistory: ScanHistory): Result<Unit> {
        return try {
            reference.child("scan_history").child(scanHistory.id).setValue(scanHistory).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Price History operations
    suspend fun addPriceHistory(priceHistory: PriceHistory): Result<Unit> {
        return try {
            reference.child("price_history").child(priceHistory.id).setValue(priceHistory).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Presence tracking for real-time collaboration
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean) {
        val presenceRef = reference.child("presence").child(userId)
        val status = mapOf(
            "isOnline" to isOnline,
            "lastActiveAt" to System.currentTimeMillis()
        )
        presenceRef.setValue(status).await()
        
        // Set up automatic cleanup when user goes offline
        if (isOnline) {
            presenceRef.onDisconnect().setValue(mapOf(
                "isOnline" to false,
                "lastActiveAt" to ServerValue.TIMESTAMP
            ))
        }
    }
}