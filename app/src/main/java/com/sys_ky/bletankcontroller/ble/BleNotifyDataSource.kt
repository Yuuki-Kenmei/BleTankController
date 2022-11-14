/*
 * BleNotifyDataSource
 *
 * 通知用ビューモデルのデータソースクラス
 * BLE接続結果または接続先からのデータ受信で使用
 */

package com.sys_ky.bletankcontroller.ble

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BleNotifyDataSource {
    private val notifyLiveData = MutableLiveData<String>()

    fun setNotifyLiveData(newStr: String) {
        notifyLiveData.value = newStr
    }

    fun clearNotifyLiveData() {
        notifyLiveData.value = ""
    }

    fun getNotifyLiveData(): LiveData<String> {
        return notifyLiveData
    }

    companion object {
        private var INSTANCE: BleNotifyDataSource? = null

        fun getDataSource(): BleNotifyDataSource {
            return synchronized(BleNotifyDataSource::class.java) {
                val newInstance = INSTANCE ?: BleNotifyDataSource()
                INSTANCE = newInstance
                newInstance
            }
        }
    }

}