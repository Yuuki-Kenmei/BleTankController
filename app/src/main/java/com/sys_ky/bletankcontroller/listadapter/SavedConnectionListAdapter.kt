/*
 * SavedConnectionListAdapter
 *
 * 保存済み接続先一覧表示用アダプタークラス
 */

package com.sys_ky.bletankcontroller.listadapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sys_ky.bletankcontroller.R
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.room.entity.SavedConnection
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class SavedConnectionListAdapter: ListAdapter<SavedConnection, SavedConnectionListAdapter.SavedConnectionListViewHolder>(DiffCallback()) {

    //リスト内ボタンのリスナー
    private lateinit var mEditClickListener: View.OnClickListener
    private lateinit var mDeleteClickListener: View.OnClickListener
    private var mClickPosition: Int = 0

    class SavedConnectionListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val savedConnectionListItemNameTextView = view.findViewById<TextView>(R.id.savedConnectionListItemNameTextView)
        private val savedConnectionListItemAddressTextView = view.findViewById<TextView>(R.id.savedConnectionListItemAddressTextView)
        private val savedConnectionListItemEditImageButton = view.findViewById<CustomImageButton>(R.id.savedConnectionListItemEditImageButton)
        private val savedConnectionListItemDeleteImageButton = view.findViewById<CustomImageButton>(R.id.savedConnectionListItemDeleteImageButton)
        private val savedConnectionListItemImageImageView = view.findViewById<ImageView>(R.id.savedConnectionListItemImageImageView)

        fun bind(savedConnection: SavedConnection) {
            savedConnectionListItemNameTextView.text = savedConnection.connection_name
            savedConnectionListItemAddressTextView.text = savedConnection.bd_address

            savedConnectionListItemImageImageView.setImageBitmap(null)
            try {
                val path = savedConnection.image_path
                if (!path.isNullOrEmpty()) {
                    val file = File(path)
                    val inputStream = FileInputStream(file)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    savedConnectionListItemImageImageView.setImageBitmap(bitmap)
                }
            } catch (e: IOException) {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedConnectionListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.saved_connection_list_item, parent, false)
        return SavedConnectionListViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedConnectionListViewHolder, position: Int) {
        holder.bind(getItem(position))
        val clickPosition: Int = position
        holder.itemView.findViewById<CustomImageButton>(R.id.savedConnectionListItemEditImageButton).setOnClickListener(View.OnClickListener { view ->
            mClickPosition = clickPosition
            mEditClickListener.onClick(view)
        })

        holder.itemView.findViewById<CustomImageButton>(R.id.savedConnectionListItemDeleteImageButton).setOnClickListener(View.OnClickListener { view ->
            mClickPosition = clickPosition
            mDeleteClickListener.onClick(view)
        })
    }

    fun setOnEditClickListener(listener: View.OnClickListener) {
        mEditClickListener = listener
    }
    fun setOnDeleteClickListener(listener: View.OnClickListener) {
        mDeleteClickListener = listener
    }
    fun getClickPosition(): Int {
        return mClickPosition
    }

    private class DiffCallback: DiffUtil.ItemCallback<SavedConnection>() {

        override fun areItemsTheSame(oldItem: SavedConnection, newItem: SavedConnection): Boolean {
            return oldItem.bd_address == newItem.bd_address
        }

        override fun areContentsTheSame(oldItem: SavedConnection, newItem: SavedConnection): Boolean {
            return oldItem == newItem
        }
    }
}

