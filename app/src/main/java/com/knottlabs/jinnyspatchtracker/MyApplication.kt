package com.knottlabs.jinnyspatchtracker

import android.app.Application
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyApplication : Application() {
    private val appScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            initializeAdMob()
        }
    }

    private suspend fun initializeAdMob() = withContext(Dispatchers.IO) {
        MobileAds.initialize(this@MyApplication) {
            // Keep this callback minimal!

        }
    }
}