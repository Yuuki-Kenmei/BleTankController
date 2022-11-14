/*
 * ConnectFragment
 *
 * 接続画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sys_ky.bletankcontroller.ble.*
import com.sys_ky.bletankcontroller.common.Constants
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.room.DbCtrl
import com.sys_ky.bletankcontroller.room.entity.BaseSetting
import com.sys_ky.bletankcontroller.room.entity.SavedConnection
import java.util.*

class ConnectFragment : Fragment() {

    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    lateinit var mHandler: Handler

    private val mBlePeripheralList: MutableList<BlePeripheral> = mutableListOf<BlePeripheral>()
    var mTryConnectFlg: Boolean = false

    lateinit var mDbCtrl: DbCtrl
    lateinit var mServiceUUID : UUID
    lateinit var mCharacteristicRxUUID : UUID
    lateinit var mCharacteristicTxUUID : UUID


    private val mBleScanDataViewModel by viewModels<BleScanDataViewModel> {
        BleScanListViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connect, container, false)

        val connectBackImageButton = view.findViewById<CustomImageButton>(R.id.connectBackImageButton)
        connectBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.closeConnectFragment()
            }
        })

        val bleScanAdapter = BleScanDataAdapter{ blePeripheral -> adapterOnClick(blePeripheral) }
        val recycleView = view.findViewById<RecyclerView>(R.id.connectRecyclerView)
        recycleView.adapter = bleScanAdapter

        mBleScanDataViewModel.getScanLiveData().observe(requireActivity()) {
            it?.let {
                bleScanAdapter.setScanList(it)
            }
        }

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.connectSwipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            if (!mTryConnectFlg) {
                startScan()
            }
        })

        mBluetoothLeScanner = MainActivity.gBluetoothAdapter?.bluetoothLeScanner
        mHandler = Handler(Looper.getMainLooper())

        mDbCtrl = DbCtrl(requireContext())
        val baseSettingList :List<BaseSetting> = mDbCtrl.BaseSetting().selectAll()
        baseSettingList.forEach {
            mServiceUUID = UUID.fromString(it.uuid_service)
            mCharacteristicRxUUID = UUID.fromString(it.uuid_characteristic_rx)
            mCharacteristicTxUUID = UUID.fromString(it.uuid_characteristic_tx)
        }

        val bleNotifyViewModel: BleNotifyViewModel? = ViewModelProvider(requireActivity(), BleNotifyViewModelFactory()).get(BleNotifyViewModel::class.java)
        bleNotifyViewModel!!.getBleNotifyLiveData().observe(requireActivity()) {
            it?.let {
                if (it.isNullOrEmpty()) {
                    return@let
                }
                Log.d("bleNotifyViewModel",it)

                MainActivity.closeNowLoadingFragment()

                if (it == Constants.cNotifyReady) {
                    Toast.makeText(requireContext(), R.string.connect_success, Toast.LENGTH_SHORT).show()

                    if (!MainActivity.gCurrentBlePeripheral!!.mSavedFlg) {
                        MainActivity.showConnectionEditFragment(null)
                    } else {
                        val dbCtrl = DbCtrl.getInstance(requireContext())
                        dbCtrl.SavedConnection().updateUuidByBdAddress(
                            MainActivity.gCurrentBlePeripheral!!.getAddress(),
                            MainActivity.gCurrentBlePeripheral!!.mServiceUUID.toString(),
                            MainActivity.gCurrentBlePeripheral!!.mCharacteristicTxUUID.toString(),
                            MainActivity.gCurrentBlePeripheral!!.mCharacteristicRxUUID.toString()
                        )

                        MainActivity.showControllerPadFragment(
                            MainActivity.gCurrentBlePeripheral!!.mLayoutFlg,
                            MainActivity.gCurrentBlePeripheral!!.mConnectionName + "（" + MainActivity.gCurrentBlePeripheral!!.getAddress() + "）"
                        )
                    }

//                    val data = "!B"+6+"1"
//                    val buffer = ByteBuffer.allocate(data.length).order(ByteOrder.LITTLE_ENDIAN)
//                    buffer.put(data.toByteArray())
//
//                    // Calculate checksum
//                    var checksum: Byte = 0
//                    var checksum2: Int = 0
//                    for (aData in buffer.array()) {
//                        checksum2 += aData
//                    }
//                    checksum = checksum2.inv().toByte()
//
//                    val dataCrc = ByteArray(buffer.array().size + 1)
//                    System.arraycopy(buffer.array(), 0, dataCrc, 0, buffer.array().size)
//                    dataCrc[buffer.array().size] = checksum
//
//                    MainActivity.gCurrentBlePeripheral?.writeCharacteristicTx(dataCrc)
//
//                    mHandler.postDelayed({
//                        val data2 = "!B"+6+"0"
//                        val buffer2 = ByteBuffer.allocate(data2.length).order(ByteOrder.LITTLE_ENDIAN)
//                        buffer2.put(data2.toByteArray())
//
//                        // Calculate checksum
//                        var checksum3: Byte = 0
//                        var checksum4: Int = 0
//                        for (aData in buffer2.array()) {
//                            checksum4 += aData
//                        }
//                        checksum3 = checksum4.inv().toByte()
//
//                        val dataCrc2 = ByteArray(buffer2.array().size + 1)
//                        System.arraycopy(buffer2.array(), 0, dataCrc2, 0, buffer2.array().size)
//                        dataCrc2[buffer2.array().size] = checksum3
//
//                        MainActivity.gCurrentBlePeripheral?.writeCharacteristicTx(dataCrc2)
//
//                        Log.d("","sendend")
//                    }, 3000)

                }
                if (it == Constants.cNotifyReadyNonRx) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.alert)
                        .setMessage(R.string.connect_error_rx_not_found)
                        .setPositiveButton(
                            R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                Toast.makeText(requireContext(), R.string.connect_success, Toast.LENGTH_SHORT).show()

                                if (!MainActivity.gCurrentBlePeripheral!!.mSavedFlg) {
                                    MainActivity.showConnectionEditFragment(null)
                                } else {
                                    val dbCtrl = DbCtrl.getInstance(requireContext())
                                    dbCtrl.SavedConnection().updateUuidByBdAddress(
                                        MainActivity.gCurrentBlePeripheral!!.getAddress(),
                                        MainActivity.gCurrentBlePeripheral!!.mServiceUUID.toString(),
                                        MainActivity.gCurrentBlePeripheral!!.mCharacteristicTxUUID.toString(),
                                        MainActivity.gCurrentBlePeripheral!!.mCharacteristicRxUUID.toString()
                                    )

                                    MainActivity.showControllerPadFragment(
                                        MainActivity.gCurrentBlePeripheral!!.mLayoutFlg,
                                        MainActivity.gCurrentBlePeripheral!!.mConnectionName + "（" + MainActivity.gCurrentBlePeripheral!!.getAddress() + "）"
                                    )
                                }
                            })
                        .setNegativeButton(
                            R.string.cancel,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                MainActivity.showUuidEditFragment()
                            })
                        .show()
                }
                if (it == Constants.cNotifyServiceNull) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error)
                        .setMessage(R.string.connect_error_service_not_found)
                        .setPositiveButton(
                            R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                MainActivity.showUuidEditFragment()
                            })
                        .setNegativeButton(
                            R.string.cancel,
                            null)
                        .show()
                }
                if (it == Constants.cNotifyCharacteristicNull) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error)
                        .setMessage(R.string.connect_error_characteristic_not_found)
                        .setPositiveButton(
                            R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                MainActivity.showUuidEditFragment()
                            })
                        .setNegativeButton(
                            R.string.cancel,
                            null)
                        .show()
                }
                if (it == Constants.cNotifyGattFailed) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error)
                        .setMessage(R.string.connect_error_gatt_not_found)
                        .setPositiveButton(
                            R.string.ok,
                            null)
                        .show()
                }
                if (it == Constants.cNotifyConnectFailed) {
                    if (mTryConnectFlg) {
                        AlertDialog.Builder(requireContext())
                            .setTitle(R.string.error)
                            .setMessage(R.string.connect_error_connect_failed)
                            .setPositiveButton(
                                R.string.ok,
                                null)
                            .show()
                    }
                }

                mTryConnectFlg = false

                Handler(Looper.getMainLooper()).post {
                    bleNotifyViewModel!!.clearBleNotify()
                }
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        Log.d("","start")

        //スキャン
        startScan()
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        mBluetoothLeScanner?.stopScan(mScanCallback)
        //requireView().findViewById<SwipeRefreshLayout>(R.id.connectSwipeRefreshLayout)?.isRefreshing = false

        super.onDestroy()
    }

    fun startScan() {

        if ((Build.VERSION.SDK_INT <= 30 && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) ||
            (Build.VERSION.SDK_INT <= 30 && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) ||
            (Build.VERSION.SDK_INT > 30 && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) ||
            (Build.VERSION.SDK_INT > 30 && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requireView().findViewById<SwipeRefreshLayout>(R.id.connectSwipeRefreshLayout)?.isRefreshing = false
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.error)
                .setMessage(R.string.connect_error_permission)
                .setPositiveButton(
                    R.string.ok,
                    null)
                .show()
            return
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            //.setReportDelay(500)
            .build()
        try {
            mHandler.removeCallbacksAndMessages(null)
            if (requireView().findViewById<SwipeRefreshLayout>(R.id.connectSwipeRefreshLayout)?.isRefreshing == true) {
                mBluetoothLeScanner?.stopScan(mScanCallback)
                requireView().findViewById<SwipeRefreshLayout>(R.id.connectSwipeRefreshLayout)?.isRefreshing = false
            }
            mHandler.postDelayed({
                mBluetoothLeScanner?.stopScan(mScanCallback)
                requireView().findViewById<SwipeRefreshLayout>(R.id.connectSwipeRefreshLayout)?.isRefreshing = false
            }, 5000)
            mBlePeripheralList.clear()
            mBleScanDataViewModel.clearBlePeripheralList()
            mBluetoothLeScanner?.startScan(null, settings, mScanCallback)
            requireView().findViewById<SwipeRefreshLayout>(R.id.connectSwipeRefreshLayout)?.isRefreshing = true
        } catch (e: IllegalStateException) {

        }
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            makeScanList(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            makeScanList(results)
        }

        override fun onScanFailed(errorCode: Int) {
        }
    }

    private fun makeScanList(result: ScanResult) {
        makeScanListSub(result)
    }
    private fun makeScanList(results: List<ScanResult>) {
        results.forEach { result ->
            makeScanListSub(result)
        }
    }
    @SuppressLint("MissingPermission")
    private fun makeScanListSub(result: ScanResult) {
        if (result.device == null) {
            return
        } else {
            var count: Int = 0
            var index: Int = -1
            //val nowBleScanList = mBleScanDataViewModel.getScanInfoList()
            //nowBleScanList.forEach {
            mBlePeripheralList.forEach { blePeripheral ->
                if (blePeripheral.getAddress() == result.device.address) {
                    return
                }
                if (index < 0) {
                    var name = result.device.name
                    if (name.isNullOrEmpty()) {
                        name = result.scanRecord?.deviceName
                    }
                    if (!name.isNullOrEmpty()) {
                        if (blePeripheral.getName().isNullOrEmpty()) {
                            index = count
                        } else if (blePeripheral.getName() >= name) {
                            index = count
                        }
                    } else if (blePeripheral.getAddress() >= result.device.address) {
                        if (blePeripheral.getName().isNullOrEmpty()) {
                            index = count
                        }
                    }
                }
                count++
            }
            if (index < 0) {
                //index = nowBleScanList.size
                index = mBlePeripheralList.size
            }
            val blePeripheral = BlePeripheral(result)

            val savedConnectionList: List<SavedConnection> = mDbCtrl.SavedConnection().selectByBdAddress(blePeripheral.getAddress())
            savedConnectionList.forEach {
                blePeripheral.mConnectionName = it.connection_name
                blePeripheral.mImagePath = it.image_path
                blePeripheral.mLayoutFlg = it.layout_id
                if (it.uuid_service != "") {
                    blePeripheral.mServiceUUID = UUID.fromString(it.uuid_service)
                }
                if (it.uuid_characteristic_rx != "") {
                    blePeripheral.mCharacteristicRxUUID = UUID.fromString(it.uuid_characteristic_rx)
                }
                if (it.uuid_characteristic_tx != "") {
                    blePeripheral.mCharacteristicTxUUID = UUID.fromString(it.uuid_characteristic_tx)
                }
                blePeripheral.mSavedFlg = true
            }
            if (blePeripheral.mServiceUUID == null) {
                blePeripheral.mServiceUUID = mServiceUUID
            }
            if (blePeripheral.mCharacteristicRxUUID == null) {
                blePeripheral.mCharacteristicRxUUID = mCharacteristicRxUUID
            }
            if (blePeripheral.mCharacteristicTxUUID == null) {
                blePeripheral.mCharacteristicTxUUID = mCharacteristicTxUUID
            }
            mBlePeripheralList.add(index, blePeripheral)
            mBleScanDataViewModel.setBlePeripheralList(blePeripheral, index)
            return
        }
    }

    private fun adapterOnClick(blePeripheral: BlePeripheral) {
        if (Build.VERSION.SDK_INT > 30 && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.error)
                .setMessage(R.string.connect_error_permission)
                .setPositiveButton(
                    R.string.ok,
                    null)
                .show()
            return
        }
        if (!mTryConnectFlg) {
            mTryConnectFlg = true
            Log.d("connectStart","")
            MainActivity.gCurrentBlePeripheral = blePeripheral
            blePeripheral.connectGatt(requireContext())
            MainActivity.showNowLoadingFragment()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ConnectFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}