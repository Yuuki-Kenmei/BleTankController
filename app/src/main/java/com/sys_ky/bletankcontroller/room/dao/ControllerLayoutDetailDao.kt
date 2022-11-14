/*
 * ControllerLayoutDetailDao
 */

package com.sys_ky.bletankcontroller.room.dao

import androidx.room.*
import com.sys_ky.bletankcontroller.room.entity.ControllerLayoutDetail

@Dao
interface ControllerLayoutDetailDao {
    @Query("SELECT * FROM controller_layout_detail ORDER BY layout_id, control_id")
    fun selectAll(): List<ControllerLayoutDetail>

    @Query("SELECT * FROM controller_layout_detail WHERE layout_id = :layoutId ORDER BY control_id")
    fun selectByLayoutId(layoutId: Int): List<ControllerLayoutDetail>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg controllerLayoutDetail: ControllerLayoutDetail)

    @Query("DELETE FROM controller_layout_detail WHERE layout_id = :layoutId")
    fun deleteByLayoutId(layoutId: Int)

    @Query("DELETE FROM controller_layout_detail WHERE layout_id <> 0")
    fun deleteAll()
}