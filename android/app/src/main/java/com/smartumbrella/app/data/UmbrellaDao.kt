package com.smartumbrella.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UmbrellaDao {
    @Query("SELECT * FROM umbrella_slots ORDER BY slotNumber")
    fun observeSlots(): Flow<List<UmbrellaSlotEntity>>

    @Query("SELECT * FROM umbrella_slots WHERE slotNumber = :slotNumber")
    suspend fun getSlot(slotNumber: Int): UmbrellaSlotEntity?

    @Query("SELECT COUNT(*) FROM umbrella_slots")
    suspend fun slotCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<UmbrellaSlotEntity>)

    @Query("UPDATE umbrella_slots SET hasUmbrella = :hasUmbrella, locked = :locked, lastUpdated = :updatedAt WHERE slotNumber = :slotNumber")
    suspend fun updateSlot(slotNumber: Int, hasUmbrella: Boolean, locked: Boolean, updatedAt: Long)

    @Insert
    suspend fun insertEvent(event: LoanEventEntity): Long

    @Insert
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("SELECT * FROM loan_events ORDER BY occurredAt DESC")
    fun observeEvents(): Flow<List<LoanEventEntity>>

    @Query("SELECT * FROM loan_events WHERE id = :eventId")
    suspend fun getEvent(eventId: Long): LoanEventEntity?

    @Query("SELECT * FROM sync_queue WHERE synced = 0 ORDER BY createdAt")
    fun observePendingSync(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE synced = 0 ORDER BY createdAt")
    suspend fun getPendingSyncOnce(): List<SyncQueueEntity>

    @Query("UPDATE sync_queue SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}
