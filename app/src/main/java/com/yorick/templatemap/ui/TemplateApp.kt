package com.yorick.templatemap.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yorick.common.data.model.SettingsUiState
import com.yorick.common.ui.components.ErrorDialog
import com.yorick.templatemap.data.model.UserData
import com.yorick.templatemap.ui.navigation.AppRoute
import com.yorick.templatemap.ui.screens.MainScreen
import com.yorick.templatemap.ui.viewmodels.AppViewModel
import com.yorick.templatemap.ui.viewmodels.SettingViewModel

@Composable
fun TemplateApp(
    modifier: Modifier = Modifier,
    settingViewModel: SettingViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
    uiState: SettingsUiState
) {
    val userData =
        if (uiState is SettingsUiState.Success) uiState.userData as UserData else UserData()

    val appUiState by appViewModel.uiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()


    // 显示错误对话框 - AppViewModel
    appUiState.error?.let { error ->
        ErrorDialog(
            error = error,
            onDismiss = {
                appViewModel.clearError()
            }
        )
    }

    NavHost(
        modifier = modifier,
        navController = navController, startDestination = AppRoute.MAIN
    ) {
        composable(AppRoute.MAIN) {
            MainScreen(
                settingViewModel = settingViewModel,
                appUiState = appUiState,
                userData = userData,
            )
        }
    }
}