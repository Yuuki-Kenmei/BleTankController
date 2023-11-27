/*
 * EditLeverFragment
 *
 * レバー編集画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.sys_ky.bletankcontroller.common.SendValueMap
import com.sys_ky.bletankcontroller.common.ViewConfig
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.control.LeverView
import java.util.ArrayList

private const val ARG_PARAM1 = "param1"

class EditLeverFragment : Fragment() {
    private var mParamViewId: Int = -1
    private lateinit var mViewConfig: ViewConfig
    private var mInitStep: Int = 0
    private var mInitDefaultStep: Int = 0
    private var mInitVertical: Boolean = true
    private var mInitReturnDefault: Boolean = true
    private var mSendValueMap: SendValueMap = SendValueMap()

    private val mViewIdToPoint: MutableMap<Int, Int> = mutableMapOf()

    private var mAlertFlg: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamViewId = it.getInt(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_edit_lever, container, false)
        mViewConfig = MainActivity.getControllerLayoutFragment().getViewConfig(mParamViewId) as ViewConfig
        mSendValueMap = mViewConfig.sendValueMap

        val editLeverControllerNameTextView = view.findViewById<TextView>(R.id.editLeverControllerNameTextView)
        editLeverControllerNameTextView.text = MainActivity.getControllerLayoutFragment().getControllerName()

        val editLeverSampleLever = view.findViewById<LeverView>(R.id.editLeverSampleLever)
        editLeverSampleLever.setStepNum(mViewConfig.step)
        editLeverSampleLever.setDefaultStep(mViewConfig.split)
        editLeverSampleLever.setVertical(mViewConfig.vertical)
        editLeverSampleLever.setReturnDefault(mViewConfig.returnDefault)
        editLeverSampleLever.setEnableLever(false)
        mInitStep = mViewConfig.step
        mInitDefaultStep = mViewConfig.split
        mInitVertical = mViewConfig.vertical
        mInitReturnDefault = mViewConfig.returnDefault

        var setWidth = mViewConfig.width
        var setHeight = mViewConfig.height
        val maxWidth: Int = (MainActivity.getFragmentContainer().width * 0.35).toInt()
        val maxHeight: Int = MainActivity.getFragmentContainer().height - (MainActivity.getFragmentContainer().height * 0.3).toInt()
        val ratioWtoH: Float = mViewConfig.width.toFloat() / mViewConfig.height.toFloat()
        if (maxWidth < mViewConfig.width && maxHeight < mViewConfig.height) {
            if (ratioWtoH > 1) {
                setWidth = (maxHeight * ratioWtoH).toInt()
                setHeight = maxHeight
                if (setWidth > maxWidth) {
                    setWidth = maxWidth
                }
                if (setWidth > mViewConfig.width) {
                    setWidth = mViewConfig.width
                }
            } else {
                setWidth = maxWidth
                setHeight = (maxWidth * ratioWtoH).toInt()
                if (setHeight > maxHeight) {
                    setHeight = maxHeight
                }
                if (setHeight > mViewConfig.height) {
                    setHeight = mViewConfig.height
                }
            }
        } else if (maxWidth < mViewConfig.width) {
            setWidth = maxWidth
            setHeight = (maxWidth * ratioWtoH).toInt()
            if (setHeight > maxHeight) {
                setHeight = maxHeight
            }
            if (setHeight > mViewConfig.height) {
                setHeight = mViewConfig.height
            }
        } else if (maxHeight < mViewConfig.height) {
            setWidth = (maxHeight * ratioWtoH).toInt()
            setHeight = maxHeight
            if (setWidth > maxWidth) {
                setWidth = maxWidth
            }
            if (setWidth > mViewConfig.width) {
                setWidth = mViewConfig.width
            }
        }
        editLeverSampleLever.invalidate()

        val editLeverStepSpinner = view.findViewById<Spinner>(R.id.editLeverStepSpinner)
        val stepAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            R.layout.control_spinner_layout,
            resources.getStringArray(R.array.step_array)
        )
        stepAdapter.setDropDownViewResource(R.layout.control_spinner_item)
        editLeverStepSpinner.adapter = stepAdapter
        editLeverStepSpinner.setSelection(resources.getStringArray(R.array.step_array).indexOf(mViewConfig.step.toString()))
        editLeverStepSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = p0 as Spinner
                val step = spinner.selectedItem.toString().toInt()

                var flg = false
                if (mInitStep != step) {
                    if (!mAlertFlg) {

                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.alert)
                        .setMessage(R.string.edit_lever_alert_edit_send)
                        .setPositiveButton(
                            R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                editLeverSampleLever.setStepNum(step)

                                initDefaultStepSpinner(step, view, -1)

                                editLeverSampleLever.invalidate()

                                mSendValueMap = SendValueMap.createInitLeverSendValueMap(step, MainActivity.getControllerLayoutFragment().getViewNoFromId(mParamViewId))
                                initSendValueLayout(spinner.selectedItem.toString().toInt(), requireView())

                                mInitStep = step
                                mAlertFlg = true
                            })
                        .setNegativeButton(
                            R.string.cancel,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                editLeverStepSpinner.setSelection(resources.getStringArray(R.array.step_array).indexOf(mInitStep.toString()))
                            })
                        .show()

                    } else {
                        editLeverSampleLever.setStepNum(step)

                        initDefaultStepSpinner(step, view, -1)

                        editLeverSampleLever.invalidate()

                        mSendValueMap = SendValueMap.createInitLeverSendValueMap(step, MainActivity.getControllerLayoutFragment().getViewNoFromId(mParamViewId))
                        initSendValueLayout(spinner.selectedItem.toString().toInt(), requireView())

                        mInitStep = step
                    }
                }
            }
        }

        initDefaultStepSpinner(mViewConfig.step, view, mViewConfig.split)
        val editLeverDefaultStepSpinner = view.findViewById<Spinner>(R.id.editLeverDefaultStepSpinner)
        editLeverDefaultStepSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = p0 as Spinner
                editLeverSampleLever.setDefaultStep(spinner.selectedItem.toString().toInt())
                editLeverSampleLever.invalidate()
            }
        }

        val editLeverVerticalSwitch = view.findViewById<Switch>(R.id.editLeverVerticalSwitch)
        editLeverVerticalSwitch.isChecked = mViewConfig.vertical
        editLeverVerticalSwitch.setOnCheckedChangeListener { compoundButton, b ->
            editLeverSampleLever.setVertical(b)
            editLeverSampleLever.invalidate()
        }

        val editLeverReturnSwitch = view.findViewById<Switch>(R.id.editLeverReturnSwitch)
        editLeverReturnSwitch.isChecked = mViewConfig.returnDefault

        initSendValueLayout(mViewConfig.step, view)

        val editLeverBackImageButton = view.findViewById<CustomImageButton>(R.id.editLeverBackImageButton)
        editLeverBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.edit_button_confirm_back)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            MainActivity.closeEditLeverFragment()
                        })
                    .setNegativeButton(
                        R.string.cancel,
                        null
                    )
                    .show()
            }
        })

        val editLeverFixImageButton = view.findViewById<CustomImageButton>(R.id.editLeverFixImageButton)
        editLeverFixImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                mViewConfig.step = editLeverStepSpinner.selectedItem.toString().toInt()
                mViewConfig.split = editLeverDefaultStepSpinner.selectedItem.toString().toInt()
                mViewConfig.vertical = editLeverVerticalSwitch.isChecked
                mViewConfig.returnDefault = editLeverReturnSwitch.isChecked

                val sendValueMapFix = SendValueMap()
                mViewIdToPoint.keys.forEach { viewId ->
                    val step = mViewIdToPoint[viewId] as Int
                    sendValueMapFix.setSendValue(step, 0, requireView().findViewById<EditText>(viewId).text.toString())
                }
                mViewConfig.sendValueMap = sendValueMapFix

                MainActivity.getControllerLayoutFragment().refViewConfig(mParamViewId, mViewConfig)
                MainActivity.getControllerLayoutFragment().invalidateView(mParamViewId)
                MainActivity.closeEditLeverFragment()
            }
        })

        return view
    }

    private fun initDefaultStepSpinner(step: Int, view: View, setStep: Int) {
        val editLeverDefaultStepSpinner = view.findViewById<Spinner>(R.id.editLeverDefaultStepSpinner)
        var nowDefaultStep = 0
        if (editLeverDefaultStepSpinner.selectedItem != null) {
            nowDefaultStep = editLeverDefaultStepSpinner.selectedItem.toString().toInt()
        }
        var defaultStepList: ArrayList<String> = arrayListOf()
        for (cnt in 0.. step) {
            defaultStepList.add(cnt.toString())
        }
        val defaultStepAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            R.layout.control_spinner_layout,
            defaultStepList
        )
        defaultStepAdapter.setDropDownViewResource(R.layout.control_spinner_item)
        editLeverDefaultStepSpinner.adapter = defaultStepAdapter

        var selectDefaultStep = nowDefaultStep
        if (nowDefaultStep > step) {
            selectDefaultStep = step
        }
        if (setStep >= 0) {
            editLeverDefaultStepSpinner.setSelection(defaultStepList.indexOf(setStep.toString()))
        } else {
            editLeverDefaultStepSpinner.setSelection(defaultStepList.indexOf(selectDefaultStep.toString()))
        }
    }

    private fun initSendValueLayout(step: Int, view: View) {
        val editLeverSampleLever = view.findViewById<LeverView>(R.id.editLeverSampleLever)

        var editText = EditText(requireContext())
        val editLeverLinearLayoutV1 = view.findViewById<LinearLayout>(R.id.editLeverLinearLayoutV1)
        val editLeverLinearLayoutV2 = view.findViewById<LinearLayout>(R.id.editLeverLinearLayoutV2)
        editLeverLinearLayoutV1.removeAllViews()
        editLeverLinearLayoutV2.removeAllViews()
        mViewIdToPoint.clear()
        for (step in 0..step) {
            editText = EditText(requireContext())
            editText.id = View.generateViewId()
            editText.setText("($step)", TextView.BufferType.NORMAL)
            editText.setTextColor(Color.BLACK)
            editText.maxLines = 1
            editText.isEnabled = false
            editText.background = resources.getDrawable(R.drawable.border_rectangle3, null)
            editText.gravity = Gravity.CENTER
            editText.setPadding(0,20,0,20)
            editLeverLinearLayoutV1.addView(editText)

            editText = EditText(requireContext())
            editText.id = View.generateViewId()
            mViewIdToPoint[editText.id] = step
            editText.setText(mSendValueMap.getSendValue(step, 0), TextView.BufferType.NORMAL)
            editText.setTextColor(Color.BLACK)
            editText.maxLines = 1
            editText.inputType = EditorInfo.TYPE_CLASS_TEXT
            editText.imeOptions = EditorInfo.IME_ACTION_DONE
            editText.background = resources.getDrawable(R.drawable.border_rectangle3, null)
            editText.setPadding(20,20,0,20)
            editText.setOnFocusChangeListener { v, b ->
                if (b) {
                    editLeverSampleLever.drawDotStopPoint(step)
                    editLeverSampleLever.invalidate()
                }
            }
            editText.setOnTouchListener(View.OnTouchListener { v, event ->
                val edittext = v as EditText
                val inType = edittext.inputType
                if (!v.hasFocus()) {
                    edittext.inputType = InputType.TYPE_NULL
                }
                edittext.onTouchEvent(event)
                edittext.inputType = inType

                return@OnTouchListener true
            })
            editLeverLinearLayoutV2.addView(editText)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(viewId: Int) =
            EditLeverFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, viewId)
                }
            }
    }
}