package com.yorick.templatemap.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LayersClear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.melody.map.baidu_compose.BDMap
import com.melody.map.baidu_compose.model.BDCameraPosition
import com.melody.map.baidu_compose.overlay.Marker
import com.melody.map.baidu_compose.overlay.rememberMarkerState
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.position.CameraPositionState
import com.yorick.common.R
import com.yorick.common.ui.components.AppTopBar
import com.yorick.templatemap.ui.viewmodels.AppUiState
import com.yorick.templatemap.ui.viewmodels.LocationUiState

@Composable
fun MapScreen(
    appUiState: AppUiState,
    locationUiState: LocationUiState,
    cameraPositionState: CameraPositionState,
    startLocation: () -> Unit,
    stopLocation: () -> Unit,
    onClickLocation: () -> Unit,
    onToggleMapType: () -> Unit,
    onToggleMapLabels: () -> Unit
) {

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
                modifier = Modifier.align(Alignment.BottomEnd),
                mapProperties = locationUiState.mapProperties,
                onClickLocation = onClickLocation,
                onToggleMapType = onToggleMapType,
                onToggleMapLabels = onToggleMapLabels,
            )
        }
    }
}

@Composable
fun MapContent(
    modifier: Modifier,
    locationUiState: LocationUiState,
    cameraPositionState: CameraPositionState
) {
    val TAG = "MapContent"
    var isMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(isMapLoaded, locationUiState.locationLatLng) {
        if (isMapLoaded && locationUiState.locationLatLng != null) {
            cameraPositionState.position =
                BDCameraPosition(locationUiState.locationLatLng, 18F, 0f, 0f)
            Log.d(TAG, "MapContent: Updating camera position to ${locationUiState.locationLatLng}")
        }
    }

    BDMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = locationUiState.mapProperties,
        uiSettings = locationUiState.mapUiSettings,
        locationSource = locationUiState.locationSource,
        onMapLoaded = {
            isMapLoaded = true
            Log.d(TAG, "onMapLoaded")
        }
    ) {
        // 当前定位蓝点
        locationUiState.locationLatLng?.let {
            Marker(
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location_self),
                state = rememberMarkerState(position = it),
                rotation = -(locationUiState.locationSource?.direction ?: 0.0f),
                anchor = Offset(0.5f, 0.5f),
                isClickable = false,
                zIndex = 100
            )
        }
    }
}

@Composable
fun MapActions(
    modifier: Modifier,
    mapProperties: MapProperties,
    onClickLocation: () -> Unit,
    onToggleMapType: () -> Unit,
    onToggleMapLabels: () -> Unit
) {
    Column(
        modifier = modifier.padding(bottom = 110.dp, end = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 地图类型切换
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onToggleMapType,
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = MaterialTheme.shapes.small
                )
            ) {
                Icon(imageVector = Icons.Default.Map, contentDescription = null)
            }
            Text(
                text = stringResource(id = R.string.switch_map),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onToggleMapLabels,
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = MaterialTheme.shapes.small
                )
            ) {
                Icon(
                    imageVector = if (mapProperties.isShowMapLabels) {
                        Icons.Outlined.Layers
                    } else {
                        Icons.Outlined.LayersClear
                    },
                    contentDescription = null
                )
            }
            Text(
                text = stringResource(id = R.string.map_label),
                style = MaterialTheme.typography.labelSmall
            )
        }

        // 定位按钮
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onClickLocation,
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    shape = MaterialTheme.shapes.small
                )
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
            }
            Text(
                text = stringResource(id = R.string.location),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
