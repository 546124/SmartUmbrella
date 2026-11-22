package com.smartumbrella.app.data

import androidx.room.withTransaction
import com.smartumbrella.app.hardware.UmbrellaHardware
import kotlinx.coroutines.flow.Flow

class UmbrellaRepository(
    private val database: UmbrellaDatabase,
    private val hardware: UmbrellaHardware
) {
    private val dao = database.umbrellaDao()

    val slots: Flow<List<UmbrellaSlotEntity>> = dao.observeSlots()
    val events: Flow<List<LoanEventEntity>> = dao.observeEvents()
    val pendingSync: Flow<List<SyncQueueEntity>> = dao.observePendingSync()

    suspend fun initialize() {
        if (dao.slotCount() == 0) {
            dao.insertSlots((1..6).map { UmbrellaSlotEntity(slotNumber = it, hasUmbrella = it % 2 == 1) })
        }
    }

    suspend fun borrow(slotNumber: Int): OperationResult {
        val slot = dao.getSlot(slotNumber) ?: return OperationResult(false, "伞位不存在")
        if (!slot.hasUmbrella) return OperationResult(false, "该伞位当前没有可借的雨伞")
        if (!hardware.verifyFingerprint()) return OperationResult(false, "指纹验证未通过")
        if (!hardware.unlock(slotNumber)) return OperationResult(false, "伞柜解锁失败")

        recordOperation(slotNumber, "BORROW", null)
        return OperationResult(true, "借伞成功")
    }

    suspend fun returnUmbrella(slotNumber: Int): OperationResult {
        val slot = dao.getSlot(slotNumber) ?: return OperationResult(false, "伞位不存在")
        if (slot.hasUmbrella) return OperationResult(false, "该伞位已有雨伞")
        val tag = hardware.readRfid(slotNumber) ?: return OperationResult(false, "未读取到 RFID 标签")
        recordOperation(slotNumber, "RETURN", tag)
        return OperationResult(true, "归还成功，已读取 $tag")
    }

    /** The caller should throw on upload failure so WorkManager retries without acknowledging the row. */
    suspend fun syncPending(uploader: suspend (SyncQueueEntity) -> Unit): Int {
        var syncedCount = 0
        for (item in dao.getPendingSyncOnce()) {
            uploader(item)
            dao.markSynced(item.id)
            syncedCount++
        }
        return syncedCount
    }

    suspend fun eventForSync(item: SyncQueueEntity): LoanEventEntity? = dao.getEvent(item.eventId)

    private suspend fun recordOperation(slotNumber: Int, operation: String, tag: String?) {
        val now = System.currentTimeMillis()
        database.withTransaction {
            dao.updateSlot(
                slotNumber = slotNumber,
                hasUmbrella = operation == "RETURN",
                locked = true,
                updatedAt = now
            )
            val eventId = dao.insertEvent(
                LoanEventEntity(slotNumber = slotNumber, action = operation, hardwareTag = tag, occurredAt = now)
            )
            dao.enqueue(SyncQueueEntity(eventId = eventId, operation = operation, slotNumber = slotNumber, createdAt = now))
        }
    }
}

data class OperationResult(val success: Boolean, val message: String)
