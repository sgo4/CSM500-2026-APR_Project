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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sawears.data.settings.AppSettings
import com.sawears.ui.shared.components.ContentWithTopBar
import com.sawears.ui.shared.components.NumericStepper
import com.sawears.ui.shared.components.Tooltip

/**
 * Screen for manipulating global application settings
 *
 * @param settings
 * @param onUpdateMin function to change range min
 * @param onUpdateMax function to change range max
 * @param onClickMenu
 * @param onClickAccount
 */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateMin: (Int) -> Unit,
    onUpdateMax: (Int) -> Unit,
    onClickMenu: () -> Unit,
    onClickAccount: () -> Unit,
    onClickSettings: () -> Unit,
    modifier: Modifier = Modifier
){
    ContentWithTopBar(
        content = { innerPadding ->
            SettingsScreenContent(
                settings,
                onUpdateMin,
                onUpdateMax,
                modifier = Modifier.padding(innerPadding)
            )
        },
        title = "Settings",
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
fun SettingsScreenContent(
    settings: AppSettings,
    onUpdateMin: (Int) -> Unit,
    onUpdateMax: (Int) -> Unit,
    modifier: Modifier = Modifier,
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ){
        SettingsGroup("Instrument settings"){
            SettingRow(
                label="Range min",
                tooltip="Set the lowest note your instrument can produce, as a midi value. This allows the application to transpose songs to fit your playable range."
            ){
                NumericStepper(
                    value = settings.rangeMinMidi,
                    onValueChange = onUpdateMin, // TODO
                    min = 0,
                    max = maxOf(127,settings.rangeMaxMidi)
                    )
            }

            SettingRow(
                label="Range max",
                tooltip="Set the highest note your instrument can produce, as a midi value. This allows the application to transpose songs to fit your playable range."
            ){
                NumericStepper(
                    value = settings.rangeMaxMidi,
                    onValueChange = onUpdateMax,
                    min = 0,
                    max = maxOf(127,settings.rangeMaxMidi)
                )
            }
        }
    }

}

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
                .weight(0.35f)
                .padding(end = 24.dp)
        )
        Tooltip(
            heading= label,
            text = tooltip
        )
        Row(
            modifier = Modifier.weight(0.65f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            content = content
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
        {},
        {},
        {}
    )
}