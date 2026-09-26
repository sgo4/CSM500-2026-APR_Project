package com.sawears.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sawears.domain.Song
import com.sawears.domain.settings.Difficulty
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.ui.screens.lessonsettings.LessonSettings
import com.sawears.ui.screens.components.ContentWithTopBar
import com.sawears.ui.screens.components.NumericStepper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.common_buttons_done
import sawears.shared.generated.resources.settings_errors_range_too_small_for_song
import sawears.shared.generated.resources.settings_options_tempo_label
import sawears.shared.generated.resources.settings_options_tempo_tooltip
import sawears.shared.generated.resources.settings_section_titles_display
import sawears.shared.generated.resources.settings_section_titles_instrument
import sawears.shared.generated.resources.settings_section_titles_lesson
import sawears.shared.generated.resources.settings_section_titles_song

/**
 * Screen for manipulating global application settings
 *
 * @param settings lesson scoped settings
 * @param error a [SettingsError] state flow representing errors adjusting settings
 * @param transpositionStatus flag representing whether range is big enough for current song
 * @param onUpdateMin function to change range min
 * @param onUpdateMax function to change range max
 * @param onUpdateDifficulty
 * @param onUpdateTempo change song tempo
 * @param onUpdateMode change song mode (PRACTICE / PERFORMANCE)
 * @param onUpdatePitchMargin update margin either side of melody range
 * @param onClickHome
 * @param onClickAccount
 */
@Composable
fun LessonSettingsScreen(
    lessonId: Int,
    error: StateFlow<SettingsError?>,
    transpositionStatus: StateFlow<Boolean>,
    settings: LessonSettings,
    onUpdateMin: (Int) -> Unit,
    onUpdateMax: (Int) -> Unit,
    onUpdateDifficulty: (Int) -> Unit,
    onUpdateTempo: (Int) -> Unit,
    onUpdateMode: (LessonMode) -> Unit,
    onUpdatePitchMargin: (Int) -> Unit,
    onClickDone: (Int) -> Unit,
    onClickHome: () -> Unit,
    onClickAccount: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
){

    val error by error.collectAsStateWithLifecycle()
    val transpositionStatus by transpositionStatus.collectAsStateWithLifecycle()

    ContentWithTopBar(
        content = { innerPadding ->
            LessonSettingsScreenContent(
                lessonId=lessonId,
                error=error,
                transpositionStatus = transpositionStatus,
                settings=settings,
                onUpdateMin=onUpdateMin,
                onUpdateMax=onUpdateMax,
                onUpdateDifficulty=onUpdateDifficulty,
                onUpdateTempo=onUpdateTempo,
                onUpdateMode=onUpdateMode,
                onUpdatePitchMargin=onUpdatePitchMargin,
                onClickDone=onClickDone,
                modifier=modifier.padding(innerPadding)
            )
        },
        title = "Lesson Settings",
        onClickHome = onClickHome,
        onClickAccount = onClickAccount,
        onClickSettings = onClickSettings
    )

}

/**
 * Content for settings screen.
 * Shows groups of related settings in sections.
 *
 * @param lessonId
 * @param error [SettingsError] representing error adjusting settings (if any)
 * @param transpositionStatus tracks whether range is big enough for current song (true) or not (false)
 * @param settings
 * @param onUpdateMin callback function to change range min
 * @param onUpdateMax callback function to change range max
 * @param onUpdateDifficulty callback function to change difficulty
 * @param onUpdateTempo callback function to update tempo
 * @param onUpdateMode callback function to update mode
 * @param onUpdatePitchMargin callback function to update pitch margin
 * @param onClickDone callback function for done button
 */
@Composable
fun LessonSettingsScreenContent(
    lessonId: Int,
    error: SettingsError?,
    transpositionStatus: Boolean,
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

        // if range is too small, show warning, else show error message (or nothing if everyhting is fine)
        val transpositionWarning = if (!transpositionStatus) stringResource(Res.string.settings_errors_range_too_small_for_song) else null
        SettingsErrorMsg(error, transpositionWarning)

        SettingsGroup(
            stringResource(Res.string.settings_section_titles_instrument)
        ){
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
            stringResource(Res.string.settings_section_titles_lesson)
        ){
            DifficultySettingRow(
                settings.difficulty,
                onUpdateDifficulty
            )
            ModeSettingRow(
                onUpdateMode
            )
        }
        SettingsGroup(
            stringResource(Res.string.settings_section_titles_song)
        ){
            SettingRow(
                label=stringResource(Res.string.settings_options_tempo_label),
                tooltip=stringResource(Res.string.settings_options_tempo_tooltip)
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
            stringResource(Res.string.settings_section_titles_display)
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
            Text(stringResource(Res.string.common_buttons_done))
        }
    }
}


@Preview
@Composable
internal fun LessonSettingsScreenPreview(
){
    LessonSettingsScreen(
        lessonId=0,
        error=MutableStateFlow(null),
        transpositionStatus = MutableStateFlow(false),
        settings=LessonSettings(
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
        onUpdateMin={Int -> },
        onUpdateMax = {Int ->},
        onUpdateDifficulty={Int -> },
        onUpdateTempo={Int -> },
        onUpdateMode={ LessonMode -> },
        onUpdatePitchMargin = {Int -> },
        onClickDone={},
        onClickHome={},
        onClickAccount={},
        onClickSettings={}
    )
}

