package com.leschnitzky.dailyshiba.utils

import kotlinx.coroutines.Dispatchers

class CoroutineContextProviderImpl : CoroutineContextProvider {
    override val ui = Dispatchers.Main
    override val io = Dispatchers.IO
}