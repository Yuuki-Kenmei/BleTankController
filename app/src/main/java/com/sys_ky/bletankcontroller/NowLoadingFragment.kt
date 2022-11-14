/*
 * NowLoadingFragment
 *
 * ローディング画面フラグメント
 */

package com.sys_ky.bletankcontroller

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.sys_ky.bletankcontroller.control.CustomAnimatedDrawable
import java.io.IOException

class NowLoadingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_loading, container, false)

        return view
    }

    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= 28) {
            startGif()
        } else {
            startAnimate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun startGif() {
        try {
            val loadingImageView = requireView().findViewById<ImageView>(R.id.loadingImageView)
            val source = ImageDecoder.createSource(resources, R.raw.loading)
            val drawable = ImageDecoder.decodeDrawable(source) as AnimatedImageDrawable
            loadingImageView.setImageDrawable(drawable)
            drawable.start()
        } catch (e: IOException) {

        } catch (e: java.lang.ClassCastException) {

        }
    }

    private fun startAnimate() {
        try {
            val loadingImageView = requireView().findViewById<ImageView>(R.id.loadingImageView)
            val inputStream =resources.openRawResource(R.raw.loading)
            val drawable = CustomAnimatedDrawable(inputStream)
            loadingImageView.setImageDrawable(drawable)
        } catch (e: IOException) {

        } catch (e: java.lang.ClassCastException) {

        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            NowLoadingFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}