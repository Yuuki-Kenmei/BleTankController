/*
 * LeverView
 *
 * レバーコントロールクラス
 */

package com.sys_ky.bletankcontroller.control

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.EventListener
import kotlin.math.abs

private interface LeverMoveProperty {
    val NowStopStep: Int
    val PrevStopStep: Int
}

private class StopPointLever constructor(dist: Float, step: Int) {
    var distance: Float = 0f
    var stepId: Int = 0
    init {
        distance = dist
        stepId = step
    }
}

class LeverView : View, LeverMoveProperty {

    interface OnLeverChangeListener: EventListener {
        fun onLeverChangeEvent(view: LeverView)
    }
    private var onLeverChangeListener: OnLeverChangeListener? = null
    fun setOnLeverChangeListener(listener: OnLeverChangeListener) {
        onLeverChangeListener = listener
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mEnableFlg = true
    }

    constructor(context: Context?, attrs: AttributeSet?, enable: Boolean) : super(context, attrs) {
        mEnableFlg = enable
    }

    //ビュー領域に対して描画範囲の余白
    private val cDrawMargin: Float = 25f

    //描画範囲の背景の幅・高さ
    private var mDrawRectLeft: Float = 0f
    private var mDrawRectRight: Float = 0f
    private var mDrawRectTop: Float = 0f
    private var mDrawRectBottom: Float = 0f
    private var mDrawRectWidth: Float = 0f
    private var mDrawRectHeight: Float = 0f
    //描画範囲の中心座標
    private var mCenter: Float = 0f
    //レバーの幅・高さ
    private var mDrawLeverWidth: Float = 0f
    private var mDrawLeverHeight: Float = 0f

    //現在のレバー距離
    private var mNowDistance = 0f

    //ペイント
    private val mPaintRect: Paint = Paint()
    private val mPaintRectStroke: Paint = Paint()
    private val mPaintLever: Paint = Paint()
    private val mPaintLeverStroke: Paint = Paint()
    private val mPaintLeverDot: Paint = Paint()
    private val mPaintStep: Paint = Paint()
    private val mPaintDot: Paint = Paint()

    //初回描画判定用フラグ
    private var mFirstFlg = true

    //停止位置ドット描画位置
    private var mDrawDotDist: Float = -1f

    //タッチしてスライドした移動量
    private var mMoveX: Float = 0f
    private var mMoveY: Float = 0f

    //レバー内をタッチしてスタートしたか
    private var mTouchStartFlg: Boolean = false

    //タッチ開始時の座標
    private var mTouchStartPosX: Float = 0f
    private var mTouchStartPosY: Float = 0f

    //レバーを有効にするか
    private var mEnableFlg: Boolean = true
    //レバーを非表示にするか
    private var mHiddenFlg: Boolean = false
    //段階数
    private var mStepNum: Int = 0
    //初期位置段階
    private var mDefaultStep: Int = 0
    //初期位置
    private var mDefaultDistance: Float = 0f
    //レバー移動が縦向きか横向きか
    private var mVertical: Boolean = true
    //指を離したとき初期位置に戻すか
    private var mReturnDefault: Boolean = true

    //停止位置の座標リスト
    private val mStopPointList: MutableList<StopPointLever> = mutableListOf()

    //プロパティ
    override var NowStopStep: Int = 0
        private set
    override var PrevStopStep: Int = 0
        private set

    init {
        //描画用ペイントを定義
        //背景用の四角
        mPaintRect.style = Paint.Style.FILL
        mPaintRect.color = Color.WHITE
        mPaintRect.alpha = 128
        //レバー用の四角
        mPaintLever.style = Paint.Style.FILL
        mPaintLever.color = Color.DKGRAY
        //レバー用の枠線
        mPaintLeverStroke.color = Color.LTGRAY
        mPaintLeverStroke.style = Paint.Style.STROKE
        mPaintLeverStroke.strokeWidth = 4f
        //レバー稼働エリアの段階の線
        mPaintStep.color = Color.BLUE
        mPaintStep.style = Paint.Style.STROKE
        mPaintStep.strokeWidth = 4f
        //レバーデフォルト位置の点
        mPaintLeverDot.color = Color.RED
        mPaintLeverDot.strokeWidth = 40f
        //停止位置の点
        mPaintDot.color = Color.RED
        mPaintDot.strokeWidth = 20f
    }

