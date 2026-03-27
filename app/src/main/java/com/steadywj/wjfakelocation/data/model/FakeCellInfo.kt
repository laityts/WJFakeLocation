// FakeCellInfo.kt
package com.steadywj.wjfakelocation.data.model

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 假基站信息数据模型
 */
@Parcelize
data class FakeCellInfo(
    val id: Long = System.currentTimeMillis(),
    val name: String, // 基站名称
    val type: CellType, // 网络类型
    val mcc: String? = "460", // 移动国家代码（中国）
    val mnc: String? = "0", // 移动网络代码
    val lac: Int? = null, // 位置区码 (GSM/WCDMA/LTE)
    val cid: Int? = null, // 小区 ID
    val ci: Int? = null, // 小区标识 (LTE)
    val tac: Int? = null, // 跟踪区码 (LTE/NR)
    val psc: Int? = null, // 主扰码 (WCDMA)
    val cpid: Int? = null, // 小区物理 ID (TDSCDMA)
    val nci: Int? = null, // NR 小区 ID
    val basestationId: Int? = null, // 基站 ID (CDMA)
    val latitude: Double? = null, // 纬度 (CDMA)
    val longitude: Double? = null, // 经度 (CDMA)
    val enabled: Boolean = true
) : Parcelable {
    
    companion object {
        /**
         * 获取预设的假基站列表（北京地区示例）
         */
        fun getFakeCells(): List<FakeCellInfo> {
            return listOf(
                FakeCellInfo(
                    name = "中国移动 - 北京朝阳",
                    type = CellType.LTE,
                    mcc = "460",
                    mnc = "0",
                    lac = 12345,
                    cid = 67890,
                    ci = 1001,
                    tac = 5678,
                    enabled = true
                ),
                FakeCellInfo(
                    name = "中国联通 - 北京海淀",
                    type = CellType.WCDMA,
                    mcc = "460",
                    mnc = "1",
                    lac = 23456,
                    cid = 78901,
                    psc = 2002,
                    enabled = true
                ),
                FakeCellInfo(
                    name = "中国电信 - 北京东城",
                    type = CellType.NR, // 5G
                    mcc = "460",
                    mnc = "3",
                    nci = 3003,
                    tac = 6789,
                    enabled = true
                )
            )
        }
        
        /**
         * 获取主服务小区
         */
        fun getPrimaryCell(): FakeCellInfo {
            return getFakeCells().firstOrNull { it.enabled } ?: getFakeCells().first()
        }
    }
}

/**
 * 网络类型枚举
 */
enum class CellType(val displayName: String) {
    GSM("2G GSM"),
    CDMA("2G CDMA"),
    WCDMA("3G WCDMA"),
    TDSCDMA("3G TD-SCDMA"),
    LTE("4G LTE"),
    NR("5G NR")
}
