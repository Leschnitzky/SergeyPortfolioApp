package com.example.sergeyportfolioapp.usermanagement.room.model

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.lang.StringBuilder

@ProvidedTypeConverter
class UserTypeConverter {
    val SEPERATOR : String = "#"

        @TypeConverter
        fun ListToString(list: ArrayList<String>): String{
            val sb = StringBuilder()
            list.forEach {
                sb.append(it)
                sb.append(SEPERATOR)
            }
            return sb.toString()
        }

        @TypeConverter
        fun StringToList(string: String): ArrayList<String> {
            val list = arrayListOf<String>()
            list.addAll(string.split(SEPERATOR))
            return list
        }
}