    override fun onDraw(canvas: Canvas?) {

        //初期状態で中心座標など保存
        if (mFirstFlg || !mEnableFlg) {
            mStopPointList.clear()

            mDrawRectLeft = left.toFloat() + cDrawMargin
            mDrawRectRight = right.toFloat() - cDrawMargin
            mDrawRectTop = top.toFloat() + cDrawMargin
            mDrawRectBottom = bottom.toFloat() - cDrawMargin

            mDrawRectWidth = mDrawRectRight - mDrawRectLeft
            mDrawRectHeight = mDrawRectBottom - mDrawRectTop

            var stepDist: Float = 0f
            var nowDist: Float = 0f
            if (mVertical) {
                mCenter = (right.toFloat() - left.toFloat()) / 2f
                stepDist = mDrawRectHeight / mStepNum
                nowDist = mDrawRectHeight + cDrawMargin
                mDrawLeverWidth = mDrawRectWidth
                mDrawLeverHeight = 40f
            } else {
                mCenter = (bottom.toFloat() - top.toFloat()) / 2f
                stepDist = mDrawRectWidth / mStepNum
                nowDist = cDrawMargin
                mDrawLeverWidth = 40f
                mDrawLeverHeight = mDrawRectHeight
            }

            //停止位置
            for (i in 0 until mStepNum + 1) {
                mStopPointList.add(StopPointLever(nowDist, i))
                if (i == mDefaultStep) {
                    mDefaultDistance = nowDist
                }

                if (mVertical) {
                    nowDist -= stepDist
                } else {
                    nowDist += stepDist
                }
            }

            mNowDistance = mDefaultDistance

            mFirstFlg = false
        }

        canvas?.drawRect(cDrawMargin, cDrawMargin, cDrawMargin + mDrawRectWidth, cDrawMargin + mDrawRectHeight, mPaintRect)

        var sCnt = 0
        var sMargin = 0f
        mStopPointList.forEach {
            if (sCnt == 0 || sCnt == mStopPointList.size - 1) {
                sMargin = 0f
            } else {
                if (mVertical) {
                    sMargin = mDrawRectWidth / 4f
                } else {
                    sMargin = mDrawRectHeight / 4f
                }
            }
            if (mVertical) {
                canvas?.drawLine(cDrawMargin + sMargin, it.distance, cDrawMargin + mDrawRectWidth - sMargin, it.distance, mPaintStep)
            } else {
                canvas?.drawLine(it.distance, cDrawMargin + sMargin, it.distance, cDrawMargin + mDrawRectHeight - sMargin, mPaintStep)
            }

            if (sCnt == mDefaultStep) {
                if (mVertical) {
                    canvas?.drawPoint(mCenter, it.distance, mPaintLeverDot)
                } else {
                    canvas?.drawPoint(it.distance, mCenter, mPaintLeverDot)
                }
            }

            sCnt++
        }
        if (mVertical) {
            canvas?.drawLine(mCenter, cDrawMargin, mCenter, cDrawMargin + mDrawRectHeight, mPaintStep)
        } else {
            canvas?.drawLine(cDrawMargin, mCenter, cDrawMargin + mDrawRectWidth, mCenter, mPaintStep)
        }


        if (!mHiddenFlg) {
            if (mVertical) {
                canvas?.drawRect(cDrawMargin, mNowDistance - mDrawLeverHeight / 2, cDrawMargin + mDrawLeverWidth, mNowDistance + mDrawLeverHeight / 2, mPaintLever)
                canvas?.drawRect(cDrawMargin, mNowDistance - mDrawLeverHeight / 2, cDrawMargin + mDrawLeverWidth, mNowDistance + mDrawLeverHeight / 2, mPaintLeverStroke)
            } else {
                canvas?.drawRect(mNowDistance - mDrawLeverWidth / 2, cDrawMargin, mNowDistance + mDrawLeverWidth / 2, cDrawMargin + mDrawLeverHeight, mPaintLever)
                canvas?.drawRect(mNowDistance - mDrawLeverWidth / 2, cDrawMargin, mNowDistance + mDrawLeverWidth / 2, cDrawMargin + mDrawLeverHeight, mPaintLeverStroke)
            }
        }

        if (mDrawDotDist >= 0) {
            if (mVertical) {
                canvas?.drawPoint(mCenter, mDrawDotDist, mPaintDot)
            } else {
                canvas?.drawPoint(mDrawDotDist, mCenter, mPaintDot)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //レバーが有効でない場合、何もしない
        if (!mEnableFlg) {
            return true
        }

        when(event.action) {
            MotionEvent.ACTION_DOWN -> {

                mTouchStartFlg = true

                mTouchStartPosX = event.x
                mTouchStartPosY = event.y

                mMoveX = 0f
                mMoveY = 0f
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mTouchStartFlg) {
                    return true
                }

                mMoveX = event.x - mTouchStartPosX
                mMoveY = event.y - mTouchStartPosY
            }
            MotionEvent.ACTION_UP -> {
                if (!mTouchStartFlg) {
                    return true
                }

                if (!mReturnDefault) {
                    mDefaultDistance = mNowDistance
                }

                mTouchStartFlg = false

                mTouchStartPosX = 0f
                mTouchStartPosY = 0f

                mMoveX = 0f
                mMoveY = 0f
            }
            else -> {
            }
        }

        if (mVertical) {
            mNowDistance = mDefaultDistance + mMoveY
        } else {
            mNowDistance = mDefaultDistance + mMoveX
        }

        var nearlyShift = -1f
        var nearlyPoint = StopPointLever(mDefaultDistance, mDefaultStep)
        mStopPointList.forEach { stopPoint ->
            val calcShift = abs(mNowDistance - stopPoint.distance)
            if (nearlyShift == -1f || nearlyShift > calcShift) {
                nearlyPoint = stopPoint
                nearlyShift = calcShift
            }
        }

        mNowDistance = nearlyPoint.distance
        NowStopStep = nearlyPoint.stepId

        if (PrevStopStep != NowStopStep) {
            if (onLeverChangeListener != null) {
                onLeverChangeListener!!.onLeverChangeEvent(this)
            }
            PrevStopStep = NowStopStep
        }

        invalidate()

        return true
    }

    fun setStepNum(step: Int) {
        mStepNum = step
        mFirstFlg = true
    }

    fun setDefaultStep(step: Int) {
        mDefaultStep = step
        mFirstFlg = true
    }

    fun setVertical(vertical: Boolean) {
        mVertical = vertical
        mFirstFlg = true
    }

    fun setReturnDefault(returnDefault: Boolean) {
        mReturnDefault = returnDefault
        mFirstFlg = true
    }

    fun getStepNum(): Int {
        return mStepNum
    }

    fun getDefaultStep(): Int {
        return mDefaultStep
    }

    fun getVertical(): Boolean {
        return mVertical
    }

    fun getReturnDefault(): Boolean {
        return mReturnDefault
    }

    fun drawDotStopPoint(step: Int) {
        mDrawDotDist = -1f

        mStopPointList.forEach { stopPoint ->
            if (stopPoint.stepId == step) {
                mDrawDotDist = stopPoint.distance
                return@forEach
            }
        }
    }

    fun setEnableLever(enable: Boolean) {
        mEnableFlg = enable
    }

    fun setHiddenLever(hidden: Boolean) {
        mHiddenFlg = hidden
    }

    fun setLeverColor(r: Int, g: Int, b: Int) {
        mPaintLever.setARGB(255, r, g, b)
    }

    fun setMoveAreaColor(r: Int, g: Int, b: Int) {
        mPaintRect.setARGB(255, r, g, b)
        mPaintRectStroke.setARGB(255, r, g, b)
    }

    fun setLineColor(r: Int, g: Int, b: Int) {
        mPaintStep.setARGB(255, r, g, b)
    }
}