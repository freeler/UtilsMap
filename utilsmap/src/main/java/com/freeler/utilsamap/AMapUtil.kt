package com.freeler.utilsamap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment

import com.amap.api.maps.AMap
import com.amap.api.maps.model.TileOverlay
import com.amap.api.maps.model.TileOverlayOptions
import com.amap.api.maps.model.UrlTileProvider

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author xuzeyang
 * @date 2019/01/01
 */
object AMapUtil {

    private val tileOverlays = ArrayList<TileOverlay>()

    /**
     * 加载在线瓦片数据
     */
    fun useTileOverlay(aMap: AMap, type: MapType): TileOverlay? {
        clearTileOverlay()
        if (type == MapType.NORMAL) {
            aMap.mapType = AMap.MAP_TYPE_NORMAL
            return null
        }
        val tileOverlay = aMap.addTileOverlay(getTileOverlayOptions(type))
        tileOverlays.add(tileOverlay)
        return tileOverlay
    }

    /**
     * 清除瓦片
     */
    private fun clearTileOverlay() {
        tileOverlays.forEach { it.remove() }
    }

    /**
     * 设置瓦片Options
     */
    private fun getTileOverlayOptions(type: MapType): TileOverlayOptions {
        val url = when (type) {
            MapType.SATELLITE -> "http://www.google.cn/maps/vt?lyrs=s&gl=cn&x=%d&s=&y=%d&z=%d"
            else -> "http://www.google.cn/maps/vt?lyrs=y&gl=cn&x=%d&s=&y=%d&z=%d"
        }

        val albumPath = Environment.getExternalStorageDirectory().toString() + when (type) {
            MapType.SATELLITE -> "/amapyun/Cache/Satellite/"
            else -> "/amapyun/Cache/Hybrid/"
        }

        val diskCacheDirPath = when (type) {
            MapType.SATELLITE -> "/storage/emulated/0/amap/OMCcache/Satellite"
            else -> "/storage/emulated/0/amap/OMCcache/Hybrid"
        }

        val cacheFile = when (type) {
            MapType.SATELLITE -> "/amapyun/Cache/Satellite"
            else -> "/amapyun/Cache/Hybrid"
        }

        return TileOverlayOptions().tileProvider(object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                try {
                    val fileDirName = String.format("L%02d/", zoom + 1)
                    val fileName = String.format("%s", tileXYToQuadKey(x, y, zoom))
                    //为了不在手机的图片中显示,取消jpg后缀,文件名自己定义,写入和读取一致即可,由于有自己的bingmap图源服务,所以此处我用的bingmap的文件名
                    val lj = albumPath + fileDirName + fileName

                    return when {
                        //判断本地是否有图片文件,如果有返回本地url,如果没有,缓存到本地并返回googleUrl
                        isLocalHasBmp(fileDirName + fileName, cacheFile) -> URL("file://$lj")
                        else -> {
                            val filePath = String.format(url, x, y, zoom)
                            //不知什么原因导致有大量的图片存在坏图,所以重写InputStream写到byte数组方法
                            val bitmap = getImageBitmap(getImageStream(filePath))
                            try {
                                saveFile(bitmap, albumPath, fileDirName, fileName)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                            URL(filePath)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return null
            }
        }).apply {
            this.diskCacheEnabled(false)   //由于自带的缓存在关闭程序后会自动释放,所以无意义,关闭本地缓存
                .diskCacheDir(diskCacheDirPath)
                .diskCacheSize(1024000)
                .memoryCacheEnabled(true)
                .memCacheSize(102400)
                .zIndex(-9999f)
        }
    }


    /**
     * 瓦片数据坐标转换
     */
    private fun tileXYToQuadKey(tileX: Int, tileY: Int, levelOfDetail: Int): String {
        val quadKey = StringBuilder()
        for (i in levelOfDetail downTo 1) {
            var digit = '0'
            val mask = 1 shl i - 1
            if (tileX and mask != 0) {
                digit++
            }
            if (tileY and mask != 0) {
                digit++
                digit++
            }
            quadKey.append(digit)
        }
        return quadKey.toString()
    }

    /**
     * 判断本地有没有
     */
    private fun isLocalHasBmp(url: String, cacheFile: String): Boolean {
        var isExit = true
        val filePath = isCacheFileIsExit(cacheFile)
        val file = File(filePath, url)
        if (file.exists()) {
        } else {
            isExit = false
        }
        return isExit
    }

    /**
     * 判断缓存文件夹是否存在如果存在怎返回文件夹路径，如果不存在新建文件夹并返回文件夹路径
     */
    private fun isCacheFileIsExit(cacheFile: String): String {
        val rootPath = when (Environment.getExternalStorageState()) {
            Environment.MEDIA_MOUNTED -> Environment.getExternalStorageDirectory().toString()
            else -> ""
        }
        val filePath = rootPath + cacheFile
        val file = File(filePath)
        if (!file.exists()) {
            file.mkdirs()
        }
        return filePath
    }

    private fun getImageBitmap(inputStream: InputStream?): Bitmap? {
        // 将所有InputStream写到byte数组当中
        var targetData: ByteArray? = null
        val bytePart = ByteArray(4096)
        while (true) {
            try {
                val readLength = inputStream?.read(bytePart) ?: -1
                if (readLength == -1) {
                    break
                } else {
                    val temp = ByteArray(readLength + (targetData?.size ?: 0))
                    if (targetData != null) {
                        System.arraycopy(targetData, 0, temp, 0, targetData.size)
                        System.arraycopy(bytePart, 0, temp, targetData.size, readLength)
                    } else {
                        System.arraycopy(bytePart, 0, temp, 0, readLength)
                    }
                    targetData = temp
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        // 指使Bitmap通过byte数组获取数据
        return if (targetData == null) {
            null
        } else BitmapFactory.decodeByteArray(targetData, 0, targetData.size)


    }

    @Throws(Exception::class)
    private fun getImageStream(path: String): InputStream? {
        val conn = (URL(path).openConnection() as HttpURLConnection).apply {
            this.connectTimeout = 5 * 1000
            this.requestMethod = "GET"
        }
        return when (conn.responseCode) {
            HttpURLConnection.HTTP_OK -> conn.inputStream
            else -> null
        }
    }

    /**
     * 保存文件
     */
    @Throws(IOException::class)
    private fun saveFile(bm: Bitmap?, albumPath: String, fileDirName: String, fileName: String) {
        Thread(Runnable {
            try {
                if (bm != null) {
                    val dirFile = File(albumPath + fileDirName)
                    if (!dirFile.exists()) {
                        dirFile.mkdir()
                    }
                    val myCaptureFile = File(albumPath + fileDirName + fileName)
                    val bos = BufferedOutputStream(FileOutputStream(myCaptureFile))
                    bm.compress(Bitmap.CompressFormat.JPEG, 80, bos)
                    bos.flush()
                    bos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }

    /**
     * 删除文件
     */
    fun deleteAllFiles(root: File) {
        val files = root.listFiles()
        if (files != null)
            for (f in files) {
                if (f.isDirectory) { // 判断是否为文件夹
                    deleteAllFiles(f)
                    try {
                        f.delete()
                    } catch (e: Exception) {
                    }

                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f)
                        try {
                            f.delete()
                        } catch (e: Exception) {
                        }

                    }
                }
            }
    }


}

enum class MapType {
    SATELLITE, HYBRID, NORMAL
}
