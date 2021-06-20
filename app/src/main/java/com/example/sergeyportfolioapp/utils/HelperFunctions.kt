package com.example.sergeyportfolioapp.utils

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import java.io.*
import java.nio.file.Files


private const val TAG = "HelperFunctions"
const val FOLDER_NAME = "ShibaDaily"
fun isValidEmail(target: CharSequence?): Boolean {
    return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
}


fun getInternalFileOutstream(mcoContext: Context, sFileName: String?) :OutputStream?{
    val dir = File(mcoContext.externalCacheDir, FOLDER_NAME)
    if (!dir.exists()) {
        Log.d(TAG, "writeFileOnInternalStorage: doesn'tExist")
        dir.mkdir()
    }

    try {
        val outputFile = File("${mcoContext.externalCacheDir}/$FOLDER_NAME/$sFileName")
        if(outputFile.exists()){
            Log.d(TAG, "getInternalFileOutstream: ${outputFile.name} deleted!")
            Files.delete(outputFile.toPath())
        }
        Log.d(TAG, "getInternalFileOutstream: Getting output")
        return FileOutputStream(outputFile, false)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null

}