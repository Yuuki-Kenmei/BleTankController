/*
 * EditButtonFragment
 *
 * ボタン編集画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.sys_ky.bletankcontroller.common.ViewConfig
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener

private const val ARG_PARAM1 = "param1"

class EditButtonFragment : Fragment() {
    private var mParamViewId: Int? = null
    private var mViewConfig: ViewConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamViewId = it.getInt(ARG_PARAM1)
        }
        if (mParamViewId == null) {
            mParamViewId = -1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_edit_button, container, false)
        mViewConfig = MainActivity.getControllerLayoutFragment().getViewConfig(mParamViewId!!)

        val editButtonControllerNameTextView = view.findViewById<TextView>(R.id.editButtonControllerNameTextView)
        editButtonControllerNameTextView.text = MainActivity.getControllerLayoutFragment().getControllerName()

        val editButtonTextEditText = view.findViewById<EditText>(R.id.editButtonTextEditText)
        editButtonTextEditText.setText(mViewConfig!!.text, TextView.BufferType.NORMAL)
        editButtonTextEditText.setOnEditorActionListener { textView, i, keyEvent ->
            when(i) {
                EditorInfo.IME_ACTION_DONE -> {
                    editButtonTextEditText.clearFocus()
                }
            }
            return@setOnEditorActionListener false
        }
        editButtonTextEditText.setOnFocusChangeListener { v, b ->
            if (!b) {
                view.findViewById<Button>(R.id.editButtonSampleButton).text = editButtonTextEditText.text.toString()
            }
        }

        val editButtonSendValueEditText = view.findViewById<EditText>(R.id.editButtonSendValueEditText)
        editButtonSendValueEditText.setText(mViewConfig!!.sendValueMap.getSendValue(0, 0), TextView.BufferType.NORMAL)
        editButtonSendValueEditText.setOnEditorActionListener { textView, i, keyEvent ->
            when(i) {
                EditorInfo.IME_ACTION_DONE -> {
                    editButtonSendValueEditText.clearFocus()
                }
            }
            return@setOnEditorActionListener false
        }

        val editButtonSampleButton = view.findViewById<Button>(R.id.editButtonSampleButton)
        editButtonSampleButton.setTextColor(Color.RED)
        editButtonSampleButton.text = mViewConfig!!.text
        var setWidth = mViewConfig!!.width
        var setHeight = mViewConfig!!.height
        val maxWidth = (MainActivity.getFragmentContainer().width * 0.35).toInt()
        val maxHeight = MainActivity.getFragmentContainer().height - (MainActivity.getFragmentContainer().height * 0.3).toInt()
        val ratioWtoH: Float = mViewConfig!!.width.toFloat() / mViewConfig!!.height.toFloat()
        if (maxWidth < mViewConfig!!.width && maxHeight < mViewConfig!!.height) {
            if (ratioWtoH > 1) {
                setWidth = (maxHeight * ratioWtoH).toInt()
                setHeight = maxHeight
                if (setWidth > maxWidth) {
                    setWidth = maxWidth
                }
                if (setWidth > mViewConfig!!.width) {
                    setWidth = mViewConfig!!.width
                }
            } else {
                setWidth = maxWidth
                setHeight = (maxWidth * ratioWtoH).toInt()
                if (setHeight > maxHeight) {
                    setHeight = maxHeight
                }
                if (setHeight > mViewConfig!!.height) {
                    setHeight = mViewConfig!!.height
                }
            }
        } else if (maxWidth < mViewConfig!!.width) {
            setWidth = maxWidth
            setHeight = (maxWidth * ratioWtoH).toInt()
            if (setHeight > maxHeight) {
                setHeight = maxHeight
            }
            if (setHeight > mViewConfig!!.height) {
                setHeight = mViewConfig!!.height
            }
        } else if (maxHeight < mViewConfig!!.height) {
            setWidth = (maxHeight * ratioWtoH).toInt()
            setHeight = maxHeight
            if (setWidth > maxWidth) {
                setWidth = maxWidth
            }
            if (setWidth > mViewConfig!!.width) {
                setWidth = mViewConfig!!.width
            }
        }
        editButtonSampleButton.width = setWidth
        editButtonSampleButton.height = setHeight

        val editButtonBackImageButton = view.findViewById<CustomImageButton>(R.id.editButtonBackImageButton)
        editButtonBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.edit_button_confirm_back)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            MainActivity.closeEditButtonFragment()
                        })
                    .setNegativeButton(
                        R.string.cancel,
                        null
                    )
                    .show()
            }
        })

        val editButtonFixImageButton = view.findViewById<CustomImageButton>(R.id.editButtonFixImageButton)
        editButtonFixImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                mViewConfig!!.text = editButtonTextEditText.text.toString()
                mViewConfig!!.sendValueMap.setSendValue(0,0, editButtonSendValueEditText.text.toString())
                MainActivity.getControllerLayoutFragment().refViewConfig(mParamViewId!!, mViewConfig!!)
                MainActivity.closeEditButtonFragment()
            }
        })

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(viewId: Int) =
            EditButtonFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, viewId)
                }
            }
    }
}