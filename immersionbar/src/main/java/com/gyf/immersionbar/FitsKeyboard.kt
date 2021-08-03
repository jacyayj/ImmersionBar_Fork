package com.gyf.immersionbar

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.widget.FrameLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.gyf.immersionbar.ImmersionBar

/**
 * 适配软键盘弹出问题
 *
 * @author geyifeng
 * @date 2018/11/9 10:24 PM
 */
internal class FitsKeyboard(private val immersionBar: ImmersionBar) : OnGlobalLayoutListener {
    private val mWindow: Window? = immersionBar.window
    private val mDecorView: View = mWindow!!.decorView
    private val mContentView: View
    private var mChildView: View? = null
    private var mPaddingLeft = 0
    private var mPaddingTop = 0
    private var mPaddingRight = 0
    private var mPaddingBottom = 0
    private var mTempKeyboardHeight = 0
    private var mIsAddListener = false
    fun enable(mode: Int) {
        mWindow!!.setSoftInputMode(mode)
        if (!mIsAddListener) {
            mDecorView.viewTreeObserver.addOnGlobalLayoutListener(this)
            mIsAddListener = true
        }
    }

    fun disable() {
        if (mIsAddListener) {
            if (mChildView != null) {
                mContentView.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom)
            } else {
                mContentView.setPadding(
                    immersionBar.paddingLeft,
                    immersionBar.paddingTop,
                    immersionBar.paddingRight,
                    immersionBar.paddingBottom
                )
            }
        }
    }

    fun cancel() {
        if (mIsAddListener) {
            mDecorView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            mIsAddListener = false
        }
    }

    override fun onGlobalLayout() {
        if (immersionBar.barParams.keyboardEnable) {
            val barConfig = immersionBar.barConfig
            var bottom = 0
            var keyboardHeight: Int
            val navigationBarHeight =
                if (barConfig.isNavigationAtBottom) barConfig.navigationBarHeight else barConfig.navigationBarWidth
            var isPopup = false
            val rect = Rect()
            //获取当前窗口可视区域大小
            mDecorView.getWindowVisibleDisplayFrame(rect)
            keyboardHeight = mContentView.height - rect.bottom
            if (keyboardHeight != mTempKeyboardHeight) {
                mTempKeyboardHeight = keyboardHeight
                if (!ImmersionBar.Companion.checkFitsSystemWindows(
                        mWindow!!.decorView.findViewById<View>(
                            android.R.id.content
                        )
                    )
                ) {
                    if (mChildView != null) {
                        if (immersionBar.barParams.isSupportActionBar) {
                            keyboardHeight += immersionBar.actionBarHeight + barConfig.statusBarHeight
                        }
                        if (immersionBar.barParams.fits) {
                            keyboardHeight += barConfig.statusBarHeight
                        }
                        if (keyboardHeight > navigationBarHeight) {
                            bottom = keyboardHeight + mPaddingBottom
                            isPopup = true
                        }
                        mContentView.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, bottom)
                    } else {
                        bottom = immersionBar.paddingBottom
                        keyboardHeight -= navigationBarHeight
                        if (keyboardHeight > navigationBarHeight) {
                            bottom = keyboardHeight + navigationBarHeight
                            isPopup = true
                        }
                        mContentView.setPadding(
                            immersionBar.paddingLeft,
                            immersionBar.paddingTop,
                            immersionBar.paddingRight,
                            bottom
                        )
                    }
                } else {
                    keyboardHeight -= navigationBarHeight
                    if (keyboardHeight > navigationBarHeight) {
                        isPopup = true
                    }
                }
                if (keyboardHeight < 0) {
                    keyboardHeight = 0
                }
                if (immersionBar.barParams.onKeyboardListener != null) {
                    immersionBar.barParams.onKeyboardListener?.onKeyboardChange(
                        isPopup,
                        keyboardHeight
                    )
                }
                if (!isPopup && immersionBar.barParams.barHide != BarHide.FLAG_SHOW_BAR) {
                    immersionBar.setBar()
                }
            }
        }
    }

    init {
        val frameLayout = mDecorView.findViewById<FrameLayout>(android.R.id.content)
        if (immersionBar.isDialogFragment) {
            val supportFragment = immersionBar.dialogFragment
            if (supportFragment != null) {
                mChildView = supportFragment.view
            } else {
                val fragment = immersionBar.fragment
                if (fragment != null) {
                    mChildView = fragment.view
                }
            }
        } else {
            mChildView = frameLayout.getChildAt(0)
            if (mChildView != null) {
                if (mChildView is DrawerLayout) {
                    mChildView = (mChildView as DrawerLayout).getChildAt(0)
                }
            }
        }
        mContentView = mChildView?.let {
            mPaddingLeft = it.paddingLeft
            mPaddingTop = it.paddingTop
            mPaddingRight = it.paddingRight
            mPaddingBottom = it.paddingBottom
            it
        } ?: frameLayout
    }
}