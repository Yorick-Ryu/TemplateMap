package com.yorick.common.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.yorick.common.R


@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
    )
}

@Composable
fun ConfirmWithoutDismissDialog(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}

@Composable
fun OpenGpsDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    ConfirmWithoutDismissDialog(
        modifier = modifier,
        onDismiss = onDismissRequest,
        title = stringResource(id = R.string.open_gps),
        content = stringResource(id = R.string.open_gps_desc),
        onConfirm = onConfirm
    )
}

@Composable
fun OpenLocationDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    ConfirmWithoutDismissDialog(
        modifier = modifier,
        onDismiss = onDismissRequest,
        title = stringResource(id = R.string.permission_request),
        content = stringResource(id = R.string.need_fine_location),
        onConfirm = {
            onDismissRequest()
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent, null)
        }
    )
}

@Composable
fun SliderDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    description: String = "",
    sliderPosition: Float = 0f,
    onDismissRequest: () -> Unit = {},
    onConfirm: (input: Float) -> Unit = {},
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
) {
    var value by remember {
        mutableFloatStateOf(sliderPosition)
    }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title)
                Text(
                    text = value.toInt().toString(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = description)
                Spacer(modifier = Modifier.height(6.dp))
                Slider(
                    value = value,
                    onValueChange = { value = it },
                    valueRange = valueRange,
                    steps = steps
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(value)
                    onDismissRequest()
                }
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun TextFieldDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    description: String = "",
    value: String = "",
    onDismissRequest: () -> Unit = {},
    onConfirm: (input: String) -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1
) {
    var input by remember {
        mutableStateOf(value)
    }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = description)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(text = stringResource(id = R.string.value)) },
                    maxLines = maxLines,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = keyboardType
                    ),
                    keyboardActions = KeyboardActions(onDone = { onConfirm(input) })
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = input.trim() != "",
                onClick = {
                    onConfirm(input)
                }
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun ErrorDialog(
    modifier: Modifier = Modifier,
    error: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.error),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }
    )
}