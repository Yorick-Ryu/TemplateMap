package com.yorick.templatemap.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.melody.map.baidu_compose.position.rememberCameraPositionState
import com.yorick.common.ui.navigation.NavigationActions
import com.yorick.templatemap.data.model.UserData
import com.yorick.templatemap.ui.navigation.AppNavigationBar
import com.yorick.templatemap.ui.navigation.AppRoute
import com.yorick.templatemap.ui.viewmodels.AppUiState
import com.yorick.templatemap.ui.viewmodels.LocationViewModel
import com.yorick.templatemap.ui.viewmodels.SettingViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    locationViewModel: LocationViewModel,
    appUiState: AppUiState,
    modifier: Modifier = Modifier,
    userData: UserData,
    settingViewModel: SettingViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val navigationActions = remember(navController) { NavigationActions(navController) }
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val selectedDestination = navBackStackEntry.value?.destination?.route ?: AppRoute.HOME

    val locationUiState by locationViewModel.uiState.collectAsStateWithLifecycle()

    val cameraPositionState = rememberCameraPositionState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AppNavigationBar(
                selectedDestination = selectedDestination,
                navigateToTopLevelDestination = navigationActions::navigateTo,
            )
        }
    ) { pd ->
        NavHost(
            modifier = modifier
                .fillMaxSize()
                .padding(pd),
            navController = navController,
            startDestination = AppRoute.HOME
        ) {
            composable(AppRoute.HOME) {
                HomeScreen(
                    appUiState = appUiState
                )
            }
            composable(AppRoute.MAP) {
                MapScreen(
                    locationViewModel = locationViewModel,
                    locationUiState = locationUiState,
                    cameraPositionState = cameraPositionState,
                    onClickLocation = {
                        locationUiState.locationLatLng?.let { location ->
                            locationViewModel.moveCameraPosition(cameraPositionState, location)
                        }
                    },
                    onToggleMapType = locationViewModel::toggleMapType,
                    onToggleMapLabels = locationViewModel::toggleMapLabels,
                )
            }
            composable(AppRoute.SETTINGS) {
                SettingsScreen(
                    settingViewModel = settingViewModel,
                    userData = userData
                )
            }
        }
    }
}