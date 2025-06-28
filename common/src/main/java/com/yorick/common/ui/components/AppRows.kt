package com.yorick.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ClassRow(
    modifier: Modifier = Modifier,
    className: String,
    listContent: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = className,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
        }
        listContent()
    }
}

@Composable
fun SingleRowListItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    name: String,
    option: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Box {
            option()
        }
    }
}

@Composable
fun DualRowListItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    name: String,
    desc: String,
    option: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                Modifier
                    .widthIn(max = 260.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
        Box {
            option()
        }
    }
}

@Composable
fun DualRowListItemWithTextFieldDialog(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    name: String,
    desc: String,
    value: String,
    onConfirm: (input: String) -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1,
    option: @Composable () -> Unit = {}
) {
    var dialogState by remember {
        mutableStateOf(false)
    }
    if (dialogState) {
        TextFieldDialog(
            title = name,
            description = desc,
            value = value,
            onDismissRequest = { dialogState = false },
            onConfirm = onConfirm,
            keyboardType = keyboardType,
            maxLines = maxLines
        )
    }
    DualRowListItem(
        modifier = modifier.clickable { dialogState = true },
        icon = icon,
        name = name,
        desc = desc,
        option = option
    )
}

@Composable
fun DualRowListItemWithSliderDialog(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    name: String,
    desc: String,
    value: Float,
    onConfirm: (input: Float) -> Unit = {},
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    option: @Composable () -> Unit = {}
) {
    var dialogState by remember {
        mutableStateOf(false)
    }
    if (dialogState) {
        SliderDialog(
            title = name,
            description = desc,
            sliderPosition = value,
            onDismissRequest = { dialogState = false },
            onConfirm = onConfirm,
            valueRange = valueRange,
            steps = steps
        )
    }
    DualRowListItem(
        modifier = modifier.clickable { dialogState = true },
        icon = icon,
        name = name,
        desc = desc,
        option = option
    )
}