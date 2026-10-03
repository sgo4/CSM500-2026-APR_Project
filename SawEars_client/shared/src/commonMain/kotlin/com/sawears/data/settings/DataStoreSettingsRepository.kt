package com.sawears.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sawears.domain.settings.Difficulty
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
    private val DIFFICULTY_KEY = stringPreferencesKey("difficulty")
    private val MODE_KEY = stringPreferencesKey("mode")
    private val MARGIN_KEY = intPreferencesKey("margin")

    private val rangeMinDefault = 40
    private val rangeMaxDefault = 90
    private val defaultDifficulty = enumValues<Difficulty>().let{
        if (it.size < 3){
            it.first()
        } else {
            it[(it.size - 1) / 2]
        }
    }
    private val defaultMode = LessonMode.PRACTICE
    private val defaultPitchMargin = 5

    override val settings: Flow<AppSettings> =
        dataStore.data.map { preferences ->
            AppSettings(
                rangeMinMidi = preferences[RANGE_MIN_KEY]
                    ?: rangeMinDefault,
                rangeMaxMidi = preferences[RANGE_MAX_KEY]
                    ?: rangeMaxDefault,
                difficulty = Difficulty.valueOf(
                    preferences[DIFFICULTY_KEY]?: defaultDifficulty.name
                ),
                mode = enumValueOf(preferences[MODE_KEY]
                    ?: defaultMode.name),
                pitchDisplayMarginMidi = preferences[MARGIN_KEY]
                    ?: defaultPitchMargin
            )

        }


    override suspend fun setRange(rangeMinMidi: Int, rangeMaxMidi: Int){

        require(rangeMinMidi < rangeMaxMidi)

        dataStore.edit { preferences ->
            preferences[RANGE_MIN_KEY] = rangeMinMidi
            preferences[RANGE_MAX_KEY] = rangeMaxMidi

        }

    }

    override suspend fun setDifficulty(difficulty: Difficulty){

        dataStore.edit { preferences ->
            preferences[DIFFICULTY_KEY] = difficulty.name
        }

    }

    override suspend fun setMode(mode: LessonMode){
        dataStore.edit { preferences ->
            preferences[MODE_KEY] = mode.name
        }
    }

    override suspend fun setPitchDisplayMargin(marginMidi: Int){

        require(marginMidi >= 0) // margin must not be negative
        dataStore.edit { preferences ->
            preferences[MARGIN_KEY] = marginMidi
        }
    }

}