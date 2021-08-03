package com.gyf.immersionbar

import android.app.Application
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import java.util.*

/**
 * 华为Emui3状态栏监听器
 *
 * @author geyifeng
 * @date 2019/4/10 6:02 PM
 */

internal object EMUI3NavigationBarObserver : ContentObserver(Handler(Looper.getMainLooper())) {
    private var mCallbacks: ArrayList<ImmersionCallback>? = null
    private lateinit var mApplication: Application
    private var mIsRegister = false

    fun register(application: Application) {
        mApplication = application
        if (mApplication.contentResolver != null && !mIsRegister) {
            val uri = Settings.System.getUriFor(Constants.IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW)
            if (uri != null) {
                mApplication.contentResolver.registerContentObserver(uri, true, this)
                mIsRegister = true
            }
        }
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        if (mApplication.contentResolver != null && mCallbacks != null && mCallbacks!!.isNotEmpty()) {
            val show = Settings.System.getInt(
                mApplication.contentResolver,
                Constants.IMMERSION_EMUI_NAVIGATION_BAR_HIDE_SHOW,
                0
            )
            for (callback in mCallbacks!!) {
                callback.onNavigationBarChange(show != 1)
            }
        }
    }

    fun addOnNavigationBarListener(callback: ImmersionCallback?) {
        if (callback == null) {
            return
        }
        if (mCallbacks == null) {
            mCallbacks = ArrayList()
        }
        if (!mCallbacks!!.contains(callback)) {
            mCallbacks!!.add(callback)
        }
    }

    fun removeOnNavigationBarListener(callback: ImmersionCallback?) {
        if (callback == null || mCallbacks == null) {
            return
        }
        mCallbacks!!.remove(callback)
    }

}