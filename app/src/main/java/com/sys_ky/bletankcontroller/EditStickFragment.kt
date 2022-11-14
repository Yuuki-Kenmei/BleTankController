/*
 * EditStickFragment
 *
 * スティック編集画面フラグメント
 */


package com.sys_ky.bletankcontroller

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.sys_ky.bletankcontroller.common.SendValueMap
import com.sys_ky.bletankcontroller.common.ViewConfig
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.control.StickView

private const val ARG_PARAM1 = "param1"

class EditStickFragment : Fragment() {
    private var mParamViewId: Int = -1
    private lateinit var mViewConfig: ViewConfig
    private var mInitStep: Int = 0
    private var mInitSplit: Int = 0
    private var mSendValueMap: SendValueMap = SendValueMap()

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

        val view = inflater.inflate(R.layout.fragment_edit_stick, container, false)
        mViewConfig = MainActivity.getControllerLayoutFragment().getViewConfig(mParamViewId) as ViewConfig
        mSendValueMap = mViewConfig.sendValueMap

        val editStickControllerNameTextView = view.findViewById<TextView>(R.id.editStickControllerNameTextView)
        editStickControllerNameTextView.text = MainActivity.getControllerLayoutFragment().getControllerName()

        val editStickSampleStick = view.findViewById<StickView>(R.id.editStickSampleStick)
        editStickSampleStick.setStepNum(mViewConfig.step)
        editStickSampleStick.setSplitNum(mViewConfig.split)
        editStickSampleStick.setEnableStick(false)
        mInitStep = mViewConfig.step
        mInitSplit = mViewConfig.split
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
        editStickSampleStick.invalidate()

        //val constraintSet = ConstraintSet()
        //constraintSet.clone(view.findViewById<ConstraintLayout>(R.id.esConstraintLayout))
        //constraintSet.constrainHeight(sampleStick.id, setHeight)
        //constraintSet.constrainWidth(sampleStick.id, setWidth)
        //constraintSet.connect(sampleStick.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        //constraintSet.connect(sampleStick.id, ConstraintSet.TOP, R.id.esGuidelineHorizon, ConstraintSet.TOP, 0)
        //constraintSet.applyTo(view.findViewById<ConstraintLayout>(R.id.esConstraintLayout))

        val editStickStepSpinner = view.findViewById<Spinner>(R.id.editStickStepSpinner)
        val stepAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), R.layout.control_spinner_layout, getResources().getStringArray(R.array.step_array))
        stepAdapter.setDropDownViewResource(R.layout.control_spinner_item)
        editStickStepSpinner.adapter = stepAdapter
        editStickStepSpinner.setSelection(getResources().getStringArray(R.array.step_array).indexOf(mViewConfig.step.toString()))
        editStickStepSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = p0 as Spinner
                editStickSampleStick.setStepNum(spinner.selectedItem.toString().toInt())
                editStickSampleStick.invalidate()
            }
        }

        val editStickSplitSpinner = view.findViewById<Spinner>(R.id.editStickSplitSpinner)
        val splitAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), R.layout.control_spinner_layout, getResources().getStringArray(R.array.split_array))
        splitAdapter.setDropDownViewResource(R.layout.control_spinner_item)
        editStickSplitSpinner.adapter = splitAdapter
        editStickSplitSpinner.setSelection(getResources().getStringArray(R.array.split_array).indexOf(mViewConfig.split.toString()))
        editStickSplitSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = p0 as Spinner
                editStickSampleStick.setSplitNum(spinner.selectedItem.toString().toInt())
                editStickSampleStick.invalidate()
            }
        }

        val editStickEditSendImageButton = view.findViewById<CustomImageButton>(R.id.editStickEditSendImageButton)
        editStickEditSendImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                val step = editStickStepSpinner.selectedItem.toString().toInt()
                val split = editStickSplitSpinner.selectedItem.toString().toInt()
                if (step != mInitStep || split != mInitSplit) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.alert)
                        .setMessage(R.string.edit_stick_alert_edit_send)
                        .setPositiveButton(
                            R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                MainActivity.showEditStickSendFragment(mParamViewId, step, split , true)
                            })
                        .setNegativeButton(
                            R.string.cancel,
                            null)
                        .show()
                } else {
                    MainActivity.showEditStickSendFragment(mParamViewId, step, split, false)
                }
            }
        })

        val editStickBackImageButton = view.findViewById<CustomImageButton>(R.id.editStickBackImageButton)
        editStickBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.edit_button_confirm_back)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            MainActivity.closeEditStickFragment()
                        })
                    .setNegativeButton(
                        R.string.cancel,
                        null
                    )
                    .show()
            }
        })

        val editStickFixImageButton = view.findViewById<CustomImageButton>(R.id.editStickFixImageButton)
        editStickFixImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                val step = editStickStepSpinner.selectedItem.toString().toInt()
                val split = editStickSplitSpinner.selectedItem.toString().toInt()
                if (step != mInitStep || split != mInitSplit) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.alert)
                        .setMessage(R.string.edit_stick_alert_edit_send)
                        .setPositiveButton(
                            R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                mSendValueMap = SendValueMap.createInitStickSendValueMap(step, split, MainActivity.getControllerLayoutFragment().getViewNoFromId(mParamViewId))

                                mViewConfig.step = step
                                mViewConfig.split = split
                                mViewConfig.sendValueMap = mSendValueMap

                                MainActivity.getControllerLayoutFragment().refViewConfig(mParamViewId, mViewConfig)
                                MainActivity.getControllerLayoutFragment().invalidateView(mParamViewId)
                                MainActivity.closeEditStickFragment()
                            })
                        .setNegativeButton(
                            R.string.cancel,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                return@OnClickListener
                            })
                        .show()
                } else {
                    mViewConfig.step = step
                    mViewConfig.split = split
                    mViewConfig.sendValueMap = mSendValueMap

                    MainActivity.getControllerLayoutFragment().refViewConfig(mParamViewId, mViewConfig)
                    MainActivity.getControllerLayoutFragment().invalidateView(mParamViewId)
                    MainActivity.closeEditStickFragment()
                }
            }
        })

        return view
    }

    fun updateInitStepSplit(step: Int, split: Int) {
        mInitStep = step
        mInitSplit = split
    }

    fun getSendValueMap(): SendValueMap {
        return mSendValueMap
    }

    fun updateSendValueMap(sendValueMap: SendValueMap) {
        mSendValueMap = sendValueMap
    }

    companion object {
        @JvmStatic
        fun newInstance(viewId: Int) =
            EditStickFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, viewId)
                }
            }
    }
}