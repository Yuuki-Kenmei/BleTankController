/*
 * Constants
 *
 * 定数格納用クラス
 */

package com.sys_ky.bletankcontroller.common

import android.graphics.Color

class Constants {
    companion object {
        const val cDefaultUUIDService: String = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
        const val cDefaultUUIDCharacteristicTX: String = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
        const val cDefaultUUIDCharacteristicRX: String = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
        const val cLocateModeGrid: Int = 1
        const val cLocateModeFree: Int = 2
        const val cViewTypeButton: Int = 1
        const val cViewTypeStick: Int = 2
        const val cViewTypeWeb: Int = 3
        val cDefaultButtonBackColor = Color.rgb(89, 89, 89)
        val cDefaultButtonTextColor = Color.rgb(255, 0, 0)
        const val cNotifyReady = "READY"
        const val cNotifyReadyNonRx = "READY_NON_RX"
        const val cNotifyServiceNull = "SERVICE_NULL"
        const val cNotifyCharacteristicNull = "CHARACTERISTIC_NULL"
        const val cNotifyGattFailed = "GATT_FAILED"
        const val cNotifyConnectFailed = "CONNECT_FAILED"
        val cRegex = Regex("[0-9a-fA-F]{8}-[0-9a-fA-f]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
    }
}