package com.sys_ky.bletankcontroller.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "CONTROL_SEND_VALUE", primaryKeys = ["layout_id", "control_id", "point1", "point2"])
data class ControlSendValue (
    val layout_id: Int,
    val control_id: Int,
    val point1: Int,
    val point2: Int,
    @ColumnInfo(name = "SEND_VALUE") val send_value: String
)