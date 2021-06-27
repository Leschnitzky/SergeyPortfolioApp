package com.example.sergeyportfolioapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class MyApplication : Application(){
    val applicationScope = CoroutineScope(SupervisorJob())
}