package com.freeler.utilsmap

import com.amap.api.maps.model.LatLng

object MapLayUtil {

    /**
     * 获取坐标中心点
     */
    fun getCenterLatlng(list: List<LatLng>): LatLng {
        var lats = 0.0
        var lngs = 0.0

        list.forEach {
            lats += it.latitude
            lngs += it.longitude
        }

        val latCenter = lats / list.size
        val lngCenter = lngs / list.size
        return LatLng(latCenter, lngCenter)
    }

    /**
     * 获取地图层级
     */
    fun getZoom(list: List<LatLng>): Int {
        var maxLng = list[0].longitude
        var minLng = list[0].longitude
        var maxLat = list[0].latitude
        var minLat = list[0].latitude

        list.forEach {
            val lat = it.latitude
            val lng = it.longitude

            if (lng > maxLng) maxLng = lng
            if (lng < minLng) minLng = lng
            if (lat > maxLat) maxLat = lat
            if (lat < minLat) minLat = lat
        }

        return getZoom(maxLng, minLng, maxLat, minLat)
    }


    /**
     * 获取地图层级
     */
    private fun getZoom(maxLng: Double, minLng: Double, maxLat: Double, minLat: Double): Int {
        // 级别18到3。
        val zoom = intArrayOf(50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 25000, 50000, 100000, 200000, 500000, 1000000, 2000000)
        // 创建点坐标A,坐标B
        val pointA = LatLng(maxLat, maxLng)
        val pointB = LatLng(minLat, minLng)
        // 获取两点距离,保留小数点后两位
        val distance = SphericalUtil.computeDistanceBetween(pointA, pointB)
        for (i in zoom.indices) {
            if (zoom[i] - distance > 0) {
                // 之所以会多3，是因为地图范围常常是比例尺距离的10倍以上。所以级别会增加3。
                return 18 - i + 2
//                    return 18 - i + 3
            }
        }
        return 17
    }

}