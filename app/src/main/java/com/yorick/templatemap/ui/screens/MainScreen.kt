package com.yorick.templatemap.ui.screens

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.melody.map.baidu_compose.position.rememberCameraPositionState
import com.yorick.common.ui.components.OpenGpsDialog
import com.yorick.common.ui.components.OpenLocationDialog
import com.yorick.common.ui.components.handlerGPSLauncher
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

    // 申请通知和前台定位权限权限
    val foregroundPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    val fineLocationPermission = foregroundPermissions.permissions.find {
        it.permission == Manifest.permission.ACCESS_FINE_LOCATION
    }

    val coarseLocationPermission = foregroundPermissions.permissions.find {
        it.permission == Manifest.permission.ACCESS_COARSE_LOCATION
    }

    val showLocationRationalDialog = remember { mutableStateOf(false) }


    val hasLocationPermission = fineLocationPermission?.status?.isGranted == true ||
            coarseLocationPermission?.status?.isGranted == true

    // 处理定位权限对话框
    if (showLocationRationalDialog.value) {
        OpenLocationDialog(
            onDismissRequest = { showLocationRationalDialog.value = false },
        )
    }

    // 申请权限
    LaunchedEffect(foregroundPermissions) {
        if (!foregroundPermissions.allPermissionsGranted) {
            if (foregroundPermissions.shouldShowRationale) {
                // 此处处理的是 foregroundPermissions 的 shouldShowRationale，通常分别处理每个权限的 rationale 更佳
                // 例如，如果通知权限需要 rationale，则显示通知的 rationale
                // 如果定位权限需要 rationale，则显示定位的 rationale
                // 为了简化，这里假设如果任何一个权限需要 rationale，就可能需要用户去设置页
                // 或者可以针对性地判断是哪个权限需要 rationale
                if (fineLocationPermission?.status?.shouldShowRationale == true || coarseLocationPermission?.status?.shouldShowRationale == true) {
                    showLocationRationalDialog.value = true
                }
            } else {
                foregroundPermissions.launchMultiplePermissionRequest()
            }
        } else {
            locationViewModel.startMapLocation()
        }
    }

    // 权限日志
    LaunchedEffect(
        foregroundPermissions.permissions,
        foregroundPermissions.allPermissionsGranted
    ) {
        Log.d("MainScreen", "精确定位权限状态: ${fineLocationPermission?.status?.isGranted}")
        Log.d("MainScreen", "粗略定位权限状态: ${coarseLocationPermission?.status?.isGranted}")
        Log.d("MainScreen", "定位权限状态: $hasLocationPermission")
        Log.d("MainScreen", "前台所有权限状态: ${foregroundPermissions.allPermissionsGranted}")
    }

    // 检查前台定位权限的变化并启动定位
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            locationViewModel.startMapLocation()
        } else {
            if (fineLocationPermission?.status?.shouldShowRationale == true || coarseLocationPermission?.status?.shouldShowRationale == true) {
                showLocationRationalDialog.value = true
            } else {
                //  foregroundPermissions.launchMultiplePermissionRequest() // 或者单独请求
            }
        }
    }

    // 检查GPS是否打开
    LaunchedEffect(Unit) {
        locationViewModel.checkGpsStatus()
    }

    val openGpsLauncher = handlerGPSLauncher(locationViewModel::checkGpsStatus)

    if (locationUiState.isShowOpenGPSDialog) {
        OpenGpsDialog(
            onDismissRequest = {
                locationViewModel.updateIsShowOpenGPSDialog(
                    isShowOpenGPSDialog = false
                )
            },
            onConfirm = { locationViewModel.openGPSPermission(openGpsLauncher) }
        )
    }

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
                    appUiState = appUiState,
                    locationUiState = locationUiState,
                    cameraPositionState = cameraPositionState,
                    startLocation = {
                        // 确保有权限再执行操作
                        if (hasLocationPermission) {
                            locationViewModel.startMapLocation()
                        } else {
                            Log.w(
                                "MainScreen",
                                "Permissions not granted for starting location or accessing storage."
                            )
                            // 可以再次提示用户去授权，或者显示一个信息
                            if (foregroundPermissions.permissions.any { !it.status.isGranted }) foregroundPermissions.launchMultiplePermissionRequest()
                        }
                    },
                    stopLocation = locationViewModel::stopLocation,
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