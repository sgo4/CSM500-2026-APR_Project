package com.sawears.domain.useCases

import com.sawears.data.settings.AppSettings
import com.sawears.data.settings.LessonSettingsRepository
import com.sawears.domain.Song
import com.sawears.ui.screens.lessonsettings.LessonSettings


/**
 * Use case describing logic for creating a lesson and registering its settings
 */
class RegisterLesson(
    val globalSettings: AppSettings,
    val lessonSettingsRepository: LessonSettingsRepository
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
            pitchDisplayMarginMidi = globalSettings.pitchDisplayMarginMidi
        )
        // register lesson with the localSettingsRepository
        lessonSettingsRepository.set(lessonId,lessonSettings)

    }

}