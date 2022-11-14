/*
 * SavedConnectionDao
 */

package com.sys_ky.bletankcontroller.room.dao

import androidx.room.*
import com.sys_ky.bletankcontroller.room.entity.SavedConnection

@Dao
interface SavedConnectionDao {
    @Query("SELECT * FROM saved_connection ORDER BY connection_name, bd_address")
    fun selectAll(): List<SavedConnection>

    @Query("SELECT * FROM saved_connection WHERE bd_address = :bdAddress")
    fun selectByBdAddress(bdAddress: String): List<SavedConnection>

    @Query("SELECT COUNT(*) FROM saved_connection WHERE layout_id = :layoutId")
    fun selectCountByLayoutId(layoutId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg savedConnection: SavedConnection)

    @Query("UPDATE saved_connection SET uuid_service = :uuidService, uuid_characteristic_tx = :uuidTx, uuid_characteristic_rx = :uuidRx WHERE bd_address = :bdAddress")
    fun updateUuidByBdAddress(bdAddress: String, uuidService: String, uuidTx: String, uuidRx: String)

    @Query("UPDATE saved_connection SET layout_id = 0 WHERE layout_id = :layoutId")
    fun updateLayoutIdToDefault(layoutId: Int)

    @Query("DELETE FROM saved_connection WHERE bd_address = :bdAddress")
    fun deleteByBdAddress(bdAddress: String)

    @Query("DELETE FROM saved_connection")
    fun deleteAll()
}