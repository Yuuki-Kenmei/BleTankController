/*
 * UuidEditFragment
 *
 * UUID変更画面フラグメント
 */


package com.sys_ky.bletankcontroller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.sys_ky.bletankcontroller.common.Constants
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UuidEditFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_uuid_edit, container, false)

        val uuidEditServiceEditText = view.findViewById<EditText>(R.id.uuidEditServiceEditText)
        val uuidEditRxEditText = view.findViewById<EditText>(R.id.uuidEditRxEditText)
        val uuidEditTxEditText = view.findViewById<EditText>(R.id.uuidEditTxEditText)

        val uuidEditServiceImageButton = view.findViewById<CustomImageButton>(R.id.uuidEditServiceImageButton)
        uuidEditServiceImageButton.setOnOneClickListener(object: OneClickListener() {
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

        val uuidEditRxImageButton = view.findViewById<CustomImageButton>(R.id.uuidEditRxImageButton)
        uuidEditRxImageButton.setOnOneClickListener(object: OneClickListener() {
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

        val uuidEditTxImageButton = view.findViewById<CustomImageButton>(R.id.uuidEditTxImageButton)
        uuidEditTxImageButton.setOnOneClickListener(object: OneClickListener() {
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

        val uuidEditRetryImageButton = view.findViewById<CustomImageButton>(R.id.uuidEditRetryImageButton)
        uuidEditRetryImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                var errFlg = false
                if (!uuidEditServiceEditText.text.toString().matches(Constants.cRegex) || !uuidEditRxEditText.text.toString().matches(Constants.cRegex) || !uuidEditTxEditText.text.toString().matches(Constants.cRegex)) {
                    errFlg = true
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error)
                        .setMessage(R.string.error_uuid)
                        .setPositiveButton(
                            R.string.ok,
                            null)
                        .show()
                }

                if (!errFlg) {
                    MainActivity.gCurrentBlePeripheral?.mServiceUUID = UUID.fromString(uuidEditServiceEditText.text.toString())
                    MainActivity.gCurrentBlePeripheral?.mCharacteristicTxUUID = UUID.fromString(uuidEditTxEditText.text.toString())
                    MainActivity.gCurrentBlePeripheral?.mCharacteristicRxUUID = UUID.fromString(uuidEditRxEditText.text.toString())
                    MainActivity.gCurrentBlePeripheral?.connectGatt(requireContext())
                    MainActivity.closeUuidEditFragment()
                }
            }
        })

        val uuidEditCancelImageButton = view.findViewById<CustomImageButton>(R.id.uuidEditCancelImageButton)
        uuidEditCancelImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.gCurrentBlePeripheral = null
                MainActivity.closeUuidEditFragment()
            }
        })

        return view
    }

    override fun onStart() {
        super.onStart()

        val uuidEditServiceEditText = requireView().findViewById<EditText>(R.id.uuidEditServiceEditText)
        uuidEditServiceEditText.setText(MainActivity.gCurrentBlePeripheral?.mServiceUUID.toString(), TextView.BufferType.NORMAL)

        val uuidEditRxEditText = requireView().findViewById<EditText>(R.id.uuidEditRxEditText)
        uuidEditRxEditText.setText(MainActivity.gCurrentBlePeripheral?.mCharacteristicRxUUID.toString(), TextView.BufferType.NORMAL)

        val uuidEditTxEditText = requireView().findViewById<EditText>(R.id.uuidEditTxEditText)
        uuidEditTxEditText.setText(MainActivity.gCurrentBlePeripheral?.mCharacteristicTxUUID.toString(), TextView.BufferType.NORMAL)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UuidEditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}