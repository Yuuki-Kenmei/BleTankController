/*
 * OneClickListener
 *
 * 多重クリックを防止用のOnClickListenerの拡張クラス
 */

package com.sys_ky.bletankcontroller.control

import android.os.SystemClock
import android.view.View

abstract class OneClickListener : View.OnClickListener {

    private val mInterval: Long = 500
    private var mLastClickTime: Long = 0

    override fun onClick(view: View) {
        //前回クリックから500ミリ秒以内のクリックは無効
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - mLastClickTime > mInterval) {
            mLastClickTime = currentTime
            onOneClick(view)
        }
    }

    abstract fun onOneClick(view: View)
}