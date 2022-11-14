/*
 * EditStickSendFragment
 *
 * スティック送信値編集画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.sys_ky.bletankcontroller.common.SendValueMap
import com.sys_ky.bletankcontroller.common.ViewConfig
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.control.StickView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

class EditStickSendFragment : Fragment() {
    private var mParamViewId: Int = -1
    private var mDefaultFlg: Boolean = true
    private var mStep: Int = -1
    private var mSplit: Int = -1

    private val mViewIdToPoint: MutableMap<Int, Array<Int>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamViewId = it.getInt(ARG_PARAM1)
            mStep = it.getInt(ARG_PARAM2)
            mSplit = it.getInt(ARG_PARAM3)
            mDefaultFlg = it.getBoolean(ARG_PARAM4)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_stick_send, container, false)
        val viewConfig = MainActivity.getControllerLayoutFragment().getViewConfig(mParamViewId) as ViewConfig
        var sendValueMap: SendValueMap = if (mDefaultFlg) {
            SendValueMap.createInitStickSendValueMap(mStep, mSplit, MainActivity.getControllerLayoutFragment().getViewNoFromId(mParamViewId))
        } else {
            MainActivity.getEditStickFragment().getSendValueMap()
        }

        var editStickSendSampleStick = view.findViewById<StickView>(R.id.editStickSendSampleStick)
        editStickSendSampleStick.setStepNum(mStep)
        editStickSendSampleStick.setSplitNum(mSplit)
        editStickSendSampleStick.setEnableStick(false)
        editStickSendSampleStick.setHiddenStick(true)
        var setWidth: Int = viewConfig.width
        var setHeight: Int = viewConfig.height
        val maxWidth = (MainActivity.getFragmentContainer().width * 0.35).toInt()
        val maxHeight = MainActivity.getFragmentContainer().height - (MainActivity.getFragmentContainer().height * 0.3).toInt()
        val ratioWtoH: Float = viewConfig.width.toFloat() / viewConfig.height.toFloat()
        if (maxWidth < viewConfig.width && maxHeight < viewConfig.height) {
            if (ratioWtoH > 1) {
                setWidth = (maxHeight * ratioWtoH).toInt()
                setHeight = maxHeight
                if (setWidth > maxWidth) {
                    setWidth = maxWidth
                }
                if (setWidth > viewConfig.width) {
                    setWidth = viewConfig.width
                }
            } else {
                setWidth = maxWidth
                setHeight = (maxWidth * ratioWtoH).toInt()
                if (setHeight > maxHeight) {
                    setHeight = maxHeight
                }
                if (setHeight > viewConfig.height) {
                    setHeight = viewConfig.height
                }
            }
        } else if (maxWidth < viewConfig.width) {
            setWidth = maxWidth
            setHeight = (maxWidth * ratioWtoH).toInt()
            if (setHeight > maxHeight) {
                setHeight = maxHeight
            }
            if (setHeight > viewConfig.height) {
                setHeight = viewConfig.height
            }
        } else if (maxHeight < viewConfig.height) {
            setWidth = (maxHeight * ratioWtoH).toInt()
            setHeight = maxHeight
            if (setWidth > maxWidth) {
                setWidth = maxWidth
            }
            if (setWidth > viewConfig.width) {
                setWidth = viewConfig.width
            }
        }
        //val constraintSet = ConstraintSet()
        //constraintSet.clone(view.findViewById<ConstraintLayout>(R.id.essBaseConstraintLayout))
        //constraintSet.constrainHeight(sampleStick.id, setHeight)
        //constraintSet.constrainWidth(sampleStick.id, setWidth)
        //constraintSet.connect(sampleStick.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        //constraintSet.connect(sampleStick.id, ConstraintSet.TOP, R.id.essGuideline1, ConstraintSet.TOP, 0)
        //constraintSet.applyTo(view.findViewById<ConstraintLayout>(R.id.essBaseConstraintLayout))

        val editStickSendLinearLayoutV1 = view.findViewById<LinearLayout>(R.id.editStickSendLinearLayoutV1)
        val editStickSendLinearLayoutV2 = view.findViewById<LinearLayout>(R.id.editStickSendLinearLayoutV2)
        var editText = EditText(requireContext())
        editText.id = View.generateViewId()
        editText.setText("(0,0)", TextView.BufferType.NORMAL)
        editText.setTextColor(Color.BLACK)
        editText.maxLines = 1
        editText.isEnabled = false
        editText.background = resources.getDrawable(R.drawable.border_rectangle3, null)
        editText.gravity = Gravity.CENTER
        editText.setPadding(0,20,0,20)
        editStickSendLinearLayoutV1.addView(editText)

        editText = EditText(requireContext())
        editText.id = View.generateViewId()
        mViewIdToPoint[editText.id] = arrayOf(0, 0)
        editText.setText(sendValueMap.getSendValue(0, 0), TextView.BufferType.NORMAL)
        editText.setTextColor(Color.BLACK)
        editText.maxLines = 1
        editText.inputType = EditorInfo.TYPE_CLASS_TEXT
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.background = resources.getDrawable(R.drawable.border_rectangle3, null)
        editText.setPadding(20,20,0,20)
        editText.setOnFocusChangeListener { v, b ->
            if (b) {
                editStickSendSampleStick.drawDotStopPoint(0, 0)
                editStickSendSampleStick.invalidate()
            }
        }

        editText.setOnTouchListener(OnTouchListener { v, event ->
            val edittext = v as EditText
            val inType = edittext.inputType
            if (!v.hasFocus()) {
                edittext.inputType = InputType.TYPE_NULL
            }
            edittext.onTouchEvent(event)
            edittext.inputType = inType

            return@OnTouchListener true
        })

        editStickSendLinearLayoutV2.addView(editText)
        for (step in 1..mStep) {
            for (split in 0 until mSplit) {
                editText = EditText(requireContext())
                editText.id = View.generateViewId()
                editText.setText("($step,$split)", TextView.BufferType.NORMAL)
                editText.setTextColor(Color.BLACK)
                editText.maxLines = 1
                editText.isEnabled = false
                editText.background = resources.getDrawable(R.drawable.border_rectangle3, null)
                editText.gravity = Gravity.CENTER
                editText.setPadding(0,20,0,20)
                editStickSendLinearLayoutV1.addView(editText)

                editText = EditText(requireContext())
                editText.id = View.generateViewId()
                mViewIdToPoint[editText.id] = arrayOf(step, split)
                editText.setText(sendValueMap.getSendValue(step, split), TextView.BufferType.NORMAL)
                editText.setTextColor(Color.BLACK)
                editText.maxLines = 1
                editText.inputType = EditorInfo.TYPE_CLASS_TEXT
                editText.imeOptions = EditorInfo.IME_ACTION_DONE
                editText.background = resources.getDrawable(R.drawable.border_rectangle3, null)
                editText.setPadding(20,20,0,20)
                editText.setOnFocusChangeListener { v, b ->
                    if (b) {
                        editStickSendSampleStick.drawDotStopPoint(step, split)
                        editStickSendSampleStick.invalidate()
                    }
                }
                editText.setOnTouchListener(OnTouchListener { v, event ->
                    val edittext = v as EditText
                    val inType = edittext.inputType
                    if (!v.hasFocus()) {
                        edittext.inputType = InputType.TYPE_NULL
                    }
                    edittext.onTouchEvent(event)
                    edittext.inputType = inType

                    return@OnTouchListener true
                })
                editStickSendLinearLayoutV2.addView(editText)
            }
        }

        val editStickSendBackImageButton = view.findViewById<CustomImageButton>(R.id.editStickSendBackImageButton)
        editStickSendBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.edit_button_confirm_back)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            MainActivity.closeEditStickSendFragment()
                        })
                    .setNegativeButton(
                        R.string.cancel,
                        null
                    )
                    .show()
            }
        })

        val essFixImageButton = view.findViewById<CustomImageButton>(R.id.editStickSendFixImageButton)
        essFixImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                val sendValueMapFix = SendValueMap()
                mViewIdToPoint.keys.forEach { viewId ->
                    val pointArray = mViewIdToPoint[viewId] as Array<Int>
                    sendValueMapFix.setSendValue(pointArray[0], pointArray[1], requireView().findViewById<EditText>(viewId).text.toString())
                }
                MainActivity.getEditStickFragment().updateSendValueMap(sendValueMapFix)
                MainActivity.getEditStickFragment().updateInitStepSplit(mStep, mSplit)
                MainActivity.closeEditStickSendFragment()
            }
        })

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int, param2: Int, param3: Int, param4: Boolean) =
            EditStickSendFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, param2)
                    putInt(ARG_PARAM3, param3)
                    putBoolean(ARG_PARAM4, param4)
                }
            }
    }
}