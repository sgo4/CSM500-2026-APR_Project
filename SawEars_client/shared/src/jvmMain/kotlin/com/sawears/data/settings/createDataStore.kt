package com.sawears.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import java.io.File

/**
 * Platform specific datastore instance creation
 *
 * Boilerplate adapted from:
 * https://developer.android.com/kotlin/multiplatform/datastore
 */
fun createDataStore(): DataStore<Preferences> = createDataStore(
    storage = FileStorage(
        serializer = PreferencesFileSerializer,
        produceFile = {
            File(System.getProperty("user.home"), ".sawears").apply {
                mkdirs()
            }.resolve(dataStoreFileName)
        }
    )
)