package com.sawears.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.sawears.AppContainer
import com.sawears.ui.screens.lesson.LessonMainScreen
import com.sawears.ui.screens.lesson.LessonViewModel
import com.sawears.ui.screens.error.GenericErrorScreen
import com.sawears.ui.screens.guide.UserGuideScreen
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
import org.jetbrains.compose.resources.getString

/**
 * Root for app navigation. Provides logic for navigating between
 * screens.
 *
 * This approach is mostly boilerplate navigation3 code, but was partially adapted
 * from this tutorial by Philip Lackner:
 * https://www.youtube.com/watch?v=G_e9XL5tI6U
 *
 * @param container application level DI container
 */
@Composable
fun NavigationRoot(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    // get locale for localization purposes
    val locale = LocalLocale.current.language

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
        // entry decorators ensure view models are scoped to the screen rather
        // than the application, preventing bugs causes by multiple screns sharing
        // the same view model (see:
        // https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html#viewmodel-scoping-with-navigation-3)
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        backStack = backStack,
        entryProvider = { key ->
            // home function that returns to menu and safely
            // clears backstack
            val home: () -> Unit = {
                while (backStack.size > 1){
                    backStack.removeAt(backStack.lastIndex)
                }
            }
            val onClickSettings: () -> Unit = {
                backStack.add(Route.SettingsScreen)
            }

            when(key) {
                is Route.MainScreen -> {
                    NavEntry(key){
                        MainScreen(
                            onOpenLesson = {
                                backStack.add(
                                    Route.SongSelectionScreen
                                )
                            },
                            onClickHome = home,
                            onClickInfo = { backStack.add(Route.UserGuideScreen(locale=locale))},
                            onClickSettings = onClickSettings,
                        )
                    }
                }
                is Route.UserGuideScreen -> {
                    NavEntry(key){
                        UserGuideScreen(
                            locale = locale,
                            onClickInfo = { }, // already on user guide screen
                            onClickHome = home,
                            onClickSettings = { backStack.add(Route.SettingsScreen) }
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
                                    is SongSelectionEvent.SelectionError -> {

                                        val msg = getString(
                                            event.message
                                        )
                                        backStack.add(
                                            Route.ErrorScreen(
                                                msg
                                            )
                                        )
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
                            onClickHome = home,
                            onClickInfo = { backStack.add(Route.UserGuideScreen(locale=locale)) },
                            onClickSettings = { backStack.add(Route.SettingsScreen) },
                        )
                    }
                }
                is Route.LessonScreen -> {

                    NavEntry(key){
                        val viewModel = viewModel {
                            LessonViewModel(
                                key.lessonId,
                                container.audioPlayerFactory,
                                container.estimator,
                                container.difficultySettingsProvider,
                                container.audioInputSourceFactory,
                                container.lessonSettingsRepository,
                                container.latencyTracker
                            )
                        }

                        LessonMainScreen(
                            viewModel=viewModel,
                            onClickHome = home,
                            onClickInfo = { backStack.add(Route.UserGuideScreen(locale=locale))},
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
                            error=viewModel.error,
                            onUpdateMin=viewModel::updateRangeMin,
                            onUpdateMax=viewModel::updateRangeMax,
                            onUpdateMode=viewModel::updateMode,
                            onUpdateDifficulty = viewModel::updateDifficulty,
                            onUpdatePitchMargin = viewModel::updatePitchMargin,
                            onClickDone = {backStack.removeLastOrNull()}, // send user back to the screen from which they clicked settings
                            onClickInfo = { backStack.add(Route.UserGuideScreen(locale=locale))},
                            onClickHome = home
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
                            error = viewModel.error,
                            transpositionStatus = viewModel.transpositionStatus,
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
                            onClickInfo = { backStack.add(Route.UserGuideScreen(locale=locale))},
                            onClickHome = home,
                            onClickSettings = {} // settings button does nothing on lesson settings screen
                        )
                    }
                }

                is Route.ErrorScreen -> {
                    NavEntry(key){
                        GenericErrorScreen(
                            errorMsg = key.message,
                            onClickHome = home,
                            onClickSettings = onClickSettings,
                            onClickInfo = { backStack.add(Route.UserGuideScreen(locale=locale))}
                        )
                    }
                }
                else -> error("Unknown nav key")
            }
        }
    )
}


