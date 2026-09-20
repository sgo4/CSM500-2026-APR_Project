package com.sawears.ui.screens.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawears.data.settings.AppSettings
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.ui.screens.components.ContentWithTopBar
import com.sawears.ui.theme.SawEarsTheme
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.common_buttons_done
import sawears.shared.generated.resources.settings_errors_failed_to_persist
import sawears.shared.generated.resources.settings_errors_range_less_than_one
import sawears.shared.generated.resources.settings_errors_range_too_small_for_song
import sawears.shared.generated.resources.settings_page_title
import sawears.shared.generated.resources.settings_section_titles_display
import sawears.shared.generated.resources.settings_section_titles_instrument
import sawears.shared.generated.resources.settings_section_titles_lesson

/**
 * Screen for manipulating global application settings
 *
 * @param settings
 * @param error a [SettingsError] state flow representing errors adjusting settings
 * @param onUpdateMin function to change range min
 * @param onUpdateMax function to change range max
 * @param onUpdateDifficulty
 * @param onUpdateMode change song mode (PRACTICE / PERFORMANCE)
 * @param onUpdatePitchMargin update margin either side of melody range
 * @param onClickDone return to home screen
 * @param onClickHome
 * @param onClickAccount
 */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    error: StateFlow<SettingsError?>,
    onUpdateMin: (Int) -> Unit,
    onUpdateMax: (Int) -> Unit,
    onUpdateDifficulty: (Int) -> Unit,
    onUpdateMode: (LessonMode) -> Unit,
    onUpdatePitchMargin: (Int) -> Unit,
    onClickDone: () -> Unit,
    onClickHome: () -> Unit,
    onClickAccount: () -> Unit,
    modifier: Modifier = Modifier
){
    val error by error.collectAsStateWithLifecycle()

    ContentWithTopBar(
        content = { innerPadding ->
            SettingsScreenContent(
                settings,
                error,
                onUpdateMin,
                onUpdateMax,
                onUpdateDifficulty,
                onUpdateMode,
                onUpdatePitchMargin,
                onClickDone,
                modifier = modifier.padding(innerPadding)
            )
        },
        title = stringResource(Res.string.settings_page_title),
        onClickMenu = onClickHome,
        onClickAccount = onClickAccount,
        onClickSettings = { } // we're already on the settings page so this function doesn't need to do anything
    )

}

/**
 * Content for settings screen.
 * Shows groups of related settings in sections.
 *
 * @param settings app wide settings state
 * @param error [SettingsError] representing error adjusting settings (if any)
 * @param onUpdateMin function to change range min
 * @param onUpdateMax function to change range max
 * @param onUpdateDifficulty
 * @param onUpdateMode
 * @param onUpdatePitchMargin
 * @param onClickDone
 */
@Composable
fun SettingsScreenContent(
    settings: AppSettings,
    error: SettingsError?, // TODO implement and document
    onUpdateMin: (Int) -> Unit,
    onUpdateMax: (Int) -> Unit,
    onUpdateDifficulty: (Int) -> Unit,
    onUpdateMode: (LessonMode) -> Unit,
    onUpdatePitchMargin: (Int) -> Unit,
    onClickDone: () -> Unit,
    modifier: Modifier = Modifier,
){

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ){
        // Will show blank text if no error present
        // Always visible  to preserve layout on error
        SettingsErrorMsg(error)

        // instrument settings
        SettingsGroup(stringResource(Res.string.settings_section_titles_instrument)){
            RangeMinSettingRow(
                settings.rangeMinMidi,
                settings.rangeMaxMidi,
                onUpdateMin
            )
            RangeMaxSettingRow(
                settings.rangeMinMidi,
                settings.rangeMaxMidi,
                onUpdateMax
            )
        }
        // lesson settings
        SettingsGroup(
            stringResource(Res.string.settings_section_titles_lesson)
        ){
            DifficultySettingRow(
                settings.difficulty,
                onUpdateDifficulty
            )
            ModeSettingRow(onUpdateMode)
        }
        // display settings
        SettingsGroup(
            stringResource(Res.string.settings_section_titles_display)
        ){
            DisplayMarginSettingRow(
                settings.pitchDisplayMarginMidi,
                onUpdatePitchMargin
            )
        }
        // Done button, sends the user home
        Button(
            onClick = onClickDone
        ){
            Text(stringResource(Res.string.common_buttons_done))
        }
    }

}