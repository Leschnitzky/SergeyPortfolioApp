package com.leschnitzky.dailyshiba

import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import kotlin.test.assertEquals


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@HiltAndroidTest
class ExampleUnitTest {
    @Test
    fun  addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}