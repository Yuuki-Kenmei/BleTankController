/*
 * MenuButtonsFragment
 *
 * メニューボタン画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener

class MenuButtonsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu_buttons, container, false)

        val menuConnectImageButton = view.findViewById<CustomImageButton>(R.id.menuConnectImageButton)
        menuConnectImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.closeSettingFragment()
                MainActivity.closeBaseSettingFragment()
                MainActivity.closeControllerListFragment()
                MainActivity.closeSavedConnectionListFragment()
                MainActivity.closeHelpFragment()

                MainActivity.showConnectFragment()
            }
        })

        val menuSettingImageButton = view.findViewById<CustomImageButton>(R.id.menuSettingImageButton)
        menuSettingImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.closeConnectFragment()
                MainActivity.closeHelpFragment()

                MainActivity.showSettingFragment()
            }
        })

        val menuHelpImageButton = view.findViewById<CustomImageButton>(R.id.menuHelpImageButton)
        menuHelpImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.closeSettingFragment()
                MainActivity.closeBaseSettingFragment()
                MainActivity.closeControllerListFragment()
                MainActivity.closeSavedConnectionListFragment()
                MainActivity.closeHelpFragment()
                MainActivity.closeConnectFragment()

                MainActivity.showHelpFragment()
            }
        })

        return view
    }
}