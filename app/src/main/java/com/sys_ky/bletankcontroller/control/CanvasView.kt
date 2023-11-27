/*
 * CanvasView
 *
 * コントローラーレイアウト用コントロール
 */

package com.sys_ky.bletankcontroller.control

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.sys_ky.bletankcontroller.MainActivity
import com.sys_ky.bletankcontroller.R
import java.lang.Exception
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
import com.sys_ky.bletankcontroller.common.Constants
import com.sys_ky.bletankcontroller.common.GridPoint
import com.sys_ky.bletankcontroller.common.SendValueMap
import com.sys_ky.bletankcontroller.common.ViewConfig
import com.sys_ky.bletankcontroller.control.LeverView

class CanvasView constructor(context: Context, attrs: AttributeSet) :ConstraintLayout(context, attrs) {

    //各コントロールの最小ブロック数
    private val cStickMinBlocks: Int = 4
    private val cButtonMinBlocks: Int = 2
    private val cWebMinBlocks: Int = 4
    private val cLeverMinBlocks: Int = 4

    //各コントロールの最小サイズ
    private var mStickMinWidth: Int = 0
    private var mStickMinHeight: Int = 0
    private var mButtonMinWidth: Int = 0
    private var mButtonMinHeight: Int = 0
    private var mWebMinWidth: Int = 0
    private var mWebMinHeight: Int = 0
    private var mLeverMinWidth: Int = 0
    private var mLeverMinHeight: Int = 0

    var mViewType: Int = Constants.cViewTypeButton
    var mLocateMode: Int = Constants.cLocateModeGrid

    private var mGridPointMap: MutableMap<Array<Int>, GridPoint> = mutableMapOf()
    private val mViewConfigList: MutableMap<Int, ViewConfig> = mutableMapOf()

    private var mPlaceTouchStartPosX: Float = 0f
    private var mPlaceTouchStartPosY: Float = 0f
    private var mPlaceTouchNowPosX: Float = 0f
    private var mPlaceTouchNowPosY: Float = 0f
    private var mPlacingFlg: Boolean = false

    private var mPlacingViewId: Int = -1

    //コントロール配置領域限界
    private var mPlaceMaxTop: Int = 0
    private var mPlaceMaxLeft: Int = 0
    private var mPlaceMaxRight: Int = 0
    private var mPlaceMaxBottom: Int = 0

    //各コントロールの配置数
    private var mButtonCount: Int = 0
    private var mStickCount: Int = 0
    private var mWebCount: Int = 0
    private var mLeverCount: Int = 0
    private var mButtonViewNo: MutableMap<Int, Int> = mutableMapOf()
    private var mStickViewNo: MutableMap<Int, Int> = mutableMapOf()
    private var mWebViewNo: MutableMap<Int, Int> = mutableMapOf()
    private var mLeverViewNo: MutableMap<Int, Int> = mutableMapOf()

    private val mPaintMinRect: Paint = Paint()
    private val mPaintOverlapRect: Paint = Paint()
    private val mPaintPlacingViewRect: Paint = Paint()
    private val mPaintMaxTopLine: Paint = Paint()
    private val mPaintGridLine: Paint = Paint()

    //ゴミ箱イメージ
    private var mTrashImageView: ImageView? = null
    private var mTrashImageWidth: Int = 0
    private var mTrashImageHeight: Int = 0
    private var mTrashImageTop: Int = 0
    private var mTrashImageLeft: Int = 0

    //コントロールタッチ時の編集ボタンID
    private var mEditButtonViewId: Int = -1

    private lateinit var mLongPressHandler: Handler

    private var mChildViewTouchStartLeft: Int = 0
    private var mChildViewTouchStartTop: Int = 0
    private var mChildViewTouchStartX: Int = 0
    private var mChildViewTouchStartY: Int = 0
    private var mChildViewTouchStartWidth: Int = 0
    private var mChildViewTouchStartHeight: Int = 0

    private var mMovingFlg = false

    private var mResizeStartLeft: Int = 0
    private var mResizeStartTop: Int = 0
    private var mResizeStartX: Int = 0
    private var mResizeStartY: Int = 0
    private var mResizeStartWidth: Int = 0
    private var mResizeStartHeight: Int = 0

    private var mResizeMode: Int = 0
    private var mSelectingFlg = false

    private var mSelectingViewId = -1
    private var mMovingViewId = -1
    private var mMovingViewWidthBlock = -1
    private var mMovingViewHeightBlock = -1

    init {
        mPaintMinRect.color = Color.BLUE
        mPaintMinRect.style = Paint.Style.STROKE
        mPaintMinRect.strokeWidth = 4f

        mPaintOverlapRect.color = Color.RED
        mPaintOverlapRect.style = Paint.Style.STROKE
        mPaintOverlapRect.strokeWidth = 4f

        mPaintPlacingViewRect.color = Color.BLACK
        mPaintPlacingViewRect.style = Paint.Style.STROKE
        mPaintPlacingViewRect.strokeWidth = 4f

        mPaintMaxTopLine.color = Color.BLACK
        mPaintMaxTopLine.style = Paint.Style.STROKE
        mPaintMaxTopLine.strokeWidth = 4f

        mPaintGridLine.color = Color.LTGRAY
        mPaintGridLine.style = Paint.Style.STROKE
        mPaintGridLine.strokeWidth = 4f
        mPaintGridLine.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
    }

