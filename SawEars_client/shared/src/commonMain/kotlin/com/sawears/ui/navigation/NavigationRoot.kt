package com.sawears.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.sawears.AppContainer
import com.sawears.ui.lesson.LessonMainScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import com.sawears.ui.screens.home.MainScreen

/**
 * Root for app navigation
 *
 * This approach is mostly boilerplate navigation3 code, but was partially adapted
 * from this tutorial by Philip Lackner:
 * https://www.youtube.com/watch?v=G_e9XL5tI6U
 *
 */
@Composable
fun NavigationRoot(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Route.MainScreen::class, Route.MainScreen.serializer())
                    subclass(Route.LessonScreen::class, Route.LessonScreen.serializer())
                }
            }
        },
        Route.MainScreen
    )
    NavDisplay(
        backStack = backStack,
        entryProvider = { key ->
            when(key) {
                is Route.MainScreen -> {
                    NavEntry(key){
                        MainScreen(
                            onOpenLesson = {
                                backStack.add(
                                    Route.LessonScreen(container)
                                )
                            }
                        )
                    }
                }
                is Route.LessonScreen -> {
                    NavEntry(key){
                        LessonMainScreen(
                            container
                        )
                    }
                }
                else -> error("Unknown nav key")
            }
        }
    )
}