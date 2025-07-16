package com.yorick.templatemap.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.yorick.common.R
import com.yorick.common.data.model.DarkThemeConfig
import com.yorick.common.data.utils.CommonUtils
import com.yorick.common.data.utils.LogUtils
import com.yorick.common.data.utils.ShareLogResult
import com.yorick.common.ui.components.AppTopBar
import com.yorick.common.ui.components.ClassRow
import com.yorick.common.ui.components.ConfirmDialog
import com.yorick.common.ui.components.DualRowListItem
import com.yorick.common.ui.components.SingleRowListItem
import com.yorick.common.ui.theme.supportsDynamicTheming
import com.yorick.templatemap.data.model.UserData
import com.yorick.templatemap.ui.viewmodels.SettingViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    userData: UserData,
    settingViewModel: SettingViewModel,
    supportDynamicColor: Boolean = supportsDynamicTheming()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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
            ClassRow(className = "日志") {
                DualRowListItem(
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            when (val result = LogUtils.shareLogFiles(context)) {
                                is ShareLogResult.NoLogs -> {
                                    Toast.makeText(
                                        context,
                                        "没有日志文件可以分享",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                is ShareLogResult.Failed -> {
                                    Toast.makeText(
                                        context,
                                        "日志文件分享失败：${result.error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                ShareLogResult.Success -> {
                                    Timber.i("日志文件分享成功")
                                }
                            }
                        }
                    },
                    icon = Icons.Outlined.Share,
                    name = "分享日志",
                    desc = "导出并分享应用日志文件"
                )
                DualRowListItem(
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            val success = LogUtils.clearLogFiles(context)
                            if (success) {
                                Toast.makeText(context, "日志文件清除成功", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(context, "部分日志文件清除失败", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    },
                    icon = Icons.Outlined.Description,
                    name = "清除日志",
                    desc = "删除所有本地日志文件"
                )
                ClassRow(className = stringResource(id = R.string.about)) {
                    SingleRowListItem(
                        icon = Icons.Outlined.Info,
                        name = "版本",
                    ) {
                        Text(
                            text = CommonUtils.getVersionName(context = context),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
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