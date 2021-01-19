package com.freeler.utilsmap

import android.content.Context
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.LocationSource

/**
 * 高德地图定位帮助类
 */
class LocationHelper(private val context: Context) : LocationSource, AMapLocationListener {

    /**
     * 高德地图定位监听
     */
    private var mOnLocationChangedListener: LocationSource.OnLocationChangedListener? = null

    /**
     * 高德地图定位服务
     */
    private var mLocationClient: AMapLocationClient? = null

    private var isFirstLoc = true   // 显示出系统蓝点就置为false
    private var mLocationListener: ((AMapLocation) -> Unit)? = null

    fun startLocation(map: AMap, onLocationListener: (AMapLocation) -> Unit) {
        mLocationListener = onLocationListener
        map.setLocationSource(this)
    }

    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener?) {
        mOnLocationChangedListener = onLocationChangedListener
        if (mLocationClient == null) {
            mLocationClient = AMapLocationClient(context).apply {
                // 设置定位监听
                setLocationListener(this@LocationHelper)
                // 设置定位参数
                setLocationOption(
                    AMapLocationClientOption().apply {
                        // 设置为高精度定位模式
                        this.locationMode =
                            AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    })
            }
            mLocationClient?.startLocation()

        }
    }

    override fun deactivate() {
        mOnLocationChangedListener = null
        mLocationClient?.let {
            it.stopLocation()
            it.onDestroy()
        }
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (mOnLocationChangedListener != null && amapLocation != null) {
            if (amapLocation.errorCode == 0) {
                //个人位置
                mLocationListener?.invoke(amapLocation)

                // 显示系统小蓝点
                if (isFirstLoc) {
                    mOnLocationChangedListener?.onLocationChanged(amapLocation)
                    isFirstLoc = false
                }
            } else {
                mLocationListener?.invoke(amapLocation)
            }
        }
    }

}