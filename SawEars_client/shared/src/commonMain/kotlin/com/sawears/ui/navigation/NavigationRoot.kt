package com.sawears.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.sawears.AppContainer
import com.sawears.ui.screens.lesson.LessonMainScreen
import com.sawears.ui.screens.lesson.LessonViewModel
import com.sawears.ui.screens.account.AccountScreen
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import com.sawears.ui.screens.home.MainScreen
import com.sawears.ui.screens.lessonsettings.LessonSettingsViewModel
import com.sawears.ui.screens.selectsong.SongSelectionEvent
import com.sawears.ui.screens.selectsong.SongSelectionScreen
import com.sawears.ui.screens.selectsong.SongSelectionViewModel
import com.sawears.ui.screens.settings.LessonSettingsScreen
import com.sawears.ui.screens.settings.SettingsScreen
import com.sawears.ui.screens.settings.SettingsViewModel

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
                            },
                            onClickMenu = { openMenu() },
                            onClickAccount = { backStack.add(Route.AccountScreen) },
                            onClickSettings = { backStack.add(Route.SettingsScreen) },
                        )
                    }
                }
                is Route.AccountScreen -> {
                    NavEntry(key){
                        AccountScreen(
                            onClickMenu = { openMenu() },
                            onClickAccount = { backStack.add(Route.AccountScreen) },
                            onClickSettings = { backStack.add(Route.SettingsScreen) },
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                }
                is Route.SongSelectionScreen -> {
                    NavEntry(key){

                        val viewModel = viewModel {SongSelectionViewModel(
                            container.db.songDao(),
                            songRepository = container.songRepository,
                            lessonSettingsRepository = container.lessonSettingsRepository,
                            globalSettingsRepository = container.settingsRepository
                        )}
                        LaunchedEffect(viewModel) {
                            viewModel.events.collect { event ->
                                when (event) {
                                    is SongSelectionEvent.OpenLessonSettings -> {
                                        backStack.add(
                                            Route.LessonSettingsScreen(event.lessonId)
                                        )
                                    }
                                    is SongSelectionEvent.Error -> {
                                        println(event.message) // TODO implement error path
                                    }
                                }


                            }
                        }
                        val state by viewModel.state.collectAsStateWithLifecycle()
                        SongSelectionScreen(
                            state,
                            onSearch = { query ->
                                viewModel.searchTitleFuzzy(query)
                            },
                            onSelectSong = { songId, resourcePath ->
                                viewModel.selectSong(songId, resourcePath)
                            },
                            onClickMenu = { openMenu() },
                            onClickAccount = { backStack.add(Route.AccountScreen) },
                            onClickSettings = { backStack.add(Route.SettingsScreen) },
                        )
                    }
                }
                is Route.LessonScreen -> {


                    NavEntry(key){
                        val viewModel = viewModel {
                            LessonViewModel(
                                key.lessonId,
                                container.audioPlayer,
                                container.estimator,
                                container.difficultySettingsProvider,
                                container.audioInputSourceFactory,
                                container.lessonSettingsRepository
                            )
                        }

                        LessonMainScreen(
                            viewModel=viewModel,
                            onClickMenu = { openMenu() },
                            onClickAccount = { backStack.add(Route.AccountScreen) },
                            onClickSettings = { backStack.add(Route.LessonSettingsScreen(key.lessonId)) }
                        )
                    }
                }
                is Route.SettingsScreen -> {
                    NavEntry(key){
                        val viewModel = viewModel {
                            SettingsViewModel(
                                container.settingsRepository
                            )
                        }

                        val settings = viewModel.settings.collectAsStateWithLifecycle()
                        SettingsScreen(
                            settings=settings.value,
                            onUpdateMin=viewModel::updateRangeMin,
                            onUpdateMax=viewModel::updateRangeMax,
                            onUpdateMode=viewModel::updateMode,
                            onUpdateDifficulty = viewModel::updateDifficulty,
                            onUpdatePitchMargin = viewModel::updatePitchMargin,
                            onClickDone = {backStack.removeLastOrNull()}, // send user back to the screen from which they clicked settings
                            onClickAccount = {backStack.add(Route.AccountScreen)},
                            onClickMenu = {openMenu()}
                        )
                    }
                }
                is Route.LessonSettingsScreen ->{

                    NavEntry(key){

                        val viewModel = viewModel {
                            LessonSettingsViewModel(
                                key.lessonId,
                                container.lessonSettingsRepository,
                                container.settingsRepository
                            )
                        }



                        val settings = viewModel.lessonSettings.collectAsStateWithLifecycle()


                        LessonSettingsScreen(
                            lessonId = key.lessonId,
                            settings = settings.value,
                            onUpdateMin = viewModel::updateRangeMin,
                            onUpdateMax= viewModel::updateRangeMax,
                            onUpdateDifficulty = viewModel::updateDifficulty,
                            onUpdateTempo = viewModel::updateTempo,
                            onUpdateMode = viewModel::updateMode,
                            onUpdatePitchMargin = viewModel::updatePitchMargin,
                            onClickDone = {
                                backStack.add(
                                    Route.LessonScreen(key.lessonId)
                                )
                            },
                            onClickAccount = {backStack.add(Route.AccountScreen)},
                            onClickMenu = {openMenu()},
                            onClickSettings = { backStack.add(Route.SettingsScreen) }
                        )
                    }
                }
                else -> error("Unknown nav key")
            }
        }
    )
}



/**
 * Common open menu action
 */
fun openMenu(){
    //TODO
}
