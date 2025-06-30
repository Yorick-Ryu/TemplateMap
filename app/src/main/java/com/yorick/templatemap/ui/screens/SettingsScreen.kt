package com.yorick.templatemap.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yorick.common.R
import com.yorick.common.data.model.DarkThemeConfig
import com.yorick.common.data.utils.CommonUtils
import com.yorick.common.ui.components.AppTopBar
import com.yorick.common.ui.components.ClassRow
import com.yorick.common.ui.components.ConfirmDialog
import com.yorick.common.ui.components.DualRowListItem
import com.yorick.common.ui.components.SingleRowListItem
import com.yorick.common.ui.theme.supportsDynamicTheming
import com.yorick.templatemap.data.model.UserData
import com.yorick.templatemap.ui.viewmodels.SettingViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    userData: UserData,
    settingViewModel: SettingViewModel,
    supportDynamicColor: Boolean = supportsDynamicTheming()
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var themeIcon: ImageVector by remember { mutableStateOf(Icons.Outlined.BrightnessAuto) }
    var isOpenRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(id = R.string.settings)
                    )
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.settings),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    IconButton(onClick = { isOpenRestoreDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.SettingsBackupRestore,
                            contentDescription = stringResource(id = R.string.restore),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { pd ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(pd)
        ) {
            ClassRow(className = stringResource(id = R.string.theme)) {
                SingleRowListItem(
                    modifier = Modifier.clickable { isExpanded = true },
                    icon = themeIcon,
                    name = stringResource(id = R.string.dark_mode)
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                        LocalContentColor provides MaterialTheme.colorScheme.primary
                    ) {
                        when (userData.darkThemeConfig) {
                            DarkThemeConfig.FOLLOW_SYSTEM -> {
                                Text(text = stringResource(id = R.string.auto))
                                themeIcon = Icons.Outlined.BrightnessAuto
                            }

                            DarkThemeConfig.LIGHT -> {
                                Text(text = stringResource(id = R.string.light))
                                themeIcon = Icons.Outlined.LightMode
                            }

                            DarkThemeConfig.DARK -> {
                                Text(text = stringResource(id = R.string.dark))
                                themeIcon = Icons.Outlined.DarkMode
                            }
                        }
                    }
                    DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.auto)) },
                            onClick = {
                                isExpanded = false
                                settingViewModel.updateUserData(userData.copy(darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM))
                            })
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.light)) },
                            onClick = {
                                isExpanded = false
                                settingViewModel.updateUserData(userData.copy(darkThemeConfig = DarkThemeConfig.LIGHT))
                            })
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.dark)) },
                            onClick = {
                                isExpanded = false
                                settingViewModel.updateUserData(userData.copy(darkThemeConfig = DarkThemeConfig.DARK))
                            })
                    }
                }
                if (supportDynamicColor) {
                    DualRowListItem(
                        icon = Icons.Outlined.ColorLens,
                        name = stringResource(id = R.string.dynamic_color),
                        desc = stringResource(id = R.string.dynamic_color_desc)
                    ) {
                        Switch(
                            checked = userData.useDynamicColor,
                            onCheckedChange = {
                                settingViewModel.updateUserData(userData.copy(useDynamicColor = !userData.useDynamicColor))
                            }
                        )
                    }
                }
            }
            ClassRow(className = stringResource(id = R.string.about)) {
                DualRowListItem(
                    modifier = Modifier.clickable {
                        CommonUtils.dialPhoneNumber(context, "13888888888")
                    },
                    icon = Icons.Outlined.SupportAgent,
                    name = stringResource(id = R.string.service_feedback),
                    desc = stringResource(id = R.string.service_feedback_desc),
                )
            }
        }
    }
    if (isOpenRestoreDialog) {
        ConfirmDialog(
            title = stringResource(id = R.string.restore),
            content = stringResource(id = R.string.restore_confirm),
            onConfirm = {
                isOpenRestoreDialog = false
                settingViewModel.clearUserData()
            },
            onDismiss = { isOpenRestoreDialog = false })
    }
}