    override fun onDraw(canvas: Canvas?) {

        //コントロール配置領域限界を設定
        if (mPlaceMaxTop == 0) {
            mPlaceMaxTop = (this.height * 0.2).toInt()
            mPlaceMaxLeft = 0
            mPlaceMaxRight = this.width
            mPlaceMaxBottom = this.height
        }
        canvas?.drawLine(mPlaceMaxLeft.toFloat(), mPlaceMaxTop.toFloat(), mPlaceMaxRight.toFloat(), mPlaceMaxTop.toFloat(), mPaintMaxTopLine)

        //グリッドの交差ポイントのリスト作成
        if (mGridPointMap.isEmpty()) {

            mGridPointMap = GridPoint.createGridPointMap(this.height, this.width, mPlaceMaxTop).toMutableMap()

            //各コントロールの最小サイズ（px）を設定
            val pitch: Array<Float> = GridPoint.getPitch(this.height, this.width, mPlaceMaxTop)
            mStickMinWidth = (pitch[1] * cStickMinBlocks).toInt()
            mStickMinHeight = (pitch[0] * cStickMinBlocks).toInt()
            mButtonMinWidth = (pitch[1] * cButtonMinBlocks).toInt()
            mButtonMinHeight = (pitch[0] * cButtonMinBlocks).toInt()
            mWebMinWidth = (pitch[1] * cWebMinBlocks).toInt()
            mWebMinHeight = (pitch[0] * cWebMinBlocks).toInt()
            mLeverMinWidth = (pitch[1] * cLeverMinBlocks).toInt()
            mLeverMinHeight = (pitch[0] * cLeverMinBlocks).toInt()
        }

        //グリッドモードならグリッド描画
        if (mLocateMode == Constants.cLocateModeGrid) {
            val pitch: Array<Float> = GridPoint.getPitch(this.height, this.width, mPlaceMaxTop)
            for (i in 1 until GridPoint.getHeightIntervalCount()) {
                val y = round(mPlaceMaxTop + pitch[0] * i)
                canvas?.drawLine(0f, y, this.width.toFloat(), y, mPaintGridLine)
            }
            for (i in 1 until pitch[2].toInt()) {
                val x = round(pitch[1] * i)
                canvas?.drawLine(x, mPlaceMaxTop.toFloat(), x, this.height.toFloat(), mPaintGridLine)
            }
        }

        //ゴミ箱イメージのセットアップ
        if (mTrashImageView == null) {
            mTrashImageHeight = mPlaceMaxTop
            mTrashImageWidth = mTrashImageHeight
            mTrashImageTop = 0
            mTrashImageLeft = (this.width / 2 - mTrashImageWidth / 2)
            mTrashImageView = ImageView(context, null)
            mTrashImageView!!.id = View.generateViewId()
            mTrashImageView!!.setImageResource(R.drawable.img_trash_close)
            mTrashImageView!!.isVisible = false
            this.addView(mTrashImageView)

            setConstraintSet(mTrashImageView!!.id, mTrashImageWidth, mTrashImageHeight, mTrashImageLeft, mTrashImageTop)
        }

        //項目配置中の場合
        if (mPlacingFlg) {
            val config = mViewConfigList[mPlacingViewId]

            //最小サイズに合わせて枠線を引く
            var minWidth: Int = 0
            var minHeight: Int = 0
            when(mViewType) {
                Constants.cViewTypeButton -> {
                    minWidth = mButtonMinWidth
                    minHeight = mButtonMinHeight
                }
                Constants.cViewTypeStick -> {
                    minWidth = mStickMinWidth
                    minHeight = mStickMinHeight
                }
                Constants.cViewTypeWeb -> {
                    minWidth = mWebMinWidth
                    minHeight = mWebMinHeight
                }
                Constants.cViewTypeLever -> {
                    minWidth = mLeverMinWidth
                    minHeight = mLeverMinHeight
                }
            }
            if (config == null || config.width < minWidth || config.height < minHeight) {
                var rectLeft: Float = 0f
                var rectRight: Float = 0f
                var rectTop: Float = 0f
                var rectBottom: Float = 0f
                if (mPlaceTouchStartPosX < mPlaceTouchNowPosX) {
                    rectLeft = mPlaceTouchStartPosX
                    rectRight = mPlaceTouchStartPosX + minWidth
                } else {
                    rectLeft = mPlaceTouchStartPosX - minWidth
                    rectRight = mPlaceTouchStartPosX
                }
                if (mPlaceTouchStartPosY < mPlaceTouchNowPosY) {
                    rectTop = mPlaceTouchStartPosY
                    rectBottom = mPlaceTouchStartPosY + minHeight
                } else {
                    rectTop = mPlaceTouchStartPosY - minHeight
                    rectBottom = mPlaceTouchStartPosY
                }
                canvas?.drawRect(rectLeft, rectTop, rectRight - 4f, rectBottom - 4f, mPaintMinRect)
            }

            //既存項目と被っていたら赤枠
            if (config != null && isOverlap(mPlacingViewId, canvas)) {
                canvas?.drawRect(config.start.toFloat(), config.top.toFloat(), config.start.toFloat() + config.width.toFloat(), config.top.toFloat() + config.height.toFloat(), mPaintOverlapRect)
            } else {
                //既存項目と被ってない状態かつ、配置中なら項目に黒枠をつける
                if (config != null) {
                    canvas?.drawRect(config.start.toFloat(), config.top.toFloat(), config.start.toFloat() + config.width.toFloat(), config.top.toFloat() + config.height.toFloat(), mPaintPlacingViewRect)
                }
            }
        }

        //項目移動中の場合
        if (mMovingFlg) {
            val view = findViewById<View>(mMovingViewId)

            val paintRect = Paint()
            paintRect.color = Color.RED
            paintRect.style = Paint.Style.STROKE
            paintRect.strokeWidth = 4f

            //既存項目と被っているか（被っている項目に赤枠をつける）
            isOverlap(mMovingViewId, canvas)

            //移動中の項目に赤枠をつける
            canvas?.drawRect(view.left.toFloat(), view.top.toFloat(), view.right.toFloat(), view.bottom.toFloat(), paintRect)
        }

        //項目選択中の場合
        if (mSelectingFlg) {
            val view = findViewById<View>(mSelectingViewId)

            val paintRect = Paint()
            paintRect.color = Color.BLACK
            paintRect.style = Paint.Style.STROKE
            paintRect.strokeWidth = 4f

            val paintRectPoint = Paint()
            paintRectPoint.color = Color.RED
            paintRectPoint.strokeWidth = 30f
            paintRectPoint.strokeCap = Paint.Cap.ROUND

            if (mSelectingFlg && mResizeMode != 0) {
                val config = mViewConfigList[mSelectingViewId]
                //既存項目と被っているか
                if (isOverlap(mSelectingViewId, canvas)) {
                    paintRect.color = Color.RED
                }
            }
            //項目に枠をつける
            canvas?.drawRect(view.left.toFloat(), view.top.toFloat(), view.right.toFloat(), view.bottom.toFloat(), paintRect)

            //上下左右に点をつける
            canvas?.drawPoint(view.left.toFloat() - 10f, view.top.toFloat() + view.height.toFloat() / 2f, paintRectPoint)
            canvas?.drawPoint(view.right.toFloat() + 10f, view.top.toFloat() + view.height.toFloat() / 2f, paintRectPoint)
            canvas?.drawPoint(view.left.toFloat() + view.width.toFloat() / 2f, view.top.toFloat() - 4f, paintRectPoint)
            canvas?.drawPoint(view.left.toFloat() + view.width.toFloat() / 2f, view.bottom.toFloat() + 4f, paintRectPoint)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                //項目選択中の場合
                if (mSelectingFlg) {
                    val view = findViewById<View>(mSelectingViewId)
                    mResizeMode = 0
                    val limitDist = 50
                    val touchDistLeft = sqrt((view.left.toFloat() - event.x).pow(2) + (view.top.toFloat() + view.height.toFloat() / 2f - event.y).pow(2))
                    val touchDistRight = sqrt((view.right.toFloat() - event.x).pow(2) + (view.top.toFloat() + view.height.toFloat() / 2f - event.y).pow(2))
                    val touchDistTop = sqrt((view.left.toFloat() + view.width.toFloat() / 2f - event.x).pow(2) + (view.top.toFloat() - event.y).pow(2))
                    val touchDistBottom = sqrt((view.left.toFloat() + view.width.toFloat() / 2f - event.x).pow(2) + (view.bottom.toFloat() - event.y).pow(2))
                    //タッチした場所がサイズ変更点から一定距離以内か
                    if (touchDistLeft < limitDist) {
                        //left
                        mResizeMode = 1
                    } else if (touchDistRight < limitDist) {
                        //right
                        mResizeMode = 3
                    } else if (touchDistTop < limitDist) {
                        //top
                        mResizeMode = 2
                    } else if (touchDistBottom < limitDist) {
                        //bottom
                        mResizeMode = 4
                    }
                }
                //選択中かつサイズ変更点をタッチしている場合
                if (mSelectingFlg && mResizeMode != 0) {
                    val view = findViewById<View>(mSelectingViewId)
                    mResizeStartLeft = view.left
                    mResizeStartTop = view.top

                    mResizeStartX = event.x.toInt()
                    mResizeStartY = event.y.toInt()

                    mResizeStartWidth = view.width
                    mResizeStartHeight = view.height
                } else {

                    //選択状態を解除
                    clearSelectState()

                    //描画範囲内の場合のみ
                    if (mPlaceMaxTop <= event.y) {
                        mPlacingFlg = true

                        if (mLocateMode == Constants.cLocateModeGrid) {
                            val point = getNearlyPoint(event.x, event.y)
                            mPlaceTouchStartPosX = point.x.toFloat()
                            mPlaceTouchStartPosY = point.y.toFloat()
                        } else if (mLocateMode == Constants.cLocateModeFree) {
                            mPlaceTouchStartPosX = event.x
                            mPlaceTouchStartPosY = event.y
                        }
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                //リサイズ中の場合
                if (mSelectingFlg && mResizeMode != 0) {
                    val view = findViewById<View>(mSelectingViewId)

                    //移動量
                    var diffLeft: Int = mResizeStartX - event.x.toInt()
                    var diffTop: Int = mResizeStartY - event.y.toInt()

                    var l: Int = view.left
                    var t: Int = view.top
                    var r: Int = view.left + view.width
                    var b: Int = view.top + view.height

                    var minWidth = 0
                    var minHeight = 0
                    when(mViewConfigList[view.id]!!.viewType) {
                        Constants.cViewTypeButton -> {
                            minWidth = mButtonMinWidth
                            minHeight = mButtonMinHeight
                        }
                        Constants.cViewTypeStick -> {
                            minWidth = mStickMinWidth
                            minHeight = mStickMinHeight
                        }
                        Constants.cViewTypeWeb -> {
                            minWidth = mWebMinWidth
                            minHeight = mWebMinHeight
                        }
                        Constants.cViewTypeLever -> {
                            minWidth = mLeverMinWidth
                            minHeight = mLeverMinHeight
                        }
                    }

                    //上下左右で分岐
                    when (mResizeMode) {
                        //left
                        1 -> {
                            if (mResizeStartLeft - diffLeft < mPlaceMaxLeft) {
                                diffLeft = mResizeStartLeft - mPlaceMaxLeft
                            }
                            if (r - (mResizeStartLeft - diffLeft) > minWidth) {
                                l = mResizeStartLeft - diffLeft
                            } else {
                                l = r - minWidth
                            }
                        }
                        //top
                        2 -> {
                            if (mResizeStartTop - diffTop < mPlaceMaxTop) {
                                diffTop = mResizeStartTop - mPlaceMaxTop
                            }
                            if (b - (mResizeStartTop - diffTop) > minHeight) {
                                t = mResizeStartTop - diffTop
                            } else {
                                t = b - minHeight
                            }
                        }
                        //right
                        3 -> {
                            if (mResizeStartLeft - diffLeft + mResizeStartWidth > mPlaceMaxRight) {
                                diffLeft = mResizeStartLeft - mPlaceMaxRight + mResizeStartWidth
                            }
                            if ((mResizeStartLeft - diffLeft + mResizeStartWidth) - l > minWidth) {
                                r = mResizeStartLeft - diffLeft + mResizeStartWidth
                            } else {
                                r = l + minWidth
                            }
                        }
                        //bottom
                        4 -> {
                            if (mResizeStartTop - diffTop + mResizeStartHeight > mPlaceMaxBottom) {
                                diffTop = mResizeStartTop - mPlaceMaxBottom + mResizeStartHeight
                            }
                            if ((mResizeStartTop - diffTop + mResizeStartHeight) - t > minHeight) {
                                b = mResizeStartTop - diffTop + mResizeStartHeight
                            } else {
                                b = t + minHeight
                            }
                        }
                    }

                    if (mLocateMode == Constants.cLocateModeGrid) {
                        var point = getNearlyPoint(l.toFloat(), t.toFloat())
                        l = point.x
                        t = point.y
                        point = getNearlyPoint(r.toFloat(), b.toFloat())
                        r = point.x
                        b = point.y
                    }
                    view.layout(l, t, r, b)

                    storeViewConfig(view.id, view.width, view.height, view.left, view.top)

                    setConstraintSet(view.id, view.width, view.height, view.left, view.top)

                    editButtonLayout(view)

                    this.invalidate()

                } else if (mPlacingFlg) {

                    if (mLocateMode == Constants.cLocateModeGrid) {
                        val point = getNearlyPoint(event.x, event.y)
                        mPlaceTouchNowPosX = point.x.toFloat()
                        mPlaceTouchNowPosY = point.y.toFloat()
                    } else if (mLocateMode == Constants.cLocateModeFree) {
                        mPlaceTouchNowPosX = event.x
                        mPlaceTouchNowPosY = event.y
                    }

                    //ボタンまたはスティックまたはウェブビューを生成
                    when(mViewType) {
                        Constants.cViewTypeButton -> {
                            if (mPlacingViewId < 0) {
                                var button: Button = Button(context)
                                mPlacingViewId = View.generateViewId()
                                mButtonCount++
                                mButtonViewNo[mPlacingViewId] = mButtonCount
                                button.id = mPlacingViewId
                                button.isAllCaps = false
                                button.text = "BUTTON" + mButtonCount.toString()
                                button.setBackgroundColor(Constants.cDefaultButtonBackColor)
                                button.setTextColor(Constants.cDefaultButtonTextColor)
                                button.setOnTouchListener { view, motionEvent ->
                                    return@setOnTouchListener onTouchChildEvent(view, motionEvent)
                                }
                                this.addView(button)
                            }
                        }
                        Constants.cViewTypeStick -> {
                            if (mPlacingViewId < 0) {
                                var stick: StickView = StickView(context, null, false)
                                mPlacingViewId = View.generateViewId()
                                mStickCount++
                                mStickViewNo[mPlacingViewId] = mStickCount
                                stick.id = mPlacingViewId
                                stick.setSplitNum(8)
                                stick.setStepNum(2)
                                stick.setOnTouchListener { view, motionEvent ->
                                    return@setOnTouchListener onTouchChildEvent(view, motionEvent)
                                }
                                this.addView(stick)
                            }
                        }
                        Constants.cViewTypeWeb -> {
                            if (mPlacingViewId < 0) {
                                var webView: WebView = WebView(context)
                                mPlacingViewId = View.generateViewId()
                                mWebCount++
                                mWebViewNo[mPlacingViewId] = mWebCount
                                webView.id = mPlacingViewId
                                webView.setBackgroundColor(Color.GRAY)
                                webView.isClickable = true
                                webView.loadUrl("")

                                webView.setOnTouchListener { view, motionEvent ->
                                    return@setOnTouchListener onTouchChildEvent(view, motionEvent)
                                }
                                this.addView(webView)
                            }
                        }
                        Constants.cViewTypeLever -> {
                            if (mPlacingViewId < 0) {
                                var lever: LeverView = LeverView(context, null, false)
                                mPlacingViewId = View.generateViewId()
                                mLeverCount++
                                mLeverViewNo[mPlacingViewId] = mLeverCount
                                lever.id = mPlacingViewId
                                lever.setStepNum(2)
                                lever.setDefaultStep(0)
                                lever.setVertical(true)
                                lever.setReturnDefault(true)
                                lever.setOnTouchListener { view, motionEvent ->
                                    return@setOnTouchListener onTouchChildEvent(view, motionEvent)
                                }
                                this.addView(lever)
                            }
                        }
                    }

                    //現時点での配置
                    var viewWidth: Int = 0
                    var viewHeight: Int = 0
                    var viewStart: Int = 0
                    var viewTop: Int = 0
                    if (mPlaceTouchStartPosX < mPlaceTouchNowPosX) {
                        viewWidth = (mPlaceTouchNowPosX - mPlaceTouchStartPosX).toInt()
                        viewStart = mPlaceTouchStartPosX.toInt()
                        if (viewStart + viewWidth > this.width) {
                            viewWidth = this.width - viewStart
                        }
                    } else if (mPlaceTouchStartPosX > mPlaceTouchNowPosX) {
                        viewStart = mPlaceTouchNowPosX.toInt()
                        if (viewStart < this.left) {
                            viewStart = this.left
                        }
                        viewWidth = (mPlaceTouchStartPosX - viewStart).toInt()
                    } else {
                        viewStart = mPlaceTouchNowPosX.toInt()
                        viewWidth = 1
                    }
                    if (mPlaceTouchStartPosY < mPlaceTouchNowPosY) {
                        viewHeight = (mPlaceTouchNowPosY - mPlaceTouchStartPosY).toInt()
                        viewTop = mPlaceTouchStartPosY.toInt()
                        if (viewTop + viewHeight > this.height) {
                            viewHeight = this.height - viewTop
                        }
                    } else if (mPlaceTouchStartPosY > mPlaceTouchNowPosY) {
                        viewTop = mPlaceTouchNowPosY.toInt()
                        if (viewTop < mPlaceMaxTop) {
                            viewTop = mPlaceMaxTop
                        }
                        viewHeight = (mPlaceTouchStartPosY - viewTop).toInt()
                    } else {
                        viewTop = mPlaceTouchNowPosY.toInt()
                        viewHeight = 1
                    }

                    storeViewConfig(mPlacingViewId, viewWidth, viewHeight, viewStart, viewTop)

                    setConstraintSet(mPlacingViewId, viewWidth, viewHeight, viewStart, viewTop)

                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                //項目配置中の場合
                if (mPlacingViewId != -1) {
                    val viewConfig = mViewConfigList[mPlacingViewId]
                    when(mViewType) {
                        Constants.cViewTypeButton -> {
                            //最小サイズ以下なら項目削除
                            if (viewConfig!!.width < mButtonMinWidth || viewConfig.height < mButtonMinHeight) {
                                val view = this.findViewById<Button>(mPlacingViewId)
                                this.removeView(view)
                                mViewConfigList.remove(mPlacingViewId)
                                mButtonViewNo.remove(mPlacingViewId)
                                mButtonCount--
                            }
                        }
                        Constants.cViewTypeStick -> {
                            //最小サイズ以下なら項目削除
                            if (viewConfig!!.width < mStickMinWidth || viewConfig.height < mStickMinHeight) {
                                val view = this.findViewById<StickView>(mPlacingViewId)
                                this.removeView(view)
                                mViewConfigList.remove(mPlacingViewId)
                                mStickViewNo.remove(mPlacingViewId)
                                mStickCount--
                            }
                        }
                        Constants.cViewTypeWeb -> {
                            //最小サイズ以下なら項目削除
                            if (viewConfig!!.width < mWebMinWidth || viewConfig.height < mWebMinHeight) {
                                val view = this.findViewById<WebView>(mPlacingViewId)
                                this.removeView(view)
                                mViewConfigList.remove(mPlacingViewId)
                                mWebViewNo.remove(mPlacingViewId)
                                mWebCount--
                            }
                        }
                        Constants.cViewTypeLever -> {
                            //最小サイズ以下なら項目削除
                            if (viewConfig!!.width < mLeverMinWidth || viewConfig.height < mLeverMinHeight) {
                                val view = this.findViewById<LeverView>(mPlacingViewId)
                                this.removeView(view)
                                mViewConfigList.remove(mPlacingViewId)
                                mLeverViewNo.remove(mPlacingViewId)
                                mLeverCount--
                            }
                        }
                    }

                    //既存の項目と被っている場合もエラーで項目削除
                    if (isOverlap(mPlacingViewId, null)) {
                        Toast.makeText(context, R.string.canvas_error_overlap, Toast.LENGTH_SHORT).show()

                        when(mViewType) {
                            Constants.cViewTypeButton -> {
                                val view = this.findViewById<Button>(mPlacingViewId)
                                this.removeView(view)
                                mViewConfigList.remove(mPlacingViewId)
                                mButtonViewNo.remove(mPlacingViewId)
                                mButtonCount--
                            }
                            Constants.cViewTypeStick -> {
                                val view = this.findViewById<StickView>(mPlacingViewId)
                                this.removeView(view)
                                mViewConfigList.remove(mPlacingViewId)
                                mStickViewNo.remove(mPlacingViewId)
                                mStickCount--
                            }
                            Constants.cViewTypeWeb -> {
                                val view = this.findViewById<WebView>(mPlacingViewId)
                                this.removeView(view)
                                mViewConfigList.remove(mPlacingViewId)
                                mWebViewNo.remove(mPlacingViewId)
                                mWebCount--
                            }
                            Constants.cViewTypeLever -> {
                                val view = this.findViewById<LeverView>(mPlacingViewId)
                                this.removeView(view)
                                mViewConfigList.remove(mPlacingViewId)
                                mLeverViewNo.remove(mPlacingViewId)
                                mLeverCount--
                            }
                        }
                    }
                }

                //リサイズ中の場合
                if (mSelectingFlg && mResizeMode != 0) {
                    //既存項目と被っている場合はエラーで元の配置に戻す
                    if (isOverlap(mSelectingViewId, null)) {
                        Toast.makeText(context, R.string.canvas_error_overlap, Toast.LENGTH_SHORT).show()

                        val view: View = findViewById(mSelectingViewId)
                        view.layout(mResizeStartLeft, mResizeStartTop, mResizeStartLeft + mResizeStartWidth, mResizeStartTop + mResizeStartHeight)

                        storeViewConfig(mSelectingViewId, view.width, view.height, view.left, view.top)

                        setConstraintSet(mSelectingViewId, mResizeStartWidth, mResizeStartHeight, mResizeStartLeft, mResizeStartTop)

                        editButtonLayout(view)
                    }
                }

                mPlaceTouchStartPosX = 0f
                mPlaceTouchStartPosY = 0f
                mPlaceTouchNowPosX = 0f
                mPlaceTouchNowPosY = 0f
                mPlacingViewId = -1
                mPlacingFlg = false

                invalidate()
            }
        }
        return true
    }

    //指定したビューが既存のビューと被っているか判定
    private fun isOverlap(id: Int, canvas: Canvas?): Boolean {
        val viewConfig = mViewConfigList[id]

        if (viewConfig == null) {
            return false
        }

        var overlapFlg = false
        for (it in mViewConfigList) {
            if (it.key == id) {
                continue
            }

            //Y軸
            var yAxisOverlap = false
            if (viewConfig.top > it.value.top) {
                //新規のY軸始点が既存の始点より下にある
                if (viewConfig.top < it.value.top + it.value.height) {
                    //新規のY軸始点が既存の始点より下にあるかつ
                    //新規のY軸始点が既存の終点より上にある
                    yAxisOverlap = true
                }
            } else {
                //新規のY軸始点が既存の始点より上にある
                if (viewConfig.top + viewConfig.height > it.value.top) {
                    //新規のY軸始点が既存の始点より上にあるかつ
                    //新規のY軸終点が既存の始点より下にある
                    yAxisOverlap = true
                }
            }
            //X軸
            var xAxisOverlap = false
            if (viewConfig.start > it.value.start) {
                //新規のX軸始点が既存の始点より右にある
                if (viewConfig.start < it.value.start + it.value.width) {
                    //新規のX軸始点が既存の始点より右にあるかつ
                    //新規のX軸始点が既存の終点より左にある
                    xAxisOverlap = true
                }
            } else {
                //新規のX軸始点が既存の始点より左にある
                if (viewConfig.start + viewConfig.width > it.value.start) {
                    //新規のX軸始点が既存の始点より左にあるかつ
                    //新規のX軸終点が既存の始点より右にある
                    xAxisOverlap = true
                }
            }

            if (xAxisOverlap && yAxisOverlap) {
                //被っている
                overlapFlg = true
                //被っている既存項目に赤枠をつける
                if (canvas != null) {
                    canvas.drawRect(it.value.start.toFloat(), it.value.top.toFloat(), it.value.start.toFloat() + it.value.width.toFloat(), it.value.top.toFloat() + it.value.height.toFloat(), mPaintOverlapRect)
                } else {
                    break
                }
            }
        }
        return overlapFlg
    }

    //指定ポイントから最も近いグリッドポイントを返す
    private fun getNearlyPoint(x: Float, y: Float): GridPoint {
        var nx: Int = 0
        var ny: Int = mPlaceMaxTop
        var nd: Float = -1.0f
        mGridPointMap.forEach {
            val d = sqrt((x - it.value.x).pow(2) + (y - it.value.y).pow(2))
            if (nd < 0 || nd > d) {
                nd = d
                nx = it.value.x
                ny = it.value.y
            }
        }

        return GridPoint(nx, ny)
    }

    //移動中のビューのグリッドモードの場合のブロック数を設定する
    private fun setMovingViewBlocks() {
        val view = findViewById<View>(mMovingViewId)
        var nd: Float = -1.0f
        var nd2: Float = -1.0f
        var sx: Int = 0
        var sy: Int = 0
        var ex: Int = 0
        var ey: Int = 0
        mGridPointMap.forEach {
            val d = sqrt((view.left - it.value.x).toFloat().pow(2) + (view.top - it.value.y).toFloat().pow(2))
            if (nd < 0 || nd > d) {
                nd = d
                sx = it.key[0]
                sy = it.key[1]
            }
            val d2 = sqrt((view.right - it.value.x).toFloat().pow(2) + (view.bottom - it.value.y).toFloat().pow(2))
            if (nd2 < 0 || nd2 > d2) {
                nd2 = d2
                ex = it.key[0]
                ey = it.key[1]
            }
        }
        mMovingViewWidthBlock = ex - sx
        mMovingViewHeightBlock = ey - sy
    }

    //移動中のビューのグリッドモードの場合の位置を取得する
    private fun getMoveNearlyPoint(l: Int, t: Int, r: Int, b: Int): Array<GridPoint> {
        var nx: Int = 0
        var ny: Int = mPlaceMaxTop
        var nx2: Int = 0
        var ny2: Int = mPlaceMaxTop
        var nd: Float = -1.0f
        var nd2: Float = -1.0f
        var sx: Int = 0
        var sy: Int = 0
        mGridPointMap.forEach {
            val d = sqrt((l - it.value.x).toFloat().pow(2) + (t - it.value.y).toFloat().pow(2))
            if (nd < 0 || nd > d) {
                nd = d
                nx = it.value.x
                ny = it.value.y
                sx = it.key[0]
                sy = it.key[1]
            }
            val d2 = sqrt((r - it.value.x).toFloat().pow(2) + (b - it.value.y).toFloat().pow(2)
            )
            if (nd2 < 0 || nd2 > d2) {
                nd2 = d2
                nx2 = it.value.x
                ny2 = it.value.y
            }
        }

        if (mLocateMode == Constants.cLocateModeGrid) {
            val g = mGridPointMap[arrayOf(sx + mMovingViewWidthBlock, sy + mMovingViewHeightBlock)]
            if (g != null) {
                nx2 = g.x
                nx2 = g.y
            }
        }

        return arrayOf(GridPoint(nx, ny), GridPoint(nx2, ny2))
    }

    //ゴミ箱イメージの設定
    private fun setDustBoxImage(view: View, motionEvent: MotionEvent) {
        if (mTrashImageView != null) {
            mTrashImageView!!.isVisible = true
            if ((view.x + motionEvent.x) >= mTrashImageLeft && (view.x + motionEvent.x) <= mTrashImageLeft + mTrashImageWidth &&
                (view.y + motionEvent.y) >= mTrashImageTop && (view.y + motionEvent.y) <= mTrashImageTop + mTrashImageHeight
            ) {
                mTrashImageView!!.setImageResource(R.drawable.img_trash_open)
            } else {
                mTrashImageView!!.setImageResource(R.drawable.img_trash_close)
            }
        }
    }

    //ゴミ箱にビューを捨てたか
    private fun throwDustBox(view: View, motionEvent: MotionEvent) {
        if (mTrashImageView != null) {
            mTrashImageView!!.isVisible = false
            if ((view.x + motionEvent.x) >= mTrashImageLeft && (view.x + motionEvent.x) <= mTrashImageLeft + mTrashImageWidth &&
                (view.y + motionEvent.y) >= mTrashImageTop && (view.y + motionEvent.y) <= mTrashImageTop + mTrashImageHeight
            ) {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    this.removeView(view)
                }, 500)
                if (mViewConfigList[view.id] != null) {
                    when(mViewConfigList[view.id]!!.viewType) {
                        Constants.cViewTypeButton -> {
                            mButtonViewNo.remove(view.id)
                        }
                        Constants.cViewTypeStick -> {
                            mStickViewNo.remove(view.id)
                        }
                        Constants.cViewTypeWeb -> {
                            mWebViewNo.remove(view.id)
                        }
                        Constants.cViewTypeLever -> {
                            mLeverViewNo.remove(view.id)
                        }
                    }
                }
                mViewConfigList.remove(view.id)
                view.isVisible = false
            }
            mTrashImageView!!.setImageResource(R.drawable.img_trash_close)
        }
    }

