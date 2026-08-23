package com.sawears.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.sawears.AppContainer
import com.sawears.ui.lesson.LessonMainScreen
import com.sawears.ui.lesson.LessonViewModel
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import com.sawears.ui.screens.home.MainScreen
import com.sawears.ui.screens.selectsong.SongSelectionScreen
import com.sawears.ui.screens.selectsong.SongSelectionViewModel

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
                                    Route.SongSelectionScreen
                                )
                            }
                        )
                    }
                }
                is Route.SongSelectionScreen -> {
                    NavEntry(key){

                        val viewModel = viewModel {SongSelectionViewModel(
                            container.db.songDao()
                        )}
                        val state by viewModel.state.collectAsStateWithLifecycle()
                        SongSelectionScreen(
                            state,
                            onSelectSong = {
                                backStack.add(
                                    Route.LessonScreen(it)
                                )
                            }
                        )
                    }
                }
                is Route.LessonScreen -> {

                    val route = key

                    NavEntry(key){
                        val viewModel = viewModel {
                            LessonViewModel(
                                container
                            )
                        }
                        LessonMainScreen(
                            viewModel,
                            route.resourcePath
                        )
                    }

                }
                else -> error("Unknown nav key")
            }
        }
    )
}