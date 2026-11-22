package com.smartumbrella.app.hardware

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Basic USB bulk transport for a controller exposing one bulk-IN and one bulk-OUT endpoint.
 * It assumes newline-terminated ASCII commands/responses; adapt framing to the selected vendor.
 * The host must obtain USB permission before calling exchange().
 */
class AndroidUsbBulkTransport(
    context: Context,
    private val device: UsbDevice,
    private val timeoutMs: Int = 2_000,
    private val maxResponseBytes: Int = 512
) : ControllerTransport, AutoCloseable {
    private val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private val connection: UsbDeviceConnection
    private val usbInterface: UsbInterface
    private val input: UsbEndpoint
    private val output: UsbEndpoint
    private val lock = Any()

    init {
        if (!manager.hasPermission(device)) throw SecurityException("USB device permission has not been granted")
        val selected = (0 until device.interfaceCount)
            .map(device::getInterface)
            .firstNotNullOfOrNull { candidate ->
                val endpoints = (0 until candidate.endpointCount).map(candidate::getEndpoint)
                val inEndpoint = endpoints.firstOrNull {
                    it.type == UsbConstants.USB_ENDPOINT_XFER_BULK && it.direction == UsbConstants.USB_DIR_IN
                }
                val outEndpoint = endpoints.firstOrNull {
                    it.type == UsbConstants.USB_ENDPOINT_XFER_BULK && it.direction == UsbConstants.USB_DIR_OUT
                }
                if (inEndpoint != null && outEndpoint != null) Triple(candidate, inEndpoint, outEndpoint) else null
            } ?: throw IOException("USB device has no interface with bulk IN and OUT endpoints")
        usbInterface = selected.first
        input = selected.second
        output = selected.third
        connection = manager.openDevice(device) ?: throw IOException("Unable to open USB device")
        if (!connection.claimInterface(usbInterface, true)) {
            connection.close()
            throw IOException("Unable to claim USB interface")
        }
    }

    override suspend fun exchange(command: String): String = withContext(Dispatchers.IO) {
        require(command.isNotBlank() && '\n' !in command && '\r' !in command) { "Invalid controller command" }
        synchronized(lock) {
            val bytes = (command + "\n").toByteArray(Charsets.US_ASCII)
            val sent = connection.bulkTransfer(output, bytes, bytes.size, timeoutMs)
            if (sent != bytes.size) throw IOException("USB write failed or timed out")

            val response = ByteArray(maxResponseBytes)
            val received = connection.bulkTransfer(input, response, response.size, timeoutMs)
            if (received <= 0) throw IOException("USB response failed or timed out")
            val text = response.copyOf(received).toString(Charsets.US_ASCII).trimEnd('\r', '\n')
            if ('\n' in text || '\r' in text) throw IOException("Unexpected multi-line USB response")
            text
        }
    }

    override fun close() {
        synchronized(lock) {
            connection.releaseInterface(usbInterface)
            connection.close()
        }
    }
}
