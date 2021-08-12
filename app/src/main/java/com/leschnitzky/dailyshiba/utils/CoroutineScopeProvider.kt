package com.leschnitzky.dailyshiba.utils

import kotlinx.coroutines.CoroutineScope

interface CoroutineScopeProvider {
    val coroutineScope : CoroutineScope?
}