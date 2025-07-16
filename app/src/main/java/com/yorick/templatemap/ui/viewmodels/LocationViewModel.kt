package com.yorick.templatemap.ui.viewmodels

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.melody.map.baidu_compose.model.BDCameraPosition
import com.melody.map.baidu_compose.model.MapType
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.melody.map.baidu_compose.position.CameraPositionState
import com.yorick.common.data.model.ISensorDegreeListener
import com.yorick.common.data.utils.openAppPermissionSettingPage
import com.yorick.common.data.utils.safeLaunch
import com.yorick.templatemap.data.repository.LocationRepository
import com.yorick.templatemap.data.utils.BDMapUtils.locationErrorMessage
import com.yorick.templatemap.data.utils.SensorEventHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val sensorEventHelper: SensorEventHelper,
    application: Application,
) : AndroidViewModel(application), ISensorDegreeListener {

    private var mLocClient: LocationClient? = null

    private val _uiState = MutableStateFlow(
        LocationUiState(
            mapProperties = locationRepository.initMapProperties(),
            mapUiSettings = locationRepository.initMapUiSettings()
        )
    )
    val uiState: StateFlow<LocationUiState> = _uiState

    companion object {
        const val TAG = "LocationViewModel"
    }

    // 定位相关
    fun checkGpsStatus() {
        Timber.d("Checking GPS status")
        viewModelScope.launch {
            try {
                val isOpenGps = locationRepository.checkGPSIsOpen()
                _uiState.update { it.copy(isOpenGps = isOpenGps, isShowOpenGPSDialog = !isOpenGps) }
                Timber.i("GPS status check completed: enabled=$isOpenGps")
            } catch (e: Exception) {
                Timber.e(e, "Failed to check GPS status")
            }
        }
    }

    fun updateIsShowOpenGPSDialog(isShowOpenGPSDialog: Boolean) {
        Timber.d("GPS dialog visibility changed: show=$isShowOpenGPSDialog")
        _uiState.update { it.copy(isShowOpenGPSDialog = isShowOpenGPSDialog) }
    }

    fun openGPSPermission(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        if (locationRepository.checkGPSIsOpen()) {
            // 已打开系统GPS，APP还没授权，跳权限页面
            Timber.i("GPS enabled but app permission missing - opening app settings")
            openAppPermissionSettingPage(application)
        } else {
            // 打开系统GPS开关页面
            Timber.i("GPS disabled - opening system GPS settings")
            try {
                launcher.safeLaunch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                Timber.d("GPS settings intent launched successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to launch GPS settings intent")
            }
        }
    }

    fun startMapLocation() {
        Timber.i("Starting map location services")
        sensorEventHelper.registerSensorListener(this)
        if (null == mLocClient) {
            Timber.d("Initializing new location client")
            mLocClient = locationRepository.initLocationClient()
            mLocClient?.registerLocationListener(mLocationListener)
            mLocClient?.start()
            Timber.d("Location client started")
        } else {
            Timber.d("Restarting existing location client")
            mLocClient?.restart()
        }
    }

    private val mLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(bdLocation: BDLocation?) {
            Timber.v("Location callback received: $bdLocation")
            if (null != bdLocation) {
                val checkErrorMsg = locationErrorMessage(bdLocation.locType)
                if (checkErrorMsg != null) {
                    _uiState.update {
                        it.copy(
                            isLocationSuccess = false,
                            error = checkErrorMsg
                        )
                    }
                    Timber.e("Location error (type=${bdLocation.locType}): $checkErrorMsg")
                    return
                }
                if (bdLocation.latitude.toString() != "4.9E-324") {
                    // 设置定位数据
                    val lat = bdLocation.latitude
                    val lon = bdLocation.longitude
                    val accuracy = bdLocation.radius

                    _uiState.update {
                        it.copy(
                            locationData = bdLocation,
                            locationLatLng = LatLng(lat, lon),
                            isLocationSuccess = true,
                        )
                    }
                    Timber.i("Location updated: lat=$lat, lon=$lon, accuracy=${accuracy}m")
                } else {
                    // 定位出错了
                    _uiState.value = _uiState.value.copy(isLocationSuccess = false)
                    Timber.e("Location failed: invalid coordinates received")
                }
            } else {
                Timber.w("Location callback received null location")
            }
        }
    }

    fun moveCameraPosition(cameraPositionState: CameraPositionState, location: LatLng) {
        cameraPositionState.position = BDCameraPosition(
            latLng = location,
            zoom = 18F,
            rotate = 0f,
            overlook = 0f
        )
    }

    fun toggleMapType() {
        val currentType = _uiState.value.mapProperties.mapType
        val newMapType = if (currentType == MapType.NORMAL) {
            MapType.SATELLITE
        } else {
            MapType.NORMAL
        }
        _uiState.update {
            it.copy(mapProperties = it.mapProperties.copy(mapType = newMapType))
        }
        Timber.d("Map type changed: $currentType -> $newMapType")
    }

    fun toggleMapLabels() {
        val newLabelsState = !_uiState.value.mapProperties.isShowMapLabels
        _uiState.update {
            it.copy(mapProperties = it.mapProperties.copy(isShowMapLabels = newLabelsState))
        }
        Timber.d("Map labels toggled: show=$newLabelsState")
    }

    fun clearError() {
        Timber.d("Clearing location error state")
        _uiState.update { it.copy(error = null) }
    }

    fun zoomIn(cameraPositionState: CameraPositionState) {
        val currentPosition = cameraPositionState.position
        val newZoom = (currentPosition.zoom + 1).coerceIn(4f, 21f)
        cameraPositionState.position = BDCameraPosition(
            latLng = currentPosition.latLng,
            zoom = newZoom,
            rotate = currentPosition.rotate,
            overlook = currentPosition.overlook
        )
        Timber.d("Map zoomed in: zoom level $newZoom")
    }

    fun zoomOut(cameraPositionState: CameraPositionState) {
        val currentPosition = cameraPositionState.position
        val newZoom = (currentPosition.zoom - 1).coerceIn(4f, 21f)
        cameraPositionState.position = BDCameraPosition(
            latLng = currentPosition.latLng,
            zoom = newZoom,
            rotate = currentPosition.rotate,
            overlook = currentPosition.overlook
        )
        Timber.d("Map zoomed out: zoom level $newZoom")
    }


    fun exit() {
        _uiState.update {
            it.copy(
                loading = false,
                error = null,
                isLocationSuccess = false,
                locationData = null,
                locationLatLng = null,
            )
        }
        stopLocation()
    }

    fun stopLocation() {
        sensorEventHelper.unRegisterSensorListener()
        mLocClient?.unRegisterLocationListener(mLocationListener)
        mLocClient?.stop()
        mLocClient = null
    }


    override fun onSensorDegree(degree: Float) {
        _uiState.value.locationData?.let { data ->
            _uiState.update {
                it.copy(
                    locationSource = locationRepository.bDLocation2MyLocation(
                        data, degree
                    )
                )
            }
//            Timber.v("Sensor degree updated: $degree")
        }
    }

    override fun onCleared() {
        stopLocation()
        super.onCleared()
    }

}

@Stable
data class LocationUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val isOpenGps: Boolean = false,
    val isLocationSuccess: Boolean = false,
    val locationData: BDLocation? = null,
    val locationLatLng: LatLng? = null,
    val locationSource: MyLocationData? = null,
    val isShowOpenGPSDialog: Boolean = false,
    val mapProperties: MapProperties,
    val mapUiSettings: MapUiSettings,
) 