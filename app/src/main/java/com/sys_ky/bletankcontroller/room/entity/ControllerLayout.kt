package com.sys_ky.bletankcontroller.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CONTROLLER_LAYOUT")
data class ControllerLayout (
    @PrimaryKey val layout_id: Int,
    @ColumnInfo(name = "LAYOUT_NAME") val layout_name: String
)