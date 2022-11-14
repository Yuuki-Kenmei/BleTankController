package com.sys_ky.bletankcontroller.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BASE_SETTING")
data class BaseSetting (
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "UUID_SERVICE") val uuid_service: String,
    @ColumnInfo(name = "UUID_CHARACTERISTIC_TX") val uuid_characteristic_tx: String,
    @ColumnInfo(name = "UUID_CHARACTERISTIC_RX") val uuid_characteristic_rx: String
)