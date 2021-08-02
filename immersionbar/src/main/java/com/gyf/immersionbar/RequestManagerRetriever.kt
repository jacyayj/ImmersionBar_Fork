package com.gyf.immersionbar

import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.util.*

/**
 * The type Request manager retriever.
 *
 * @author geyifeng
 * @date 2019 /4/12 4:21 PM
 */
internal object RequestManagerRetriever : Handler.Callback {
    private val mTag = ImmersionBar::class.java.name
    private val mHandler: Handler = Handler(Looper.getMainLooper(), this)

    private val mPendingFragments: MutableMap<FragmentManager, Fragment> = HashMap()
    private val mPendingSupportFragments: MutableMap<FragmentManager, SupportRequestManagerFragment> =
        HashMap()

    /**
     * Get immersion bar.
     *
     * @param activity the activity
     * @return the immersion bar
     */
    operator fun get(activity: AppCompatActivity): ImmersionBar? {
        val tag = mTag + System.identityHashCode(activity)
        return getSupportFragment(activity.supportFragmentManager, tag)?.get(activity)
    }

    /**
     * Get immersion bar.
     *
     * @param fragment the fragment
     * @param isOnly   the is only
     * @return the immersion bar
     */
    operator fun get(fragment: Fragment, isOnly: Boolean): ImmersionBar? {
        checkNotNull(fragment.activity, { "fragment.getActivity() is null" })
        if (fragment is DialogFragment) {
            checkNotNull(fragment.dialog, { "fragment.getDialog() is null" })
        }
        var tag = mTag
        if (isOnly) {
            tag += fragment.javaClass.name
        } else {
            tag += System.identityHashCode(fragment)
        }
        return getSupportFragment(fragment.childFragmentManager, tag)!![fragment]
    }


    /**
     * Get immersion bar.
     *
     * @param activity the activity
     * @param dialog   the dialog
     * @return the immersion bar
     */
    operator fun get(activity: AppCompatActivity, dialog: Dialog): ImmersionBar? {
        val tag = mTag + System.identityHashCode(dialog)
        return getSupportFragment(activity.supportFragmentManager, tag)?.get(activity, dialog)
    }

    /**
     * Destroy.
     *
     * @param fragment the fragment
     */
    fun destroy(fragment: Fragment?, isOnly: Boolean) {
        if (fragment == null) {
            return
        }
        var tag = mTag
        if (isOnly) {
            tag += fragment.javaClass.name
        } else {
            tag += System.identityHashCode(fragment)
        }
        getSupportFragment(fragment.childFragmentManager, tag, true)
    }

    /**
     * Destroy.
     *
     * @param activity the activity
     * @param dialog   the dialog
     */
    fun destroy(activity: AppCompatActivity, dialog: Dialog?) {
        val tag = mTag + System.identityHashCode(dialog)
        val fragment = getSupportFragment(activity.supportFragmentManager, tag, true)
        if (fragment != null) {
            fragment[activity, dialog]!!.onDestroy()
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        var handled = true
        when (msg.what) {
            ID_REMOVE_FRAGMENT_MANAGER -> {
                val fm = msg.obj as FragmentManager
                mPendingFragments.remove(fm)
            }
            ID_REMOVE_SUPPORT_FRAGMENT_MANAGER -> {
                val supportFm = msg.obj as FragmentManager
                mPendingSupportFragments.remove(supportFm)
            }
            else -> handled = false
        }
        return handled
    }

    private fun getFragment(fm: FragmentManager, tag: String): Fragment? {
        return getFragment(fm, tag, false)
    }

    private fun getFragment(
        fm: FragmentManager,
        tag: String,
        destroy: Boolean
    ): Fragment? {
        var fragment: Fragment? = fm.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = mPendingFragments[fm]
            if (fragment == null) {
                if (destroy) {
                    return null
                }
                fragment = RequestManagerFragment()
                mPendingFragments[fm] = fragment
                fm.beginTransaction().add(fragment, tag).commitAllowingStateLoss()
                mHandler.obtainMessage(ID_REMOVE_FRAGMENT_MANAGER, fm).sendToTarget()
            }
        }
        if (destroy) {
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss()
            return null
        }
        return fragment
    }

    private fun getSupportFragment(
        fm: FragmentManager,
        tag: String
    ): SupportRequestManagerFragment? {
        return getSupportFragment(fm, tag, false)
    }

    private fun getSupportFragment(
        fm: FragmentManager,
        tag: String,
        destroy: Boolean
    ): SupportRequestManagerFragment? {
        var fragment = fm.findFragmentByTag(tag) as SupportRequestManagerFragment?
        if (fragment == null) {
            fragment = mPendingSupportFragments[fm]
            if (fragment == null) {
                if (destroy) {
                    return null
                }
                fragment = SupportRequestManagerFragment()
                mPendingSupportFragments[fm] = fragment
                fm.beginTransaction().add(fragment, tag).commitAllowingStateLoss()
                mHandler.obtainMessage(ID_REMOVE_SUPPORT_FRAGMENT_MANAGER, fm).sendToTarget()
            }
        }
        if (destroy) {
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss()
            return null
        }
        return fragment
    }

    private const val ID_REMOVE_FRAGMENT_MANAGER = 1
    private const val ID_REMOVE_SUPPORT_FRAGMENT_MANAGER = 2

}