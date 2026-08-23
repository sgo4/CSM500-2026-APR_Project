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

    private val rangeMinDefault = 20
    private val rangeMaxDefault = 90

    override val settings: Flow<AppSettings> =
        dataStore.data.map { preferences ->
            AppSettings(
                rangeMinMidi = preferences[RANGE_MIN_KEY]
                    ?: rangeMinDefault,
                rangeMaxMidi = preferences[RANGE_MAX_KEY]
                    ?: rangeMaxDefault
            )

        }

    /**
     * Set minimum range, used in calculating transpositions
     */
    override suspend fun setRangeMinMidi(pitchMidi: Int){
        dataStore.edit { preferences ->
            preferences[RANGE_MIN_KEY] = pitchMidi
        }
    }

    /**
     * Set max range, used in calculating transpositions
     */
    override suspend fun setRangeMaxMidi(pitchMidi: Int){
        dataStore.edit { preferences ->
            preferences[RANGE_MAX_KEY] = pitchMidi
        }
    }

    /**
     * Set full range, used in calculating transpositions
     */
    override suspend fun setRangeMidi(minMidi: Int, maxMidi: Int){
        require(minMidi < maxMidi){
            "Range minimum must be lower than range maximum"
        }
    }
}