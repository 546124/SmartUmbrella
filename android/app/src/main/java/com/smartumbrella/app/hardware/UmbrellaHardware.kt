package com.smartumbrella.app.hardware

/** App-facing operations. Hardware failures must return false/null or throw; never assume success. */
interface UmbrellaHardware {
    suspend fun verifyFingerprint(): Boolean
    suspend fun unlock(slotNumber: Int): Boolean
    suspend fun readRfid(slotNumber: Int): String?
}

/** Vendor SDK or controller protocol boundary. Fingerprint templates stay on the biometric device. */
interface UmbrellaController {
    suspend fun verifyFingerprint(): Boolean
    suspend fun unlockSlot(slotNumber: Int): Boolean
    suspend fun readRfidTag(slotNumber: Int): String?
}

/** Validates slots and delegates to a controller implementation; it never drives a lock directly. */
class ControllerUmbrellaHardware(private val controller: UmbrellaController) : UmbrellaHardware {
    override suspend fun verifyFingerprint(): Boolean = controller.verifyFingerprint()

    override suspend fun unlock(slotNumber: Int): Boolean {
        require(slotNumber in 1..6) { "Invalid umbrella slot: $slotNumber" }
        return controller.unlockSlot(slotNumber)
    }

    override suspend fun readRfid(slotNumber: Int): String? {
        require(slotNumber in 1..6) { "Invalid umbrella slot: $slotNumber" }
        return controller.readRfidTag(slotNumber)?.takeIf { it.isNotBlank() }
    }
}

/** Minimal line-oriented transport interface, easy to replace with a vendor SDK or serial library. */
fun interface ControllerTransport {
    suspend fun exchange(command: String): String
}

/** Generic controller protocol scaffold; this is not a vendor protocol until adapted and verified. */
class LineProtocolUmbrellaController(private val transport: ControllerTransport) : UmbrellaController {
    override suspend fun verifyFingerprint(): Boolean = transport.exchange("VERIFY_FINGERPRINT").trim() == "OK 1"

    override suspend fun unlockSlot(slotNumber: Int): Boolean {
        require(slotNumber in 1..6) { "Invalid umbrella slot: $slotNumber" }
        return transport.exchange("UNLOCK $slotNumber").trim() == "OK 1"
    }

    override suspend fun readRfidTag(slotNumber: Int): String? {
        require(slotNumber in 1..6) { "Invalid umbrella slot: $slotNumber" }
        val response = transport.exchange("READ_RFID $slotNumber").trim()
        if (!response.startsWith("OK ")) return null
        return response.removePrefix("OK ").trim().takeIf { it.isNotEmpty() && it.length <= 128 }
    }
}

/** Deterministic local simulator; it does not access physical hardware or biometric data. */
class SimulatedUmbrellaHardware : UmbrellaHardware {
    override suspend fun verifyFingerprint(): Boolean = true
    override suspend fun unlock(slotNumber: Int): Boolean = slotNumber in 1..6
    override suspend fun readRfid(slotNumber: Int): String? = "SIM-RFID-$slotNumber"
}
