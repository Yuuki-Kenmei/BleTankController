/*
 * ControllerPadFragment
 *
 * コントローラーパッド画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.red
import androidx.lifecycle.ViewModelProvider
import com.sys_ky.bletankcontroller.ble.BleNotifyViewModel
import com.sys_ky.bletankcontroller.ble.BleNotifyViewModelFactory
import com.sys_ky.bletankcontroller.common.Constants
import com.sys_ky.bletankcontroller.common.SendValueMap
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.LeverView
import com.sys_ky.bletankcontroller.control.OneClickListener
import com.sys_ky.bletankcontroller.control.StickView
import com.sys_ky.bletankcontroller.room.DbCtrl
import com.sys_ky.bletankcontroller.room.entity.ControlSendValue
import com.sys_ky.bletankcontroller.room.entity.ControllerLayoutDetail
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.concurrent.scheduleAtFixedRate

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ControllerPadFragment : Fragment() {
    private var mLayoutId: Int = 0
    private var mName: String? = null

    private var mSendValueList: MutableMap<Int, SendValueMap> = mutableMapOf()

    private var mStickViewIdList: MutableList<Int> = mutableListOf()
    private var mLeverViewIdList: MutableList<Int> = mutableListOf()
    private var mSendAlways: Int = 0

    private var mSendDataQueue: ArrayDeque<String> = ArrayDeque()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mLayoutId = it.getInt(ARG_PARAM1)
            mName = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_controller_pad, container, false)

        val controllerPadNameTextView = view.findViewById<TextView>(R.id.controllerPadNameTextView)
        controllerPadNameTextView.text = mName
        controllerPadNameTextView.setOnLongClickListener {
            mSendAlways++
            if (mSendAlways > 3) {
                mSendAlways = 0
            }
            var message: String = ""
            when(mSendAlways) {
                0 -> {
                    message = "定期的に送信しない"
                }
                1 -> {
                    message = "スティックのみ定期的に送信する"
                }
                2 -> {
                    message = "レバーのみ定期的に送信する"
                }
                3 -> {
                    message = "スティックとレバーのどちらも定期的に送信する"
                }
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }

        val controllerPadNoticeTextView = view.findViewById<TextView>(R.id.controllerPadNoticeTextView)
        controllerPadNoticeTextView.text = ""

        val controllerPadEndImageButton = view.findViewById<CustomImageButton>(R.id.controllerPadEndImageButton)
        controllerPadEndImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.controller_pad_confirm_end)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            MainActivity.gCurrentBlePeripheral?.disconnectGatt()
                            MainActivity.gCurrentBlePeripheral = null
                            MainActivity.closeControllerPadFragment(true)
                        })
                    .setNegativeButton(
                        R.string.cancel,
                        null
                    )
                    .show()
            }
        })

        val controllerPadConstraintLayout = view.findViewById<ConstraintLayout>(R.id.controllerPadConstraintLayout)
        val dbCtrl = DbCtrl.getInstance(requireContext())
        val controllerLayoutDetailList: List<ControllerLayoutDetail> = dbCtrl.ControllerLayoutDetail().selectByLayoutId(mLayoutId)
        controllerLayoutDetailList.forEach { controllerLayoutDetail ->
            var viewId: Int = -1
            when(controllerLayoutDetail.control_type) {
                Constants.cViewTypeButton -> {
                    var button: Button = Button(requireContext())
                    viewId = View.generateViewId()
                    button.id = viewId
                    button.isAllCaps = false
                    button.text = controllerLayoutDetail.text
                    button.setBackgroundColor(Color.rgb(controllerLayoutDetail.c1_red, controllerLayoutDetail.c1_green, controllerLayoutDetail.c1_blue))
                    button.setTextColor(Color.rgb(controllerLayoutDetail.c2_red, controllerLayoutDetail.c2_green, controllerLayoutDetail.c2_blue))
                    button.setOnTouchListener { view, motionEvent ->
                        button.onTouchEvent(motionEvent)
                        when(motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                button.alpha = 0.5f
                                mSendDataQueue.add(mSendValueList[view.id]!!.getSendValue(0,0))
                                //Log.d("button", mSendValueList[view.id]!!.getSendValue(0,0))
                            }
                            MotionEvent.ACTION_UP -> {
                                button.alpha = 1.0f
                            }
                            MotionEvent.ACTION_CANCEL -> {
                                button.alpha = 1.0f
                            }
                        }
                        return@setOnTouchListener true
                    }
                    controllerPadConstraintLayout.addView(button)
                    setConstraintSet(controllerPadConstraintLayout, viewId, controllerLayoutDetail.width, controllerLayoutDetail.height, controllerLayoutDetail.start, controllerLayoutDetail.top)
                }
                Constants.cViewTypeStick -> {
                    var stick: StickView = StickView(requireContext(), null)
                    viewId = View.generateViewId()
                    stick.id = viewId
                    mStickViewIdList.add(viewId)
                    stick.setSplitNum(controllerLayoutDetail.split)
                    stick.setStepNum(controllerLayoutDetail.step)
                    stick.setOnStickChangeListener(object:StickView.OnStickChangeListener{
                        override fun onStickChangeEvent(view: StickView) {
                            mSendDataQueue.add(mSendValueList[view.id]!!.getSendValue(view.NowStopStep,view.NowStopSplit))
                            //Log.d("stick", mSendValueList[view.id]!!.getSendValue(view.NowStopStep,view.NowStopSplit).toString())
                        }
                    })
                    controllerPadConstraintLayout.addView(stick)
                    setConstraintSet(controllerPadConstraintLayout, viewId, controllerLayoutDetail.width, controllerLayoutDetail.height, controllerLayoutDetail.start, controllerLayoutDetail.top)
                }
                Constants.cViewTypeWeb -> {
                    var webView: WebView = WebView(requireContext())
                    viewId = View.generateViewId()
                    webView.id = viewId
                    webView.setBackgroundColor(Color.GRAY)
                    webView.isClickable = true
                    webView.webViewClient = WebViewClient()
                    webView.clearCache(true)
                    webView.clearHistory()
                    webView.setInitialScale(100)
                    webView.loadUrl(controllerLayoutDetail.text)
                    val settings: WebSettings = webView.getSettings()
                    settings.javaScriptEnabled = true
                    controllerPadConstraintLayout.addView(webView)
                    setConstraintSet(controllerPadConstraintLayout, viewId, controllerLayoutDetail.width, controllerLayoutDetail.height, controllerLayoutDetail.start, controllerLayoutDetail.top)
                }
                Constants.cViewTypeLever -> {
                    var lever: LeverView = LeverView(requireContext(), null)
                    viewId = View.generateViewId()
                    lever.id = viewId
                    mLeverViewIdList.add(viewId)
                    lever.setStepNum(controllerLayoutDetail.step)
                    lever.setDefaultStep(controllerLayoutDetail.split)
                    lever.setVertical(controllerLayoutDetail.vertical)
                    lever.setReturnDefault(controllerLayoutDetail.return_default)
                    lever.setOnLeverChangeListener(object:LeverView.OnLeverChangeListener{
                        override fun onLeverChangeEvent(view: LeverView) {
                            mSendDataQueue.add(mSendValueList[view.id]!!.getSendValue(view.NowStopStep,0))
                            //Log.d("lever", mSendValueList[view.id]!!.getSendValue(view.NowStopStep,0).toString())
                        }
                    })
                    controllerPadConstraintLayout.addView(lever)
                    setConstraintSet(controllerPadConstraintLayout, viewId, controllerLayoutDetail.width, controllerLayoutDetail.height, controllerLayoutDetail.start, controllerLayoutDetail.top)
                }
            }

            val controlSendValueList: List<ControlSendValue> = dbCtrl.ControlSendValue().selectByIds(mLayoutId, controllerLayoutDetail.control_id)
            val sendValueMap = SendValueMap()
            controlSendValueList.forEach { controlSendValue ->
                sendValueMap.setSendValue(controlSendValue.point1, controlSendValue.point2, controlSendValue.send_value)
            }
            mSendValueList[viewId] = sendValueMap
        }

        val bleNotifyViewModel: BleNotifyViewModel? = ViewModelProvider(requireActivity(), BleNotifyViewModelFactory()).get(
            BleNotifyViewModel::class.java)
        bleNotifyViewModel!!.getBleNotifyLiveData().observe(requireActivity()) {
            it?.let {
                if (it.isNullOrEmpty() ||
                    it == Constants.cNotifyReady ||
                    it == Constants.cNotifyReadyNonRx ||
                    it == Constants.cNotifyGattFailed ||
                    it == Constants.cNotifyConnectFailed ||
                    it == Constants.cNotifyServiceNull ||
                    it == Constants.cNotifyCharacteristicNull
                ) {
                    return@let
                }
                view.findViewById<TextView>(R.id.controllerPadNoticeTextView).text = it
            }
        }

        Timer().scheduleAtFixedRate(500, 500) {
            var delay: Long = 0
            when (mSendAlways) {
                1 -> {
                    mStickViewIdList.forEach {  id ->
                        var stickView = controllerPadConstraintLayout.findViewById<StickView>(id)
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            mSendDataQueue.add(mSendValueList[stickView.id]!!.getSendValue(stickView.NowStopStep,stickView.NowStopSplit))
                        }, delay)
                        delay += 100
                    }
                }
                2 -> {
                    mLeverViewIdList.forEach {  id ->
                        var leverView = controllerPadConstraintLayout.findViewById<LeverView>(id)
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            mSendDataQueue.add(mSendValueList[leverView.id]!!.getSendValue(leverView.NowStopStep,0))
                        }, delay)
                        delay += 100
                    }
                }
                3 -> {
                    mStickViewIdList.forEach {  id ->
                        var stickView = controllerPadConstraintLayout.findViewById<StickView>(id)
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            mSendDataQueue.add(mSendValueList[stickView.id]!!.getSendValue(stickView.NowStopStep,stickView.NowStopSplit))
                        }, delay)
                        delay += 100
                    }
                    mLeverViewIdList.forEach {  id ->
                        var leverView = controllerPadConstraintLayout.findViewById<LeverView>(id)
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            mSendDataQueue.add(mSendValueList[leverView.id]!!.getSendValue(leverView.NowStopStep,0))
                        }, delay)
                        delay += 100
                    }
                }
            }
        }

        Timer().scheduleAtFixedRate(0, 10) {
            if (!mSendDataQueue.isEmpty()) {
                sendValueBle(mSendDataQueue.removeFirst())
            }
        }

        return view
    }

    private fun setConstraintSet(constraintLayout: ConstraintLayout, viewId: Int, width: Int, height: Int, start: Int, top: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.constrainHeight(viewId, height)
        constraintSet.constrainWidth(viewId, width)
        constraintSet.connect(viewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, start)
        constraintSet.connect(viewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, top)
        constraintSet.applyTo(constraintLayout)
    }

    private fun sendValueBle(str: String) {
        if (MainActivity.gCurrentBlePeripheral == null) {
            return
        }

        if (MainActivity.gCurrentBlePeripheral!!.isConnected()) {
            MainActivity.gCurrentBlePeripheral!!.writeCharacteristicTx(str.toByteArray())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(layoutId: Int, connectionName: String) =
            ControllerPadFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, layoutId)
                    putString(ARG_PARAM2, connectionName)
                }
            }
    }
}