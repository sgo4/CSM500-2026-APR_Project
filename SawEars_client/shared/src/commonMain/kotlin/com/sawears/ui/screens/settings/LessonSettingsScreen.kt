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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sawears.model.Song
import com.sawears.model.settings.Difficulty
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.ui.screens.lessonsettings.LessonSettings
import com.sawears.ui.shared.components.ContentWithTopBar
import com.sawears.ui.shared.components.NumericStepper

/**
 * Screen for manipulating global application settings
 *
 * @param settings lesson scoped settings
 * @param onUpdateMin function to change range min
 * @param onUpdateMax function to change range max
 * @param onUpdateDifficulty
 * @param onUpdateTempo change song tempo
 * @param onUpdateMode change song mode (PRACTICE / PERFORMANCE)
 * @param onUpdatePitchMargin update margin either side of melody range
 * @param onClickMenu
 * @param onClickAccount
 */
@Composable
fun LessonSettingsScreen(
    lessonId: Int,
    settings: LessonSettings,
    onUpdateMin: (Int) -> Unit,
    onUpdateMax: (Int) -> Unit,
    onUpdateDifficulty: (Int) -> Unit,
    onUpdateTempo: (Int) -> Unit,
    onUpdateMode: (LessonMode) -> Unit,
    onUpdatePitchMargin: (Int) -> Unit,
    onClickDone: (Int) -> Unit,
    onClickMenu: () -> Unit,
    onClickAccount: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
){
    ContentWithTopBar(
        content = { innerPadding ->
            LessonSettingsScreenContent(
                lessonId,
                settings,
                onUpdateMin,
                onUpdateMax,
                onUpdateDifficulty,
                onUpdateTempo,
                onUpdateMode,
                onUpdatePitchMargin,
                onClickDone,
                modifier = Modifier.padding(innerPadding)
            )
        },
        title = "Lesson Settings",
        onClickMenu = onClickMenu,
        onClickAccount = onClickAccount,
        onClickSettings = onClickSettings
    )

}

/**
 * Content for settings screen.
 * Shows groups of related settings in sections.
 * // TODO finish KDOC
 * @param settings
 * @param onUpdateMin function to change range min
 * @param onUpdateMax function to change range max
 */
@Composable
fun LessonSettingsScreenContent(
    lessonId: Int,
    settings: LessonSettings,
    onUpdateMin: (Int) -> Unit,
    onUpdateMax: (Int) -> Unit,
    onUpdateDifficulty: (Int) -> Unit,
    onUpdateTempo: (Int) -> Unit,
    onUpdateMode: (LessonMode) -> Unit,
    onUpdatePitchMargin: (Int) -> Unit,
    onClickDone: (Int) -> Unit,
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
                settings.rangeMin,
                settings.rangeMax,
                onUpdateMin
            )

            RangeMaxSettingRow(
                settings.rangeMin,
                settings.rangeMax,
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
            ModeSettingRow(
                onUpdateMode
            )
        }
        SettingsGroup("Song"){
            SettingRow(
                label="Tempo",
                tooltip="Control lesson tempo in BPM"
            ){
                NumericStepper(
                    value = settings.tempo,
                    onValueChange = onUpdateTempo,
                    min = 0,
                    max = 500 // set max higher than anyone would reasonably need to set the tempo
                )
            }
        }
        SettingsGroup(
            "Display"
        ){
            DisplayMarginSettingRow(
                settings.pitchDisplayMarginMidi,
                onUpdatePitchMargin
            )
        }
        // Done button, sends the user to the lesson
        Button(
            onClick = {onClickDone(lessonId)}
        ){
            Text("Done")
        }
    }
}


@Preview
@Composable
internal fun LessonSettingsScreenPreview(


){

    LessonSettingsScreen(
        0,
        LessonSettings(
            Song(
                "",
                listOf(""),
                null,
                null,
                null,
                listOf()),
            90,
            Difficulty.STANDARD,
            LessonMode.PERFORMANCE,
            0,
            1000,
            5
        ),
        {Int -> },
        {Int ->},
        {Int -> },
        {Int -> },
        { LessonMode -> },
        {Int -> },
        {},
        {},
        {},
        {}
    )
}

