package com.sys_ky.bletankcontroller.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SAVED_CONNECTION")
data class SavedConnection (
    @PrimaryKey val bd_address: String,
    @ColumnInfo(name = "CONNECTION_NAME") val connection_name: String,
    @ColumnInfo(name = "IMAGE_PATH") val image_path: String,
    @ColumnInfo(name = "LAYOUT_ID") val layout_id: Int,
    @ColumnInfo(name = "UUID_SERVICE") val uuid_service: String,
    @ColumnInfo(name = "UUID_CHARACTERISTIC_TX") val uuid_characteristic_tx: String,
    @ColumnInfo(name = "UUID_CHARACTERISTIC_RX") val uuid_characteristic_rx: String
)