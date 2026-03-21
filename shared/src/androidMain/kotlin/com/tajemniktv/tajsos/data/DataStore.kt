package com.tajemniktv.tajsos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

fun createDataStore(context: Context): DataStore<Preferences> {
    return context.dataStore
}
