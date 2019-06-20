package com.freeler.demoutilsmap

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.MyLocationStyle
import com.freeler.utilsamap.AMapUtil
import com.freeler.utilsamap.MapType

class MainActivity : AppCompatActivity(),
    View.OnClickListener,
    LocationSource,
    AMapLocationListener {

    private lateinit var mMap: AMap
    private lateinit var mMapView: MapView


    /**
     * 高德地图定位监听
     */
    private var mOnLocationChangedListener: LocationSource.OnLocationChangedListener? = null

    /**
     * 高德地图定位服务
     */
    private var mLocationClient: AMapLocationClient? = null

    private var isFirstLoc = true   // 显示出系统蓝点就置为false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMapView = findViewById<MapView>(R.id.map)
        mMapView.onCreate(savedInstanceState)
        mMap = mMapView.map
        mMap.mapType = AMap.MAP_TYPE_NORMAL

        setLocationStyle(mMap)
        setUpMapSettings(mMap)

        findViewById<Button>(R.id.tv1).setOnClickListener(this)
        findViewById<Button>(R.id.tv2).setOnClickListener(this)
        findViewById<Button>(R.id.tv3).setOnClickListener(this)
    }

    private fun setLocationStyle(map: AMap) {
        map.myLocationStyle = MyLocationStyle()
            .radiusFillColor(Color.argb(0, 0, 0, 0))
            .strokeColor(Color.argb(0, 0, 0, 0))
            .myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked))
        map.setLocationSource(this)
    }

    /**
     * 设置地图属性
     */
    private fun setUpMapSettings(map: AMap) {
        with(map.uiSettings) {
            isMyLocationButtonEnabled = true
            isCompassEnabled = true//指南针
            isZoomControlsEnabled = true//缩放比例
            isTiltGesturesEnabled = false // 禁用倾斜手势
            isRotateGesturesEnabled = false // 禁用旋转手势
            isScaleControlsEnabled = true//控制比例尺控件是否显示
        }
        //显示定位层并且可以触发定位,默认是flase
        mMap.isMyLocationEnabled = true
    }

    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener?) {
        mOnLocationChangedListener = onLocationChangedListener
        if (mLocationClient == null) {
            mLocationClient = AMapLocationClient(this)
                .apply {
                    // 设置定位监听
                    setLocationListener(this@MainActivity)
                    // 设置定位参数
                    setLocationOption(
                        AMapLocationClientOption()
                            .apply {
                                // 设置为高精度定位模式
                                this.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
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
//                mMyLocation = LatLng(amapLocation.latitude, amapLocation.longitude)
//                LogUtil.i("高德onLocationChanged;", "lat=${mMyLocation.latitude};lng=${mMyLocation.longitude}")
//                if (isShowPolyLine) {
//                    drawPolyLine(mMyLocation)
//                }

                // 显示系统小蓝点
                if (isFirstLoc) {
                    mOnLocationChangedListener?.onLocationChanged(amapLocation)
                    isFirstLoc = false
                }

            } else {
//                val errText = "定位失败,${amapLocation.errorCode}: ${amapLocation.errorInfo}"
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv1 -> AMapUtil.useTileOverlay(mMap, MapType.NORMAL)
            R.id.tv2 -> AMapUtil.useTileOverlay(mMap, MapType.SATELLITE)
            R.id.tv3 -> AMapUtil.useTileOverlay(mMap, MapType.HYBRID)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

}
