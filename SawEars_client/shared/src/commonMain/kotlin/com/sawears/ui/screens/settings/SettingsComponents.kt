package com.sawears.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import com.sawears.model.pitch.settings.Difficulty
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.ui.screens.lessonsettings.LessonSettings
import com.sawears.ui.shared.components.NumericStepper
import com.sawears.ui.shared.components.Tooltip

/**
 * A group of related settings shown together (e.g. saw range settings)
 *
 * Each group has its own subsection on screen.
 *
 * @param groupTitle header for the group (e.g. "Sound settings")
 * @param content rows for individual settings
 */
@Composable
fun SettingsGroup(
    groupTitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
){
    Column(
        modifier = Modifier.fillMaxWidth()
    ){
        Text(
            text = groupTitle,
            style = MaterialTheme.typography.titleSmallEmphasized,
            modifier = modifier.padding(bottom = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )

        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/**
 * Composable representing a single settings as a two column row
 *
 * Column 1 = Setting label (e.g. volume)
 * Column 2 = means of editing that setting, e.g. an input field
 *
 * @param label the name of the setting to be changed
 * @param content settings controls
 */
@Composable
fun SettingRow(
    label: String,
    tooltip: String,
    content: @Composable RowScope.() -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){

        Text(
            text = label,
            modifier = Modifier
                .weight(0.22f)
                .padding(end = 14.dp)
        )
        Tooltip(
            heading= label,
            text = tooltip
        )
        Row(
            modifier = Modifier.weight(0.78f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            content = content
        )

    }
}



// Settings rows shared between lesson settings and main settings screen

@Composable
fun RangeMinSettingRow(
    rangeMin: Int,
    rangeMax: Int,
    onUpdateMin: (Int) -> Unit
){
    SettingRow(
        label="Range min",
        tooltip="Set the lowest note your instrument can produce, as a midi value. This allows the application to transpose songs to fit your playable range."
    ){
        NumericStepper(
            value = rangeMin,
            onValueChange = onUpdateMin,
            min = minOf(0, rangeMin),
            max = maxOf(127,rangeMax)
        )
    }
}

@Composable
fun RangeMaxSettingRow(
    rangeMin: Int,
    rangeMax: Int,
    onUpdateMax: (Int) -> Unit
){
    SettingRow(
        label="Range max",
        tooltip="Set the highest note your instrument can produce, as a midi value. This allows the application to transpose songs to fit your playable range."
    ){
        NumericStepper(
            value = rangeMax,
            onValueChange = onUpdateMax,
            min = minOf(0, rangeMin),
            max = maxOf(127,rangeMax)
        )
    }
}
@Composable
fun DifficultySettingRow(
    difficulty: Difficulty,
    onUpdateDifficulty: (Int) -> Unit
){
    SettingRow(
        label="Difficulty",
        tooltip="Controls how sensitive the app is to changes in pitch (1=easiest, 5=hardest). "
                + "At higher difficulties you will need to meet the target pitch very precisely. " +
                "At lower difficulties a wider range of pitches will be accepted."
    ){
        NumericStepper(
            value = difficulty.ordinal + 1,
            onValueChange = onUpdateDifficulty,
            min = 1,
            max = enumValues<Difficulty>().size
        )
    }
}

@Composable
fun ModeSettingRow(
    onUpdateMode: (LessonMode) -> Unit,
){
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

@Composable
fun DisplayMarginSettingRow(
    pitchDisplayMarginMidi: Int,
    onUpdatePitchMargin: (Int) -> Unit
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
            value = pitchDisplayMarginMidi,
            onValueChange = onUpdatePitchMargin,
            min = 5,
            max = 50
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreview(){
    SettingsScreen(
        settings = AppSettings(),
        {Int -> },
        {Int -> },
        {Int ->},
        {},
        {},
        {},
        {},
        {}
    )
}