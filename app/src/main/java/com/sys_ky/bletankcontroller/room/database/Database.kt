/*
 * Database
 */

package com.sys_ky.bletankcontroller.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sys_ky.bletankcontroller.room.dao.*
import com.sys_ky.bletankcontroller.room.entity.*

@Database(entities = [
    BaseSetting::class,
    ControllerLayout::class,
    ControllerLayoutDetail::class,
    ControlSendValue::class,
    SavedConnection::class],
    exportSchema = false,
    version = 1)
abstract class Database: RoomDatabase() {
    abstract fun BaseSettingDao(): BaseSettingDao
    abstract fun ControllerLayoutDao(): ControllerLayoutDao
    abstract fun ControllerLayoutDetailDao(): ControllerLayoutDetailDao
    abstract fun ControlSendValueDao(): ControlSendValueDao
    abstract fun SavedConnectionDao(): SavedConnectionDao
}