/*
 * StickView
 *
 * スティックコントロールクラス
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
import kotlin.math.*

private interface StickMoveProperty {
    val NowStopStep: Int
    val NowStopSplit: Int
    val PrevStopStep: Int
    val PrevStopSplit: Int
}

private class SplitPoint constructor(startX: Float, startY: Float, endX: Float, endY: Float) {
    var startPointX: Float = 0f
    var startPointY: Float = 0f
    var endPointX: Float = 0f
    var endPointY: Float = 0f
    init {
        startPointX = startX
        startPointY = startY
        endPointX = endX
        endPointY = endY
    }
}

private class StopPoint constructor(deg: Float, pointX: Float, pointY: Float, step: Int, split: Int) {
    var degree: Float = 0f
    var x: Float = 0f
    var y: Float = 0f
    var stepId: Int = 0
    var splitId: Int = 0
    init {
        degree = deg
        x = pointX
        y = pointY
        stepId = step
        splitId = split
    }
}

class StickView : View, StickMoveProperty{

    interface OnStickChangeListener: EventListener {
        fun onStickChangeEvent(view: StickView)
    }
    private var onStickChangeListener: OnStickChangeListener? = null
    fun setOnStickChangeListener(listener: OnStickChangeListener) {
        onStickChangeListener = listener
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mEnableFlg = true
    }

    constructor(context: Context?, attrs: AttributeSet?, enable: Boolean) : super(context, attrs) {
        mEnableFlg = enable
    }

    //スティックとビュー領域の比率
    private val cStickRatio: Float = 4.5f
    private val cStickInRatio: Float = 4.9f

    //ビュー全体の背景の幅・高さ
    private var mDrawRectWidth: Float = 0f
    private var mDrawRectHeight: Float = 0f
    //ビューの中心座標
    private var mCenterX: Float = 0f
    private var mCenterY: Float = 0f
    //スティック可動エリアとスティック、スティック内の線の半径
    private var mDrawAreaRadius: Float = 0f
    private var mDrawStickRadius: Float = 0f
    private var mDrawStrokeInStickRadius: Float = 0f
    //スティックの最大移動距離（ビュー中心からスティック中心までの直線距離）
    private var mMaxShift: Float = 0f
    //現在のスティック中心座標
    private var mNowCenterX = 0f
    private var mNowCenterY = 0f

    //ペイント
    private val mPaintRect: Paint = Paint()
    private val mPaintRectStroke: Paint = Paint()
    private val mPaintArea: Paint = Paint()
    private val mPaintAreaStroke: Paint = Paint()
    private val mPaintStick: Paint = Paint()
    private val mPaintStickStroke: Paint = Paint()
    private val mPaintLine: Paint = Paint()
    private val mPaintStep: Paint = Paint()
    private val mPaintDot: Paint = Paint()

    //初回描画判定用フラグ
    private var mFirstFlg = true

    //停止位置ドット描画位置
    private var mDrawDotX: Float = -1f
    private var mDrawDotY: Float = -1f

    //タッチしてスライドした移動量
    private var mMoveX: Float = 0f
    private var mMoveY: Float = 0f

    //スティック内をタッチしてスタートしたか
    private var mTouchStartFlg: Boolean = false

    //タッチ開始時の座標
    private var mTouchStartPosX: Float = 0f
    private var mTouchStartPosY: Float = 0f

    //スティックを有効にするか
    private var mEnableFlg: Boolean = true
    //スティックを非表示にするか
    private var mHiddenFlg: Boolean = false
    //分割数
    private var mSplitNum: Int = 0
    //段階数
    private var mStepNum: Int = 0

    //分割線のポジションリスト
    private val mSplitList: MutableList<SplitPoint> = mutableListOf()
    //段階円の半径リスト
    private val mStepList: MutableList<Float> = mutableListOf()
    //停止位置の座標リスト
    private val mStopPointList: MutableList<StopPoint> = mutableListOf()

    //プロパティ
    override var NowStopStep: Int = 0
        private set
    override var NowStopSplit: Int = 0
        private set
    override var PrevStopStep: Int = 0
        private set
    override var PrevStopSplit: Int = 0
        private set

    init {
        //描画用ペイントを定義
        //背景用の四角
        mPaintRect.style = Paint.Style.FILL
        mPaintRect.color = Color.WHITE
        mPaintRect.alpha = 0
        //背景用の四角の枠線
        mPaintRectStroke.color = Color.BLUE
        mPaintRectStroke.style = Paint.Style.STROKE
        mPaintRectStroke.strokeWidth = 4f
        //スティック可動エリアの円
        mPaintArea.color = Color.LTGRAY
        mPaintArea.style = Paint.Style.FILL
        //スティック可動エリアの枠線
        mPaintAreaStroke.color = Color.LTGRAY
        mPaintAreaStroke.style = Paint.Style.STROKE
        mPaintAreaStroke.strokeWidth = 4f
        //スティック用の円
        mPaintStick.color = Color.DKGRAY
        mPaintStick.style = Paint.Style.FILL
        //スティックの内側の線用の円
        mPaintStickStroke.color = Color.BLACK
        mPaintStickStroke.style = Paint.Style.STROKE
        mPaintStickStroke.strokeWidth = 4f
        //スティック稼働エリアの分割の線
        mPaintLine.color = Color.WHITE
        mPaintLine.style = Paint.Style.STROKE
        mPaintLine.strokeWidth = 4f
        //スティック稼働エリアの段階の円
        mPaintStep.color = Color.WHITE
        mPaintStep.style = Paint.Style.STROKE
        mPaintStep.strokeWidth = 4f
        //停止位置の点
        mPaintDot.color = Color.RED
        mPaintDot.strokeWidth = 20f
    }

    override fun onDraw(canvas: Canvas?) {

        //初期状態で中心座標など保存
        if (mFirstFlg || !mEnableFlg) {
            mSplitList.clear()
            mStepList.clear()

            mDrawRectWidth = right.toFloat() - left.toFloat()
            mDrawRectHeight = bottom.toFloat() - top.toFloat()
            mCenterX = (right.toFloat() - left.toFloat()) / 2f
            mCenterY = (bottom.toFloat() - top.toFloat()) / 2f
            //幅と高さで小さい方に円の半径を合わせる
            if (mDrawRectWidth >= mDrawRectHeight) {
                mDrawStickRadius = (bottom.toFloat() - top.toFloat()) / 2f / cStickRatio
                mDrawStrokeInStickRadius = (bottom.toFloat() - top.toFloat()) / 2f / cStickInRatio
                mDrawAreaRadius = (bottom.toFloat() - top.toFloat()) / 2f - mDrawStickRadius
            } else {
                mDrawStickRadius = (right.toFloat() - left.toFloat()) / 2f / cStickRatio
                mDrawStrokeInStickRadius = (right.toFloat() - left.toFloat()) / 2f / cStickInRatio
                mDrawAreaRadius = (right.toFloat() - left.toFloat()) / 2f - mDrawStickRadius
            }

            //分割線の座標計算
            val splitDeg: Float = 360f / mSplitNum
            var nowDeg: Float = 0f
            while(nowDeg < 360) {
                var px = 0f
                var py = 0f
                when(nowDeg) {
                    0f -> {
                        px = 0f
                        py = mDrawAreaRadius * -1f
                    }
                    90f -> {
                        px = mDrawAreaRadius
                        py = 0f
                    }
                    180f -> {
                        px = 0f
                        py = mDrawAreaRadius
                    }
                    270f -> {
                        px = mDrawAreaRadius * -1
                        py = 0f
                    }
                    else -> {
                        val rad = Math.toRadians((nowDeg % 90f).toDouble())
                        px = mDrawAreaRadius * Math.cos(rad).toFloat()
                        py = mDrawAreaRadius * Math.sin(rad).toFloat()
                        when {
                            nowDeg <= 90 -> {
                                py *= -1
                            }
                            nowDeg <= 180 -> {
                            }
                            nowDeg <= 270 -> {
                                px *= -1
                            }
                            else -> {
                                px *= -1
                                py *= -1
                            }
                        }
                    }
                }

                val startPointX = mCenterX + px
                val startPointY = mCenterY + py
                val endPointX = mCenterX - px
                val endPointY = mCenterY - py
                mSplitList.add(SplitPoint(startPointX, startPointY, endPointX, endPointY))

                nowDeg += splitDeg
            }

            //段階円の半径計算
            val stepRadius: Float = mDrawAreaRadius / mStepNum
            var nowRadius: Float = 0f
            for (i in 2..mStepNum) {
                nowRadius += stepRadius
                mStepList.add(nowRadius)
            }

            //停止位置の計算
            var splitCount = 0
            nowDeg = 0f
            while(nowDeg < 360) {
                nowRadius = 0f
                for (stepCount in 1..mStepNum) {
                    nowRadius += stepRadius
                    var px = 0f
                    var py = 0f
                    when (nowDeg) {
                        0f -> {
                            px = 0f
                            py = nowRadius * -1f
                        }
                        90f -> {
                            px = nowRadius
                            py = 0f
                        }
                        180f -> {
                            px = 0f
                            py = nowRadius
                        }
                        270f -> {
                            px = nowRadius * -1
                            py = 0f
                        }
                        else -> {
                            val rad = Math.toRadians((nowDeg % 90f).toDouble())
                            px = nowRadius * Math.cos(rad).toFloat()
                            py = nowRadius * Math.sin(rad).toFloat()
                            when {
                                nowDeg <= 90 -> {
                                    py *= -1
                                }
                                nowDeg <= 180 -> {

                                }
                                nowDeg <= 270 -> {
                                    px *= -1
                                }
                                else -> {
                                    px *= -1
                                    py *= -1
                                }
                            }
                        }
                    }
                    mStopPointList.add(StopPoint(nowDeg, mCenterX + px, mCenterY + py, stepCount, splitCount))
                }
                nowDeg += splitDeg
                splitCount++
            }
            //最後に中心を追加
            mStopPointList.add(StopPoint(-1f, mCenterX, mCenterY, 0, 0))

            mMaxShift = mDrawAreaRadius

            mNowCenterX = mCenterX
            mNowCenterY = mCenterY

            mFirstFlg = false
        }

        canvas?.drawRect(mPaintRectStroke.strokeWidth, mPaintRectStroke.strokeWidth, mDrawRectWidth - mPaintRectStroke.strokeWidth * 2,mDrawRectHeight - mPaintRectStroke.strokeWidth * 2, mPaintRect)
        canvas?.drawCircle(mCenterX, mCenterY,mDrawAreaRadius, mPaintArea)
        mSplitList.forEach {
            canvas?.drawLine(it.startPointX, it.startPointY, it.endPointX, it.endPointY, mPaintLine)
        }
        mStepList.forEach {
            canvas?.drawCircle(mCenterX, mCenterY, it, mPaintStep)
        }

        canvas?.drawCircle(mCenterX, mCenterY, mDrawAreaRadius, mPaintAreaStroke)
        if (!mHiddenFlg) {
            canvas?.drawCircle(mNowCenterX, mNowCenterY, mDrawStickRadius, mPaintStick)
            canvas?.drawCircle(mNowCenterX, mNowCenterY, mDrawStrokeInStickRadius, mPaintStickStroke)
        }
        if (mDrawDotX >= 0 && mDrawDotY >= 0) {
            canvas?.drawPoint(mDrawDotX, mDrawDotY, mPaintDot)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //スティックが有効でない場合、何もしない
        if (!mEnableFlg) {
            return true
        }

        when(event.action) {
            MotionEvent.ACTION_DOWN -> {

                if (sqrt((mCenterX - event.x).toDouble().pow(2.0) + (mCenterY - event.y).toDouble().pow(2.0)) > mDrawStickRadius) {
                    return true
                }

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

                var nowPosX = event.x
                var nowPosY = event.y

                mMoveX = nowPosX - mTouchStartPosX
                mMoveY = nowPosY - mTouchStartPosY
            }
            MotionEvent.ACTION_UP -> {
                if (!mTouchStartFlg) {
                    return true
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

        if (mMoveX != 0f || mMoveY != 0f) {
            //移動量が最大距離を上回る場合、角度を計算して最大距離に対するXとYを計算
            val touchShift: Double = sqrt((mMoveX * mMoveX + mMoveY * mMoveY).toDouble())
            if (touchShift.toFloat() > mMaxShift) {
                val rad: Double = atan((mMoveX / mMoveY).toDouble())
                var x: Double = abs(mMaxShift * sin(rad))
                var y: Double = abs(mMaxShift * cos(rad))
                if (mMoveX < 0f) {
                    x *= -1f
                }
                if (mMoveY < 0f) {
                    y *= -1f
                }
                mNowCenterX = mCenterX + x.toFloat()
                mNowCenterY = mCenterY + y.toFloat()
            } else {
                mNowCenterX = mCenterX + mMoveX
                mNowCenterY = mCenterY + mMoveY
            }

            mMoveX = 0f
            mMoveY = 0f
        } else {
            mNowCenterX = mCenterX
            mNowCenterY = mCenterY
        }

        //スティック位置計算結果から一番近いStopPointを最終的なスティック位置にする
        var compass = 0
        if (mNowCenterX > mCenterX) {
            if (mNowCenterY > mCenterY) {
                //90～180°
                compass = 2
            } else {
                //0～90°
                compass = 1
            }
        } else {
            if (mNowCenterY > mCenterY) {
                //180～270°
                compass = 3
            } else {
                //270～360°
                compass = 4
            }
        }

        var nearlyShift: Double = -1.0
        var nearlyPoint: StopPoint = StopPoint(-1f, mCenterX, mCenterY, 0, 0)
        mStopPointList.forEach { stopPoint ->
            when(compass) {
                1 -> {
                    if (stopPoint.degree == -1f || (stopPoint.degree in 0f..90f)) {
                    } else {
                        return@forEach
                    }
                }
                2 -> {
                    if (stopPoint.degree == -1f || (stopPoint.degree in 90f..180f)) {
                    } else {
                        return@forEach
                    }
                }
                3 -> {
                    if (stopPoint.degree == -1f || (stopPoint.degree in 180f..270f)) {
                    } else {
                        return@forEach
                    }
                }
                4 -> {
                    if (stopPoint.degree == -1f || (stopPoint.degree in 270f..360f)) {
                    } else {
                        return@forEach
                    }
                }
                else -> {
                    return@forEach
                }
            }
            val shiftX: Float = mNowCenterX - stopPoint.x
            val shiftY: Float = mNowCenterY - stopPoint.y
            val calcShift: Double = Math.sqrt((shiftX * shiftX + shiftY * shiftY).toDouble())
            if (nearlyShift == -1.0 || nearlyShift > calcShift) {
                nearlyPoint = stopPoint
                nearlyShift = calcShift
            }
        }
        mNowCenterX = nearlyPoint.x
        mNowCenterY = nearlyPoint.y

        NowStopStep = nearlyPoint.stepId
        NowStopSplit = nearlyPoint.splitId

        if (PrevStopStep != NowStopStep || PrevStopSplit != NowStopSplit) {
            if (onStickChangeListener != null) {
                onStickChangeListener!!.onStickChangeEvent(this)
            }
            PrevStopStep = NowStopStep
            PrevStopSplit = NowStopSplit
        }

        invalidate()

        return true
    }

    fun setSplitNum(split: Int) {
        mSplitNum = split
        mFirstFlg = true
    }

    fun setStepNum(step: Int) {
        mStepNum = step
        mFirstFlg = true
    }

    fun getSplitNum(): Int {
        return mSplitNum
    }

    fun getStepNum(): Int {
        return mStepNum
    }

    fun drawDotStopPoint(step: Int, split: Int) {
        mDrawDotX = -1f
        mDrawDotY = -1f

        mStopPointList.forEach { stopPoint ->
            if (stopPoint.stepId == step && stopPoint.splitId == split) {
                mDrawDotX = stopPoint.x
                mDrawDotY = stopPoint.y
                return@forEach
            }
        }
    }

    fun setEnableStick(enable: Boolean) {
        mEnableFlg = enable
    }

    fun setHiddenStick(hidden: Boolean) {
        mHiddenFlg = hidden
    }

    fun setStickColor(r: Int, g: Int, b: Int) {
        mPaintStick.setARGB(255, r, g, b)
    }

    fun setMoveAreaColor(r: Int, g: Int, b: Int) {
        mPaintArea.setARGB(255, r, g, b)
        mPaintAreaStroke.setARGB(255, r, g, b)
    }

    fun setLineColor(r: Int, g: Int, b: Int) {
        mPaintLine.setARGB(255, r, g, b)
        mPaintStep.setARGB(255, r, g, b)
    }
}