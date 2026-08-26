package com.sawears.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.sawears.AppContainer
import com.sawears.ui.screens.lessonsettings.LessonSettings
import kotlinx.serialization.Serializable

/**
 * Collection of routes for navigation
 */
@Serializable
sealed interface Route: NavKey {

    @Serializable
    data object MainScreen: Route, NavKey

    @Serializable
    data object AccountScreen: Route, NavKey

    @Serializable
    data object SettingsScreen: Route, NavKey

    @Serializable
    data class LessonSettingsScreen(val lessonId: Int): Route, NavKey
    @Serializable
    data object SongSelectionScreen: Route, NavKey

    @Serializable
    data class LessonScreen(val lessonId: Int): Route, NavKey

}