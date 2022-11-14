/*
 * ControlListFragment
 *
 * コントロール一覧画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.sys_ky.bletankcontroller.common.Constants

class ControlListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_control_list, container, false)

        val controlListSpinner = view.findViewById<Spinner>(R.id.controlListSpinner)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), R.layout.control_spinner_layout, getResources().getStringArray(R.array.control_array))
        adapter.setDropDownViewResource(R.layout.control_spinner_item)
        controlListSpinner.adapter = adapter
        when(MainActivity.getControllerLayoutFragment().getViewType()) {
            Constants.cViewTypeButton -> {
                controlListSpinner.setSelection(getResources().getStringArray(R.array.control_array).indexOf("ボタン"))
            }
            Constants.cViewTypeStick -> {
                controlListSpinner.setSelection(getResources().getStringArray(R.array.control_array).indexOf("スティック"))
            }
            Constants.cViewTypeWeb -> {
                controlListSpinner.setSelection(getResources().getStringArray(R.array.control_array).indexOf("ウェブビュー"))
            }
        }

        controlListSpinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val spinner = p0 as Spinner
                var viewType = Constants.cViewTypeButton
                when(spinner.selectedItem.toString()) {
                    "ボタン" -> {
                        viewType = Constants.cViewTypeButton
                    }
                    "スティック" -> {
                        viewType = Constants.cViewTypeStick
                    }
                    "ウェブビュー" -> {
                        viewType = Constants.cViewTypeWeb
                    }
                }
                MainActivity.getControllerLayoutFragment().setViewType(viewType)
            }
        }

//        val helpButton = view.findViewById<Button>(R.id.clHelpButton)
//        helpButton.setOnClickListener {
//            Toast.makeText(requireContext(), "ヘルプ", Toast.LENGTH_SHORT).show()
//        }

        val baseConstraintLayout = view.findViewById<ConstraintLayout>(R.id.clBaseConstraintLayout)
        baseConstraintLayout.setOnTouchListener { v, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    MainActivity.closeControlListFragment()
                }
            }
            return@setOnTouchListener false
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ControlListFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}