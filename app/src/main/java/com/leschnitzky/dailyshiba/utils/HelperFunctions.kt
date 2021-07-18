package com.leschnitzky.dailyshiba.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.*
import java.nio.file.Files


private const val TAG = "HelperFunctions"
const val FOLDER_NAME = "ShibaDaily"
const val GOOGLE_SIGN_IN = 4
const val FACEBOOK_SIGN_IN = 5
const val DEFAULT_PROFILE_PIC =  "http://cdn.shibe.online/shibes/1dceabce914325b357fbd59e4ef829bc5ddfad6c.jpg"
fun isValidEmail(target: CharSequence?): Boolean {
    return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
}


fun getInternalFileOutstream(mcoContext: Context, sFileName: String?) :OutputStream?{
    val dir = File(mcoContext.externalCacheDir, FOLDER_NAME)
    if (!dir.exists()) {
        Timber.d( "writeFileOnInternalStorage: doesn'tExist")
        dir.mkdir()
    }

    try {
        val outputFile = File("${mcoContext.externalCacheDir}/$FOLDER_NAME/$sFileName")
        if(outputFile.exists()){
            Timber.d( "getInternalFileOutstream: ${outputFile.name} deleted!")
            Files.delete(outputFile.toPath())
        }
        Timber.d( "getInternalFileOutstream: Getting output")
        return FileOutputStream(outputFile, false)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null

}


@ExperimentalCoroutinesApi
fun CollectionReference.getQuerySnapshotFlow(): Flow<QuerySnapshot?> {
    return callbackFlow {
        val listenerRegistration =
            addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    cancel(
                        message = "error fetching collection data at path - $path",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                offer(querySnapshot)
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }
}

@ExperimentalCoroutinesApi
fun <T> CollectionReference.getDataFlow(mapper: (QuerySnapshot?) -> T): Flow<T> {
    return getQuerySnapshotFlow()
        .map {
            return@map mapper(it)
        }
}



@ExperimentalCoroutinesApi
fun DocumentReference.getQuerySnapshotFlow(): Flow<DocumentSnapshot?> {
    return callbackFlow {
        val listenerRegistration =
            addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    cancel(
                        message = "error fetching collection data at path - $path",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                offer(querySnapshot)
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }
}

@ExperimentalCoroutinesApi
fun <T> DocumentReference.getDataFlow(mapper: (DocumentSnapshot?) -> T): Flow<T> {
    return getQuerySnapshotFlow()
        .map {
            return@map mapper(it)
        }
}

interface UiState

interface UiEvent

interface UiEffect



fun <K, V> getKey(map: Map<K, V>, target: V): K {
    return map.keys.first { target == map[it] };
}
