package com.smartumbrella.app.sync

import com.smartumbrella.app.data.LoanEventEntity
import com.smartumbrella.app.data.SyncQueueEntity
import org.json.JSONObject

/** Uploads one queued event as JSON to the configured backend endpoint. */
class SyncApi(
    private val endpoint: String,
    private val bearerToken: String = ""
) {
    fun payload(item: SyncQueueEntity, event: LoanEventEntity): String = JSONObject()
        .put("eventId", event.id)
        .put("operation", event.action)
        .put("slotNumber", event.slotNumber)
        .put("hardwareTag", event.hardwareTag)
        .put("occurredAt", event.occurredAt)
        .toString()

    suspend fun upload(item: SyncQueueEntity, event: LoanEventEntity) {
        require(endpoint.startsWith("https://", ignoreCase = true)) {
            "SYNC_API_URL must be an HTTPS URL"
        }
        val connection = java.net.URL(endpoint).openConnection() as java.net.HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Idempotency-Key", "umbrella-event-${event.id}")
            if (bearerToken.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer $bearerToken")
            }
            val body = payload(item, event).toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(body.size)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                connection.outputStream.use { it.write(body) }
                val status = connection.responseCode
                if (status !in 200..299) {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                    throw java.io.IOException("Sync API returned HTTP $status: ${errorBody.take(300)}")
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
