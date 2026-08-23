package com.sawears.ui.screens.lessonsettings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * View model for lesson settings screen
 * This viewmodel only controls local lesson settings. Settings which are considered
 * global (i.e. not specific to a single lesson) are included on the lesson settings screen
 * but controlled by the main settings view model
 */
class LessonSettingsViewModel(
    lessonSettings: LessonSettings
): ViewModel() {


    private val _settings = MutableStateFlow(lessonSettings)
    var settings = _settings.asStateFlow()
        private set


    fun updateTempo(tempoBpm: Int){
        _settings.update { it ->
            it.copy (
                tempo = tempoBpm
            )
        }
    }

    fun updateMode(lessonMode: LessonMode){ // TODO this should really be global
        _settings.update { it ->
            it.copy (
                mode = lessonMode
            )
        }
    }
}

data class LessonSettings(
    val tempo: Int,
    val difficulty: Int,
    val mode: LessonMode
)

enum class LessonMode {
    PRACTICE,
    PERFORMANCE
}