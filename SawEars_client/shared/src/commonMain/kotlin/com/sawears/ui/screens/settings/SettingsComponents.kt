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