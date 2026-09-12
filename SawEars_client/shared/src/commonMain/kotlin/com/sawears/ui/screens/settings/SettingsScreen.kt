package com.sawears.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sawears.data.settings.AppSettings
import com.sawears.model.pitch.settings.Difficulty
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.ui.shared.components.ContentWithTopBar
import com.sawears.ui.shared.components.NumericStepper
import com.sawears.ui.shared.components.Tooltip

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
 * @param onClickMenu
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
    onClickMenu: () -> Unit,
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
                modifier = Modifier.padding(innerPadding)
            )
        },
        title = "Settings",
        onClickMenu = onClickMenu,
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

        SettingsGroup("Instrument"){
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
            "Lesson"
        ){
            DifficultySettingRow(
                settings.difficulty,
                onUpdateDifficulty
            )
            ModeSettingRow(onUpdateMode)
        }
        SettingsGroup(
            "Display"
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
            Text("Done")
        }
    }

}