    //選択状態をクリア
    private fun clearSelectState() {
        mMovingFlg = false
        mSelectingFlg = false
        if (mEditButtonViewId > 0) {
            this.removeView(findViewById(mEditButtonViewId))
        }
        mMovingViewId = -1
        mMovingViewWidthBlock = -1
        mMovingViewHeightBlock = -1
        mSelectingViewId = -1
        mResizeMode = 0
    }

    //項目選択時の編集ボタンの設定
    private fun editButtonLayout(view: View) {
        val margin: Int = 35
        var editButtonWidth: Int = 180
        var editButtonHeight: Int = 120
        var editButtonStart: Int = view.left + view.width / 2 - editButtonWidth / 2
        var editButtonTop: Int = view.top - margin - editButtonHeight

        //描画範囲内で上、左、右、下の順で余裕がある場所に配置、どこもなければビューの中心に配置
        if (editButtonTop < mPlaceMaxTop) {
            editButtonTop = view.bottom + margin

            if (editButtonTop + editButtonHeight > mPlaceMaxBottom) {
                editButtonStart = view.left - margin - editButtonWidth
                editButtonTop = view.top + view.height / 2 - editButtonHeight / 2

                if (editButtonStart < mPlaceMaxLeft) {
                    editButtonStart = view.right + margin

                    if (editButtonStart + editButtonWidth > mPlaceMaxRight) {
                        editButtonStart = view.left + view.width / 2 - editButtonWidth / 2
                        editButtonTop = view.top + view.height / 2 - editButtonHeight / 2

                        //配置して即描画だとビューの下に隠れるので時間をおいて描画
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed({
                            invalidate()
                        }, 300)
                    }
                }
            }
        }

        setConstraintSet(mEditButtonViewId, editButtonWidth, editButtonHeight, editButtonStart, editButtonTop)
    }

