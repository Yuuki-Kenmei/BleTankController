/*
 * ControllerListAdapter
 *
 * コントローラーレイアウト一覧表示用アダプタークラス
 */

package com.sys_ky.bletankcontroller.listadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sys_ky.bletankcontroller.R
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.room.entity.ControllerLayout

class ControllerListAdapter: ListAdapter<ControllerLayout, ControllerListAdapter.ControllerListViewHolder>(DiffCallback()) {

    //リスト内ボタンのリスナー
    private lateinit var mEditClickListener: View.OnClickListener
    private lateinit var mCopyClickListener: View.OnClickListener
    private lateinit var mDeleteClickListener: View.OnClickListener
    private var mClickPosition: Int = 0

    class ControllerListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val controllerListItemNameTextView: TextView = view.findViewById<TextView>(R.id.controllerListItemNameTextView)
        val controllerListItemEditImageButton: CustomImageButton = view.findViewById<CustomImageButton>(R.id.controllerListItemEditImageButton)
        val controllerListItemCopyImageButton: CustomImageButton = view.findViewById<CustomImageButton>(R.id.controllerListItemCopyImageButton)
        val controllerListItemDeleteImageButton: CustomImageButton = view.findViewById<CustomImageButton>(R.id.controllerListItemDeleteImageButton)

        fun bind(controllerLayout: ControllerLayout) {
            controllerListItemNameTextView.text = controllerLayout.layout_name

            //レイアウトIDが0（デフォルト）は編集と削除ボタン非活性
            if (controllerLayout.layout_id == 0) {
                controllerListItemEditImageButton.alpha = 0.5f
                controllerListItemEditImageButton.isClickable = false
                controllerListItemDeleteImageButton.alpha = 0.5f
                controllerListItemDeleteImageButton.isClickable = false
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.controller_list_item, parent, false)
        return ControllerListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ControllerListViewHolder, position: Int) {
        holder.bind(getItem(position))

        val clickPosition: Int = position
        if (clickPosition != 0) {
            holder.controllerListItemEditImageButton.setOnClickListener(View.OnClickListener { view ->
                mClickPosition = clickPosition
                mEditClickListener.onClick(view)
            })

            holder.controllerListItemDeleteImageButton.setOnClickListener(View.OnClickListener { view ->
                mClickPosition = clickPosition
                mDeleteClickListener.onClick(view)
            })
        }
        holder.controllerListItemCopyImageButton.setOnClickListener(View.OnClickListener { view ->
            mClickPosition = clickPosition
            mCopyClickListener.onClick(view)
        })
    }

    fun setOnEditClickListener(listener: View.OnClickListener) {
        mEditClickListener = listener
    }
    fun setOnCopyClickListener(listener: View.OnClickListener) {
        mCopyClickListener = listener
    }
    fun setOnDeleteClickListener(listener: View.OnClickListener) {
        mDeleteClickListener = listener
    }
    fun getClickPosition(): Int {
        return mClickPosition
    }

    private class DiffCallback: DiffUtil.ItemCallback<ControllerLayout>() {

        override fun areItemsTheSame(oldItem: ControllerLayout, newItem: ControllerLayout): Boolean {
            return oldItem.layout_id == newItem.layout_id
        }

        override fun areContentsTheSame(oldItem: ControllerLayout, newItem: ControllerLayout): Boolean {
            return oldItem == newItem
        }
    }
}

