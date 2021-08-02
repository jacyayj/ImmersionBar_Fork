package com.gyf.immersionbar.components

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Fragment快速实现沉浸式的代理类
 *
 * @author geyifeng
 * @date 2018/11/15 12:53 PM
 */
class SimpleImmersionProxy(
    /**
     * 要操作的Fragment对象
     */
    private var mFragment: Fragment?
) {
    /**
     * 沉浸式实现接口
     */
    private var mSimpleImmersionOwner: SimpleImmersionOwner? = null

    /**
     * Fragment的view是否已经初始化完成
     */
    private var mIsActivityCreated = false
    fun onActivityCreated(savedInstanceState: Bundle?) {
        mIsActivityCreated = true
        setImmersionBar()
    }

    fun onDestroy() {
        mFragment = null
        mSimpleImmersionOwner = null
    }

    fun onConfigurationChanged(newConfig: Configuration?) {
        setImmersionBar()
    }

    fun onHiddenChanged(hidden: Boolean) {
        if (mFragment != null) {
            mFragment!!.userVisibleHint = !hidden
        }
    }

    /**
     * 是否已经对用户可见
     * Is user visible hint boolean.
     *
     * @return the boolean
     */
    var isUserVisibleHint: Boolean
        get() = if (mFragment != null) {
            mFragment!!.userVisibleHint
        } else {
            false
        }
        set(isVisibleToUser) {
            setImmersionBar()
        }

    private fun setImmersionBar() {
        if (mFragment != null && mIsActivityCreated && mFragment!!.userVisibleHint
            && mSimpleImmersionOwner!!.immersionBarEnabled()
        ) {
            mSimpleImmersionOwner!!.initImmersionBar()
        }
    }

    init {
        if (mFragment is SimpleImmersionOwner) {
            mSimpleImmersionOwner = mFragment as SimpleImmersionOwner?
        } else {
            throw IllegalArgumentException("Fragment请实现SimpleImmersionOwner接口")
        }
    }
}