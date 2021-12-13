package com.torrydo.hoyomi.localData.preferenceDataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class myPreferenceDataStore(context: Context) {

    private companion object {
        val FIRST_LOAD = booleanPreferencesKey("mybooleankey")
        val IMAGE_POSITION = intPreferencesKey("curima")
        val MY_DAY = intPreferencesKey("myday")

        val MY_uniqueNAME = "myUniqueName"
    }

    private val prefInstance = context.createDataStore(name = MY_uniqueNAME)

    suspend fun editSync(bool: Boolean) {
        prefInstance.edit {
            it[FIRST_LOAD] = bool
        }
    }
    fun hasSync(): Flow<Boolean> = prefInstance.data.map {
        it[FIRST_LOAD] ?: false
    }


    suspend fun setDay(i: Int) {
        prefInstance.edit {
            it[MY_DAY] = i
        }
    }
    fun getDay(): Flow<Int> = prefInstance.data
        .map {
            it[MY_DAY] ?: 1
        }


    suspend fun setCurrentImagePosition(ii: Int) {
        prefInstance.edit {
            it[IMAGE_POSITION] = ii
        }
    }

    val getCurrentImagePosition: Flow<Int> = prefInstance.data
        .map {
        it[IMAGE_POSITION] ?: 0
    }

}