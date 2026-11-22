package com.smartumbrella.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartumbrella.app.BuildConfig
import com.smartumbrella.app.SmartUmbrellaApplication
import java.io.IOException

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (BuildConfig.SYNC_API_URL.isBlank()) return Result.failure()
        return try {
            val repository = (applicationContext as SmartUmbrellaApplication).repository
            val api = SyncApi(BuildConfig.SYNC_API_URL, BuildConfig.SYNC_API_TOKEN)
            repository.syncPending { item ->
                val event = repository.eventForSync(item)
                    ?: throw IOException("Event ${item.eventId} is missing for queue row ${item.id}")
                api.upload(item, event)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "umbrella-offline-sync"
    }
}
