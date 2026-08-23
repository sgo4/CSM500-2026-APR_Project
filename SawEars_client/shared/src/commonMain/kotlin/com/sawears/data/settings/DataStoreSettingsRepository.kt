package com.sawears.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.map

/**
 * DataStore settings repository implementation
 */
class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>
): SettingsRepository {

    private val RANGE_MIN_KEY = intPreferencesKey("range_min")
    private val RANGE_MAX_KEY = intPreferencesKey("range_max")
    private val DIFFICULTY_KEY = intPreferencesKey("difficulty")

    private val rangeMinDefault = 20
    private val rangeMaxDefault = 90
    private val defaultDifficulty = 3

    override val settings: Flow<AppSettings> =
        dataStore.data.map { preferences ->
            AppSettings(
                rangeMinMidi = preferences[RANGE_MIN_KEY]
                    ?: rangeMinDefault,
                rangeMaxMidi = preferences[RANGE_MAX_KEY]
                    ?: rangeMaxDefault,
                difficulty = preferences[DIFFICULTY_KEY]
                    ?: defaultDifficulty
            )

        }

    /**
     * Set minimum range, used in calculating transpositions
     *
     * @param pitchMidi
     */
    override suspend fun setRangeMinMidi(pitchMidi: Int){
        dataStore.edit { preferences ->
            preferences[RANGE_MIN_KEY] = pitchMidi
        }
    }

    /**
     * Set max range, used in calculating transpositions
     *
     * @param pitchMidi
     */
    override suspend fun setRangeMaxMidi(pitchMidi: Int){
        dataStore.edit { preferences ->
            preferences[RANGE_MAX_KEY] = pitchMidi
        }
    }

    /**
     * Set difficulty for Lesson settings
     *
     * @param difficulty
     */
    override suspend fun setDifficulty(difficulty: Int){

        dataStore.edit { preferences ->
            preferences[DIFFICULTY_KEY] = difficulty
        }

    }

}