package com.smartumbrella.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.smartumbrella.app.data.UmbrellaDatabase
import com.smartumbrella.app.data.UmbrellaRepository
import com.smartumbrella.app.hardware.SimulatedUmbrellaHardware
import com.smartumbrella.app.sync.SyncWorker
import java.util.concurrent.TimeUnit

class SmartUmbrellaApplication : Application() {
    val repository: UmbrellaRepository by lazy {
        UmbrellaRepository(UmbrellaDatabase.getInstance(this), SimulatedUmbrellaHardware())
    }

    override fun onCreate() {
        super.onCreate()
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
