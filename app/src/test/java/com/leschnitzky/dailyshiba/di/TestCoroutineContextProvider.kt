package com.leschnitzky.dailyshiba.di

import com.leschnitzky.dailyshiba.utils.CoroutineContextProvider
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlin.coroutines.CoroutineContext

class TestCoroutineContextProvider(dispatcher: TestCoroutineDispatcher) : CoroutineContextProvider {
    override val ui: CoroutineContext = dispatcher

    override val io: CoroutineContext = dispatcher

}