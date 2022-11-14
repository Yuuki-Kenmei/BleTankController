/*
 * ControllerSelectListAdapter
 *
 * 使用コントローラーレイアウト選択時の一覧表示用アダプタークラス
 */

package com.sys_ky.bletankcontroller.listadapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sys_ky.bletankcontroller.R
import com.sys_ky.bletankcontroller.room.entity.ControllerLayout

class ControllerSelectListAdapter: ListAdapter<ControllerLayout, ControllerSelectListAdapter.ControllerSelectListViewHolder>(DiffCallback()) {

    //リスト内ボタンのリスナー
    private lateinit var mClickListener: View.OnClickListener
    private var mClickPosition: Int = -1
    var mInitLayoutId: Int = 0

    class ControllerSelectListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val controllerSelectListItemNameTextView = view.findViewById<TextView>(R.id.controllerSelectListItemNameTextView)

        fun bind(controllerLayout: ControllerLayout) {
            controllerSelectListItemNameTextView.text = controllerLayout.layout_name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerSelectListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.controller_select_list_item, parent, false)
        return ControllerSelectListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ControllerSelectListViewHolder, position: Int) {

        //選択行だけ色を変える
        if ((mClickPosition >= 0 && position == mClickPosition) || (mClickPosition < 0 && getItem(position).layout_id == mInitLayoutId)) {
            holder.itemView.findViewById<LinearLayout>(R.id.controllerSelectListItemLayout).setBackgroundColor(Color.BLACK)
            holder.itemView.findViewById<TextView>(R.id.controllerSelectListItemNameTextView).setTextColor(Color.GREEN)
        } else {
            holder.itemView.findViewById<LinearLayout>(R.id.controllerSelectListItemLayout).setBackgroundResource(R.drawable.border_rectangle3)
            holder.itemView.findViewById<TextView>(R.id.controllerSelectListItemNameTextView).setTextColor(Color.BLACK)
        }

        holder.bind(getItem(position))

        val clickPosition: Int = position
        holder.itemView.setOnClickListener(View.OnClickListener { view ->
            mClickPosition = clickPosition
            mClickListener.onClick(view)
        })
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        mClickListener = listener
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

