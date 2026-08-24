package com.sawears.data.settings

import com.sawears.ui.screens.lessonsettings.LessonSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update


/**
 * Interface for local lesson settings repository. Intended for lesson specific,
 * sesson scoped settings such as tempo.
 */
interface LocalSettingsRepository {
    val settings: StateFlow<Map<Int, LessonSettings>>
    fun get(lessonId: Int): LessonSettings?
    fun settingsFor(lessonId: Int): Flow<LessonSettings>

    fun set(lessonId: Int, lessonSettings: LessonSettings)

}

/**
 * Repository for ephermeral lesson scoped settings
 */
class EphemeralLocalSettingsRepository: LocalSettingsRepository {

    private val _settings =
        MutableStateFlow<Map<Int, LessonSettings>>(emptyMap())

    override val settings: StateFlow<Map<Int, LessonSettings>> =
        _settings.asStateFlow()

    override fun get(lessonId: Int): LessonSettings?
        = settings.value.get(lessonId)

    override fun settingsFor(lessonId: Int): Flow<LessonSettings> =
        settings.map { it ->
            it[lessonId]
                ?: error("Unable to find settings for lesson $lessonId")
        }


    override fun set(lessonId: Int, lessonSettings: LessonSettings) {
        _settings.update { it ->
            it + (lessonId to lessonSettings)
        }
    }

}