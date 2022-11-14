/*
 * GridPoint
 *
 * グリッド描画およびポイント取得用クラス
 * コントローラーレイアウト編集で使用
 */

package com.sys_ky.bletankcontroller.common

import android.graphics.Point
import kotlin.math.round
import kotlin.math.roundToInt

class GridPoint: Point {

    constructor() {
        x = 0
        y = 0
    }
    constructor(px: Int, py: Int) {
        x = px
        y = py
    }

    companion object {
        private const val heightIntervalCount: Int = 10

        fun createGridPointMap(height: Int, width: Int, maxTop: Int): MutableMap<Array<Int>, GridPoint> {
            val gridPointMap: MutableMap<Array<Int>, GridPoint> = mutableMapOf()
            val pitch = getPitch(height, width, maxTop)

            for (i in 0..heightIntervalCount) {
                for (j in 0..pitch[2].toInt()) {
                    if (i != heightIntervalCount) {
                        gridPointMap[arrayOf(j, i)] = GridPoint((pitch[1] * j).roundToInt(), (maxTop + pitch[0] * i).roundToInt())
                    } else {
                        gridPointMap[arrayOf(j, i)] = GridPoint((pitch[1] * j).roundToInt(), height)
                    }
                }
            }
            return gridPointMap
        }

        fun getPitch(height: Int, width: Int, maxTop: Int): Array<Float> {
            val heightPitch: Float = round((height - maxTop.toFloat()) / heightIntervalCount)
            //横は縦ピッチと同じ幅で分割
            val widthIntervalCount: Int = (width / heightPitch).roundToInt()
            val widthPitch: Float = round(width / widthIntervalCount.toFloat())

            return arrayOf(heightPitch, widthPitch, widthIntervalCount.toFloat())
        }

        fun getHeightIntervalCount(): Int {
            return heightIntervalCount
        }
    }
}