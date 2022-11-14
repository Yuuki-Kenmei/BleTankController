/*
 * BaseSettingDao
 */

package com.sys_ky.bletankcontroller.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sys_ky.bletankcontroller.room.entity.BaseSetting

@Dao
interface BaseSettingDao {
    @Query("SELECT * FROM base_setting")
    fun selectAll(): List<BaseSetting>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg baseSetting: BaseSetting)
}