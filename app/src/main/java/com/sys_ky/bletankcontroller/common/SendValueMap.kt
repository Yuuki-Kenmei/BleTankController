/*
 * SendValueMap
 *
 * コントロールの送信値格納用クラス
 */

package com.sys_ky.bletankcontroller.common

class SendValueMap {
    private var map: MutableMap<Array<Int>, String> = mutableMapOf()
    fun getSendValue(step: Int, split: Int): String {
        var rtn: String = ""
        map.forEach {
            if (it.key[0] == step && it.key[1] == split) {
                rtn = it.value
                return@forEach
            }
        }
        return rtn
    }

    fun setSendValue(step: Int, split: Int, sendValue: String) {
        map[arrayOf(step, split)] = sendValue
    }

    fun getSendValueAll(): MutableMap<Array<Int>, String> {
        return map
    }

    companion object {
        fun createInitStickSendValueMap(step: Int, split: Int, no: Int): SendValueMap {
            val sendValueMap = SendValueMap()
            sendValueMap.setSendValue(0, 0, "!S!$no!0!0")
            for (i in 1..step) {
                for (j in 0 until split) {
                    sendValueMap.setSendValue(i, j, "!S!$no!$i!$j")
                }
            }
            return sendValueMap
        }

        fun createInitLeverSendValueMap(step: Int, no: Int): SendValueMap {
            val sendValueMap = SendValueMap()
            sendValueMap.setSendValue(0, 0, "!L!$no!0!0")
            for (i in 0..step) {
                sendValueMap.setSendValue(i, 0, "!L!$no!$i")
            }
            return sendValueMap
        }
    }
}