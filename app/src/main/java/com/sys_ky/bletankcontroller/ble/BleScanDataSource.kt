/*
 * BleScanDataSource
 *
 * BLE接続先一覧表示用のビューモデルのデータソースクラス
 */

package com.sys_ky.bletankcontroller.ble

import androidx.lifecycle.MutableLiveData

class BleScanDataSource {
    private val scanLiveData = MutableLiveData<List<BlePeripheral>>()

    fun setScanLiveData(newItem: BlePeripheral, index: Int) {
        var newList = scanLiveData.value?.toMutableList()
        newList?.add(index, newItem)
        scanLiveData.value = newList!!
    }

    fun clearScanLiveData() {
        scanLiveData.value = listOf<BlePeripheral>()
    }

    fun getScanLiveData(): MutableLiveData<List<BlePeripheral>> {
        return scanLiveData
    }

    companion object {
        private var INSTANCE: BleScanDataSource? = null

        fun getDataSource(): BleScanDataSource {
            return synchronized(BleScanDataSource::class.java) {
                val newInstance = INSTANCE ?: BleScanDataSource()
                INSTANCE = newInstance
                newInstance
            }
        }
    }
}