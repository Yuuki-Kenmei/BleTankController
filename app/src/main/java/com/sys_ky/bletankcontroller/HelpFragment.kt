/*
 * HelpFragment
 *
 * ヘルプ画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener

class HelpFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_help, container, false)

        val helpBackImageButton = view.findViewById<CustomImageButton>(R.id.helpBackImageButton)
        helpBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                MainActivity.closeHelpFragment()
            }
        })

        val helpTextView1 = view.findViewById<TextView>(R.id.helpTextView1)
        val helpFoldLayout1 = view.findViewById<LinearLayout>(R.id.helpFoldLayout1)
        helpTextView1.setOnClickListener {
            if (helpFoldLayout1.visibility == View.GONE) {
                helpFoldLayout1.visibility = View.VISIBLE
                helpTextView1.text = "ー" + resources.getString(R.string.help1)
            } else {
                helpFoldLayout1.visibility = View.GONE
                helpTextView1.text = "＋" + resources.getString(R.string.help1)
            }
        }

        val helpTextView2 = view.findViewById<TextView>(R.id.helpTextView2)
        val helpFoldLayout2 = view.findViewById<LinearLayout>(R.id.helpFoldLayout2)
        helpTextView2.setOnClickListener {
            if (helpFoldLayout2.visibility == View.GONE) {
                helpFoldLayout2.visibility = View.VISIBLE
                helpTextView2.text = "ー" + resources.getString(R.string.help2)
            } else {
                helpFoldLayout2.visibility = View.GONE
                helpTextView2.text = "＋" + resources.getString(R.string.help2)
            }
        }

        val helpTextView3 = view.findViewById<TextView>(R.id.helpTextView3)
        val helpFoldLayout3 = view.findViewById<LinearLayout>(R.id.helpFoldLayout3)
        helpTextView3.setOnClickListener {
            if (helpFoldLayout3.visibility == View.GONE) {
                helpFoldLayout3.visibility = View.VISIBLE
                helpTextView3.text = "ー" + resources.getString(R.string.help3)
            } else {
                helpFoldLayout3.visibility = View.GONE
                helpTextView3.text = "＋" + resources.getString(R.string.help3)
            }
        }

        val helpTextView4 = view.findViewById<TextView>(R.id.helpTextView4)
        val helpFoldLayout4 = view.findViewById<LinearLayout>(R.id.helpFoldLayout4)
        helpTextView4.setOnClickListener {
            if (helpFoldLayout4.visibility == View.GONE) {
                helpFoldLayout4.visibility = View.VISIBLE
                helpTextView4.text = "ー" + resources.getString(R.string.help4)
            } else {
                helpFoldLayout4.visibility = View.GONE
                helpTextView4.text = "＋" + resources.getString(R.string.help4)
            }
        }

        helpFoldLayout1.visibility = View.GONE
        helpTextView1.text = "＋" + resources.getString(R.string.help1)
        helpFoldLayout2.visibility = View.GONE
        helpTextView2.text = "＋" + resources.getString(R.string.help2)
        helpFoldLayout3.visibility = View.GONE
        helpTextView3.text = "＋" + resources.getString(R.string.help3)
        helpFoldLayout4.visibility = View.GONE
        helpTextView4.text = "＋" + resources.getString(R.string.help4)

        return view
    }
}