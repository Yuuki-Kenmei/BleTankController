/*
 * ControllerLayoutDao
 */

package com.sys_ky.bletankcontroller.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sys_ky.bletankcontroller.room.entity.ControllerLayout

@Dao
interface ControllerLayoutDao {
    @Query("SELECT * FROM controller_layout ORDER BY layout_id")
    fun selectAll(): List<ControllerLayout>

    @Query("SELECT * FROM controller_layout WHERE layout_id = :layoutId")
    fun selectByLayoutId(layoutId: Int): List<ControllerLayout>

    @Query("SELECT COUNT(*) FROM controller_layout")
    fun selectCount(): Int

    @Query("SELECT MAX(layout_id) FROM controller_layout")
    fun selectMaxId(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg controllerLayout: ControllerLayout)

    @Query("DELETE FROM controller_layout WHERE layout_id = :layoutId")
    fun deleteByLayoutId(layoutId: Int)

    @Query("DELETE FROM controller_layout WHERE layout_id <> 0")
    fun deleteAll()
}