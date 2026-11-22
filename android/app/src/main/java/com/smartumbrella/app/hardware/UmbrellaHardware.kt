package com.smartumbrella.app.hardware

/** Boundary implemented by the board/vendor SDK integration in the device-specific source set. */
interface UmbrellaHardware {
    suspend fun verifyFingerprint(): Boolean
    suspend fun unlock(slotNumber: Int): Boolean
    suspend fun readRfid(slotNumber: Int): String?
}

/** Minimal transport boundary around the device SDK, serial bridge, BLE client, or controller service. */
interface UmbrellaController {
    suspend fun verifyFingerprint(): Boolean
    suspend fun unlockSlot(slotNumber: Int): Boolean
    suspend fun readRfidTag(slotNumber: Int): String?
}

/** Production adapter that maps app operations to the concrete controller implementation. */
class ControllerUmbrellaHardware(private val controller: UmbrellaController) : UmbrellaHardware {
    override suspend fun verifyFingerprint(): Boolean = controller.verifyFingerprint()

    override suspend fun unlock(slotNumber: Int): Boolean {
        require(slotNumber in 1..6) { "Invalid umbrella slot: $slotNumber" }
        return controller.unlockSlot(slotNumber)
    }

    override suspend fun readRfid(slotNumber: Int): String? {
        require(slotNumber in 1..6) { "Invalid umbrella slot: $slotNumber" }
        return controller.readRfidTag(slotNumber)
    }
}

/** Deterministic local simulator; it does not access physical hardware or biometric data. */
class SimulatedUmbrellaHardware : UmbrellaHardware {
    override suspend fun verifyFingerprint(): Boolean = true
    override suspend fun unlock(slotNumber: Int): Boolean = slotNumber in 1..6
    override suspend fun readRfid(slotNumber: Int): String? = "SIM-RFID-$slotNumber"
}
