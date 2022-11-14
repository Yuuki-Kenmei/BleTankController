/*
 * BaseSettingFragment
 *
 * 基本設定画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.sys_ky.bletankcontroller.common.Constants
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.room.DbCtrl
import com.sys_ky.bletankcontroller.room.entity.BaseSetting

class BaseSettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_base_setting, container, false)

        val baseSettingServiceEditText = view.findViewById<EditText>(R.id.baseSettingServiceEditText)
        val baseSettingRxEditText = view.findViewById<EditText>(R.id.baseSettingRxEditText)
        val baseSettingTxEditText = view.findViewById<EditText>(R.id.baseSettingTxEditText)

        val baseBackImageButton = view.findViewById<CustomImageButton>(R.id.baseSettingBackImageButton)
        baseBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.closeBaseSettingFragment()
            }
        })

        val baseSettingServiceImageButton = view.findViewById<CustomImageButton>(R.id.baseSettingServiceImageButton)
        baseSettingServiceImageButton.setOnOneClickListener(object: OneClickListener() {
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

        val baseSettingRxImageButton = view.findViewById<CustomImageButton>(R.id.baseSettingRxImageButton)
        baseSettingRxImageButton.setOnOneClickListener(object: OneClickListener() {
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

        val baseSettingTxImageButton = view.findViewById<CustomImageButton>(R.id.baseSettingTxImageButton)
        baseSettingTxImageButton.setOnOneClickListener(object: OneClickListener() {
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

        val baseSettingUpdateImageButton = view.findViewById<CustomImageButton>(R.id.baseSettingUpdateImageButton)
        baseSettingUpdateImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                var errFlg = false
                if (!baseSettingServiceEditText.text.toString().matches(Constants.cRegex) || !baseSettingRxEditText.text.toString().matches(Constants.cRegex) || !baseSettingTxEditText.text.toString().matches(Constants.cRegex)) {
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
                    val dbCtrl = DbCtrl.getInstance(requireContext())
                    dbCtrl.BaseSetting().insertAll(BaseSetting(0, baseSettingServiceEditText.text.toString(), baseSettingTxEditText.text.toString(), baseSettingRxEditText.text.toString()))
                    Toast.makeText(requireContext(), getString(R.string.update_normal), Toast.LENGTH_SHORT).show()
                    MainActivity.closeBaseSettingFragment()
                }
            }
        })

        return view
    }

    override fun onStart() {
        super.onStart()

        val dbCtrl = DbCtrl.getInstance(requireContext())
        val baseSettingList: List<BaseSetting> = dbCtrl.BaseSetting().selectAll()
        val baseSetting: BaseSetting = baseSettingList[0]

        val baseSettingServiceEditText = requireView().findViewById<EditText>(R.id.baseSettingServiceEditText)
        baseSettingServiceEditText.setText(baseSetting.uuid_service, TextView.BufferType.NORMAL)

        val baseSettingRxEditText = requireView().findViewById<EditText>(R.id.baseSettingRxEditText)
        baseSettingRxEditText.setText(baseSetting.uuid_characteristic_rx, TextView.BufferType.NORMAL)

        val baseSettingTxEditText = requireView().findViewById<EditText>(R.id.baseSettingTxEditText)
        baseSettingTxEditText.setText(baseSetting.uuid_characteristic_tx, TextView.BufferType.NORMAL)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BaseSettingFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}