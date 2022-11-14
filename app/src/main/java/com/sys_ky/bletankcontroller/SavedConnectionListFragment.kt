/*
 * SavedConnectionListFragment
 *
 * 保存済み接続先一覧画面フラグメント
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
import androidx.recyclerview.widget.RecyclerView
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.listadapter.SavedConnectionListAdapter
import com.sys_ky.bletankcontroller.room.DbCtrl
import com.sys_ky.bletankcontroller.room.entity.SavedConnection

class SavedConnectionListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_connection_list, container, false)

        val savedConnectionListBackImageButton = view.findViewById<CustomImageButton>(R.id.savedConnectionListBackImageButton)
        savedConnectionListBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.closeSavedConnectionListFragment()
            }
        })

        return view
    }

    override fun onStart() {
        super.onStart()

        setSavedConnectionList()
    }

    fun setSavedConnectionList() {
        val dbCtrl = DbCtrl.getInstance(requireContext())
        val savedConnectionList: List<SavedConnection> = dbCtrl.SavedConnection().selectAll()

        val savedConnectionListAdapter = SavedConnectionListAdapter()
        savedConnectionListAdapter.setOnEditClickListener(View.OnClickListener {
            val position: Int = savedConnectionListAdapter.getClickPosition()
            val savedConnection: SavedConnection = savedConnectionListAdapter.currentList[position]
            MainActivity.showConnectionEditFragment(savedConnection.bd_address)
        })

        savedConnectionListAdapter.setOnDeleteClickListener(View.OnClickListener {
            val position: Int = savedConnectionListAdapter.getClickPosition()
            val savedConnection: SavedConnection = savedConnectionListAdapter.currentList[position]

            AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm)
                .setMessage(R.string.saved_connection_delete)
                .setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { dialogInterface, i ->

                        dbCtrl.SavedConnection().deleteByBdAddress(savedConnection.bd_address)

                        Toast.makeText(requireContext(), R.string.delete_normal, Toast.LENGTH_SHORT).show()

                        setSavedConnectionList()
                    })
                .setNegativeButton(
                    R.string.cancel,
                    null
                )
                .show()
        })

        savedConnectionListAdapter.submitList(savedConnectionList)

        val savedConnectionListRecyclerView = requireView().findViewById<RecyclerView>(R.id.savedConnectionListRecyclerView)
        savedConnectionListRecyclerView.adapter = savedConnectionListAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SavedConnectionListFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}