/*
 * BlePeripheral
 *
 * BLEペリフェラルの情報、GATT保持用クラス
 * 接続、送信、受信もここで行う
 */

package com.sys_ky.bletankcontroller.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.sys_ky.bletankcontroller.common.Constants
import java.util.*

class BlePeripheral constructor(scanResult: ScanResult?) {

    private var mScanResult: ScanResult? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothGattCharacteristicRx: BluetoothGattCharacteristic? = null
    private var mBluetoothGattCharacteristicTx: BluetoothGattCharacteristic? = null

    private var mConnectionState: Int =  BluetoothProfile.STATE_DISCONNECTED

    var mServiceUUID : UUID? = null
    var mCharacteristicRxUUID : UUID? = null
    var mCharacteristicTxUUID : UUID? = null

    private var mBleNotifyViewModel: BleNotifyViewModel = BleNotifyViewModelFactory().create(BleNotifyViewModel::class.java)

    var mConnectionName: String = ""
    var mImagePath: String? = null
    var mSavedFlg: Boolean = false

    var mLayoutFlg: Int = 0

    init {
        mScanResult = scanResult
    }

    private val mBluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGatt()
                //接続失敗
                setBleNotify(Constants.cNotifyConnectFailed)
            }
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            super.onServiceChanged(gatt)
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service: BluetoothGattService? = gatt?.getService(mServiceUUID)
                if (service != null) {
                    mBluetoothGattCharacteristicTx = service.getCharacteristic(mCharacteristicTxUUID)
                    if (mBluetoothGattCharacteristicTx != null) {
                        mBluetoothGatt = gatt

                        mBluetoothGattCharacteristicRx = service.getCharacteristic(mCharacteristicRxUUID)
                        if (mBluetoothGattCharacteristicRx != null) {
                            mBluetoothGatt?.setCharacteristicNotification(mBluetoothGattCharacteristicRx, true)

//                            val descriptor = mBluetoothGattCharacteristicRx?.getDescriptor(mCharacteristicRxUUID)
//                            descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                            mBluetoothGatt?.writeDescriptor(descriptor)
                        }

                        mConnectionState = BluetoothProfile.STATE_CONNECTED
                        if (mBluetoothGattCharacteristicRx != null) {
                            //すべて正常に接続
                            setBleNotify(Constants.cNotifyReady)
                        } else {
                            //rx以外は正常に接続
                            setBleNotify(Constants.cNotifyReadyNonRx)
                        }

                    } else {
                        //キャラクタリスティックUUIDが間違っている
                        setBleNotify(Constants.cNotifyCharacteristicNull)
                    }
                } else {
                    //サービスUUIDが間違っている
                    setBleNotify(Constants.cNotifyServiceNull)
                }
            } else {
                //GATTサーバに繋がらない
                setBleNotify(Constants.cNotifyGattFailed)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)

            if (characteristic?.value != null) {
                val str = String(characteristic.value)
                setBleNotify(str)
            }
        }
    }

    private fun setBleNotify(str: String) {
        Handler(Looper.getMainLooper()).post {
            mBleNotifyViewModel.setBleNotify(str)
        }
    }

    @SuppressLint("MissingPermission")
    fun connectGatt(context: Context) {
        mBluetoothGatt = mScanResult?.device?.connectGatt(context, false, mBluetoothGattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnectGatt() {
        mBluetoothGatt?.disconnect()
        mBluetoothGatt?.close()
        mBluetoothGatt = null
        mBluetoothGattCharacteristicTx = null
        mBluetoothGattCharacteristicRx = null
        mConnectionState = BluetoothProfile.STATE_DISCONNECTED
    }

    fun getDevice(): BluetoothDevice? {
        return mScanResult?.device
    }

    fun getRssi(): Int {
        var rtnValue: Int = -100
        if (mScanResult != null) {
            rtnValue = mScanResult!!.rssi
        }
        return rtnValue
    }

    @SuppressLint("MissingPermission")
    fun getName(): String {
        var rtnValue: String? = if (mScanResult?.device?.name.isNullOrEmpty()) {
            mScanResult?.scanRecord?.deviceName
        } else {
            mScanResult?.device?.name
        }

        if (rtnValue == null) {
            rtnValue = ""
        }
        return rtnValue.toString()
    }

    fun getAddress(): String {
        var rtnValue: String? = ""
        if (!mScanResult?.device?.address.isNullOrEmpty()) {
            rtnValue = mScanResult?.device?.address
        }
        return rtnValue.toString()
    }

    fun getConnectionState(): Int {
        return mConnectionState
    }

    fun isConnected(): Boolean {
        return mConnectionState == BluetoothProfile.STATE_CONNECTED
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristicTx(send: ByteArray) {
        if (mBluetoothGattCharacteristicTx != null) {
            mBluetoothGattCharacteristicTx?.setValue(send)
            mBluetoothGatt?.writeCharacteristic(mBluetoothGattCharacteristicTx)
        }
    }
}