    //ビューの新規作成または位置、サイズ変更時のViewConfigの格納
    private fun storeViewConfig(viewId: Int, width: Int, height: Int, start: Int, top: Int) {
        var config = ViewConfig()
        if (mViewConfigList.contains(viewId)) {
            config = mViewConfigList[viewId]!!
        } else {
            try {
                val v: StickView = findViewById(viewId)
                config.viewType = Constants.cViewTypeStick
                config.step = v.getStepNum()
                config.split = v.getSplitNum()
                config.sendValueMap = SendValueMap.createInitStickSendValueMap(v.getStepNum(), v.getSplitNum(), mStickViewNo[viewId]!!.toInt())
            } catch (e: Exception) {
                try{
                    val v: Button = findViewById(viewId)
                    config.viewType = Constants.cViewTypeButton
                    config.text = v.text.toString()
                    val sendValueMap: SendValueMap = SendValueMap()
                    sendValueMap.setSendValue(0, 0, "!B!" + mButtonViewNo[viewId].toString())
                    config.sendValueMap = sendValueMap
                    config.color1 = (v.background as ColorDrawable).color
                    config.color2 = v.currentTextColor
                } catch (e: Exception) {
                    try{
                        val v: WebView = findViewById(viewId)
                        config.viewType = Constants.cViewTypeWeb
                        config.text = ""
                        config.sendValueMap = SendValueMap()
                    } catch (e: Exception) {
                        val v: LeverView = findViewById(viewId)
                        config.viewType = Constants.cViewTypeLever
                        config.step = v.getStepNum()
                        config.split = v.getDefaultStep()
                        config.vertical = v.getVertical()
                        config.returnDefault = v.getReturnDefault()
                        config.sendValueMap = SendValueMap.createInitLeverSendValueMap(v.getStepNum(), mLeverViewNo[viewId]!!.toInt())
                    }
                }
            }
        }
        config.width = width
        config.height = height
        config.start = start
        config.top = top
        mViewConfigList[viewId] = config
    }

