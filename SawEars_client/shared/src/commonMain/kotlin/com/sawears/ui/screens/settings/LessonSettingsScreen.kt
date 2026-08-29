package com.sawears.ui.screens.settings



import androidx.compose.foundation.layout.Column
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
import com.sawears.model.Song
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.ui.screens.lessonsettings.LessonSettings
import com.sawears.ui.shared.components.ContentWithTopBar
import com.sawears.ui.shared.components.NumericStepper
import com.sawears.ui.shared.components.Tooltip

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
            SettingRow(
                label="Range min",
                tooltip="Set the lowest note your instrument can produce, as a midi value. This allows the application to transpose songs to fit your playable range."
            ){
                NumericStepper(
                    value = settings.rangeMin,
                    onValueChange = onUpdateMin,
                    min = 0,
                    max = maxOf(127,settings.rangeMax)
                )
            }

            SettingRow(
                label="Range max",
                tooltip="Set the highest note your instrument can produce, as a midi value. This allows the application to transpose songs to fit your playable range."
            ){
                NumericStepper(
                    value = settings.rangeMax,
                    onValueChange = onUpdateMax,
                    min = 0,
                    max = maxOf(127,settings.rangeMax)
                )
            }
        }
        SettingsGroup(
            "Lesson"
        ){
            SettingRow(
                label="Difficulty",
                tooltip="Controls how sensitive the app is to changes in pitch (1=easiest, 5=hardest). "
                        + "At higher difficulties you will need to meet the target pitch very precisely. " +
                        "At lower difficulties a wider range of pitches will be accepted."
            ){
                NumericStepper(
                    value = settings.difficulty,
                    onValueChange = onUpdateDifficulty,
                    min = 0,
                    max = 5
                )
            }
            SettingRow(
                label="Mode",
                tooltip="""
                    Control the default lesson mode
                    
                    Practice: The lesson will wait for you to produce the correct note before proceeding, allowing you to practice the song at your own pace.
                    Performance: The lesson will continue at the tempo you set, like a karaoke performance.
                """.trimIndent()
            ){
                // set component options based on all available modes
                val options = LessonMode.entries.map { it.name }
                // keep track of which option is selected
                var selectedIndex by remember { mutableIntStateOf(0) }

                // Component adapted from:
                // https://developer.android.com/develop/ui/compose/components/segmented-button
                SingleChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = {
                                onUpdateMode(enumValueOf(options[index])) // update mode based on option index
                                selectedIndex = index // update selection
                                 },
                            selected = index == selectedIndex,
                            label = { Text(label) }
                        )
                    }
                }

            }
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
            SettingRow(
                label="Display margin",
                tooltip="""
                    Controls margin to display around melody, e.g. if set to 5 the highest
                    note displayed will be 5 midi notes higher than the highest note in the 
                    melody.
                """.trimIndent()
            ){
                NumericStepper(
                    value = settings.pitchDisplayMarginMidi,
                    onValueChange = onUpdatePitchMargin,
                    min = 5,
                    max = 50
                )
            }
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
            3,
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

