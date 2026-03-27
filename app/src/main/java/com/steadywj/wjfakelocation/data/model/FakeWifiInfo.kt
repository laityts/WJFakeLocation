// FakeWifiInfo.kt
package com.steadywj.wjfakelocation.data.model

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 假 WiFi 信息数据模型
 */
@Parcelize
data class FakeWifiInfo(
    val id: Long = System.currentTimeMillis(),
    val ssid: String, // WiFi 名称
    val bssid: String? = null, // MAC 地址（可选，自动生成）
    val securityType: SecurityType, // 加密类型
    val band: WifiBand, // 频段
    val signalStrength: Int? = null, // 信号强度 dBm（-30 到 -100）
    val enabled: Boolean = true
) : Parcelable {
    
    companion object {
        /**
         * 获取预设的假 WiFi 列表
         */
        fun getFakeWifiList(): List<FakeWifiInfo> {
            return listOf(
                FakeWifiInfo(
                    ssid = "ChinaMobile-5G",
                    securityType = SecurityType.WPA2_PSK,
                    band = WifiBand.GHZ_5,
                    signalStrength = -45,
                    enabled = true
                ),
                FakeWifiInfo(
                    ssid = "ChinaUnicom-Guest",
                    securityType = SecurityType.WPA2_WPA3,
                    band = WifiBand.GHZ_5,
                    signalStrength = -50,
                    enabled = true
                ),
                FakeWifiInfo(
                    ssid = "Tencent-WeChat",
                    securityType = SecurityType.WPA3_SAE,
                    band = WifiBand.GHZ_6, // WiFi 6E
                    signalStrength = -55,
                    enabled = true
                ),
                FakeWifiInfo(
                    ssid = "Starbucks-Coffee",
                    securityType = SecurityType.OPEN,
                    band = WifiBand.GHZ_2,
                    signalStrength = -70,
                    enabled = true
                ),
                FakeWifiInfo(
                    ssid = "TP-LINK_Home",
                    securityType = SecurityType.WPA2_PSK,
                    band = WifiBand.GHZ_2,
                    signalStrength = -60,
                    enabled = true
                )
            )
        }
        
        /**
         * 获取主连接 WiFi
         */
        fun getPrimaryWifi(): FakeWifiInfo {
            return getFakeWifiList().firstOrNull { it.enabled } ?: getFakeWifiList().first()
        }
    }
}

/**
 * WiFi 安全类型
 */
enum class SecurityType(val displayName: String) {
    OPEN("无加密"),
    WEP("WEP"),
    WPA_PSK("WPA-Personal"),
    WPA2_PSK("WPA2-Personal"),
    WPA3_SAE("WPA3-Personal"),
    WPA2_WPA3("WPA2/WPA3混合")
}

/**
 * WiFi 频段
 */
enum class WifiBand(val displayName: String, val description: String) {
    GHZ_2("2.4 GHz", "覆盖广，穿透强"),
    GHZ_5("5 GHz", "速度快，干扰少"),
    GHZ_6("6 GHz", "WiFi 6E，超高速")
}
