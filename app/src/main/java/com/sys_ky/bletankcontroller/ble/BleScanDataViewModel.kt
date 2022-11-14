/*
 * BleScanDataViewModel
 *
 * BLE接続先一覧表示用のビューモデルのデータソースクラス
 */

package com.sys_ky.bletankcontroller.ble

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BleScanDataViewModel(val bleScanDataSource: BleScanDataSource): ViewModel() {

    fun setBlePeripheralList(newItem: BlePeripheral, index: Int) {
        return bleScanDataSource.setScanLiveData(newItem, index)
    }

    fun clearBlePeripheralList() {
        return bleScanDataSource.clearScanLiveData()
    }

    fun getBlePeripheralList(): List<BlePeripheral> {
        var rtnList = listOf<BlePeripheral>()
        if (bleScanDataSource.getScanLiveData().value != null) {
            rtnList = bleScanDataSource.getScanLiveData().value!!
        }
        return rtnList
    }

    fun getScanLiveData(): LiveData<List<BlePeripheral>> {
        return bleScanDataSource.getScanLiveData()
    }
}

class BleScanListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BleScanDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BleScanDataViewModel(
                bleScanDataSource = BleScanDataSource.getDataSource()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}