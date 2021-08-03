package com.gyf.immersionbar

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * @author geyifeng
 * @date 2019/4/11 6:43 PM
 */
class RequestManagerFragment : Fragment() {
    private lateinit var mDelegate: ImmersionDelegate
    operator fun get(o: Any?): ImmersionBar {
        if (::mDelegate.isInitialized.not()) {
            mDelegate = ImmersionDelegate(o)
        }
        return mDelegate.get()
    }

    operator fun get(activity: AppCompatActivity?, dialog: Dialog?): ImmersionBar? {
        if (::mDelegate.isInitialized.not()) {
            mDelegate = ImmersionDelegate(activity, dialog)
        }
        return mDelegate.get()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (::mDelegate.isInitialized) {
            mDelegate!!.onActivityCreated(resources.configuration)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mDelegate.isInitialized) {
            mDelegate.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mDelegate.isInitialized) {
            mDelegate.onDestroy()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::mDelegate.isInitialized) {
            mDelegate.onConfigurationChanged(newConfig)
        }
    }
}