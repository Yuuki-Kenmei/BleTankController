/*
 * CustomImageButton
 *
 * ImageButtonの拡張クラス
 * 通常のImageButtonに多重クリック防止とタッチ時半透過を追加
 */

package com.sys_ky.bletankcontroller.control

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageButton

class CustomImageButton constructor(context: Context, attrs: AttributeSet?) : AppCompatImageButton(context, attrs) {

    lateinit var listener: OneClickListener
    fun setOnOneClickListener(l:OneClickListener) {
        listener = l
        this.setOnClickListener(listener)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //タッチしたとき半透明にする
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                this.alpha = 0.5f
            }
            MotionEvent.ACTION_UP -> {
                this.alpha = 1.0f
            }
            MotionEvent.ACTION_CANCEL -> {
                this.alpha = 1.0f
            }
        }
        return super.onTouchEvent(event)
    }
}