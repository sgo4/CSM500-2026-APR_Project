package com.sawears.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences

/**
 * Holds global application settings during run time
 *
 * @property rangeMinMidi the lowest note the instrument can play
 * @property rangeMaxMidi the highest note the instrument can play
 */
data class AppSettings(
    // set very wide range as default, will be overwritten by more sensible values later in the flow
    val rangeMinMidi: Int = 0,
    val rangeMaxMidi: Int = 1000
)

/**
 *   Gets the singleton DataStore instance, creating it if necessary.
 */
fun createDataStore(storage: Storage<Preferences>): DataStore<Preferences> =
    DataStoreFactory.create(storage = storage)

internal const val dataStoreFileName = "settings.preferences_pb"