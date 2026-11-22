package com.smartumbrella.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartumbrella.app.SmartUmbrellaApplication

/**
 * Demo worker: acknowledges events locally to exercise the durable WorkManager pipeline.
 * Replace the simulated uploader with an authenticated API client before production use.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        val repository = (applicationContext as SmartUmbrellaApplication).repository
        repository.syncPending {
            // No backend endpoint is configured in this prototype.
            true
        }
        Result.success()
    } catch (error: Exception) {
        if (runAttemptCount < 5) Result.retry() else Result.failure()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "umbrella-offline-sync"
    }
}
