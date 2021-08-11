package com.leschnitzky.dailyshiba.utils

import kotlinx.coroutines.CoroutineScope

class CoroutineScopeProviderImpl(
    val scope: CoroutineScope?
) : CoroutineScopeProvider {
    override val coroutineScope = scope

}