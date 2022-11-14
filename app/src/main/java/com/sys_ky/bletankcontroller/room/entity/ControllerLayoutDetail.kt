package com.sys_ky.bletankcontroller.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "CONTROLLER_LAYOUT_DETAIL", primaryKeys = ["layout_id", "control_id"])
data class ControllerLayoutDetail (
    val layout_id: Int,
    val control_id: Int,
    @ColumnInfo(name = "CONTROL_TYPE") val control_type: Int,
    @ColumnInfo(name = "WIDTH") val width: Int,
    @ColumnInfo(name = "HEIGHT") val height: Int,
    @ColumnInfo(name = "TOP") val top: Int,
    @ColumnInfo(name = "START") val start: Int,
    @ColumnInfo(name = "TEXT") val text: String,
    @ColumnInfo(name = "STEP") val step: Int,
    @ColumnInfo(name = "SPLIT") val split: Int
)