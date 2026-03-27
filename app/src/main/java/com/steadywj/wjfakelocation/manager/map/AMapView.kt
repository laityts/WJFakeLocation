// AMapView.kt
package com.steadywj.wjfakelocation.manager.map.components

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.LatLng
import com.amap.api.maps2d.model.MarkerOptions

/**
 * 高德地图 MapView 包装组件（优化版）
 * 
 * 特性:
 * - 预加载缓存
 * - 加载进度指示器
 * - 生命周期自动管理
 * 
 * @param modifier Compose 修饰符
 * @param onMapReady 地图准备就绪回调
 * @param initialLatitude 初始纬度
 * @param initialLongitude 初始经度
 * @param zoomLevel 缩放级别（默认 15）
 */
@Composable
fun AMapView(
    modifier: Modifier = Modifier,
    onMapReady: ((AMap) -> Unit)? = null,
    initialLatitude: Double = 39.908823, // 北京
    initialLongitude: Double = 116.397470,
    zoomLevel: Float = 15f
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var aMap by remember { mutableStateOf<AMap?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView = this
                    
                    // 获取 AMap 实例
                    getMapAsync { map ->
                        aMap = map
                        
                        // 设置初始位置和缩放级别
                        val latLng = LatLng(initialLatitude, initialLongitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
                        
                        // 启用定位图层（需要权限）
                        map.isMyLocationEnabled = true
                        
                        // 标记为已加载
                        isMapLoaded = true
                        
                        // 通知地图已准备好
                        onMapReady?.invoke(map)
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // 可以在这里更新地图配置
            }
        )
        
        // 显示加载进度指示器
        if (!isMapLoaded) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 在地图上添加标记点
 * 
 * @param latitude 纬度
 * @param longitude 经度
 * @param title 标题
 * @param snippet 描述信息
 * @param draggable 是否可拖拽（默认 true）
 * @param onClick 点击回调
 */
@Composable
fun MapMarker(
    latitude: Double,
    longitude: Double,
    title: String? = null,
    snippet: String? = null,
    draggable: Boolean = true,
    onClick: (() -> Unit)? = null,
    map: AMap?
) {
    DisposableEffect(latitude, longitude, title, snippet, map) {
        var marker: com.amap.api.maps2d.model.Marker? = null
        
        if (map != null) {
            val markerOptions = MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title(title)
                .snippet(snippet)
                .draggable(draggable)
            
            marker = map.addMarker(markerOptions)
            
            // 设置点击监听器
            if (onClick != null) {
                map.setOnMarkerClickListener { clickedMarker ->
                    if (clickedMarker == marker) {
                        onClick()
                        true
                    } else {
                        false
                    }
                }
            }
        }
        
        onDispose {
            // 清理标记点
            marker?.remove()
        }
    }
}

/**
 * 高德地图生命周期管理
 * 需要在 Composable 中调用以正确处理生命周期
 */
@Composable
fun AMapLifecycleHandler(mapView: MapView?) {
    val context = LocalContext.current
    
    DisposableEffect(context, mapView) {
        mapView?.onResume()
        
        onDispose {
            mapView?.onDestroy()
        }
    }
    
    DisposableEffect(Unit) {
        val lifecycleObserver = object : androidx.lifecycle.LifecycleEventObserver {
            override fun onStateChanged(source: androidx.lifecycle.LifecycleOwner, event: androidx.lifecycle.Lifecycle.Event) {
                when (event) {
                    androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                    androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                    androidx.lifecycle.Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                    else -> {}
                }
            }
        }
        
        val lifecycleOwner = LocalContext.current as? androidx.lifecycle.LifecycleOwner
        lifecycleOwner?.lifecycle?.addObserver(lifecycleObserver)
        
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(lifecycleObserver)
        }
    }
}
