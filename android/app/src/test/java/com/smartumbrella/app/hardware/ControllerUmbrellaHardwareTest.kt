package com.smartumbrella.app.hardware

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ControllerUmbrellaHardwareTest {
    @Test
    fun delegatesOperationsToController() = runBlocking {
        val fake = FakeController()
        val adapter = ControllerUmbrellaHardware(fake)

        assertTrue(adapter.verifyFingerprint())
        assertTrue(adapter.unlock(2))
        assertEquals("TAG-2", adapter.readRfid(2))
        assertEquals(2, fake.unlockedSlot)
    }

    @Test
    fun rejectsInvalidSlotBeforeCallingController() = runBlocking {
        val fake = FakeController()
        val adapter = ControllerUmbrellaHardware(fake)
        assertFalse(runCatching { adapter.unlock(0) }.isSuccess)
        assertEquals(null, fake.unlockedSlot)
    }

    private class FakeController : UmbrellaController {
        var unlockedSlot: Int? = null
        override suspend fun verifyFingerprint() = true
        override suspend fun unlockSlot(slotNumber: Int): Boolean {
            unlockedSlot = slotNumber
            return true
        }
        override suspend fun readRfidTag(slotNumber: Int) = "TAG-$slotNumber"
    }
}
