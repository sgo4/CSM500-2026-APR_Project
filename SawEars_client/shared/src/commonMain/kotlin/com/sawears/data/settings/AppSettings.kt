package com.sawears.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences

/**
 * Holds global application settings during run time
 */
data class AppSettings(
    val rangeMinMidi: Int,
    val rangeMaxMidi: Int
)

/**
 *   Gets the singleton DataStore instance, creating it if necessary.
 */
fun createDataStore(storage: Storage<Preferences>): DataStore<Preferences> =
    DataStoreFactory.create(storage = storage)

internal const val dataStoreFileName = "settings.preferences_pb"