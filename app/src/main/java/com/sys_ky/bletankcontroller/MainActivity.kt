/*
 * MainActivity
 *
 * メインアクティビティ
 */

package com.sys_ky.bletankcontroller

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.Fade
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.FragmentManager
import androidx.transition.Slide
import com.sys_ky.bletankcontroller.ble.BlePeripheral
import com.sys_ky.bletankcontroller.common.Constants
import com.sys_ky.bletankcontroller.common.GridPoint
import com.sys_ky.bletankcontroller.common.SendValueMap
import com.sys_ky.bletankcontroller.room.DbCtrl
import com.sys_ky.bletankcontroller.room.entity.*
import java.util.*

class MainActivity : AppCompatActivity() {

    //Bluetooth起動確認起動用
    private val mBluetoothActiveLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.error)
                    .setMessage(R.string.main_error_bluetooth_not)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            finish()
                        })
                    .show()
                finish()
            }
        }

    //パーミッション画面起動用
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(),) { result ->
        var cancelFlg = false
        result.forEach {
            if (it.value) {
            } else {
                cancelFlg = true
                return@forEach
            }
        }
        if (cancelFlg) {
            //NG
            AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(R.string.main_error_permission)
                .setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        finish()
                    })
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFragmentManager = supportFragmentManager

        mMainBackgroundConstraintLayout = findViewById<ConstraintLayout>(R.id.mainBackgroundConstraintLayout)

        //タイトルで動かす画像のセットアップ
        mImageView = ImageView(this)
        mImageView.id = View.generateViewId()
        mImageView.setImageResource(R.drawable.img_sensya_r)
        mMainBackgroundConstraintLayout.addView(mImageView)

        mDisplayWidth = this.resources.displayMetrics.widthPixels
        mDisplayHeight = this.resources.displayMetrics.heightPixels
        mImageWidth = (mDisplayWidth * 0.1).toInt()
        mImageTopMargin = (mDisplayHeight * 0.4).toInt() - mImageWidth

        timerStart()

        mFragmentContainerConstraintLayout = ConstraintLayout(this)
        mFragmentContainerConstraintLayout.id = View.generateViewId()
        mMainBackgroundConstraintLayout.addView(mFragmentContainerConstraintLayout)
        val constraintSet = ConstraintSet()
        constraintSet.clone(mMainBackgroundConstraintLayout)
        constraintSet.constrainHeight(mFragmentContainerConstraintLayout.id, 0)
        constraintSet.constrainWidth(mFragmentContainerConstraintLayout.id, 0)
        constraintSet.connect(mFragmentContainerConstraintLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        constraintSet.connect(mFragmentContainerConstraintLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
        constraintSet.connect(mFragmentContainerConstraintLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(mFragmentContainerConstraintLayout.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
        constraintSet.applyTo(mMainBackgroundConstraintLayout)

        showMenuButtonsFragment()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gBluetoothAdapter = bluetoothManager.adapter

        //Bluetoothがサポートされていない場合、終了
        if (gBluetoothAdapter == null) {
            AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(R.string.main_error_bluetooth_support)
                .setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        finish()
                    })
                .show()
        }

        //BluetoothがOnでない場合、起動確認
        if (gBluetoothAdapter?.isEnabled == false) {
            mBluetoothActiveLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        //パーミッション
        val permissionArray = arrayListOf<String>()
        if (Build.VERSION.SDK_INT <= 30) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(Manifest.permission.BLUETOOTH)
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionArray.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionArray.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (Build.VERSION.SDK_INT > 30) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (Build.VERSION.SDK_INT > 30) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionArray.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        permissionArray.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionArray.size > 0) {
            requestPermissionLauncher.launch(permissionArray.toTypedArray())
        }

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        val dbCtrl = DbCtrl.getInstance(applicationContext)

        //基本設定が空の場合、初期セット
        if (dbCtrl.BaseSetting().selectAll().isEmpty()) {
            dbCtrl.BaseSetting().insertAll(BaseSetting(0, Constants.cDefaultUUIDService, Constants.cDefaultUUIDCharacteristicTX, Constants.cDefaultUUIDCharacteristicRX))
        }

        //デフォルトコントローラーレイアウトが空の場合、初期セット
        if (dbCtrl.ControllerLayout().selectByLayoutId(0).isEmpty()) {
            val gridPointMap: MutableMap<Array<Int>, GridPoint> = GridPoint.createGridPointMap(mMainBackgroundConstraintLayout.height, mMainBackgroundConstraintLayout.width, (mMainBackgroundConstraintLayout.height * 0.2).toInt())
            val widthIntervalCount: Int = GridPoint.getPitch(mMainBackgroundConstraintLayout.height, mMainBackgroundConstraintLayout.width, (mMainBackgroundConstraintLayout.height * 0.2).toInt())[2].toInt()
            val heightIntervalCount: Int = GridPoint.getHeightIntervalCount()

            var stickBlock: Int = 6
            var buttonWidthBlock: Int = 4
            var buttonHeightBlock: Int = 2

            while(stickBlock * 2 > widthIntervalCount) {
                stickBlock--
            }
            while(buttonWidthBlock * 2 + 1 > widthIntervalCount) {
                buttonWidthBlock--
            }

            lateinit var stick1TL: GridPoint
            lateinit var stick1BR: GridPoint
            lateinit var stick2TL: GridPoint
            lateinit var stick2BR: GridPoint
            lateinit var button1TL: GridPoint
            lateinit var button1BR: GridPoint
            lateinit var button2TL: GridPoint
            lateinit var button2BR: GridPoint
            gridPointMap.forEach {
                if (it.key[0] == 0 && it.key[1] == heightIntervalCount - stickBlock) {
                    stick1TL = it.value
                }
                if (it.key[0] == stickBlock && it.key[1] == heightIntervalCount) {
                    stick1BR = it.value
                }

                if (it.key[0] == widthIntervalCount - stickBlock && it.key[1] == heightIntervalCount - stickBlock) {
                    stick2TL = it.value
                }
                if (it.key[0] == widthIntervalCount && it.key[1] == heightIntervalCount) {
                    stick2BR = it.value
                }

                if (it.key[0] == widthIntervalCount - (buttonWidthBlock * 2 + 1) && it.key[1] == heightIntervalCount - stickBlock - buttonHeightBlock) {
                    button1TL = it.value
                }
                if (it.key[0] == widthIntervalCount - (buttonWidthBlock + 1) && it.key[1] == heightIntervalCount - stickBlock) {
                    button1BR = it.value
                }

                if (it.key[0] == widthIntervalCount - buttonWidthBlock && it.key[1] == heightIntervalCount - stickBlock - buttonHeightBlock) {
                    button2TL = it.value
                }
                if (it.key[0] == widthIntervalCount && it.key[1] == heightIntervalCount - stickBlock) {
                    button2BR = it.value
                }
            }

            val controllerLayout: ControllerLayout = ControllerLayout(0, "デフォルトコントローラー")
            dbCtrl.ControllerLayout().insertAll(controllerLayout)

            val controllerLayoutDetailList: ArrayList<ControllerLayoutDetail> = arrayListOf()
            val controlSendValueList: ArrayList<ControlSendValue> = arrayListOf()

            val layoutId: Int = 0
            var controlId: Int = 0
            val step: Int = 2
            val split: Int = 8

            controlId++
            controllerLayoutDetailList.add(
                ControllerLayoutDetail(
                    layoutId,
                    controlId,
                    Constants.cViewTypeStick,
                    stick1BR.x - stick1TL.x,
                    stick1BR.y - stick1TL.y,
                    stick1TL.y,
                    stick1TL.x,
                    "",
                    step,
                    split,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
                )
            )
            val sendValueMap1: SendValueMap = SendValueMap.createInitStickSendValueMap(step, split, controlId)
            sendValueMap1.getSendValueAll().keys.forEach { key ->
                controlSendValueList.add(
                    ControlSendValue(
                        layoutId,
                        controlId,
                        key[0],
                        key[1],
                        sendValueMap1.getSendValue(key[0], key[1])
                    )
                )
            }


            controlId++
            controllerLayoutDetailList.add(
                ControllerLayoutDetail(
                    layoutId,
                    controlId,
                    Constants.cViewTypeStick,
                    stick2BR.x - stick2TL.x,
                    stick2BR.y - stick2TL.y,
                    stick2TL.y,
                    stick2TL.x,
                    "",
                    step,
                    split,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
                )
            )
            val sendValueMap2 = SendValueMap.createInitStickSendValueMap(step, split, controlId)
            sendValueMap2.getSendValueAll().keys.forEach { key ->
                controlSendValueList.add(
                    ControlSendValue(
                        layoutId,
                        controlId,
                        key[0],
                        key[1],
                        sendValueMap2.getSendValue(key[0], key[1])
                    )
                )
            }


            controlId++
            controllerLayoutDetailList.add(
                ControllerLayoutDetail(
                    layoutId,
                    controlId,
                    Constants.cViewTypeButton,
                    button1BR.x - button1TL.x,
                    button1BR.y - button1TL.y,
                    button1TL.y,
                    button1TL.x,
                    "BUTTON1",
                    step,
                    split,
                    Constants.cDefaultButtonBackColor.red,
                    Constants.cDefaultButtonBackColor.green,
                    Constants.cDefaultButtonBackColor.blue,
                    Constants.cDefaultButtonTextColor.red,
                    Constants.cDefaultButtonTextColor.green,
                    Constants.cDefaultButtonTextColor.blue
                )
            )
            controlSendValueList.add(
                ControlSendValue(
                    layoutId,
                    controlId,
                    0,
                    0,
                    "!B!1"
                )
            )


            controlId++
            controllerLayoutDetailList.add(
                ControllerLayoutDetail(
                    layoutId,
                    controlId,
                    Constants.cViewTypeButton,
                    button2BR.x - button2TL.x,
                    button2BR.y - button2TL.y,
                    button2TL.y,
                    button2TL.x,
                    "BUTTON2",
                    step,
                    split,
                    Constants.cDefaultButtonBackColor.red,
                    Constants.cDefaultButtonBackColor.green,
                    Constants.cDefaultButtonBackColor.blue,
                    Constants.cDefaultButtonTextColor.red,
                    Constants.cDefaultButtonTextColor.green,
                    Constants.cDefaultButtonTextColor.blue
                )
            )
            controlSendValueList.add(
                ControlSendValue(
                    layoutId,
                    controlId,
                    0,
                    0,
                    "!B!2"
                )
            )

            dbCtrl.ControllerLayoutDetail().insertAll(controllerLayoutDetailList)
            dbCtrl.ControlSendValue().insertAll(controlSendValueList)
        }
    }

    override fun onBackPressed() {
        //タイトルにいるとき（＝メニューボタンフラグメントだけ表示されているとき）のみ、バックボタンでアプリ終了
        if (mFragmentManager.fragments.size == 1 && mFragmentManager.fragments[0].id == mMenuButtonsFragment.id) {
            endActivity()
        } else {
            return
        }
    }

    //確認ダイアログ後、Activityを終了する
    private fun endActivity() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm)
            .setMessage(R.string.activity_confirm_end)
            .setPositiveButton(
                R.string.ok,
                DialogInterface.OnClickListener { dialogInterface, i ->
                    finish()
                })
            .setNegativeButton(
                R.string.cancel,
                null
            )
            .show()
    }

    companion object {
        private lateinit var mFragmentManager: FragmentManager
        private lateinit var mImageView: ImageView
        private lateinit var mMainBackgroundConstraintLayout: ConstraintLayout
        private lateinit var mFragmentContainerConstraintLayout: ConstraintLayout

        private var mTimer: Timer? = null

        private var mFirstFlg: Boolean = true
        private var mDisplayWidth: Int = 0
        private var mDisplayHeight: Int = 0
        private var mImageWidth: Int = 0
        private var mImageTopMargin: Int = 0
        private var mImageLeftMargin: Int = 0
        private var mAddMargin: Int = 10

        //region フラグメント変数
        private val mMenuButtonsFragment = MenuButtonsFragment()
        private val mSettingFragment = SettingFragment()
        private val mHelpFragment = HelpFragment()
        private val mNowLoadingFragment = NowLoadingFragment()
        private val mBaseSettingFragment = BaseSettingFragment()
        private var mControllerListFragment = ControllerListFragment()
        private var mControllerLayoutFragment = ControllerLayoutFragment()
        private val mControlListFragment = ControlListFragment()
        private var mEditButtonFragment = EditButtonFragment()
        private var mEditStickFragment = EditStickFragment()
        private var mEditStickSendFragment = EditStickSendFragment()
        private var mEditWebViewFragment = EditWebViewFragment()
        private var mEditColorFragment = EditColorFragment()
        private var mConnectFragment = ConnectFragment()
        private var mUuidEditFragment = UuidEditFragment()
        private var mConnectionEditFragment = ConnectionEditFragment()
        private var mControllerPadFragment = ControllerPadFragment()
        private var mSavedConnectionListFragment = SavedConnectionListFragment()
        //endregion

        var gBluetoothAdapter: BluetoothAdapter? = null
        var gCurrentBlePeripheral: BlePeripheral? = null

        init {
            val fade = Fade()
            mHelpFragment.enterTransition = fade
            mHelpFragment.exitTransition = fade
            mSettingFragment.enterTransition = fade
            mSettingFragment.exitTransition = fade
            mBaseSettingFragment.enterTransition = fade
            mBaseSettingFragment.exitTransition = fade
            mControllerListFragment.enterTransition = fade
            mControllerListFragment.exitTransition = fade
            mConnectFragment.enterTransition = fade
            mConnectFragment.exitTransition = fade
            mSavedConnectionListFragment.enterTransition = fade
            mSavedConnectionListFragment.exitTransition = fade

            val slide = Slide()
            slide.slideEdge = Gravity.RIGHT
            mControlListFragment.enterTransition = slide
            mControlListFragment.exitTransition = slide
        }

        //タイトルで画像を端から端まで往復するように動かす
        private fun moveImage() {
            if (!mFirstFlg && (mImageLeftMargin <= 0 || mImageLeftMargin >= mDisplayWidth - mImageWidth)) {
                mAddMargin *= -1
                if (mAddMargin < 0) {
                    mImageView.setImageResource(R.drawable.img_sensya_l)
                } else {
                    mImageView.setImageResource(R.drawable.img_sensya_r)
                }
            }
            mImageLeftMargin += mAddMargin

            applyImageConstraint()

            mFirstFlg = false
        }

        //画像の配置
        private fun applyImageConstraint() {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mMainBackgroundConstraintLayout)
            constraintSet.constrainHeight(mImageView.id, mImageWidth)
            constraintSet.constrainWidth(mImageView.id, mImageWidth)
            constraintSet.connect(mImageView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, mImageLeftMargin)
            constraintSet.connect(mImageView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, mImageTopMargin)
            constraintSet.applyTo(mMainBackgroundConstraintLayout)
        }

        //タイトルで画像を動かすタイマーをスタート
        private fun timerStart() {
            if (mTimer == null) {
                mTimer= Timer()
                //100ミリ秒ごとに動かす
                mTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        Handler(Looper.getMainLooper()).post {
                            moveImage()
                        }
                    }
                }, 100, 100)
            }
        }

        //タイトルで画像を動かすタイマーをストップ（動かしっぱだと重くなりそうなので）
        private fun timerStop() {
            mTimer?.cancel()
            mTimer = null
        }

        //フラグメント表示用レイアウト取得
        fun getFragmentContainer(): ConstraintLayout {
            return mFragmentContainerConstraintLayout
        }

        //コントローラーレイアウトフラグメント取得
        fun getControllerLayoutFragment(): ControllerLayoutFragment {
            return mControllerLayoutFragment
        }

        //ボタン編集フラグメント取得
        fun getEditButtonFragment(): EditButtonFragment {
            return mEditButtonFragment
        }

        //スティック編集フラグメント取得
        fun getEditStickFragment(): EditStickFragment {
            return mEditStickFragment
        }

        //region フラグメント表示、非表示関数群
        fun showMenuButtonsFragment() {
            if (mFragmentManager.findFragmentById(mMenuButtonsFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mMenuButtonsFragment)
            fragmentTransaction.commit()
        }

        fun closeMenuButtonsFragment() {
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mMenuButtonsFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showSettingFragment() {
            if (mFragmentManager.findFragmentById(mSettingFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mSettingFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            timerStop()
        }

        fun closeSettingFragment() {
            if (mFragmentManager.findFragmentById(mSettingFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mSettingFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()

            timerStart()
        }

        fun showBaseSettingFragment() {
            if (mFragmentManager.findFragmentById(mBaseSettingFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mBaseSettingFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeBaseSettingFragment() {
            if (mFragmentManager.findFragmentById(mBaseSettingFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mBaseSettingFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showControllerListFragment() {
            if (mFragmentManager.findFragmentById(mControllerListFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mControllerListFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeControllerListFragment() {
            if (mFragmentManager.findFragmentById(mControllerListFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mControllerListFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showControllerLayoutFragment(layoutId: Int, copyFlg: Boolean) {
            mControllerLayoutFragment = ControllerLayoutFragment.newInstance(layoutId, copyFlg)
            val slide = Slide()
            slide.slideEdge = Gravity.TOP
            mControllerLayoutFragment.enterTransition = slide
            mControllerLayoutFragment.exitTransition = slide

            if (mFragmentManager.findFragmentById(mControllerLayoutFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mControllerLayoutFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeControllerLayoutFragment(listReload: Boolean) {
            if (mFragmentManager.findFragmentById(mControllerLayoutFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mControllerLayoutFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()

            if (listReload) {
                //コントローラーリストをリロード
                mControllerListFragment.setControllerList()
            }
        }

        fun showControlListFragment() {
            if (mFragmentManager.findFragmentById(mControlListFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mControlListFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeControlListFragment() {
            if (mFragmentManager.findFragmentById(mControlListFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mControlListFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showEditButtonFragment(viewId: Int) {
            mEditButtonFragment = EditButtonFragment.newInstance(viewId)
            val fade = Fade()
            mEditButtonFragment.enterTransition = fade
            mEditButtonFragment.exitTransition = fade

            if (mFragmentManager.findFragmentById(mEditButtonFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mEditButtonFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeEditButtonFragment() {
            if (mFragmentManager.findFragmentById(mEditButtonFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mEditButtonFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showEditStickFragment(viewId: Int) {
            mEditStickFragment = EditStickFragment.newInstance(viewId)
            val fade = Fade()
            mEditStickFragment.enterTransition = fade
            mEditStickFragment.exitTransition = fade

            if (mFragmentManager.findFragmentById(mEditStickFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mEditStickFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeEditStickFragment() {
            if (mFragmentManager.findFragmentById(mEditStickFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mEditStickFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showEditStickSendFragment(viewId: Int, step: Int, split: Int, flg: Boolean) {
            mEditStickSendFragment = EditStickSendFragment.newInstance(viewId, step, split, flg)
            val fade = Fade()
            mEditStickSendFragment.enterTransition = fade
            mEditStickSendFragment.exitTransition = fade

            if (mFragmentManager.findFragmentById(mEditStickSendFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mEditStickSendFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeEditStickSendFragment() {
            if (mFragmentManager.findFragmentById(mEditStickSendFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mEditStickSendFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showEditWebViewFragment(viewId: Int) {
            mEditWebViewFragment = EditWebViewFragment.newInstance(viewId)
            val fade = Fade()
            mEditWebViewFragment.enterTransition = fade
            mEditWebViewFragment.exitTransition = fade

            if (mFragmentManager.findFragmentById(mEditWebViewFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mEditWebViewFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeEditWebViewFragment() {
            if (mFragmentManager.findFragmentById(mEditWebViewFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mEditWebViewFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showEditColorFragment(viewId: Int, kbn: Int) {
            mEditColorFragment = EditColorFragment.newInstance(viewId, kbn)
            val fade = Fade()
            mEditColorFragment.enterTransition = fade
            mEditColorFragment.exitTransition = fade

            if (mFragmentManager.findFragmentById(mEditColorFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mEditColorFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeEditColorFragment() {
            if (mFragmentManager.findFragmentById(mEditColorFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mEditColorFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showConnectFragment() {
            if (mFragmentManager.findFragmentById(mConnectFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mConnectFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeConnectFragment() {
            if (mFragmentManager.findFragmentById(mConnectFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mConnectFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showUuidEditFragment() {
            val slide = Slide()
            slide.slideEdge = Gravity.TOP
            mUuidEditFragment.enterTransition = slide
            mUuidEditFragment.exitTransition = slide

            if (mFragmentManager.findFragmentById(mUuidEditFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mUuidEditFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeUuidEditFragment() {
            if (mFragmentManager.findFragmentById(mUuidEditFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mUuidEditFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showConnectionEditFragment(bdAddress: String?) {
            mConnectionEditFragment = ConnectionEditFragment.newInstance(bdAddress)
            val slide = Slide()
            slide.slideEdge = Gravity.TOP
            mConnectionEditFragment.enterTransition = slide
            mConnectionEditFragment.exitTransition = slide

            if (mFragmentManager.findFragmentById(mConnectionEditFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mConnectionEditFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeConnectionEditFragment(listReload: Boolean) {
            if (mFragmentManager.findFragmentById(mConnectionEditFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mConnectionEditFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()

            if (listReload) {
                //保存済み接続先一覧をリロード
                mSavedConnectionListFragment.setSavedConnectionList()
            }
        }

        fun showControllerPadFragment(layoutId: Int, connectionName: String) {
            mControllerPadFragment = ControllerPadFragment.newInstance(layoutId, connectionName)
            val slide = Slide()
            slide.slideEdge = Gravity.TOP
            mControllerPadFragment.enterTransition = slide
            mControllerPadFragment.exitTransition = slide

            if (mFragmentManager.findFragmentById(mControllerPadFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mControllerPadFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeControllerPadFragment(listReload: Boolean) {
            if (mFragmentManager.findFragmentById(mControllerPadFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mControllerPadFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()

            if (listReload) {
                //接続先スキャンを実行
                mConnectFragment.startScan()
            }
        }

        fun showSavedConnectionListFragment() {
            if (mFragmentManager.findFragmentById(mSavedConnectionListFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mSavedConnectionListFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeSavedConnectionListFragment() {
            if (mFragmentManager.findFragmentById(mSavedConnectionListFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mSavedConnectionListFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }

        fun showHelpFragment() {
            if (mFragmentManager.findFragmentById(mHelpFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mHelpFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

            timerStop()
        }

        fun closeHelpFragment() {
            if (mFragmentManager.findFragmentById(mHelpFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mHelpFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()

            timerStart()
        }

        fun showNowLoadingFragment() {
            if (mFragmentManager.findFragmentById(mNowLoadingFragment.id) != null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.add(mFragmentContainerConstraintLayout.id, mNowLoadingFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        fun closeNowLoadingFragment() {
            if (mFragmentManager.findFragmentById(mNowLoadingFragment.id) == null) {
                return
            }
            val fragmentTransaction = mFragmentManager.beginTransaction()
            fragmentTransaction.remove(mNowLoadingFragment)
            fragmentTransaction.commit()
            mFragmentManager.popBackStack()
        }
        //endregion
    }
}