/*
 * EditWebViewFragment
 *
 * ウェブビュー編集画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.sys_ky.bletankcontroller.common.ViewConfig
import com.sys_ky.bletankcontroller.control.CustomImageButton
import com.sys_ky.bletankcontroller.control.OneClickListener

private const val ARG_PARAM1 = "param1"

class EditWebViewFragment : Fragment() {
    private var mParamViewId: Int? = null
    private var mViewConfig: ViewConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mParamViewId = it.getInt(ARG_PARAM1)
        }
        if (mParamViewId == null) {
            mParamViewId = -1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_edit_web_view, container, false)
        mViewConfig = MainActivity.getControllerLayoutFragment().getViewConfig(mParamViewId!!)

        val editWebViewControllerNameTextView = view.findViewById<TextView>(R.id.editWebViewControllerNameTextView)
        editWebViewControllerNameTextView.text = MainActivity.getControllerLayoutFragment().getControllerName()

        val editWebViewUrlEditText = view.findViewById<EditText>(R.id.editWebViewUrlEditText)
        editWebViewUrlEditText.setText(mViewConfig!!.text, TextView.BufferType.NORMAL)
        editWebViewUrlEditText.setOnEditorActionListener { textView, i, keyEvent ->
            when(i) {
                EditorInfo.IME_ACTION_DONE -> {
                    editWebViewUrlEditText.clearFocus()
                }
            }
            return@setOnEditorActionListener false
        }
        editWebViewUrlEditText.setOnFocusChangeListener { v, b ->
            if (!b) {
                view.findViewById<WebView>(R.id.editWebViewSampleWebView).loadUrl(editWebViewUrlEditText.text.toString())
            }
        }

        val editWebViewSampleWebView = view.findViewById<WebView>(R.id.editWebViewSampleWebView)
        editWebViewSampleWebView.setBackgroundColor(Color.GRAY)
        editWebViewSampleWebView.webViewClient = WebViewClient()
        editWebViewSampleWebView.clearCache(true)
        editWebViewSampleWebView.clearHistory()
        editWebViewSampleWebView.setInitialScale(100)
        val settings: WebSettings = editWebViewSampleWebView.getSettings()
        settings.javaScriptEnabled = true
        editWebViewSampleWebView.loadUrl(mViewConfig!!.text)

        val editWebViewBackImageButton = view.findViewById<CustomImageButton>(R.id.editWebViewBackImageButton)
        editWebViewBackImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.edit_button_confirm_back)
                    .setPositiveButton(
                        R.string.ok,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            MainActivity.closeEditWebViewFragment()
                        })
                    .setNegativeButton(
                        R.string.cancel,
                        null
                    )
                    .show()
            }
        })

        val editWebViewFixImageButton = view.findViewById<CustomImageButton>(R.id.editWebViewFixImageButton)
        editWebViewFixImageButton.setOnOneClickListener(object: OneClickListener() {
            override fun onOneClick(view: View) {
                mViewConfig!!.text = editWebViewUrlEditText.text.toString()
                MainActivity.getControllerLayoutFragment().refViewConfig(mParamViewId!!, mViewConfig!!)
                MainActivity.closeEditWebViewFragment()
            }
        })

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(viewId: Int) =
            EditWebViewFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, viewId)
                }
            }
    }
}