package com.leschnitzky.dailyshiba.usermanagement.repository.firestore.model

import timber.log.Timber
import java.lang.StringBuilder

class FireStoreConverter {
    companion object {
        private const val PAIR_SEPARATOR: String = "&"
        private const val VALUE_SEPARATOR: String = "$"
        fun stringToSettings(settingsStr: String): UserForFirestore.UserSettingsForFirestore {
            val mutableMap = mutableMapOf<String,String>()
            Timber.d("${settingsStr.split(PAIR_SEPARATOR)}")
            settingsStr
                .split(PAIR_SEPARATOR)
                .filter { it.isNotEmpty()  }
                .forEach {
                    val keyAndValue = it.split(VALUE_SEPARATOR)
                    Timber.d("$keyAndValue")
                    mutableMap[keyAndValue[0]] = keyAndValue[1]
                }
            return UserForFirestore.UserSettingsForFirestore( mutableMap as Map<String, String>)
        }

        fun settingsToString(settingsForFirestore: UserForFirestore.UserSettingsForFirestore): String {
            val sb = StringBuilder()
            settingsForFirestore.settings.forEach { (key, value) ->
                sb.append(key)
                sb.append(VALUE_SEPARATOR)
                sb.append(value)
                sb.append(PAIR_SEPARATOR)
            }

            return sb.toString()
        }
    }
}