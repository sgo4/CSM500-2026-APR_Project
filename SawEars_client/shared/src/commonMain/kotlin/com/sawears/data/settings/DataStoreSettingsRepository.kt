package com.sawears.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sawears.ui.screens.lessonsettings.LessonMode
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
    private val MODE_KEY = stringPreferencesKey("mode")

    private val rangeMinDefault = 20
    private val rangeMaxDefault = 90
    private val defaultDifficulty = 3
    private val defaultMode = LessonMode.PRACTICE

    override val settings: Flow<AppSettings> =
        dataStore.data.map { preferences ->
            AppSettings(
                rangeMinMidi = preferences[RANGE_MIN_KEY]
                    ?: rangeMinDefault,
                rangeMaxMidi = preferences[RANGE_MAX_KEY]
                    ?: rangeMaxDefault,
                difficulty = preferences[DIFFICULTY_KEY]
                    ?: defaultDifficulty,
                mode = enumValueOf(preferences[MODE_KEY] ?: defaultMode.name)
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

    /**
     * Set lesson mode
     *
     * Implementation note:
     * Enum needs to be converted to string for storage. The reverse conversion
     * occurs when dataStore values are read into settings.
     *
     * @param mode enum value representing defaul lesson mode
     */
    override suspend fun setMode(mode: LessonMode){
        dataStore.edit { preferences ->
            preferences[MODE_KEY] = mode.name
        }
    }

}