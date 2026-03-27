// MainActivity.kt
package com.steadywj.wjfakelocation.manager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.steadywj.wjfakelocation.manager.ErrorScreen
import com.steadywj.wjfakelocation.manager.navigation.AppNavGraph
import com.steadywj.wjfakelocation.manager.theme.WJFakeLocationTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private var isXposedModuleEnabled by mutableStateOf(true)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d(TAG, "${it.key} = ${it.value}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查 Xposed 模块是否激活
        checkXposedModuleStatus()
        
        // 请求必要权限
        requestRequiredPermissions()

        enableEdgeToEdge()
        
        setContent {
            WJFakeLocationTheme {
                if (isXposedModuleEnabled) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                } else {
                    ErrorScreen(
                        onDismiss = { finish() },
                        onConfirm = { finish() }
                    )
                }
            }
        }
    }
    
    private fun checkXposedModuleStatus() {
        // 如果模块未激活，app 将无法使用 MODE_WORLD_READABLE
        try {
            getSharedPreferences("xposed_shared_prefs", MODE_WORLD_READABLE)
            isXposedModuleEnabled = true
        } catch (e: SecurityException) {
            isXposedModuleEnabled = false
            Log.e(TAG, "SecurityException: Xposed 模块可能未激活 - ${e.message}", e)
        } catch (e: Exception) {
            isXposedModuleEnabled = false
            Log.e(TAG, "Exception: ${e.message}", e)
        }
    }
    
    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()
        
        // 位置权限
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED -> {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
        
        // Android 13+ 通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED -> {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
