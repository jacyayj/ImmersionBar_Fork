package com.gyf.immersionbar

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AppCompatActivity

/**
 * The type Bar config.
 *
 * @author geyifeng
 * @date 2017 /5/11
 */
class BarConfig(activity: AppCompatActivity) {
    /**
     * Get the height of the system status bar.
     *
     * @return The height of the status bar (in pixels).
     */
    val statusBarHeight: Int

    /**
     * Get the height of the action bar.
     *
     * @return The height of the action bar (in pixels).
     */
    val actionBarHeight: Int

    private val mHasNavigationBar: Boolean

    /**
     * Get the height of the system navigation bar.
     *
     * @return The height of the navigation bar (in pixels). If the device does not have soft navigation keys, this will always return 0.
     */
    val navigationBarHeight: Int

    /**
     * Get the width of the system navigation bar when it is placed vertically on the screen.
     *
     * @return The width of the navigation bar (in pixels). If the device does not have soft navigation keys, this will always return 0.
     */
    val navigationBarWidth: Int
    private val mInPortrait: Boolean
    private val mSmallestWidthDp: Float

    @TargetApi(14)
    fun getActionBarHeight(activity: Activity): Int {
        var result = 0
        val actionBar = activity.window.findViewById<View>(R.id.action_bar_container)
        if (actionBar != null) {
            result = actionBar.measuredHeight
        }
        if (result == 0) {
            val tv = TypedValue()
            activity.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
            result = TypedValue.complexToDimensionPixelSize(
                tv.data,
                activity.resources.displayMetrics
            )
        }
        return result
    }

    @TargetApi(14)
    fun getNavigationBarHeight(context: Context?): Int {
        val result = 0
        if (hasNavBar(context as Activity?)) {
            val key: String = if (mInPortrait) {
                Constants.IMMERSION_NAVIGATION_BAR_HEIGHT
            } else {
                Constants.IMMERSION_NAVIGATION_BAR_HEIGHT_LANDSCAPE
            }
            return getInternalDimensionSize(context, key)
        }
        return result
    }

    @TargetApi(14)
    fun getNavigationBarWidth(context: Context?): Int {
        val result = 0
        if (hasNavBar(context as Activity?)) {
            return getInternalDimensionSize(context, Constants.IMMERSION_NAVIGATION_BAR_WIDTH)
        }
        return result
    }

    @TargetApi(14)
    private fun hasNavBar(activity: Activity?): Boolean {
        //判断小米手机是否开启了全面屏，开启了，直接返回false
        if (Settings.Global.getInt(
                activity!!.contentResolver,
                Constants.IMMERSION_MIUI_NAVIGATION_BAR_HIDE_SHOW,
                0
            ) != 0
        ) {
            return false
        }
        //判断华为手机是否隐藏了导航栏，隐藏了，直接返回false
        if (OSUtils.isEMUI) {
            if (OSUtils.isEMUI3_x) {
                if (Settings.System.getInt(
                        activity.contentResolver,
                        Constants.IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW,
                        0
                    ) != 0
                ) {
                    return false
                }
            }
        }
        //其他手机根据屏幕真实高度与显示高度是否相同来判断
        val windowManager = activity.windowManager
        val d = windowManager.defaultDisplay
        val realDisplayMetrics = DisplayMetrics()
        d.getRealMetrics(realDisplayMetrics)
        val realHeight = realDisplayMetrics.heightPixels
        val realWidth = realDisplayMetrics.widthPixels
        val displayMetrics = DisplayMetrics()
        d.getMetrics(displayMetrics)
        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels
        return realWidth - displayWidth > 0 || realHeight - displayHeight > 0
    }

    private fun getInternalDimensionSize(context: Context?, key: String?): Int {
        val result = 0
        try {
            val resourceId = Resources.getSystem().getIdentifier(key, "dimen", "android")
            if (resourceId > 0) {
                val sizeOne = context!!.resources.getDimensionPixelSize(resourceId)
                val sizeTwo = Resources.getSystem().getDimensionPixelSize(resourceId)
                return if (sizeTwo >= sizeOne) {
                    sizeTwo
                } else {
                    val densityOne = context.resources.displayMetrics.density
                    val densityTwo = Resources.getSystem().displayMetrics.density
                    val f = sizeOne * densityTwo / densityOne
                    (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
                }
            }
        } catch (ignored: NotFoundException) {
            return 0
        }
        return result
    }

    @SuppressLint("NewApi")
    private fun getSmallestWidthDp(activity: AppCompatActivity): Float {
        val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getRealMetrics(metrics)
        val widthDp = metrics.widthPixels / metrics.density
        val heightDp = metrics.heightPixels / metrics.density
        return widthDp.coerceAtMost(heightDp)
    }

    /**
     * Should a navigation bar appear at the bottom of the screen in the current
     * device configuration? A navigation bar may appear on the right side of
     * the screen in certain configurations.
     *
     * @return True if navigation should appear at the bottom of the screen, False otherwise.
     */
    val isNavigationAtBottom: Boolean
        get() = mSmallestWidthDp >= 600 || mInPortrait

    /**
     * Does this device have a system navigation bar?
     *
     * @return True if this device uses soft key navigation, False otherwise.
     */
    fun hasNavigationBar(): Boolean {
        return mHasNavigationBar
    }

    /**
     * Instantiates a new Bar config.
     *
     * @param activity the activity
     */
    init {
        val res = activity.resources
        mInPortrait = res.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        mSmallestWidthDp = getSmallestWidthDp(activity)
        statusBarHeight = getInternalDimensionSize(activity, Constants.IMMERSION_STATUS_BAR_HEIGHT)
        actionBarHeight = getActionBarHeight(activity)
        navigationBarHeight = getNavigationBarHeight(activity)
        navigationBarWidth = getNavigationBarWidth(activity)
        mHasNavigationBar = navigationBarHeight > 0
    }
}