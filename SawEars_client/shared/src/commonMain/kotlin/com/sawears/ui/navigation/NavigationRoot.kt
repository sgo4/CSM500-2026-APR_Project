package com.sawears.ui.navigation

import androidx.compose.runtime.Composable
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
                            },
                            onClickMenu = { openMenu() },
                            onClickAccount = { openAccountSettings(backStack) }
                        )
                    }
                }
                is Route.AccountScreen -> {
                    NavEntry(key){
                        AccountScreen(
                            onClickMenu = { openMenu() },
                            onClickAccount = { openAccountSettings(backStack) },
                            onBack = { onBack(backStack) }
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
                            onSearch = { query ->
                                viewModel.searchTitleFuzzy(query)
                            },
                            onSelectSong = {
                                backStack.add(
                                    Route.LessonScreen(it)
                                )
                            },
                            onClickMenu = { openMenu() },
                            onClickAccount = { openAccountSettings(backStack)},
                        )
                    }
                }
                is Route.LessonScreen -> {

                    NavEntry(key){
                        val viewModel = viewModel {
                            LessonViewModel(
                                container
                            )
                        }
                        LessonMainScreen(
                            viewModel,
                            key.resourcePath
                        )
                    }
                }
                else -> error("Unknown nav key")
            }
        }
    )
}


/**
 * Common back button function
 */
fun onBack(
    backStack: NavBackStack<NavKey>
){
    backStack.removeLastOrNull()
}

/**
 * Common open menu action
 */
fun openMenu(){

}

/**
 * Common account settings navigation function
 */
fun openAccountSettings(backStack: NavBackStack<NavKey>){
    backStack.add(Route.AccountScreen)
}