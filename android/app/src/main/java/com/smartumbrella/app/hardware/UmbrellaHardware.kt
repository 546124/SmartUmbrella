package com.smartumbrella.app.hardware

/** Replace these methods with the controller/vendor SDK adapter when hardware is available. */
interface UmbrellaHardware {
    suspend fun verifyFingerprint(): Boolean
    suspend fun unlock(slotNumber: Int): Boolean
    suspend fun readRfid(slotNumber: Int): String?
}

/** Deterministic local simulator; it does not access physical hardware or biometric data. */
class SimulatedUmbrellaHardware : UmbrellaHardware {
    override suspend fun verifyFingerprint(): Boolean = true
    override suspend fun unlock(slotNumber: Int): Boolean = slotNumber in 1..6
    override suspend fun readRfid(slotNumber: Int): String? = "SIM-RFID-$slotNumber"
}
