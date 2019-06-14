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
object AMapHybridUtil {

    private val ALBUM_PATH = Environment.getExternalStorageDirectory().toString() + "/amapyun/Cache/Hybrid/"
    private const val DISKCACHEDIR_PATH = "/storage/emulated/0/amap/OMCcache/Hybrid"
    private const val CACHE_FILE = "/amapyun/Cache/Hybrid"

    private var mTileOverlay: TileOverlay? = null

    /**
     * 判断缓存文件夹是否存在如果存在怎返回文件夹路径，如果不存在新建文件夹并返回文件夹路径
     */
    private val isCacheFileIsExit: String
        get() {
            val rootPath = when (Environment.getExternalStorageState()) {
                Environment.MEDIA_MOUNTED -> Environment.getExternalStorageDirectory().toString()
                else -> ""
            }
            val filePath = rootPath + CACHE_FILE
            val file = File(filePath)
            if (!file.exists()) {
                file.mkdirs()
            }
            return filePath
        }

    /**
     * 加载在线瓦片数据
     */
    fun useOMCMap(aMap: AMap): TileOverlay? {
        val tileOverlayOptions = getTileOverlayOptions()
        mTileOverlay = aMap.addTileOverlay(tileOverlayOptions)
        return mTileOverlay
    }

    /**
     * 设置瓦片Options
     */
    private fun getTileOverlayOptions(): TileOverlayOptions {
        val url = "http://www.google.cn/maps/vt?lyrs=y&gl=cn&x=%d&s=&y=%d&z=%d"

        return TileOverlayOptions().tileProvider(object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                try {
                    val fileDirName = String.format("L%02d/", zoom + 1)
                    val fileName = String.format("%s", tileXYToQuadKey(x, y, zoom))
                    //为了不在手机的图片中显示,取消jpg后缀,文件名自己定义,写入和读取一致即可,由于有自己的bingmap图源服务,所以此处我用的bingmap的文件名
                    val lj = ALBUM_PATH + fileDirName + fileName

                    return when {
                        //判断本地是否有图片文件,如果有返回本地url,如果没有,缓存到本地并返回googleUrl
                        isLocalHasBmp(fileDirName + fileName) -> URL("file://$lj")
                        else -> {
                            val filePath = String.format(url, x, y, zoom)
                            //不知什么原因导致有大量的图片存在坏图,所以重写InputStream写到byte数组方法
                            val bitmap = getImageBitmap(getImageStream(filePath))
                            try {
                                saveFile(bitmap, fileName, fileDirName)
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
                    .diskCacheDir(DISKCACHEDIR_PATH)
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
    private fun isLocalHasBmp(url: String): Boolean {
        var isExit = true
        val filePath = isCacheFileIsExit
        val file = File(filePath, url)
        if (file.exists()) {
        } else {
            isExit = false
        }
        return isExit
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
    private fun saveFile(bm: Bitmap?, fileName: String, fileDirName: String) {
        Thread(Runnable {
            try {
                if (bm != null) {
                    val dirFile = File(ALBUM_PATH + fileDirName)
                    if (!dirFile.exists()) {
                        dirFile.mkdir()
                    }
                    val myCaptureFile = File(ALBUM_PATH + fileDirName + fileName)
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

    fun clearOMCMap() {
        //清除所有瓦片
        if (mTileOverlay != null) {
            mTileOverlay!!.remove()
        }
    }



}
