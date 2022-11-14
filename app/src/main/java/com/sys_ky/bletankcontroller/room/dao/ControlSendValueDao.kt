/*
 * ControlSendValueDao
 */

package com.sys_ky.bletankcontroller.room.dao

import androidx.room.*
import com.sys_ky.bletankcontroller.room.entity.ControlSendValue

@Dao
interface ControlSendValueDao {
    @Query("SELECT * FROM control_send_value ORDER BY layout_id, control_id, point1, point2")
    fun selectAll(): List<ControlSendValue>

    @Query("SELECT * FROM control_send_value WHERE layout_id = :layoutId ORDER BY control_id, point1, point2")
    fun selectByLayoutId(layoutId: Int): List<ControlSendValue>

    @Query("SELECT * FROM control_send_value WHERE layout_id = :layoutId AND control_id = :controlId ORDER BY point1, point2")
    fun selectByIds(layoutId: Int, controlId: Int): List<ControlSendValue>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg controlSendValue: ControlSendValue)

    @Query("DELETE FROM control_send_value WHERE layout_id = :layoutId")
    fun deleteByLayoutId(layoutId: Int)

    @Query("DELETE FROM control_send_value WHERE layout_id <> 0")
    fun deleteAll()
}