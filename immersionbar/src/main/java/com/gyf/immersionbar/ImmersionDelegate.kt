package com.gyf.immersionbar

import android.app.Dialog
import android.content.res.Configuration
import android.os.Build
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

/**
 * @author geyifeng
 * @date 2019/4/12 4:01 PM
 */
internal class ImmersionDelegate : Runnable {
    private lateinit var mImmersionBar: ImmersionBar
    private var mBarProperties: BarProperties? = null
    private var mOnBarListener: OnBarListener? = null
    private var mNotchHeight = 0

    constructor(o: Any?) {
        if (o is AppCompatActivity) {
            if (mImmersionBar == null) {
                mImmersionBar = ImmersionBar(o as AppCompatActivity?)
            }
        } else if (o is Fragment) {
            if (mImmersionBar == null) {
                mImmersionBar = if (o is DialogFragment) {
                    ImmersionBar(dialogFragment = o)
                } else {
                    ImmersionBar(fragment = o)
                }
            }
        }
    }

    constructor(activity: AppCompatActivity?, dialog: Dialog?) {
        mImmersionBar = ImmersionBar(activity, dialog)
    }

    fun get(): ImmersionBar {
        return mImmersionBar
    }

    fun onActivityCreated(configuration: Configuration) {
        barChanged(configuration)
    }

    fun onResume() {
        mImmersionBar.onResume()
    }

    fun onDestroy() {
        mBarProperties = null
        mImmersionBar.onDestroy()
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        mImmersionBar.onConfigurationChanged(newConfig)
        barChanged(newConfig)
    }

    /**
     * 横竖屏切换监听
     * Orientation change.
     *
     * @param configuration the configuration
     */
    private fun barChanged(configuration: Configuration) {
        if (mImmersionBar != null && mImmersionBar!!.initialized() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mOnBarListener = mImmersionBar.barParams.onBarListener
            if (mOnBarListener != null) {
                val activity = mImmersionBar.activity
                if (mBarProperties == null) {
                    mBarProperties = BarProperties()
                }
                mBarProperties?.let {
                    it.isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                    when (activity?.windowManager?.defaultDisplay?.rotation) {
                        Surface.ROTATION_90 -> {
                            it.isLandscapeLeft = true
                            it.isLandscapeRight = false
                        }
                        Surface.ROTATION_270 -> {
                            it.isLandscapeLeft = false
                            it.isLandscapeRight = true
                        }
                        else -> {
                            it.isLandscapeLeft = false
                            it.isLandscapeRight = false
                        }
                    }
                }

                activity!!.window.decorView.post(this)
            }
        }
    }

    override fun run() {
        mImmersionBar.activity?.let {
            val barConfig = BarConfig(it)
            mBarProperties?.let {properties->
                properties.statusBarHeight=barConfig.statusBarHeight
                properties.setNavigationBar(barConfig.hasNavigationBar())
                properties.navigationBarHeight=barConfig.navigationBarHeight
                properties.navigationBarWidth=barConfig.navigationBarWidth
                properties.actionBarHeight = barConfig.actionBarHeight
                val notchScreen = NotchUtils.hasNotchScreen(it)
                properties.isNotchScreen = notchScreen
                if (notchScreen && mNotchHeight == 0) {
                    mNotchHeight = NotchUtils.getNotchHeight(it)
                    properties.notchHeight = (mNotchHeight)
                }
            }
            mOnBarListener?.onBarChange(mBarProperties)
        }
    }
}