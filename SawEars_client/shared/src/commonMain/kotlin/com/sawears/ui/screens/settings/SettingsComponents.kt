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
import com.sawears.domain.settings.Difficulty
import com.sawears.ui.screens.lessonsettings.LessonMode
import com.sawears.ui.screens.components.NumericStepper
import com.sawears.ui.screens.components.Tooltip
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.stringResource
import sawears.shared.generated.resources.Res
import sawears.shared.generated.resources.settings_errors_failed_to_persist
import sawears.shared.generated.resources.settings_errors_negative_pitch_margin
import sawears.shared.generated.resources.settings_errors_range_less_than_one
import sawears.shared.generated.resources.settings_options_difficulty_label
import sawears.shared.generated.resources.settings_options_difficulty_tooltip
import sawears.shared.generated.resources.settings_options_displaymargin_label
import sawears.shared.generated.resources.settings_options_displaymargin_tooltip
import sawears.shared.generated.resources.settings_options_mode_label
import sawears.shared.generated.resources.settings_options_mode_performance_name
import sawears.shared.generated.resources.settings_options_mode_practice_name
import sawears.shared.generated.resources.settings_options_mode_tooltip
import sawears.shared.generated.resources.settings_options_rangemax_label
import sawears.shared.generated.resources.settings_options_rangemax_tooltip
import sawears.shared.generated.resources.settings_options_rangemin_label
import sawears.shared.generated.resources.settings_options_rangemin_tooltip

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
            style = MaterialTheme.typography.titleSmall,
            modifier = modifier.padding(bottom = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )

        HorizontalDivider(
            modifier = modifier.padding(top = 16.dp)
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
                .padding(end = 0.dp)
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
        label=stringResource(Res.string.settings_options_rangemin_label),
        tooltip=stringResource(Res.string.settings_options_rangemin_tooltip)
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
        label=stringResource(Res.string.settings_options_rangemax_label),
        tooltip=stringResource(Res.string.settings_options_rangemax_tooltip)
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
        label=stringResource(Res.string.settings_options_difficulty_label),
        tooltip=stringResource(Res.string.settings_options_difficulty_tooltip)
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
    onUpdateMode: (LessonMode) -> Unit
){
    SettingRow(
        label=stringResource(Res.string.settings_options_mode_label),
        tooltip=stringResource(Res.string.settings_options_mode_tooltip)
    ){
        // set component options based on all available modes
        val options = LessonMode.entries
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
                        onUpdateMode(options[index]) // update mode based on option index
                        selectedIndex = index // update selection
                    },
                    selected = index == selectedIndex,
                    label = { when (label){
                        LessonMode.PRACTICE -> Text(stringResource(Res.string.settings_options_mode_practice_name))
                        LessonMode.PERFORMANCE -> Text(stringResource(Res.string.settings_options_mode_performance_name))
                    } }
                )
            }
        }

    }
}

/**
 * Row to set display margin. This is exposed to user as "zoom" as this is a more intuitive
 * way of understanding the effect (a higher margin has the effect of zooming out), but it is referred to within the code as display margin
 */
@Composable
fun DisplayMarginSettingRow(
    pitchDisplayMarginMidi: Int,
    onUpdatePitchMargin: (Int) -> Unit
){
    SettingRow(
        label=stringResource(Res.string.settings_options_displaymargin_label),
        tooltip=stringResource(Res.string.settings_options_displaymargin_tooltip)
    ){
        NumericStepper(
            value = pitchDisplayMarginMidi,
            onValueChange = onUpdatePitchMargin,
            min = 5,
            max = 50
        )
    }
}

/**
 * Display settings related error messages, e.g. invalid ranges
 *
 * Shows blank text if no error to hold space in the UI and prevent
 * layout shifting on error.
 *
 * @param error any reported error
 * @param overrideText allows error message to be overridden with custom warnign
 */
@Composable
fun SettingsErrorMsg(
    error: SettingsError?,
    overrideText: String? = null
){
    overrideText?.let { text ->
        Text(text)
    } ?: error?.let { error ->
        Text(
            text = when (error){
                SettingsError.FailedToPersist -> stringResource(Res.string.settings_errors_failed_to_persist)
                SettingsError.RangeLessThanOne -> stringResource(Res.string.settings_errors_range_less_than_one)
                SettingsError.NegativePitchMargin -> stringResource(Res.string.settings_errors_negative_pitch_margin)
                SettingsError.InvalidTempo -> "Invalid tempo" // this is not actually reachable in current code
            },
            color = MaterialTheme.colorScheme.error
        )
    } ?: Text("")
}

@Preview
@Composable
fun SettingsScreenPreview(){
    SettingsScreen(
        settings = AppSettings(),
        error = MutableStateFlow(null),
        { Int -> },
        {Int -> },
        {Int ->},
        {},
        {},
        {},
        {},
        {}
    )
}