    //ボタン編集、スティック編集の内容をViewConfigに反映
    fun refViewConfig(viewId: Int, config: ViewConfig) {
        mViewConfigList[viewId] = config

        when(config.viewType) {
            Constants.cViewTypeButton -> {
                val view = findViewById<TextView>(viewId)
                view.text = config.text
                view.setBackgroundColor(config.color1)
                view.setTextColor(config.color2)
            }
            Constants.cViewTypeStick -> {
                val view = findViewById<StickView>(viewId)
                view.setStepNum(config.step)
                view.setSplitNum(config.split)
            }
            Constants.cViewTypeWeb -> {
                val view = findViewById<WebView>(viewId)
                if (config.text.isNullOrEmpty()) {
                    view.setBackgroundColor(Color.GRAY)
                } else {
                    view.setBackgroundColor(Color.GREEN)
                }
            }
            Constants.cViewTypeLever -> {
                val view = findViewById<LeverView>(viewId)
                view.setStepNum(config.step)
                view.setDefaultStep(config.split)
                view.setVertical(config.vertical)
                view.setReturnDefault(config.returnDefault)
            }
        }
    }

    //ビューIDからViewConfigを取得
    fun getViewConfig(viewId: Int): ViewConfig? {
        return mViewConfigList[viewId]
    }

