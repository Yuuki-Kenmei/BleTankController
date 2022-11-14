/*
 * ControllerListFragment
 *
 * コントローラーレイアウト一覧画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.sys_ky.bletankcontroller.listadapter.ControllerListAdapter
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.room.DbCtrl
import com.sys_ky.bletankcontroller.room.entity.ControllerLayout


class ControllerListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_controller_list, container, false)

        val controllerListNewImageButton = view.findViewById<CustomImageButton>(R.id.controllerListNewImageButton)
        controllerListNewImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.showControllerLayoutFragment(-1, false)
            }
        })

        val controllerListBackImageButton = view.findViewById<CustomImageButton>(R.id.controllerListBackImageButton)
        controllerListBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.closeControllerListFragment()
            }
        })

        return view
    }

    override fun onStart() {
        super.onStart()

        setControllerList()
    }

    fun setControllerList() {
        val dbCtrl = DbCtrl.getInstance(requireContext())
        val controllerLayoutList: List<ControllerLayout> = dbCtrl.ControllerLayout().selectAll()

        val controllerListAdapter = ControllerListAdapter()
        controllerListAdapter.setOnEditClickListener(View.OnClickListener {
            val position: Int = controllerListAdapter.getClickPosition()
            val controllerLayout: ControllerLayout = controllerListAdapter.currentList[position]
            MainActivity.showControllerLayoutFragment(controllerLayout.layout_id, false)
        })

        controllerListAdapter.setOnCopyClickListener(View.OnClickListener {
            val position: Int = controllerListAdapter.getClickPosition()
            val controllerLayout: ControllerLayout = controllerListAdapter.currentList[position]
            MainActivity.showControllerLayoutFragment(controllerLayout.layout_id, true)
        })

        controllerListAdapter.setOnDeleteClickListener(View.OnClickListener {
            val position: Int = controllerListAdapter.getClickPosition()
            val controllerLayout: ControllerLayout = controllerListAdapter.currentList[position]

            val cnt: Int = dbCtrl.SavedConnection().selectCountByLayoutId(controllerLayout.layout_id)
            var message: String = if (cnt > 0) {
                resources.getString(R.string.controller_list_confirm_delete2)
            } else {
                resources.getString(R.string.controller_list_confirm_delete)
            }

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm)
                .setMessage(message)
                .setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { dialogInterface, i ->

                        dbCtrl.ControllerLayout().deleteByLayoutId(controllerLayout.layout_id)
                        dbCtrl.ControllerLayoutDetail().deleteByLayoutId(controllerLayout.layout_id)
                        dbCtrl.ControlSendValue().deleteByLayoutId(controllerLayout.layout_id)

                        if (cnt > 0) {
                            dbCtrl.SavedConnection().updateLayoutIdToDefault(controllerLayout.layout_id)
                        }

                        Toast.makeText(requireContext(), R.string.delete_normal, Toast.LENGTH_SHORT).show()

                        setControllerList()

                    })
                .setNegativeButton(
                    R.string.cancel,
                    null
                )
                .show()
        })

        controllerListAdapter.submitList(controllerLayoutList)

        val controllerListRecyclerView = requireView().findViewById<RecyclerView>(R.id.controllerListRecyclerView)
        controllerListRecyclerView.adapter = controllerListAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ControllerListFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}