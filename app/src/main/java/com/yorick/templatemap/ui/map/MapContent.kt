package com.yorick.templatemap.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.melody.map.baidu_compose.BDMap
import com.melody.map.baidu_compose.model.BDCameraPosition
import com.melody.map.baidu_compose.overlay.Marker
import com.melody.map.baidu_compose.overlay.MarkerState
import com.melody.map.baidu_compose.position.CameraPositionState
import com.yorick.common.R
import com.yorick.templatemap.ui.viewmodels.LocationUiState
import timber.log.Timber

@Composable
fun MapContent(
    modifier: Modifier,
    locationUiState: LocationUiState,
    cameraPositionState: CameraPositionState,
) {
    val TAG = "MapContent"
    var isMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(isMapLoaded, locationUiState.isLocationSuccess) {
        if (isMapLoaded && locationUiState.locationLatLng != null) {
            cameraPositionState.position =
                BDCameraPosition(locationUiState.locationLatLng, 18F, 0f, 0f)
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
            Timber.tag(TAG).d("onMapLoaded")
        }
    ) {
        // Current location marker
        locationUiState.locationLatLng?.let {
            Marker(
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_location_self),
                state = MarkerState(position = it),
                rotation = locationUiState.locationSource?.direction ?: 0.0f,
                anchor = Offset(0.5f, 0.5f),
                isClickable = false,
                zIndex = 100
            )
        }
    }
}