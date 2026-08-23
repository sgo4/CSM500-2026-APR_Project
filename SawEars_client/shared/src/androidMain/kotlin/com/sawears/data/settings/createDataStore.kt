package com.sawears.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer


/**
 * Platform specific datastore instance creation
 *
 * Boilerplate adapted from:
 * https://developer.android.com/kotlin/multiplatform/datastore
 */
fun createDataStore(context: Context): DataStore<Preferences> = createDataStore(
    storage = FileStorage(
        serializer = PreferencesFileSerializer,
        produceFile = { context.filesDir.resolve(dataStoreFileName) }
    )
)


