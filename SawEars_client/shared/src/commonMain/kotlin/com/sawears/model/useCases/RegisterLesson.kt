package com.sawears.model.useCases

import com.sawears.data.SongRepository
import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.LocalSettingsRepository
import com.sawears.data.settings.SettingsRepository
import com.sawears.model.Song
import com.sawears.ui.screens.lessonsettings.LessonSettings
import kotlinx.coroutines.flow.first


/**
 * Use case describing logic for creating a lesson and registering its settings
 */
class RegisterLesson(
    val globalSettings: AppSettings,
    val lessonSettingsRepository: LocalSettingsRepository
) {

    private val defaultTempo = 80

    /**
     * Register lesson so that its settings are available on screens that need to refer
     * to specific lessons
     */
    fun register(
        song: Song,
        lessonId: Int
    ) {

        // return if lesson with that Id already registered
        lessonSettingsRepository.get(lessonId)?.let {
            return
        }
        // otherwise, create initial lesson settings combining
        val lessonSettings = LessonSettings(
            song=song,
            tempo=song.tempo?: defaultTempo,
            difficulty=globalSettings.difficulty,
            mode=globalSettings.mode,
            rangeMin=globalSettings.rangeMinMidi,
            rangeMax=globalSettings.rangeMaxMidi,
        )
        // register lesson with the localSettingsRepository
        lessonSettingsRepository.set(lessonId,lessonSettings)

    }

}