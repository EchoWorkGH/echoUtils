package com.echo.library.network

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.echo.library.util.CommonUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/12/7
 * change   :
 * describe : 负责管理订阅
 */
open class Subscription {

    var mActivity: Activity? = null
    var mFragment: Fragment? = null
    var mView: View? = null

    var mSubscriptions: CompositeDisposable? = CompositeDisposable()

    constructor(mActivity: Activity) {
        initActivity(mActivity)
    }

    constructor(fragment: Fragment) {
        this.mFragment = fragment
        try {
            fragment.parentFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentDestroyed(fm, f)
                    if (f === mFragment) {
                        CommonUtils.log("BaseCarDiscoveryFragment onFragmentDestroyed ", mFragment?.javaClass?.name, mFragment?.hashCode())
                        close()
                    }
                    fm.unregisterFragmentLifecycleCallbacks(this)
                }
            }, false)
            return
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        fragment.activity?.run { initActivity(this) }
    }

    constructor(view: View) {
        this.mView = view
        view.viewTreeObserver.addOnWindowAttachListener(
                object : ViewTreeObserver.OnWindowAttachListener {
                    override fun onWindowAttached() {
                    }

                    override fun onWindowDetached() {
                        close()
                    }
                })
        if (view.context is Activity) {
            initActivity(view.context as Activity)
        }
    }


    private fun initActivity(activity: Activity) {
        mActivity = activity
        activity.application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (mActivity === activity) {
                    mActivity!!.application.unregisterActivityLifecycleCallbacks(this)
                    close()
                }
            }
        })
    }


    val context: Context?
        get() = mActivity

    fun close() {
        mSubscriptions?.dispose()
    }

    fun addSubscription(disposable: Disposable) {
        getSubscriptions().add(disposable)
    }

    fun getSubscriptions(): CompositeDisposable {
        return mSubscriptions?.run {
            if (isDisposed) {
                CompositeDisposable()
            } else {
                this
            }
        } ?: CompositeDisposable()
    }


}