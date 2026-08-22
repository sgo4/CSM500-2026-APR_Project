package com.sawears.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.sawears.AppContainer
import kotlinx.serialization.Serializable

/**
 * Collection of routes for navigation
 */
@Serializable
sealed interface Route: NavKey {

    @Serializable
    data object MainScreen: Route, NavKey

    @Serializable
    data class LessonScreen(val container: AppContainer): Route, NavKey

}