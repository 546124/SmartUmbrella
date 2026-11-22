package com.smartumbrella.app.hardware

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LineProtocolUmbrellaControllerTest {
    @Test
    fun mapsCommandsAndReplies() = runBlocking {
        val commands = mutableListOf<String>()
        val controller = LineProtocolUmbrellaController(ControllerTransport { command ->
            commands += command
            when (command) {
                "VERIFY_FINGERPRINT", "UNLOCK 3" -> "OK 1"
                "READ_RFID 3" -> "OK TAG-ABC"
                else -> "ERR"
            }
        })
        assertTrue(controller.verifyFingerprint())
        assertTrue(controller.unlockSlot(3))
        assertEquals("TAG-ABC", controller.readRfidTag(3))
        assertEquals(listOf("VERIFY_FINGERPRINT", "UNLOCK 3", "READ_RFID 3"), commands)
    }

    @Test
    fun treatsErrorsAndEmptyTagsAsFailure() = runBlocking {
        val controller = LineProtocolUmbrellaController(ControllerTransport { "ERR" })
        assertFalse(controller.verifyFingerprint())
        assertFalse(controller.unlockSlot(1))
        assertNull(controller.readRfidTag(1))
    }

    @Test
    fun rejectsInvalidSlots() = runBlocking {
        val controller = LineProtocolUmbrellaController(ControllerTransport { "OK 1" })
        assertFalse(runCatching { controller.unlockSlot(0) }.isSuccess)
        assertFalse(runCatching { controller.readRfidTag(7) }.isSuccess)
    }
}
