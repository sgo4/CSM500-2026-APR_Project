package com.sawears.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Storage
import androidx.datastore.preferences.core.Preferences
import com.sawears.model.pitch.settings.Difficulty
import com.sawears.ui.screens.lessonsettings.LessonMode

/**
 * Holds global application settings during run time
 *
 * @property rangeMinMidi the lowest note the instrument can play
 * @property rangeMaxMidi the highest note the instrument can play
 * @property difficulty how challenging the lesson mode is (1=easiest, 5=most difficult)
 * @property mode default lesson mode (PERFORMANCE or PRACTICE)
 * @property pitchDisplayMarginMidi how much space to leave outside the song's range
 *      (e.g. if set to 5 highest note displayed will be 5 midi notes higher than the song's
 *      highest note)
 */
data class AppSettings(
    // set very wide range as default, will be overwritten by more sensible values later in the flow
    val rangeMinMidi: Int = 0,
    val rangeMaxMidi: Int = 1000,
    val difficulty: Difficulty = Difficulty.STANDARD,
    val mode: LessonMode = LessonMode.PRACTICE,
    val pitchDisplayMarginMidi: Int = 5
)

/**
 *   Gets the singleton DataStore instance, creating it if necessary.
 */
fun createDataStore(storage: Storage<Preferences>): DataStore<Preferences> =
    DataStoreFactory.create(storage = storage)

internal const val dataStoreFileName = "settings.preferences_pb"