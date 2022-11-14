/*
 * ControllerLayoutFragment
 *
 * コントローラーレイアウト編集画面フラグメント
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
import com.sys_ky.bletankcontroller.control.CanvasView
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.room.DbCtrl
import com.sys_ky.bletankcontroller.room.entity.ControlSendValue
import com.sys_ky.bletankcontroller.room.entity.ControllerLayout
import com.sys_ky.bletankcontroller.room.entity.ControllerLayoutDetail

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ControllerLayoutFragment : Fragment() {

    private var mParamLayoutId: Int? = -1
    private var mCopyFlg: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamLayoutId = it.getInt(ARG_PARAM1)
            mCopyFlg = it.getBoolean(ARG_PARAM2)
        }
        if (mParamLayoutId == null) {
            mParamLayoutId = -1
            mCopyFlg = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_controller_layout, container, false)

        val controllerLayoutNameEditText = view.findViewById<EditText>(R.id.controllerLayoutNameEditText)
        val controllerLayoutCanvasView = view.findViewById<CanvasView>(R.id.controllerLayoutCanvasView)
        controllerLayoutCanvasView.setOnTouchListener { v, motionEvent ->
            controllerLayoutNameEditText.clearFocus()
            return@setOnTouchListener false
        }

        val controllerLayoutSaveImageButton = view.findViewById<CustomImageButton>(R.id.controllerLayoutSaveImageButton)
        controllerLayoutSaveImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                var errFlg: Boolean = false

                if (controllerLayoutNameEditText.text.toString().isNullOrEmpty()) {
                    //コントローラー名が空白の場合、エラー
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error)
                        .setMessage(R.string.controller_layout_error_name_empty)
                        .setPositiveButton(
                            R.string.ok,
                            null
                        )
                        .show()
                    errFlg = true
                } else if (controllerLayoutCanvasView.getViewConfigList().isEmpty()) {
                    //コントロールが1つも配置されていない場合、エラー
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.error)
                        .setMessage(R.string.controller_layout_error_no_control)
                        .setPositiveButton(
                            R.string.ok,
                            null
                        )
                        .show()
                    errFlg = true
                }

                //エラーなしで保存
                if (!errFlg) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.controller_layout_confirm_save)
                        .setPositiveButton(
                            R.string.ok,
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                saveControllerLayout(controllerLayoutCanvasView.getViewConfigList(), controllerLayoutNameEditText.text.toString())
                                MainActivity.closeControllerLayoutFragment(true)
                            })
                        .setNegativeButton(
                            R.string.cancel,
                            null
                        )
                        .show()
                }
            }
        })

        val controllerLayoutBackImageButton = view.findViewById<CustomImageButton>(R.id.controllerLayoutBackImageButton)
        controllerLayoutBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.controller_layout_confirm_back)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            MainActivity.closeControllerLayoutFragment(true)
                        })
                    .setNegativeButton(
                        R.string.cancel,
                        null
                    )
                    .show()
            }
        })

        val controllerLayoutGearImageButton = view.findViewById<CustomImageButton>(R.id.controllerLayoutGearImageButton)
        controllerLayoutGearImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.showControlListFragment()
            }
        })

        return view
    }

    override fun onStart() {
        super.onStart()

        val dbCtrl = DbCtrl.getInstance(requireContext())

        if (mParamLayoutId!! < 0) {
            //新規作成
            //val controllerLayoutCount: Int = dbCtrl.CONTROLLER_LAYOUT().selectCount()
            val controllerLayoutMaxId: Int = dbCtrl.ControllerLayout().selectMaxId()
            val controllerLayoutNameEditText = requireView().findViewById<EditText>(R.id.controllerLayoutNameEditText)
            //clayNameEditText.setText(resources.getText(R.string.clay_default_name).toString() + controllerLayoutCount.toString(), TextView.BufferType.NORMAL)
            controllerLayoutNameEditText.setText(resources.getText(R.string.controller_layout_default_name).toString() + (controllerLayoutMaxId + 1).toString(), TextView.BufferType.NORMAL)
        } else {
            //編集
            val controllerLayoutList: List<ControllerLayout> = dbCtrl.ControllerLayout().selectByLayoutId(mParamLayoutId!!)
            val controllerLayoutDetailList: List<ControllerLayoutDetail> = dbCtrl.ControllerLayoutDetail().selectByLayoutId(mParamLayoutId!!)
            val controllerLayout: ControllerLayout = controllerLayoutList[0]

            val controllerLayoutNameEditText = requireView().findViewById<EditText>(R.id.controllerLayoutNameEditText)
            if (!mCopyFlg!!) {
                controllerLayoutNameEditText.setText(controllerLayout.layout_name, TextView.BufferType.NORMAL)
            } else {
                val controllerLayoutMaxId: Int = dbCtrl.ControllerLayout().selectMaxId()
                controllerLayoutNameEditText.setText(resources.getText(R.string.controller_layout_default_name).toString() + (controllerLayoutMaxId + 1).toString(), TextView.BufferType.NORMAL)
            }

            val viewConfigList: MutableList<ViewConfig> = mutableListOf()
            controllerLayoutDetailList.forEach { controllerLayoutDetail ->
                val viewConfig = ViewConfig()
                viewConfig.viewType = controllerLayoutDetail.control_type
                viewConfig.start = controllerLayoutDetail.start
                viewConfig.top = controllerLayoutDetail.top
                viewConfig.width = controllerLayoutDetail.width
                viewConfig.height = controllerLayoutDetail.height
                viewConfig.text = controllerLayoutDetail.text
                viewConfig.step = controllerLayoutDetail.step
                viewConfig.split = controllerLayoutDetail.split

                var sendValueMap: SendValueMap = SendValueMap()
                val controlSendValueList: List<ControlSendValue> = dbCtrl.ControlSendValue().selectByIds(controllerLayoutDetail.layout_id, controllerLayoutDetail.control_id)
                controlSendValueList.forEach { controlSendValue ->
                    sendValueMap.setSendValue(controlSendValue.point1, controlSendValue.point2, controlSendValue.send_value)
                }
                viewConfig.sendValueMap = sendValueMap

                viewConfigList.add(viewConfig)
            }

            requireView().findViewById<CanvasView>(R.id.controllerLayoutCanvasView).initFromViewConfigList(viewConfigList)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    fun refViewConfig(viewId: Int, config: ViewConfig) {
        return requireView().findViewById<CanvasView>(R.id.controllerLayoutCanvasView).refViewConfig(viewId, config)
    }

    fun getViewConfig(viewId: Int): ViewConfig? {
        return requireView().findViewById<CanvasView>(R.id.controllerLayoutCanvasView).getViewConfig(viewId)
    }

    fun getViewNoFromId(viewId: Int): Int {
        return requireView().findViewById<CanvasView>(R.id.controllerLayoutCanvasView).getViewNoFromId(viewId)
    }

    fun getControllerName(): String {
        return requireView().findViewById<EditText>(R.id.controllerLayoutNameEditText).text.toString()
    }

    fun invalidateView(viewId: Int) {
        requireView().findViewById<CanvasView>(R.id.controllerLayoutCanvasView).invalidateView(viewId)
    }

    fun getViewType(): Int {
        return requireView().findViewById<CanvasView>(R.id.controllerLayoutCanvasView).mViewType
    }

    fun setViewType(viewType: Int) {
        requireView().findViewById<CanvasView>(R.id.controllerLayoutCanvasView).mViewType = viewType
    }

    private fun saveControllerLayout(viewConfigList: MutableMap<Int, ViewConfig>, layoutName: String) {
        val dbCtrl = DbCtrl.getInstance(requireContext())
        if (mParamLayoutId!! < 0 || mCopyFlg!!) {
            mParamLayoutId = dbCtrl.ControllerLayout().selectMaxId() + 1
        }
        val controllerLayout: ControllerLayout = ControllerLayout(mParamLayoutId!!, layoutName)
        dbCtrl.ControllerLayout().insertAll(controllerLayout)

        //先に削除
        dbCtrl.ControllerLayoutDetail().deleteByLayoutId(mParamLayoutId!!)
        dbCtrl.ControlSendValue().deleteByLayoutId(mParamLayoutId!!)

        var controlId: Int = 0
        val controllerLayoutDetailList: ArrayList<ControllerLayoutDetail> = arrayListOf()
        val controlSendValueList: ArrayList<ControlSendValue> = arrayListOf()
        viewConfigList.values.forEach { viewConfig ->
            controlId++
            controllerLayoutDetailList.add(
                ControllerLayoutDetail(
                    mParamLayoutId!!,
                    controlId,
                    viewConfig.viewType,
                    viewConfig.width,
                    viewConfig.height,
                    viewConfig.top,
                    viewConfig.start,
                    viewConfig.text,
                    viewConfig.step,
                    viewConfig.split
                )
            )

            viewConfig.sendValueMap.getSendValueAll().keys.forEach { key ->
                controlSendValueList.add(
                    ControlSendValue(
                        mParamLayoutId!!,
                        controlId,
                        key[0],
                        key[1],
                        viewConfig.sendValueMap.getSendValue(key[0], key[1])
                    )
                )
            }
        }
        dbCtrl.ControllerLayoutDetail().insertAll(controllerLayoutDetailList)
        dbCtrl.ControlSendValue().insertAll(controlSendValueList)

        Toast.makeText(requireContext(), getString(R.string.save_normal), Toast.LENGTH_SHORT).show()
    }

    companion object {
        @JvmStatic
        fun newInstance(layoutId: Int, copyFlg: Boolean) =
            ControllerLayoutFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, layoutId)
                    putBoolean(ARG_PARAM2, copyFlg)
                }
            }
    }
}