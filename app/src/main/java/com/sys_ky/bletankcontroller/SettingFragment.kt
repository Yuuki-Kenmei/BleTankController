/*
 * SettingFragment
 *
 * 設定一覧画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.sys_ky.bletankcontroller.common.Constants
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.room.DbCtrl
import com.sys_ky.bletankcontroller.room.entity.BaseSetting

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        val settingBackImageButton = view.findViewById<CustomImageButton>(R.id.settingBackImageButton)
        settingBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.closeSettingFragment()
            }
        })

        val settingBaseImageButton = view.findViewById<CustomImageButton>(R.id.settingBaseImageButton)
        settingBaseImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.showBaseSettingFragment()
            }
        })

        val settingEditLayoutImageButton = view.findViewById<CustomImageButton>(R.id.settingEditLayoutImageButton)
        settingEditLayoutImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.showControllerListFragment()
            }
        })

        val settingSavedImageButton = view.findViewById<CustomImageButton>(R.id.settingSavedImageButton)
        settingSavedImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.showSavedConnectionListFragment()
            }
        })

        val settingInitImageButton = view.findViewById<CustomImageButton>(R.id.settingInitImageButton)
        settingInitImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.setting_confirm_initialize)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->

                            AlertDialog.Builder(requireContext())
                                .setTitle(R.string.confirm)
                                .setMessage(R.string.setting_confirm_initialize2)
                                .setPositiveButton(
                                    R.string.ok,
                                    DialogInterface.OnClickListener { dialogInterface2, j ->

                                        val dbCtrl = DbCtrl.getInstance(requireContext())
                                        dbCtrl.BaseSetting().insertAll(
                                            BaseSetting(
                                                0,
                                                Constants.cDefaultUUIDService,
                                                Constants.cDefaultUUIDCharacteristicTX,
                                                Constants.cDefaultUUIDCharacteristicRX
                                            )
                                        )

                                        dbCtrl.ControllerLayout().deleteAll()
                                        dbCtrl.ControllerLayoutDetail().deleteAll()
                                        dbCtrl.ControlSendValue().deleteAll()
                                        dbCtrl.SavedConnection().deleteAll()

                                        Toast.makeText(requireContext(), R.string.setting_initialize_end, Toast.LENGTH_SHORT).show()
                                    })
                                .setNegativeButton(
                                    R.string.cancel,
                                    null
                                )
                                .show()

                        })
                    .setNegativeButton(
                        R.string.cancel,
                        null
                    )
                    .show()
            }
        })

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}