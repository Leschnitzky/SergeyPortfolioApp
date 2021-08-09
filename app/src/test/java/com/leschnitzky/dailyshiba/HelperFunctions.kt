package com.leschnitzky.dailyshiba

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

fun <T> T.toDeferred() = GlobalScope.async { this@toDeferred }