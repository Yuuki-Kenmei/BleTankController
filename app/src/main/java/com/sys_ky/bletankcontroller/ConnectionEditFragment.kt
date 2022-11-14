/*
 * ConnectionEditFragment
 *
 * 接続先設定編集画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.sys_ky.bletankcontroller.common.Constants
import com.sys_ky.bletankcontroller.listadapter.ControllerSelectListAdapter
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.room.DbCtrl
import com.sys_ky.bletankcontroller.room.entity.ControllerLayout
import com.sys_ky.bletankcontroller.room.entity.SavedConnection
import java.io.File
import java.io.FileInputStream
import java.io.IOException

private const val ARG_PARAM1 = "param1"

class ConnectionEditFragment : Fragment() {
    private var param1: String? = null

    var mConnectFlg: Boolean = false
    var mBdAddress: String = ""
    var mControllerLayoutId: Int = 0
    var mImagePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connection_edit, container, false)

        val connectionEditImageView = view.findViewById<ImageView>(R.id.connectionEditImageView)

        val connectionEditSelectImageButton = view.findViewById<CustomImageButton>(R.id.connectionEditSelectImageButton)
        connectionEditSelectImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "image/*"
                }
                imageLauncher.launch(intent)
            }
        })

        val connectionEditClearImageButton = view.findViewById<CustomImageButton>(R.id.connectionEditClearImageButton)
        connectionEditClearImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                mImagePath = ""
                connectionEditImageView.setImageBitmap(null)
            }
        })

        val connectionEditNameEditText = view.findViewById<EditText>(R.id.connectionEditNameEditText)
        val connectionEditServiceEditText = view.findViewById<EditText>(R.id.connectionEditServiceEditText)
        val connectionEditTxEditText = view.findViewById<EditText>(R.id.connectionEditTxEditText)
        val connectionEditRxEditText = view.findViewById<EditText>(R.id.connectionEditRxEditText)

        val connectionEditServiceImageButton = view.findViewById<CustomImageButton>(R.id.connectionEditServiceImageButton)
        connectionEditServiceImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.info)
                    .setMessage(R.string.base_setting_service_help)
                    .setPositiveButton(
                        R.string.ok,
                        null)
                    .show()
            }
        })

        val connectionEditTxImageButton = view.findViewById<CustomImageButton>(R.id.connectionEditTxImageButton)
        connectionEditTxImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.info)
                    .setMessage(R.string.base_setting_tx_help)
                    .setPositiveButton(
                        R.string.ok,
                        null)
                    .show()
            }
        })

        val connectionEditRxImageButton = view.findViewById<CustomImageButton>(R.id.connectionEditRxImageButton)
        connectionEditRxImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.info)
                    .setMessage(R.string.base_setting_rx_help)
                    .setPositiveButton(
                        R.string.ok,
                        null)
                    .show()
            }
        })

        val connectionEditBackImageButton = view.findViewById<CustomImageButton>(R.id.connectionEditBackImageButton)
        connectionEditBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.connection_edit_confirm_back)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            if (!mConnectFlg) {
                                if (MainActivity.gCurrentBlePeripheral?.isConnected() == true) {
                                    MainActivity.gCurrentBlePeripheral?.disconnectGatt()
                                }
                                MainActivity.gCurrentBlePeripheral = null
                            }
                            MainActivity.closeConnectionEditFragment(!mConnectFlg)
                        })
                    .setNegativeButton(
                        R.string.cancel,
                        null
                    )
                    .show()
            }
        })

        val connectionEditFixImageButton = view.findViewById<CustomImageButton>(R.id.connectionEditFixImageButton)
        connectionEditFixImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                var errFlg = false

                if (connectionEditNameEditText.text.toString().isNullOrEmpty()) {
                    errFlg = true
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error)
                        .setMessage(R.string.error_uuid)
                        .setPositiveButton(
                            R.string.ok,
                            null)
                        .show()
                }

                if (!connectionEditServiceEditText.text.toString().matches(Constants.cRegex) || !connectionEditTxEditText.text.toString().matches(Constants.cRegex) || !connectionEditRxEditText.text.toString().matches(Constants.cRegex)) {
                    errFlg = true
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error)
                        .setMessage(R.string.connection_edit_error_noname)
                        .setPositiveButton(
                            R.string.ok,
                            null)
                        .show()
                }

                if (!errFlg) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.connection_edit_confirm_fix)
                        .setPositiveButton(
                            R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                val dbCtrl = DbCtrl.getInstance(requireContext())
                                val savedConnection: SavedConnection = SavedConnection(
                                    mBdAddress,
                                    requireView().findViewById<EditText>(R.id.connectionEditNameEditText).text.toString(),
                                    mImagePath,
                                    mControllerLayoutId,
                                    requireView().findViewById<EditText>(R.id.connectionEditServiceEditText).text.toString(),
                                    requireView().findViewById<EditText>(R.id.connectionEditTxEditText).text.toString(),
                                    requireView().findViewById<EditText>(R.id.connectionEditRxEditText).text.toString()
                                )
                                dbCtrl.SavedConnection().insertAll(savedConnection)
                                if (mConnectFlg) {
                                    MainActivity.closeConnectionEditFragment(!mConnectFlg)
                                    MainActivity.showControllerPadFragment(
                                        mControllerLayoutId,
                                        requireView().findViewById<EditText>(R.id.connectionEditNameEditText).text.toString() + "（" + mBdAddress + "）"
                                    )
                                } else {
                                    Toast.makeText(requireContext(), R.string.update_normal, Toast.LENGTH_SHORT).show()
                                    MainActivity.closeConnectionEditFragment(!mConnectFlg)
                                }
                            })
                        .setNegativeButton(
                            R.string.cancel,
                            null
                        )
                        .show()
//                val dbCtrl = DbCtrl.getInstance(requireContext())
//                dbCtrl.BASE_SETTING().insertAll(BaseSetting(0, baseServiceEditText.text.toString(), baseTxEditText.text.toString(), baseRxEditText.text.toString()))
//                Toast.makeText(requireContext(), getString(R.string.update_normal), Toast.LENGTH_SHORT).show()
//                MainActivity.closeBaseSettingFragment()
                }
            }
        })

        return view
    }

    override fun onStart() {
        super.onStart()

        if (MainActivity.gCurrentBlePeripheral != null) {
            mConnectFlg = true
            mBdAddress = MainActivity.gCurrentBlePeripheral!!.getAddress()
        } else {
            mConnectFlg = false
            mBdAddress = param1.toString()
        }

        val dbCtrl = DbCtrl.getInstance(requireContext())
        val savedConnectionList: List<SavedConnection> = dbCtrl.SavedConnection().selectByBdAddress(mBdAddress)
        var savedConnection: SavedConnection? = null
        if (savedConnectionList.isNotEmpty()) {
            savedConnection = savedConnectionList[0]
        }

        val connectionEditTitleTextView = requireView().findViewById<TextView>(R.id.connectionEditTitleTextView)
        connectionEditTitleTextView.text = mBdAddress

        val connectionEditNameEditText = requireView().findViewById<EditText>(R.id.connectionEditNameEditText)
        var name: String = ""
        if (savedConnection != null) {
            name = savedConnection.connection_name
        } else if (mConnectFlg) {
            name = MainActivity.gCurrentBlePeripheral!!.getName()
        }
        connectionEditNameEditText.setText(name, TextView.BufferType.NORMAL)

        val connectionEditServiceEditText = requireView().findViewById<EditText>(R.id.connectionEditServiceEditText)
        var serviceUUID: String = ""
        if (savedConnection != null) {
            serviceUUID = savedConnection.uuid_service
            connectionEditServiceEditText.isEnabled = true
            connectionEditServiceEditText.setTextColor(Color.BLACK)
        } else if (mConnectFlg) {
            serviceUUID = MainActivity.gCurrentBlePeripheral!!.mServiceUUID.toString()
            connectionEditServiceEditText.isEnabled = false
            connectionEditServiceEditText.isEnabled = false
            connectionEditServiceEditText.setTextColor(Color.GRAY)
        }
        connectionEditServiceEditText.setText(serviceUUID, TextView.BufferType.NORMAL)

        val connectionEditTxEditText = requireView().findViewById<EditText>(R.id.connectionEditTxEditText)
        var txUUID: String = ""
        if (savedConnection != null) {
            txUUID = savedConnection.uuid_characteristic_tx
            connectionEditTxEditText.isEnabled = true
            connectionEditTxEditText.setTextColor(Color.BLACK)
        } else if (mConnectFlg) {
            txUUID = MainActivity.gCurrentBlePeripheral!!.mCharacteristicTxUUID.toString()
            connectionEditTxEditText.isEnabled = false
            connectionEditTxEditText.setTextColor(Color.GRAY)
        }
        connectionEditTxEditText.setText(txUUID, TextView.BufferType.NORMAL)

        val connectionEditRxEditText = requireView().findViewById<EditText>(R.id.connectionEditRxEditText)
        var rxUUID: String = ""
        if (savedConnection != null) {
            rxUUID = savedConnection.uuid_characteristic_rx
            connectionEditRxEditText.isEnabled = true
            connectionEditRxEditText.setTextColor(Color.BLACK)
        } else if (mConnectFlg) {
            rxUUID = MainActivity.gCurrentBlePeripheral!!.mCharacteristicRxUUID.toString()
            connectionEditRxEditText.isEnabled = false
            connectionEditRxEditText.setTextColor(Color.GRAY)
        }
        connectionEditRxEditText.setText(rxUUID, TextView.BufferType.NORMAL)

        if (savedConnection != null && savedConnection.image_path.isNotEmpty()) {
            showImage(savedConnection.image_path)
        }

        val connectionEditRecyclerView = requireView().findViewById<RecyclerView>(R.id.connectionEditRecyclerView)
        val controllerSelectListAdapter = ControllerSelectListAdapter()
        controllerSelectListAdapter.setOnClickListener(View.OnClickListener {
            val position: Int = controllerSelectListAdapter.getClickPosition()
            val controllerLayout: ControllerLayout = controllerSelectListAdapter.currentList[position]
            mControllerLayoutId = controllerLayout.layout_id
            controllerSelectListAdapter.notifyDataSetChanged()
        })
        if (savedConnection != null) {
            controllerSelectListAdapter.mInitLayoutId = savedConnection.layout_id
        }
        val controllerLayoutList: List<ControllerLayout> = dbCtrl.ControllerLayout().selectAll()
        controllerSelectListAdapter.submitList(controllerLayoutList)
        connectionEditRecyclerView.adapter = controllerSelectListAdapter
    }

    val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                try {
                    var path: String? = getPathFromUri(requireContext(), uri)
                    showImage(path)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun showImage(path: String?) {
        mImagePath = ""
        if (!path.isNullOrEmpty()) {
            mImagePath  = path
        }
        try {
            val file = File(mImagePath)
            val inputStream0 = FileInputStream(file)
            val bitmap = BitmapFactory.decodeStream(inputStream0)
            val connectionEditImageView = requireView().findViewById<ImageView>(R.id.connectionEditImageView)
            connectionEditImageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
        }
    }

    private fun getPathFromUri(context: Context, uri: Uri): String? {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents" == uri.authority) {// ExternalStorageProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true))
                {
                    return (Environment.getExternalStorageDirectory().path + "/" + split[1])
                } else
                {
                    return  "/stroage/" + type + "/" + split[1]
                }
            } else if ("com.android.providers.downloads.documents" == uri.authority) {// DownloadsProvider
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if ("com.android.providers.media.documents" == uri.authority) {// MediaProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var contentUri: Uri? = MediaStore.Files.getContentUri("external")
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {//MediaStore
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {// File
            return uri.path
        }
        return null
    }
    // 下ごしらえ③-2 変換関数
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val cindex = cursor.getColumnIndexOrThrow(projection[0])
                return cursor.getString(cindex)
            }
        } finally {
            if (cursor != null)
                cursor.close()
        }
        return null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String?) =
            ConnectionEditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}