package com.smartumbrella.app

import android.app.Application
import com.smartumbrella.app.data.UmbrellaDatabase
import com.smartumbrella.app.data.UmbrellaRepository
import com.smartumbrella.app.hardware.SimulatedUmbrellaHardware

class SmartUmbrellaApplication : Application() {
    val repository: UmbrellaRepository by lazy {
        UmbrellaRepository(UmbrellaDatabase.getInstance(this), SimulatedUmbrellaHardware())
    }
}
