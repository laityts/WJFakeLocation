// BaiduMapManager.kt
package com.steadywj.wjfakelocation.manager.map.utils

import android.content.Context
import com.baidu.mapapi.search.geocode.GeoCodeResult
import com.baidu.mapapi.search.geocode.GeoCoder
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 百度地图管理器
 * 
 * 功能:
 * - BD-09 ↔ WGS-84 坐标转换
 * - 地理编码搜索
 * - 逆地理编码
 */
@Singleton
class BaiduMapManager @Inject constructor(
    private val context: Context
) {
    
    /**
     * BD-09 转 WGS-84
     * 百度坐标系 → 火星坐标系 → GPS 坐标系
     */
    fun bd09ToWgs84(bdLat: Double, bdLng: Double): Pair<Double, Double> {
        // BD-09 → GCJ-02
        val (gcjLat, gcjLng) = bd09ToGcj02(bdLat, bdLng)
        
        // GCJ-02 → WGS-84
        return gcj02ToWgs84(gcjLat, gcjLng)
    }
    
    /**
     * WGS-84 转 BD-09
     * GPS 坐标系 → 火星坐标系 → 百度坐标系
     */
    fun wgs84ToBd09(wgsLat: Double, wgsLng: Double): Pair<Double, Double> {
        // WGS-84 → GCJ-02
        val (gcjLat, gcjLng) = wgs84ToGcj02(wgsLat, wgsLng)
        
        // GCJ-02 → BD-09
        return gcj02ToBd09(gcjLat, gcjLng)
    }
    
    /**
     * BD-09 转 GCJ-02
     */
    private fun bd09ToGcj02(bdLat: Double, bdLng: Double): Pair<Double, Double> {
        val xPi = Math.PI * 3000.0 / 180.0
        val x = bdLng - 0.0065
        val y = bdLat - 0.006
        val z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * xPi)
        val theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * xPi)
        
        val gcjLng = z * Math.cos(theta)
        val gcjLat = z * Math.sin(theta)
        
        return gcjLat to gcjLng
    }
    
    /**
     * GCJ-02 转 BD-09
     */
    private fun gcj02ToBd09(gcjLat: Double, gcjLng: Double): Pair<Double, Double> {
        val xPi = Math.PI * 3000.0 / 180.0
        val z = Math.sqrt(gcjLng * gcjLng + gcjLat * gcjLat) + 0.00002 * Math.sin(gcjLat * xPi)
        val theta = Math.atan2(gcjLat, gcjLng) + 0.000003 * Math.cos(gcjLng * xPi)
        
        val bdLng = z * Math.cos(theta) + 0.0065
        val bdLat = z * Math.sin(theta) + 0.006
        
        return bdLat to bdLng
    }
    
    /**
     * GCJ-02 转 WGS-84
     */
    private fun gcj02ToWgs84(gcjLat: Double, gcjLng: Double): Pair<Double, Double> {
        return com.steadywj.wjfakelocation.xposed.utils.LocationUtil.gcj02ToWgs84(gcjLat, gcjLng)
    }
    
    /**
     * WGS-84 转 GCJ-02
     */
    private fun wgs84ToGcj02(wgsLat: Double, wgsLng: Double): Pair<Double, Double> {
        return com.steadywj.wjfakelocation.xposed.utils.LocationUtil.wgs84ToGcj02(wgsLat, wgsLng)
    }
    
    /**
     * 地理编码搜索（地址转坐标）
     */
    fun geocodeAddress(address: String): Flow<Result<Pair<Double, Double>>> = callbackFlow {
        try {
            val geoCoder = GeoCoder.newInstance()
            
            geoCoder.setOnGetGeoCodeResultListener(object : OnGetGeoCoderResultListener {
                override fun onGetGeoCodeResult(result: GeoCodeResult?) {
                    if (result != null && result.error == 0) {
                        val latLng = result.location
                        trySend(Result.success(latLng.latitude to latLng.longitude))
                    } else {
                        trySend(Result.failure(Exception("地理编码失败")))
                    }
                    close()
                }
                
                override fun onGetReverseGeoCodeResult(result: Any?) {
                    // 不使用逆地理编码
                }
            })
            
            // 执行地理编码
            geoCoder.geoCodeLocation(address, "全国")
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close()
        }
        
        awaitClose {}
    }
    
    /**
     * 逆地理编码（坐标转地址）
     */
    fun reverseGeocode(latitude: Double, longitude: Double): Flow<Result<String>> = callbackFlow {
        try {
            val geoCoder = GeoCoder.newInstance()
            
            geoCoder.setOnGetReverseGeoCodeResultListener(object : OnGetGeoCoderResultListener {
                override fun onGetReverseGeoCodeResult(result: Any?) {
                    // 适配百度地图的逆地理编码结果
                    try {
                        if (result != null) {
                            // 使用反射获取 address 字段
                            val addressField = result.javaClass.getDeclaredMethod("getAddress")
                            val address = addressField.invoke(result) as? String
                            
                            if (!address.isNullOrBlank()) {
                                trySend(Result.success(address))
                            } else {
                                trySend(Result.failure(Exception("无法解析地址")))
                            }
                        } else {
                            trySend(Result.failure(Exception("逆地理编码结果为空")))
                        }
                    } catch (e: Exception) {
                        trySend(Result.failure(e))
                    } finally {
                        close()
                    }
                }
                
                override fun onGetGeoCodeResult(result: GeoCodeResult?) {
                    // 不使用
                }
            })
            
            val location = com.baidu.mapapi.model.LatLng(latitude, longitude)
            geoCoder.reverseGeoCode(ReverseGeoCodeOption().location(location))
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close()
        }
        
        awaitClose {}
    }
}
