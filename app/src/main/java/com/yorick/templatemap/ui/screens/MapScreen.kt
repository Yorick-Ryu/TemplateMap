package com.yorick.templatemap.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.melody.map.baidu_compose.position.CameraPositionState
import com.yorick.common.ui.components.AppTopBar
import com.yorick.common.ui.components.OpenGpsDialog
import com.yorick.common.ui.components.OpenLocationDialog
import com.yorick.common.ui.components.handlerGPSLauncher
import com.yorick.templatemap.ui.map.MapActions
import com.yorick.templatemap.ui.map.MapContent
import com.yorick.templatemap.ui.viewmodels.LocationUiState
import com.yorick.templatemap.ui.viewmodels.LocationViewModel
import timber.log.Timber

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    locationViewModel: LocationViewModel,
    locationUiState: LocationUiState,
    cameraPositionState: CameraPositionState,
    onClickLocation: () -> Unit,
    onToggleMapType: () -> Unit,
    onToggleMapLabels: () -> Unit
) {

    val TAG = "MapScreen"

    // 定位相关权限Start
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    )

    val fineLocationPermission = locationPermissions.permissions.find {
        it.permission == Manifest.permission.ACCESS_FINE_LOCATION
    }

    val coarseLocationPermission = locationPermissions.permissions.find {
        it.permission == Manifest.permission.ACCESS_COARSE_LOCATION
    }

    var showLocationRationalDialog by remember { mutableStateOf(false) }

    val hasLocationPermission = fineLocationPermission?.status?.isGranted == true ||
            coarseLocationPermission?.status?.isGranted == true

    if (showLocationRationalDialog) {
        OpenLocationDialog(
            onDismissRequest = { showLocationRationalDialog = false },
        )
    }

    LaunchedEffect(locationPermissions) {
        if (!locationPermissions.allPermissionsGranted) {
            if (locationPermissions.shouldShowRationale) {
                if (fineLocationPermission?.status?.shouldShowRationale == true ||
                    coarseLocationPermission?.status?.shouldShowRationale == true
                ) {
                    showLocationRationalDialog = true
                }
            } else {
                locationPermissions.launchMultiplePermissionRequest()
            }
        }
    }

    // 权限状态日志
    LaunchedEffect(
        locationPermissions.permissions,
        locationPermissions.allPermissionsGranted
    ) {
        Timber.tag(TAG).d("精确定位权限状态: ${fineLocationPermission?.status?.isGranted}")
        Timber.tag(TAG).d("粗略定位权限状态: ${coarseLocationPermission?.status?.isGranted}")
        Timber.tag(TAG).d("定位权限状态: $hasLocationPermission")
        Timber.tag(TAG).d("定位权限全部授予: ${locationPermissions.allPermissionsGranted}")
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            locationViewModel.startMapLocation()
        }
    }
    // 定位相关权限End

    // 检查GPS是否打开Start
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
    // 检查GPS是否打开End


    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            modifier = Modifier.fillMaxWidth(),
            title = {
                Text(
                    text = stringResource(id = com.yorick.templatemap.R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            MapContent(
                modifier = Modifier.fillMaxSize(),
                locationUiState = locationUiState,
                cameraPositionState = cameraPositionState,
            )

            MapActions(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 12.dp
                    ),
                mapProperties = locationUiState.mapProperties,
                onClickLocation = onClickLocation,
                onToggleMapType = onToggleMapType,
                onToggleMapLabels = onToggleMapLabels,
                onZoomIn = { locationViewModel.zoomIn(cameraPositionState) },
                onZoomOut = { locationViewModel.zoomOut(cameraPositionState) }
            )
        }
    }
}
