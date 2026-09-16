package com.sawears.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sawears.data.settings.AppSettings
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.ui.screens.components.ContentWithTopBar
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.common_buttons_done
import sawears.shared.generated.resources.settings_page_title
import sawears.shared.generated.resources.settings_section_titles_display
import sawears.shared.generated.resources.settings_section_titles_instrument
import sawears.shared.generated.resources.settings_section_titles_lesson

/**
 * Screen for manipulating global application settings
 *
 * @param settings
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
    ContentWithTopBar(
        content = { innerPadding ->
            SettingsScreenContent(
                settings,
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
        SettingsGroup(
            stringResource(Res.string.settings_section_titles_lesson)
        ){
            DifficultySettingRow(
                settings.difficulty,
                onUpdateDifficulty
            )
            ModeSettingRow(onUpdateMode)
        }
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