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
    @ColumnInfo(name = "SPLIT") val split: Int,
    @ColumnInfo(name = "VERTICAL") val vertical: Boolean,
    @ColumnInfo(name = "RETURN_DEFAULT") val return_default: Boolean,
    @ColumnInfo(name = "C1_RED") val c1_red: Int,
    @ColumnInfo(name = "C1_GREEN") val c1_green: Int,
    @ColumnInfo(name = "C1_BLUE") val c1_blue: Int,
    @ColumnInfo(name = "C2_RED") val c2_red: Int,
    @ColumnInfo(name = "C2_GREEN") val c2_green: Int,
    @ColumnInfo(name = "C2_BLUE") val c2_blue: Int
)