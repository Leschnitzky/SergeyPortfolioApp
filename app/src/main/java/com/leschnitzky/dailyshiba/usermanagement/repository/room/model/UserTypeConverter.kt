package com.leschnitzky.dailyshiba.usermanagement.repository.room.model

import android.util.Log
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import timber.log.Timber
import java.lang.StringBuilder

@ProvidedTypeConverter


class UserTypeConverter {
    private val TAG = "UserTypeConverter"
    val SEPERATOR : String = "#####"
    val LINK_MAP_SEPERATOR : String = "^^^^^"

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
            Timber.d( "StringToList: Converting ${string.split(SEPERATOR)}")
            val list = arrayListOf<String>()
            if(string.isNotEmpty()){
                list.addAll(string.split(SEPERATOR))
            }
            return list
        }

        @TypeConverter
        fun MapToString(map: Map<String,String>) : String{
            val sb = StringBuilder()
            map.forEach { (key, value) ->
                sb
                    .append(key)
                    .append(LINK_MAP_SEPERATOR)
                    .append(value)
                    .append(SEPERATOR)
            }
            return sb.toString()
        }

        @TypeConverter
        fun StringToMap(string: String) : Map<String,String>{
            val map = mutableMapOf<String,String>()
            string.split(SEPERATOR).map {
                it.split(LINK_MAP_SEPERATOR).toList()
            }.forEach {
                if(it.size >= 2){
                    map[it[0]] = it[1]
                }
            }
            return map
        }
}