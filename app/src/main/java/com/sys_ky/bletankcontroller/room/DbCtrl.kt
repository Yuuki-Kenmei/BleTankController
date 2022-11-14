/*
 * DbCtrl
 *
 * DBアクセス用クラス
 */

package com.sys_ky.bletankcontroller.room

import android.content.Context
import androidx.room.Room
import com.sys_ky.bletankcontroller.room.database.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DbCtrl constructor(context: Context) {

    var database: Database

    init {
        database = Room.databaseBuilder(
            context,
            Database::class.java,
            "ble_tank_controller_db"
        ).fallbackToDestructiveMigration().build()
    }

    inner class BaseSetting {
        fun selectAll(): List<com.sys_ky.bletankcontroller.room.entity.BaseSetting> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.BaseSetting> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.BaseSettingDao().selectAll()
            }
            job.join()
            return@runBlocking rtn
        }

        fun insertAll(baseSetting: com.sys_ky.bletankcontroller.room.entity.BaseSetting) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.BaseSettingDao().insertAll(baseSetting)
            }
            job.join()
            return@runBlocking
        }
    }

    inner class ControllerLayout {
        fun selectAll(): List<com.sys_ky.bletankcontroller.room.entity.ControllerLayout> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.ControllerLayout> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.ControllerLayoutDao().selectAll()
            }
            job.join()
            return@runBlocking rtn
        }

        fun selectByLayoutId(layoutId: Int): List<com.sys_ky.bletankcontroller.room.entity.ControllerLayout> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.ControllerLayout> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.ControllerLayoutDao().selectByLayoutId(layoutId)
            }
            job.join()
            return@runBlocking rtn
        }

        fun selectCount(): Int = runBlocking {
            var rtn: Int = 0
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.ControllerLayoutDao().selectCount()
            }
            job.join()
            return@runBlocking rtn
        }

        fun selectMaxId(): Int = runBlocking {
            var rtn: Int = 0
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.ControllerLayoutDao().selectMaxId()
            }
            job.join()
            return@runBlocking rtn
        }

        fun insertAll(controllerLayout: com.sys_ky.bletankcontroller.room.entity.ControllerLayout) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.ControllerLayoutDao().insertAll(controllerLayout)
            }
            job.join()
            return@runBlocking
        }

        fun deleteByLayoutId(layoutId: Int) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.ControllerLayoutDao().deleteByLayoutId(layoutId)
            }
            job.join()
            return@runBlocking
        }

        fun deleteAll() = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.ControllerLayoutDao().deleteAll()
            }
            job.join()
            return@runBlocking
        }
    }

    inner class ControllerLayoutDetail {
        fun selectAll(): List<com.sys_ky.bletankcontroller.room.entity.ControllerLayoutDetail> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.ControllerLayoutDetail> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.ControllerLayoutDetailDao().selectAll()
            }
            job.join()
            return@runBlocking rtn
        }

        fun selectByLayoutId(layoutId: Int): List<com.sys_ky.bletankcontroller.room.entity.ControllerLayoutDetail> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.ControllerLayoutDetail> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.ControllerLayoutDetailDao().selectByLayoutId(layoutId)
            }
            job.join()
            return@runBlocking rtn
        }

        fun insertAll(controllerLayoutDetail: com.sys_ky.bletankcontroller.room.entity.ControllerLayoutDetail) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.ControllerLayoutDetailDao().insertAll(controllerLayoutDetail)
            }
            job.join()
            return@runBlocking
        }

        fun insertAll(controllerLayoutDetailList: ArrayList<com.sys_ky.bletankcontroller.room.entity.ControllerLayoutDetail>) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                controllerLayoutDetailList.forEach {
                    database.ControllerLayoutDetailDao().insertAll(it)
                }
            }
            job.join()
            return@runBlocking
        }

        fun deleteByLayoutId(layoutId: Int) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.ControllerLayoutDetailDao().deleteByLayoutId(layoutId)
            }
            job.join()
            return@runBlocking
        }

        fun deleteAll() = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.ControllerLayoutDetailDao().deleteAll()
            }
            job.join()
            return@runBlocking
        }
    }

    inner class ControlSendValue {
        fun selectAll(): List<com.sys_ky.bletankcontroller.room.entity.ControlSendValue> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.ControlSendValue> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.ControlSendValueDao().selectAll()
            }
            job.join()
            return@runBlocking rtn
        }

        fun selectByLayoutId(layoutId: Int): List<com.sys_ky.bletankcontroller.room.entity.ControlSendValue> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.ControlSendValue> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.ControlSendValueDao().selectByLayoutId(layoutId)
            }
            job.join()
            return@runBlocking rtn
        }

        fun selectByIds(layoutId: Int, controlId: Int): List<com.sys_ky.bletankcontroller.room.entity.ControlSendValue> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.ControlSendValue> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.ControlSendValueDao().selectByIds(layoutId, controlId)
            }
            job.join()
            return@runBlocking rtn
        }

        fun insertAll(controlSendValue: com.sys_ky.bletankcontroller.room.entity.ControlSendValue) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.ControlSendValueDao().insertAll(controlSendValue)
            }
            job.join()
            return@runBlocking
        }

        fun insertAll(controlSendValueList: ArrayList<com.sys_ky.bletankcontroller.room.entity.ControlSendValue>) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                controlSendValueList.forEach {
                    database.ControlSendValueDao().insertAll(it)
                }
            }
            job.join()
            return@runBlocking
        }

        fun deleteByLayoutId(layoutId: Int) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.ControllerLayoutDetailDao().deleteByLayoutId(layoutId)
            }
            job.join()
            return@runBlocking
        }

        fun deleteAll() = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.ControllerLayoutDetailDao().deleteAll()
            }
            job.join()
            return@runBlocking
        }
    }

    inner class SavedConnection {
        fun selectAll(): List<com.sys_ky.bletankcontroller.room.entity.SavedConnection> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.SavedConnection> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.SavedConnectionDao().selectAll()
            }
            job.join()
            return@runBlocking rtn
        }

        fun selectByBdAddress(bdAddress: String): List<com.sys_ky.bletankcontroller.room.entity.SavedConnection> = runBlocking {
            var rtn: List<com.sys_ky.bletankcontroller.room.entity.SavedConnection> = listOf()
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.SavedConnectionDao().selectByBdAddress(bdAddress)
            }
            job.join()
            return@runBlocking rtn
        }

        fun selectCountByLayoutId(layoutId: Int): Int = runBlocking {
            var rtn: Int = 0
            val job = CoroutineScope(Dispatchers.IO).launch {
                rtn = database.SavedConnectionDao().selectCountByLayoutId(layoutId)
            }
            job.join()
            return@runBlocking rtn
        }

        fun insertAll(savedConnection: com.sys_ky.bletankcontroller.room.entity.SavedConnection) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.SavedConnectionDao().insertAll(savedConnection)
            }
            job.join()
            return@runBlocking
        }

        fun updateUuidByBdAddress(bdAddress: String, uuidService: String, uuidTx: String, uuidRx: String) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.SavedConnectionDao().updateUuidByBdAddress(bdAddress, uuidService, uuidTx, uuidRx)
            }
            job.join()
            return@runBlocking
        }

        fun updateLayoutIdToDefault(layoutId: Int) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.SavedConnectionDao().updateLayoutIdToDefault(layoutId)
            }
            job.join()
            return@runBlocking
        }

        fun deleteByBdAddress(bdAddress: String) = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.SavedConnectionDao().deleteByBdAddress(bdAddress)
            }
            job.join()
            return@runBlocking
        }

        fun deleteAll() = runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                database.SavedConnectionDao().deleteAll()
            }
            job.join()
            return@runBlocking
        }
    }

    companion object {
        private var instance: DbCtrl? = null

        fun getInstance(context: Context?): DbCtrl {
            if (instance == null && context != null) {
                instance = DbCtrl(context)
            }
            return instance!!
        }
    }
}