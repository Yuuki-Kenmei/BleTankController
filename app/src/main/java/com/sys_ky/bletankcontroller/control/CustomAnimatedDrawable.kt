/*
 * CustomAnimatedDrawable
 *
 * gif再生用Drawable取得用クラス
 * Android9.0（SDK28）未満
 */

package com.sys_ky.bletankcontroller.control

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.SystemClock
import java.io.InputStream

class CustomAnimatedDrawable(private val inputStream: InputStream): Drawable() {
    private val mMovie by lazy {
        Movie.decodeStream(inputStream)
    }

    private var mMovieStart = 0
    private var mLoop = true
    private var mStop = false
    private var mRelativeMillisecond = 0

    override fun draw(canvas: Canvas) {
        canvas.apply {
            drawColor(Color.TRANSPARENT)
//            scale(width / mMovie.width().toFloat(), height / mMovie.height().toFloat() )
        }
        val now = SystemClock.uptimeMillis()

        if (mMovieStart == 0) {
            mMovieStart = now.toInt()
        }

        mRelativeMillisecond = when {
            mStop -> {
                mMovieStart = 0
                mRelativeMillisecond
            }
            mLoop -> ((now - mMovieStart) % mMovie.duration()).toInt()
            else -> (now - mMovieStart).toInt()
        }

        mMovie.apply {
            setTime(mRelativeMillisecond)
            draw(canvas, 0f, 0f)
        }

        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity(): Int = PixelFormat.UNKNOWN

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    fun stop() {
        mStop = true
    }

    fun start() {
        mStop = false
        mMovieStart = 0
    }

    fun isRunning() = !mStop
}