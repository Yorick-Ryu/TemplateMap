package com.yorick.templatemap.ui.viewmodels

import android.app.Application
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
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
import com.yorick.common.data.utils.safeLaunch
import com.yorick.templatemap.data.repository.LocationRepository
import com.yorick.templatemap.data.utils.BDMapUtils.locationErrorMessage
import com.yorick.templatemap.data.utils.SensorEventHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            val isOpenGps = locationRepository.checkGPSIsOpen()
            _uiState.value =
                _uiState.value.copy(isOpenGps = isOpenGps, isShowOpenGPSDialog = !isOpenGps)
        }
    }

    fun updateIsShowOpenGPSDialog(isShowOpenGPSDialog: Boolean) {
        _uiState.value = _uiState.value.copy(isShowOpenGPSDialog = isShowOpenGPSDialog)
    }

    fun openGPSPermission(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        if (locationRepository.checkGPSIsOpen()) {
            // 已打开系统GPS，APP还没授权，跳权限页面
            // openAppPermissionSettingPage(application)
        } else {
            // 打开系统GPS开关页面
            launcher.safeLaunch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    fun startMapLocation() {
        sensorEventHelper.registerSensorListener(this)
        if (null == mLocClient) {
            mLocClient = locationRepository.initLocationClient()
            mLocClient?.registerLocationListener(mLocationListener)
            mLocClient?.start()
        } else {
            mLocClient?.restart()
        }
    }

    private val mLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(bdLocation: BDLocation?) {
            Log.d(TAG, "onReceiveLocation: $bdLocation")
            if (null != bdLocation) {
                val checkErrorMsg = locationErrorMessage(bdLocation.locType)
                if (checkErrorMsg != null) {
                    _uiState.value = _uiState.value.copy(
                        isLocationSuccess = false,
                        error = checkErrorMsg
                    )
                    Log.e(TAG, "onReceiveLocation: $checkErrorMsg")
                    return
                }
                if (bdLocation.latitude.toString() != "4.9E-324") {
                    // 设置定位数据
                    _uiState.value = _uiState.value.copy(
                        locationData = bdLocation,
                        locationLatLng = LatLng(bdLocation.latitude, bdLocation.longitude),
                        isLocationSuccess = true,
                        locationSource = locationRepository.bDLocation2MyLocation(
                            bdLocation,
                            sensorEventHelper.getSensorDegree() // 设置方向指针
                        )
                    )
                } else {
                    // 定位出错了
                    _uiState.value = _uiState.value.copy(isLocationSuccess = false)
                    Log.e(TAG, "onReceiveLocation: 定位失败")
                }
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
        val newMapType = if (_uiState.value.mapProperties.mapType == MapType.NORMAL) {
            MapType.SATELLITE
        } else {
            MapType.NORMAL
        }
        _uiState.value = _uiState.value.copy(
            mapProperties = _uiState.value.mapProperties.copy(mapType = newMapType)
        )
    }

    fun toggleMapLabels() {
        _uiState.value = _uiState.value.copy(
            mapProperties = _uiState.value.mapProperties.copy(
                isShowMapLabels = !_uiState.value.mapProperties.isShowMapLabels
            )
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun exit() {
        _uiState.value =
            _uiState.value.copy(
                loading = false,
                error = null,
                isLocationSuccess = false,
                locationData = null,
                locationLatLng = null,
            )
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
            _uiState.value = _uiState.value.copy(
                locationSource = locationRepository.bDLocation2MyLocation(
                    data, 360 - degree
                )
            )
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