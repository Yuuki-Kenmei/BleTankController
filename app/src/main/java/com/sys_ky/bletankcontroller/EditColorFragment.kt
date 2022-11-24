/*
 * EditColorFragment
 *
 * ボタン色編集画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.sys_ky.bletankcontroller.common.ViewConfig
import com.sys_ky.colorparette.ColorPalette

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class EditColorFragment : Fragment() {
    private var mKbn: Int? = null
    private var mParamViewId: Int? = null
    private var mViewConfig: ViewConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamViewId = it.getInt(ARG_PARAM1)
            mKbn = it.getInt(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_color, container, false)

        mViewConfig = MainActivity.getControllerLayoutFragment().getViewConfig(mParamViewId!!)

        val colorPalette = view.findViewById<ColorPalette>(R.id.editColorColorPalette)
        if (mKbn == 1) {
            colorPalette.setColor(mViewConfig!!.color1.red, mViewConfig!!.color1.green, mViewConfig!!.color1.blue)
        } else if (mKbn == 2) {
            colorPalette.setColor(mViewConfig!!.color2.red, mViewConfig!!.color2.green, mViewConfig!!.color2.blue)
        }

        colorPalette.setOnColorChangeListener(object: ColorPalette.OnColorChangeListener {
            override fun onColorChangeEvent(r: Int, g: Int, b: Int, fixFlg: Boolean) {
                MainActivity.getEditButtonFragment().applyColorChangedToSample(r, g, b, mKbn!!)

                if (fixFlg) {
                    MainActivity.closeEditColorFragment()
                }
            }
        })

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int, param2: Int) =
            EditColorFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, param2)
                }
            }
    }
}