    //ViewConfigListを取得
    fun getViewConfigList(): MutableMap<Int, ViewConfig> {
        return mViewConfigList
    }

    //ビューIDからボタン、スティック、ウェブビュー、レバーの番号（連番）を取得
    fun getViewNoFromId(viewId: Int): Int {
        var rtn: Int? = null
        val config = mViewConfigList[viewId]
        if (config != null) {
            when(config.viewType) {
                Constants.cViewTypeButton -> {
                    rtn = mButtonViewNo[viewId]
                }
                Constants.cViewTypeStick -> {
                    rtn = mStickViewNo[viewId]
                }
                Constants.cViewTypeWeb -> {
                    rtn = mWebViewNo[viewId]
                }
                Constants.cViewTypeLever -> {
                    rtn = mLeverViewNo[viewId]
                }
            }
        }
        if (rtn == null) {
            rtn = 0
        }

        return rtn
    }

    //ViewConfigのリストからキャンバスを初期化（編集時の初期）
    fun initFromViewConfigList(viewConfigList: MutableList<ViewConfig>) {

        for (it in mViewConfigList) {
            val view = this.findViewById<View>(it.key)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                this.removeView(view)
            }, 500)
        }

        mViewConfigList.clear()
        mButtonViewNo.clear()
        mStickViewNo.clear()
        mWebViewNo.clear()
        mLeverViewNo.clear()

        viewConfigList.forEach { viewConfig ->
            var viewId: Int = -1
            when(viewConfig.viewType) {
                Constants.cViewTypeButton -> {
                    var button: Button = Button(context)
                    viewId = View.generateViewId()
                    mButtonCount++
                    mButtonViewNo[viewId] = mButtonCount
                    button.id = viewId
                    button.isAllCaps = false
                    button.text = viewConfig.text
                    button.setBackgroundColor(viewConfig.color1)
                    button.setTextColor(viewConfig.color2)
                    button.setOnTouchListener { view, motionEvent ->
                        return@setOnTouchListener onTouchChildEvent(view, motionEvent)
                    }
                    this.addView(button)
                }
                Constants.cViewTypeStick -> {
                    var stick: StickView = StickView(context, null, false)
                    viewId = View.generateViewId()
                    mStickCount++
                    mStickViewNo[viewId] = mStickCount
                    stick.id = viewId
                    stick.setSplitNum(viewConfig.split)
                    stick.setStepNum(viewConfig.step)
                    stick.setOnTouchListener { view, motionEvent ->
                        return@setOnTouchListener onTouchChildEvent(view, motionEvent)
                    }
                    this.addView(stick)
                }
                Constants.cViewTypeWeb -> {
                    var webView: WebView = WebView(context)
                    viewId = View.generateViewId()
                    mWebCount++
                    mWebViewNo[viewId] = mWebCount
                    webView.id = viewId
                    webView.setBackgroundColor(Color.GRAY)
                    webView.isClickable = true
                    webView.loadUrl(viewConfig.text)
                    webView.setOnTouchListener { view, motionEvent ->
                        return@setOnTouchListener onTouchChildEvent(view, motionEvent)
                    }
                    this.addView(webView)
                }
                Constants.cViewTypeLever -> {
                    var lever: LeverView = LeverView(context, null, false)
                    viewId = View.generateViewId()
                    mLeverCount++
                    mLeverViewNo[viewId] = mLeverCount
                    lever.id = viewId
                    lever.setStepNum(viewConfig.step)
                    lever.setDefaultStep(viewConfig.split)
                    lever.setVertical(viewConfig.vertical)
                    lever.setReturnDefault(viewConfig.returnDefault)
                    lever.setOnTouchListener { view, motionEvent ->
                        return@setOnTouchListener onTouchChildEvent(view, motionEvent)
                    }
                    this.addView(lever)
                }
            }
            setConstraintSet(viewId, viewConfig.width, viewConfig.height, viewConfig.start, viewConfig.top)
            mViewConfigList[viewId] = viewConfig
        }

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            invalidate()
        }, 300)
    }

    //ビュー指定で再描画
    fun invalidateView(viewId: Int) {
        findViewById<View>(viewId).invalidate()
    }

    //画面制約の設定
    private fun setConstraintSet(viewId: Int, width: Int, height: Int, start: Int, top: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        constraintSet.constrainHeight(viewId, height)
        constraintSet.constrainWidth(viewId, width)
        constraintSet.connect(viewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, start)
        constraintSet.connect(viewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, top)
        constraintSet.applyTo(this)
    }

    //配置した子項目のタッチイベント
    private fun onTouchChildEvent(view: View, event: MotionEvent): Boolean {
        val maxLeft: Int = 0
        val maxTop: Int = (this.height * 0.2).toInt()
        val maxRight: Int = this.width
        val maxBottom: Int = this.height

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                //選択状態をクリア
                clearSelectState()

                //編集ボタンが表示されている場合は削除
                if (mEditButtonViewId > 0) {
                    this.removeView(findViewById(mEditButtonViewId))
                }

                mChildViewTouchStartLeft = view.left
                mChildViewTouchStartTop = view.top

                mChildViewTouchStartWidth = view.width
                mChildViewTouchStartHeight = view.height

                mChildViewTouchStartX = event.rawX.toInt()
                mChildViewTouchStartY = event.rawY.toInt()

                //長押し判定用ハンドラ
                mLongPressHandler = Handler(Looper.getMainLooper())
                mLongPressHandler.postDelayed({
                    //5秒で長押し判定、対象を移動中にする
                    mMovingFlg = true
                    mMovingViewId = view.id
                    setMovingViewBlocks()
                    this.invalidate()
                }, 500)
            }
            MotionEvent.ACTION_MOVE -> {

                //項目移動中の場合
                if (mMovingFlg) {
                    var diffLeft: Int = mChildViewTouchStartX - event.rawX.toInt()
                    var diffTop: Int = mChildViewTouchStartY - event.rawY.toInt()

                    if (mChildViewTouchStartLeft - diffLeft < maxLeft) {
                        diffLeft = mChildViewTouchStartLeft - maxLeft
                    }
                    if (mChildViewTouchStartTop - diffTop < maxTop) {
                        diffTop = mChildViewTouchStartTop - maxTop
                    }
                    if (mChildViewTouchStartLeft - diffLeft + view.width > maxRight) {
                        diffLeft = mChildViewTouchStartLeft - maxRight + view.width
                    }
                    if (mChildViewTouchStartTop - diffTop + view.height > maxBottom) {
                        diffTop = mChildViewTouchStartTop - maxBottom + view.height
                    }

                    var l: Int = mChildViewTouchStartLeft - diffLeft
                    var t: Int = mChildViewTouchStartTop - diffTop
                    var r: Int = mChildViewTouchStartLeft - diffLeft + view.width
                    var b: Int = mChildViewTouchStartTop - diffTop + view.height
                    if (mLocateMode == Constants.cLocateModeGrid) {
                        val point: Array<GridPoint> = getMoveNearlyPoint(l, t, r, b)
                        l = point[0].x
                        t = point[0].y
                        r = point[1].x
                        b = point[1].y
                    }
                    view.layout(l, t, r, b)

                    storeViewConfig(view.id, view.width, view.height, view.left, view.top)

                    setConstraintSet(view.id, view.width, view.height, view.left, view.top)

                    setDustBoxImage(view, event)

                    this.invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                //長押し判定用ハンドラをキャンセル
                mLongPressHandler.removeCallbacksAndMessages(null)

                //項目移動中でない場合、選択状態にする
                if (!mMovingFlg) {
                    mSelectingFlg = true
                    mSelectingViewId = view.id

                    //編集ボタンを作成
                    var editButton: Button = Button(context)
                    mEditButtonViewId = View.generateViewId()
                    editButton.id = mEditButtonViewId
                    editButton.text = resources.getText(R.string.controller_layout_edit_text)
                    editButton.setTextColor(Color.BLACK)
                    editButton.setBackgroundResource(R.drawable.border_rectangle2)
                    editButton.setOnClickListener {
                        when(getViewConfig(mSelectingViewId)!!.viewType) {
                            Constants.cViewTypeButton -> {
                                MainActivity.showEditButtonFragment(mSelectingViewId)
                            }
                            Constants.cViewTypeStick -> {
                                MainActivity.showEditStickFragment(mSelectingViewId)
                            }
                            Constants.cViewTypeWeb -> {
                                MainActivity.showEditWebViewFragment(mSelectingViewId)
                            }
                            Constants.cViewTypeLever -> {
                                MainActivity.showEditLeverFragment(mSelectingViewId)
                            }
                        }
                    }
                    this.addView(editButton)
                    editButtonLayout(view)
                }

                //項目移動中の場合
                if (mMovingFlg) {
                    //既存項目と被っている場合、エラーにして元の場所に配置
                    if (isOverlap(mMovingViewId, null)) {
                        Toast.makeText(context, R.string.canvas_error_overlap, Toast.LENGTH_SHORT).show()

                        storeViewConfig(mMovingViewId, mChildViewTouchStartWidth, mChildViewTouchStartHeight, mChildViewTouchStartLeft, mChildViewTouchStartTop)

                        setConstraintSet(mMovingViewId, mChildViewTouchStartWidth, mChildViewTouchStartHeight, mChildViewTouchStartLeft, mChildViewTouchStartTop)
                    }
                }

                mMovingFlg = false
                mMovingViewId = -1
                mMovingViewWidthBlock = -1
                mMovingViewHeightBlock = -1

                //ごみ箱に捨てたか
                throwDustBox(view, event)

                invalidate()
            }
        }
        return false
    }
}