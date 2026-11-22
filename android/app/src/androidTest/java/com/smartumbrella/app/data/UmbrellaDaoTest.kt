package com.smartumbrella.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UmbrellaDaoTest {
    private lateinit var database: UmbrellaDatabase
    private lateinit var dao: UmbrellaDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UmbrellaDatabase::class.java).allowMainThreadQueries().build()
        dao = database.umbrellaDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun slotsAndPendingSyncArePersistedAndCanBeAcknowledged() = runBlocking {
        dao.insertSlots(listOf(UmbrellaSlotEntity(slotNumber = 1, hasUmbrella = true)))
        assertEquals(1, dao.slotCount())
        assertTrue(dao.getSlot(1)!!.hasUmbrella)

        val eventId = dao.insertEvent(LoanEventEntity(slotNumber = 1, action = "BORROW"))
        dao.enqueue(SyncQueueEntity(eventId = eventId, operation = "BORROW", slotNumber = 1))
        assertEquals(1, dao.observePendingSync().first().size)

        val queued = dao.getPendingSyncOnce().single()
        dao.markSynced(queued.id)
        assertTrue(dao.observePendingSync().first().isEmpty())
        assertFalse(dao.getPendingSyncOnce().any { it.id == queued.id })
    }
}
