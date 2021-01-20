package com.freeler.demoutilsmap

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.MyLocationStyle
import com.freeler.utilsmap.AMapHelper
import com.freeler.utilsmap.MapType
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mMap: AMap
    private lateinit var mMapView: MapView
    private lateinit var mMyLocationStyle: MyLocationStyle
    private val mapHelper by lazy { AMapHelper() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMapView = findViewById<MapView>(R.id.map)
        mMapView.onCreate(savedInstanceState)
        mMap = mMapView.map
        mMap.mapType = AMap.MAP_TYPE_NORMAL

        requestPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) {
            initLocation()
        }

        setUpMapSettings(mMap)

        tv1.setOnClickListener { mapHelper.useTileOverlay(mMap, MapType.NORMAL) }
        tv2.setOnClickListener { mapHelper.useTileOverlay(mMap, MapType.SATELLITE) }
        tv3.setOnClickListener { mapHelper.useTileOverlay(mMap, MapType.HYBRID) }
        mBtnShowLocation.setOnClickListener { mMyLocationStyle.showMyLocation(true) }
        mBtnHideLocation.setOnClickListener { mMyLocationStyle.showMyLocation(false) }
    }

    private fun initLocation() {
        setLocationStyle(mMap)
        mMyLocationStyle = MyLocationStyle().apply {
            this.interval(5000)
            this.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
        }
        //设置定位蓝点的Style
        mMap.myLocationStyle = mMyLocationStyle
        // 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mMap.isMyLocationEnabled = true
        mMap.setOnMyLocationChangeListener {
            Log.e("onLocationChanged", "lat=${it.latitude};lng=${it.longitude}")
        }
    }


    /**
     * 设置定位样式
     */
    private fun setLocationStyle(map: AMap) {
        map.myLocationStyle = MyLocationStyle()
                .radiusFillColor(Color.argb(0, 0, 0, 0))
                .strokeColor(Color.argb(0, 0, 0, 0))
                .myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked))
    }


    /**
     * 设置地图属性
     */
    private fun setUpMapSettings(map: AMap) {
        with(map.uiSettings) {
            isCompassEnabled = true//指南针
            isZoomControlsEnabled = true//缩放比例
            isTiltGesturesEnabled = false // 禁用倾斜手势
            isRotateGesturesEnabled = false // 禁用旋转手势
            isScaleControlsEnabled = true//控制比例尺控件是否显示
            isMyLocationButtonEnabled = true
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

}
