package io.github.ryoaonetsuki.mira.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.ryoaonetsuki.mira.data.local.entities.WhatsAppGuardEntity

@Dao
interface WhatsAppGuardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: WhatsAppGuardEntity)

    @Query("SELECT * FROM whatsapp_guard_state WHERE conversation = :conversation AND latestIncomingText = :latestIncomingText AND notificationKey = :notificationKey")
    suspend fun findByKey(conversation: String, latestIncomingText: String, notificationKey: String): WhatsAppGuardEntity?

    @Query("SELECT * FROM whatsapp_guard_state WHERE conversation = :conversation")
    suspend fun findByConversation(conversation: String): List<WhatsAppGuardEntity>

    @Query("SELECT * FROM whatsapp_guard_state")
    suspend fun findAll(): List<WhatsAppGuardEntity>

    @Query("DELETE FROM whatsapp_guard_state WHERE lastRepliedAt < :cutoff")
    suspend fun pruneOlderThan(cutoff: Long)

    @Query("DELETE FROM whatsapp_guard_state")
    suspend fun deleteAll()
}
