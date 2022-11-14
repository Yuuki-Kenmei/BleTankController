/*
 * ViewConfig
 *
 * 配置コントロールの情報格納用クラス
 * コントローラーレイアウト編集で使用
 */

package com.sys_ky.bletankcontroller.common

class ViewConfig {
    var viewType: Int = 0
    var start: Int = 0
    var top: Int = 0
    var width: Int = 0
    var height: Int = 0
    var text: String = ""
    var step: Int = 2
    var split: Int = 8
    var sendValueMap: SendValueMap = SendValueMap()
}