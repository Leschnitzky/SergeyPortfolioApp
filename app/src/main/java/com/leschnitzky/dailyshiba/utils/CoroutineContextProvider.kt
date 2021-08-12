package com.leschnitzky.dailyshiba.utils

import kotlin.coroutines.CoroutineContext

interface CoroutineContextProvider {
    val ui: CoroutineContext
    val io: CoroutineContext
}