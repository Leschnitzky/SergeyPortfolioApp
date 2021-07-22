package com.leschnitzky.dailyshiba

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

@HiltAndroidApp
class MyApplication : Application(){
    val applicationScope = CoroutineScope(
        SupervisorJob() +
            Dispatchers.Main +
            CoroutineName("application"))

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
    }
    }
}