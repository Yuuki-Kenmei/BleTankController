/*
 * BleNotifyViewModel
 *
 * 通知用ビューモデルクラス
 * BLE接続結果または接続先からのデータ受信で使用
 */

package com.sys_ky.bletankcontroller.ble

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BleNotifyViewModel(val bleNotifyDataSource: BleNotifyDataSource): ViewModel() {

    fun setBleNotify(newStr: String) {
        return bleNotifyDataSource.setNotifyLiveData(newStr)
    }

    fun clearBleNotify() {
        return bleNotifyDataSource.clearNotifyLiveData()
    }

    fun getBleNotify(): String {
        var rtnStr = ""
        if (bleNotifyDataSource.getNotifyLiveData().value != null) {
            rtnStr = bleNotifyDataSource.getNotifyLiveData().value!!
        }
        return rtnStr
    }

    fun getBleNotifyLiveData(): LiveData<String> {
        return bleNotifyDataSource.getNotifyLiveData()
    }
}

class BleNotifyViewModelFactory() : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BleNotifyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BleNotifyViewModel(
                bleNotifyDataSource = BleNotifyDataSource.getDataSource()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}