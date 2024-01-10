/*
 * ConstraintLayoutWithTouchGif
 *
 * タッチ時GIF再生用レイアウト
 */

package com.sys_ky.bletankcontroller.control

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.sys_ky.bletankcontroller.R
import java.io.IOException
import java.util.Timer
import java.util.TimerTask

class ConstraintLayoutWithTouchGif constructor(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val cWidth: Int = 100
    private val cHeight: Int = 100

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (Build.VERSION.SDK_INT >= 28) {
                    startGif(event!!)
                } else {
                    startAnimate(event!!)
                }
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startGif(event: MotionEvent) {
        try {
            var imageView = ImageView(context, null)
            imageView.id = generateViewId()
            addView(imageView)
            var constraintSet: ConstraintSet = ConstraintSet()
            constraintSet.clone(this)
            constraintSet.connect(imageView.id, ConstraintSet.LEFT, this.id, ConstraintSet.LEFT, event.x.toInt() - cWidth / 2)
            constraintSet.connect(imageView.id, ConstraintSet.TOP, this.id, ConstraintSet.TOP, event.y.toInt() - cHeight / 2)
            constraintSet.constrainWidth(imageView.id, cWidth)
            constraintSet.constrainHeight(imageView.id, cHeight)
            constraintSet.applyTo(this)

            val source = ImageDecoder.createSource(resources, R.raw.explosion)
            val drawable = ImageDecoder.decodeDrawable(source) as AnimatedImageDrawable
            drawable.repeatCount = 1
            imageView.setImageDrawable(drawable)
            drawable.start()

            var timer: Timer? = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        drawable.stop()
                        removeView(imageView)
                    }
                }
            }, 500)

        } catch (e: IOException) {

        } catch (e: java.lang.ClassCastException) {

        }
    }

    private fun startAnimate(event: MotionEvent) {
        try {
            var imageView = ImageView(context, null)
            imageView.id = generateViewId()
            addView(imageView)
            var constraintSet: ConstraintSet = ConstraintSet()
            constraintSet.clone(this)
            constraintSet.connect(imageView.id, ConstraintSet.LEFT, this.id, ConstraintSet.LEFT, event.x.toInt() - cWidth / 2)
            constraintSet.connect(imageView.id, ConstraintSet.TOP, this.id, ConstraintSet.TOP, event.y.toInt() - cHeight / 2)
            constraintSet.constrainWidth(imageView.id, cWidth)
            constraintSet.constrainHeight(imageView.id, cHeight)
            constraintSet.applyTo(this)

            val inputStream =resources.openRawResource(R.raw.explosion)
            val drawable = CustomAnimatedDrawable(inputStream)
            imageView.setImageDrawable(drawable)

            var timer: Timer? = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    Handler(Looper.getMainLooper()).post {
                        drawable.stop()
                        removeView(imageView)
                    }
                }
            }, 500)

        } catch (e: IOException) {

        } catch (e: java.lang.ClassCastException) {

        }
    }
}