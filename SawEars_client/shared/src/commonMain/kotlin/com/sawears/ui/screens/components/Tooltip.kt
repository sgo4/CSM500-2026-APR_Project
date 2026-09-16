package com.sawears.ui.screens.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

/**
 * Tooltip component. With a title and multiline text.
 *
 * Boilerplate adapted from:
 * https://developer.android.com/develop/ui/compose/components/tooltip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Tooltip(
    modifier: Modifier = Modifier,
    heading: String = "Heading",
    text: String = "Text"
) {

    val tooltipState = rememberTooltipState()
    val coroutineScope = rememberCoroutineScope()


    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = { Text(heading) }
            ) {
                Text(text)
            }
        },
        state = tooltipState
    ) {
        IconButton(onClick = {
            coroutineScope.launch {
                tooltipState.show()
            }
        }) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Show more information"
            )
        }
    }
}