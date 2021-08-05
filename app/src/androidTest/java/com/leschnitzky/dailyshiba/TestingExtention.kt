package com.leschnitzky.dailyshiba

import com.leschnitzky.dailyshiba.usermanagement.ui.UserViewModel
import java.lang.reflect.Modifier


fun Any.mockPrivateFields(vararg mocks: Any): Any {
    mocks.forEach { mock ->
        javaClass.declaredFields
            .filter { it.modifiers.and(Modifier.PRIVATE) > 0 || it.modifiers.and(Modifier.PROTECTED) > 0 }
            .firstOrNull { it.type == mock.javaClass}
            ?.also { it.isAccessible = true }
            ?.set(this, mock)
    }
    return this
}