package com.freeler.demoutilsmap

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.AMap
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.MyLocationStyle
import com.freeler.utilsmap.AMapHelper
import com.freeler.utilsmap.LocationHelper
import com.freeler.utilsmap.MapType

class MainActivity : AppCompatActivity(),
    View.OnClickListener {

    private lateinit var mMap: AMap
    private lateinit var mMapView: MapView
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
            setLocationStyle(mMap)
            LocationHelper(this).startLocation(mMap) {
                Log.e(
                    "onLocationChanged",
                    "code=${it.errorCode};errorInfo=${it.errorInfo};lat=${it.latitude};lng=${it.longitude}"
                )
            }
        }

        setUpMapSettings(mMap)

        findViewById<Button>(R.id.tv1).setOnClickListener(this)
        findViewById<Button>(R.id.tv2).setOnClickListener(this)
        findViewById<Button>(R.id.tv3).setOnClickListener(this)
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


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv1 -> mapHelper.useTileOverlay(mMap, MapType.NORMAL)
            R.id.tv2 -> mapHelper.useTileOverlay(mMap, MapType.SATELLITE)
            R.id.tv3 -> mapHelper.useTileOverlay(mMap, MapType.HYBRID)
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
