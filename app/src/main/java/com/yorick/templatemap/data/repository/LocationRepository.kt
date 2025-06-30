package com.yorick.templatemap.data.repository

import android.content.Context
import android.location.LocationManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.melody.map.baidu_compose.poperties.MapProperties
import com.melody.map.baidu_compose.poperties.MapUiSettings
import com.yorick.common.R
import com.yorick.templatemap.data.utils.Utils

class LocationRepository(
    private val context: Context,
    private val locationManager: LocationManager,
    private val locationClient: LocationClient
) {
    fun checkGPSIsOpen(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun initMapProperties(): MapProperties {
        val locationIcon =
            Utils.getDrawable(context, R.drawable.ic_map_location_self)
        return MapProperties(
            // 关闭定位图层，否则影响点击，使用自定义定位点
            isMyLocationEnabled = false,
            myLocationStyle = MyLocationConfiguration(
                // 更新定位数据时不对地图做任何操作
                MyLocationConfiguration.LocationMode.NORMAL,
                true,
                // 修改默认小蓝点的图标
                locationIcon,
                // 设置圆形的填充颜色
                Color(0x80DAA217).toArgb(),
                // 设置圆形的边框颜色
                Color(0xAA4453B4).toArgb()
            )
        )
    }

    fun initMapUiSettings(): MapUiSettings {
        return MapUiSettings(
            isZoomGesturesEnabled = true,
            isZoomEnabled = true,
            isScaleControlsEnabled = true,
            isDoubleClickZoomEnabled = true,
            isScrollGesturesEnabled = true,
        )
    }

    fun initLocationClient(): LocationClient {
        val clientOption = LocationClientOption().apply {
            // 可选，默认false，设置是否开启卫星定位
            isOpenGnss = true
            // 可选，默认gcj02，设置返回的定位结果坐标系
            setCoorType("bd09ll")
            // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            locationMode = LocationClientOption.LocationMode.Hight_Accuracy
            // 设置发起定位请求的间隔时间
            setScanSpan(1000)
            // 返回的定位结果包含地址信息
            setIsNeedAddress(true)
            // 可选，默认false，设置是否收集CRASH信息，默认收集
            SetIgnoreCacheException(false)
            // 可选，默认false，设置是否当卫星定位有效时按照1S1次频率输出卫星定位结果
            isLocationNotify = true
            // 设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
            setOpenAutoNotifyMode()
            // 返回的定位结果包含手机机头的方向
            setNeedDeviceDirect(true)
            // 设置定位时是否需要海拔信息， 默认不需要
            setIsNeedAltitude(true)
            // 需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        }
        locationClient.locOption = clientOption
        return locationClient
    }

    fun bDLocation2MyLocation(bdLocation: BDLocation, degree: Float): MyLocationData {
        return MyLocationData.Builder()
            .accuracy(bdLocation.radius) // 设置定位数据的精度信息，单位：米
            .direction(degree) // 此处设置开发者获取到的方向信息，顺时针0-360
            .latitude(bdLocation.latitude)
            .longitude(bdLocation.longitude)
            .build()
    }

}
