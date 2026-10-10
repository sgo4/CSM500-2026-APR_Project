package com.sawears.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.sawears.AppContainer
import com.sawears.ui.screens.lessonsettings.LessonSettings
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

/**
 * Collection of routes for navigation
 */
@Serializable
sealed interface Route: NavKey {

    @Serializable
    data object MainScreen: Route, NavKey

    /*
    User guide screen takes a locale because it takes its strings
    from a markdown file and therefore must specify locale explicitly.
    Other screens use string resources, which are automatically localized
    to the device locale (if available)
     */
    @Serializable
    data class UserGuideScreen(val locale: String): Route, NavKey
    @Serializable
    data object SettingsScreen: Route, NavKey

    @Serializable
    data class LessonSettingsScreen(val lessonId: Int): Route, NavKey
    @Serializable
    data object SongSelectionScreen: Route, NavKey

    @Serializable
    data class LessonScreen(val lessonId: Int): Route, NavKey

    @Serializable
    data class ErrorScreen(val message: String): Route, NavKey
}