package com.sawears.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/**
 * Numeric stepper for precise numerical input.
 *
 * Credit to:
 * https://dev.to/myougatheaxo/slider-stepper-in-compose-numeric-input-ui-patterns-4jpg
 */
@Composable
fun NumericStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 0,
    max: Int = 100
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        Button(
            onClick = { if (value > min) onValueChange(value - 1) }
        ) {
            Text("−")
        }
        Text(
            text = value.toString(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Button(
            onClick = { if (value < max) onValueChange(value + 1) }
        ) {
            Text("+")
        }
    }
}


