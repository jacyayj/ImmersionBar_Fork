package com.gyf.immersionbar

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.*
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Flyme OS 修改状态栏字体颜色工具类
 *
 * @author gyf
 * @date 2017 /05/30
 */
internal object SpecialBarFontUtils {
    private var mSetStatusBarColorIcon: Method? = null
    private var mSetStatusBarDarkIcon: Method? = null
    private var mStatusBarColorFiled: Field? = null
    private var SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 0

    /**
     * 判断颜色是否偏黑色
     *
     * @param color 颜色
     * @param level 级别
     * @return boolean boolean
     */
    fun isBlackColor(color: Int, level: Int): Boolean {
        val grey = toGrey(color)
        return grey < level
    }

    /**
     * 颜色转换成灰度值
     *
     * @param rgb 颜色
     * @return the int
     * @return　灰度值
     */
    fun toGrey(rgb: Int): Int {
        val blue = rgb and 0x000000FF
        val green = rgb and 0x0000FF00 shr 8
        val red = rgb and 0x00FF0000 shr 16
        return red * 38 + green * 75 + blue * 15 shr 7
    }

    /**
     * 设置状态栏字体图标颜色
     *
     * @param activity 当前activity
     * @param color    颜色
     */
    fun setStatusBarDarkIcon(activity: Activity?, color: Int) {
        if (mSetStatusBarColorIcon != null) {
            try {
                mSetStatusBarColorIcon!!.invoke(activity, color)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        } else {
            val whiteColor = isBlackColor(color, 50)
            if (mStatusBarColorFiled != null) {
                setStatusBarDarkIcon(activity, whiteColor, whiteColor)
                setStatusBarDarkIcon(activity!!.window, color)
            } else {
                setStatusBarDarkIcon(activity, whiteColor)
            }
        }
    }

    /**
     * 设置状态栏字体图标颜色(只限全屏非activity情况)
     *
     * @param window 当前窗口
     * @param color  颜色
     */
    fun setStatusBarDarkIcon(window: Window, color: Int) {
        try {
            setStatusBarColor(window, color)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                setStatusBarDarkIcon(window.decorView, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置状态栏字体图标颜色
     *
     * @param activity 当前activity
     * @param dark     是否深色 true为深色 false 为白色
     */
    fun setStatusBarDarkIcon(activity: Activity?, dark: Boolean) {
        setStatusBarDarkIcon(activity, dark, true)
    }

    private fun changeMeizuFlag(
        winParams: WindowManager.LayoutParams,
        flagName: String,
        on: Boolean
    ): Boolean {
        try {
            val f = winParams.javaClass.getDeclaredField(flagName)
            f.isAccessible = true
            val bits = f.getInt(winParams)
            val f2 = winParams.javaClass.getDeclaredField("meizuFlags")
            f2.isAccessible = true
            var meizuFlags = f2.getInt(winParams)
            val oldFlags = meizuFlags
            meizuFlags = if (on) {
                meizuFlags or bits
            } else {
                meizuFlags and bits.inv()
            }
            if (oldFlags != meizuFlags) {
                f2.setInt(winParams, meizuFlags)
                return true
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 设置状态栏颜色
     *
     * @param view
     * @param dark
     */
    private fun setStatusBarDarkIcon(view: View, dark: Boolean) {
        val oldVis = view.systemUiVisibility
        var newVis = oldVis
        newVis = if (dark) {
            newVis or SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            newVis and SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        if (newVis != oldVis) {
            view.systemUiVisibility = newVis
        }
    }

    /**
     * 设置状态栏颜色
     *
     * @param window
     * @param color
     */
    private fun setStatusBarColor(window: Window, color: Int) {
        val winParams = window.attributes
        if (mStatusBarColorFiled != null) {
            try {
                val oldColor = mStatusBarColorFiled!!.getInt(winParams)
                if (oldColor != color) {
                    mStatusBarColorFiled!![winParams] = color
                    window.attributes = winParams
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 设置状态栏字体图标颜色(只限全屏非activity情况)
     *
     * @param window 当前窗口
     * @param dark   是否深色 true为深色 false 为白色
     */
    fun setStatusBarDarkIcon(window: Window, dark: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            changeMeizuFlag(window.attributes, "MEIZU_FLAG_DARK_STATUS_BAR_ICON", dark)
        } else {
            val decorView = window.decorView
            setStatusBarDarkIcon(decorView, dark)
            setStatusBarColor(window, 0)
        }
    }

    private fun setStatusBarDarkIcon(activity: Activity?, dark: Boolean, flag: Boolean) {
        if (mSetStatusBarDarkIcon != null) {
            try {
                mSetStatusBarDarkIcon!!.invoke(activity, dark)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        } else {
            if (flag) {
                setStatusBarDarkIcon(activity!!.window, dark)
            }
        }
    }

    @SuppressLint("PrivateApi")
    fun setMIUIBarDark(window: Window?, key: String?, dark: Boolean) {
        if (window != null) {
            val clazz: Class<out Window> = window.javaClass
            try {
                val darkModeFlag: Int
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField(key)
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod(
                    "setExtraFlags",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                if (dark) {
                    //状态栏透明且黑色字体
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag)
                } else {
                    //清除黑色字体
                    extraFlagField.invoke(window, 0, darkModeFlag)
                }
            } catch (ignored: Exception) {
            }
        }
    }

    init {
        try {
            mSetStatusBarColorIcon =
                Activity::class.java.getMethod("setStatusBarDarkIcon", Int::class.javaPrimitiveType)
        } catch (ignored: NoSuchMethodException) {
        }
        try {
            mSetStatusBarDarkIcon = Activity::class.java.getMethod(
                "setStatusBarDarkIcon",
                Boolean::class.javaPrimitiveType
            )
        } catch (ignored: NoSuchMethodException) {
        }
        try {
            mStatusBarColorFiled = WindowManager.LayoutParams::class.java.getField("statusBarColor")
        } catch (ignored: NoSuchFieldException) {
        }
        try {
            val field = View::class.java.getField("SYSTEM_UI_FLAG_LIGHT_STATUS_BAR")
            SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = field.getInt(null)
        } catch (ignored: NoSuchFieldException) {
        } catch (ignored: IllegalAccessException) {
        }
    }
}