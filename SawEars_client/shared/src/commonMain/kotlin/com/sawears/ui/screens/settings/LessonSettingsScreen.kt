package com.sawears.ui.screens.settings



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
 * @param onClickMenu
 * @param onClickAccount
 */
@Composable
fun LessonSettingsScreen(
    settings: LessonSettings,
    onUpdateMin: (Int) -> Unit,
    onUpdateMax: (Int) -> Unit,
    onUpdateDifficulty: (Int) -> Unit,
    onUpdateTempo: (Int) -> Unit,
    onUpdateMode: (LessonMode) -> Unit,
    onClickMenu: () -> Unit,
    onClickAccount: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
){
    ContentWithTopBar(
        content = { innerPadding ->
            LessonSettingsScreenContent(
                settings,
                onUpdateMin,
                onUpdateMax,
                onUpdateDifficulty,
                onUpdateTempo,
                onUpdateMode,
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
 *
 * @param settings
 * @param onUpdateMin function to change range min
 * @param onUpdateMax function to change range max
 */
@Composable
fun LessonSettingsScreenContent(
    settings: LessonSettings,
    onUpdateMin: (Int) -> Unit,
    onUpdateMax: (Int) -> Unit,
    onUpdateDifficulty: (Int) -> Unit,
    onUpdateTempo: (Int) -> Unit,
    onUpdateMode: (LessonMode) -> Unit,
    modifier: Modifier = Modifier,
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
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
                tooltip="Change mode" // TODO expand
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
                            onClick = { onUpdateMode(
                                    enumValueOf(options.get(index)) // update mode based on option index
                                ) },
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
                    max = 5
                )
            }
        }
        // TODO transposition can also go here
    }

}


@Preview
@Composable
internal fun LessonSettingsScreenPreview(

){
    LessonSettingsScreen(
        LessonSettings(90,3, LessonMode.PERFORMANCE,0,1000),
        {Int -> },
        {Int ->},
        {Int -> },
        {Int -> },
        { LessonMode -> },
        {},
        {},
        {}
    )
}

