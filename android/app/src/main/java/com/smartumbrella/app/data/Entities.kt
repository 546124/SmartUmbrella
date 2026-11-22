package com.smartumbrella.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "umbrella_slots")
data class UmbrellaSlotEntity(
    @PrimaryKey val slotNumber: Int,
    val hasUmbrella: Boolean,
    val locked: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "loan_events")
data class LoanEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slotNumber: Int,
    val action: String,
    val hardwareTag: String? = null,
    val occurredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val operation: String,
    val slotNumber: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
