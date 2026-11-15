package io.github.ryoaonetsuki.mira.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "whatsapp_guard_state",
    indices = [Index(value = ["conversation", "latestIncomingText", "notificationKey"], unique = true)]
)
data class WhatsAppGuardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val conversation: String,
    val latestIncomingText: String,
    val notificationKey: String,
    val lastRepliedAt: Long
)
