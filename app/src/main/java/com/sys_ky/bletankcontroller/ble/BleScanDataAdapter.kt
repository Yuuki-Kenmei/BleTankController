/*
 * BleScanDataAdapter
 *
 * BLE接続先一覧表示用のアダプタークラス
 */

package com.sys_ky.bletankcontroller.ble

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sys_ky.bletankcontroller.R
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class BleScanDataAdapter(private val onClick: (BlePeripheral) -> Unit) : RecyclerView.Adapter<BleScanDataAdapter.BleScanDataViewHolder>() {
    private var mRecyclerView: RecyclerView? = null
    private var mScanList: List<BlePeripheral>? = null

    class BleScanDataViewHolder(view: View, val onClick: (BlePeripheral) -> Unit): RecyclerView.ViewHolder(view) {
        private val connectListItemNameTextView = view.findViewById<TextView>(R.id.connectListItemNameTextView)
        private val connectListItemAddressTextView = view.findViewById<TextView>(R.id.connectListItemAddressTextView)
        private val connectListItemRSSIImageView = view.findViewById<ImageView>(R.id.connectListItemRSSIImageView)
        private val connectListItemImageImageView = view.findViewById<ImageView>(R.id.connectListItemImageImageView)
        private val connectListItemConnectImageButton = view.findViewById<ImageView>(R.id.connectListItemConnectImageButton)
        private var mBlePeripheral: BlePeripheral? = null

        init {
            connectListItemConnectImageButton.setOnClickListener {
                mBlePeripheral?.let {
                    onClick(it)
                }
            }
        }

        @SuppressLint("MissingPermission")
        fun bind(blePeripheral: BlePeripheral) {
            mBlePeripheral = blePeripheral

            //名前が取れるなら名前とアドレス、取れないなら名前の場所にアドレス表示だけ
            //名前は既に接続名があれば接続名（名前）で表示
            var connectionName = blePeripheral.mConnectionName
            var name = blePeripheral.getName()
            var address = blePeripheral.getAddress()
            var displayName = ""
            var displayAddress = ""
            if (!connectionName.isNullOrEmpty()) {
                if (!name.isNullOrEmpty() && connectionName != name) {
                    //接続名がある、かつデバイス名がある、かつそれらが異なる場合、接続先名（デバイス名）と（アドレス）で表示
                    displayName = "$connectionName  ($name)"
                    displayAddress = "($address)"
                } else {
                    //接続名がある、かつデバイス名がある、かつそれらが同じ場合、接続先名と（アドレス）で表示
                    displayName = connectionName
                    displayAddress = "($address)"
                }
            } else {
                if (!name.isNullOrEmpty()) {
                    //接続名がない、かつデバイス名がある場合、デバイス名と（アドレス）で表示
                    displayName = name
                    displayAddress = "($address)"
                } else {
                    //接続名がない、かつデバイス名がない場合、アドレスだけ表示
                    displayName = address
                    displayAddress = ""
                }
            }

            connectListItemNameTextView.text = displayName
            connectListItemAddressTextView.text = displayAddress

            //RSSIで適度にアンテナ画像表示
            if (blePeripheral.getRssi() < -72 || blePeripheral.getRssi() == 127) {
                connectListItemRSSIImageView.setImageResource(R.drawable.img_rssi0)
            } else if (blePeripheral.getRssi() < -60) {
                connectListItemRSSIImageView.setImageResource(R.drawable.img_rssi1)
            } else if (blePeripheral.getRssi() < -48) {
                connectListItemRSSIImageView.setImageResource(R.drawable.img_rssi2)
            } else {
                connectListItemRSSIImageView.setImageResource(R.drawable.img_rssi3)
            }
            connectListItemImageImageView.setImageBitmap(null)

            //接続先に紐づけられたイメージをパスから取得して表示
            try {
                val path = blePeripheral.mImagePath
                if (path != null) {
                    val file = File(path)
                    val inputStream = FileInputStream(file)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    connectListItemImageImageView.setImageBitmap(bitmap)
                }
            } catch (e: IOException) {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleScanDataViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.connect_list_item, parent, false)
        return BleScanDataViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: BleScanDataViewHolder, position: Int) {
        if (mScanList != null) {
            val scan = mScanList!![position]
            holder.bind(scan)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    fun setScanList(scanList: List<BlePeripheral>) {
        var diffResult: DiffUtil.DiffResult? = null
        if (mScanList == null) {
            mScanList = scanList
            notifyItemRangeInserted(0, scanList.size)
        } else {
            var callback = ScanDiffCallback(mScanList!!, scanList)
            diffResult = DiffUtil.calculateDiff(callback)
            mScanList = scanList
        }

        val layoutManager: RecyclerView.LayoutManager? = mRecyclerView?.getLayoutManager()
        if (layoutManager != null) {
            val recyclerViewState = layoutManager.onSaveInstanceState()
            diffResult?.dispatchUpdatesTo(this)
            layoutManager.onRestoreInstanceState(recyclerViewState)
        }
    }

    override fun getItemCount(): Int {
        var rtn = 0
        if (mScanList !=  null) {
            rtn = mScanList!!.size
        }
        return rtn
    }
}

class ScanDiffCallback constructor(old:List<BlePeripheral>, new:List<BlePeripheral>) : DiffUtil.Callback() {
    private val oldList: List<BlePeripheral>
    private val newList: List<BlePeripheral>

    init {
        oldList = old
        newList = new
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItemAddress: String = oldList[oldItemPosition].getAddress()
        val newItemAddress: String = newList[newItemPosition].getAddress()

        return oldItemAddress == newItemAddress
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].equals(newList[newItemPosition])
    }
}