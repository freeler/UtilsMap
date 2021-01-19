package com.freeler.demoutilsmap

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * @Author Edward
 * @Date 2019/4/3
 */

private val TAG = "ResultHelper"

fun FragmentActivity.startForResult(intent: Intent, listener: (resultCode: Int, data: Intent?) -> Unit) {
    getFragment().startForResult(intent, listener)
}

fun Fragment.startForResult(intent: Intent, listener: (resultCode: Int, data: Intent?) -> Unit) {
    activity?.getFragment()?.startForResult(intent, listener)
}

fun FragmentActivity.requestPermission(vararg permissions: String, listener: (isGrant: Boolean) -> Unit) {
    getFragment().getPermissions(*permissions) { listener.invoke(it) }
}

fun Fragment.requestPermission(vararg permissions: String, listener: (isGrant: Boolean) -> Unit) {
    activity?.getFragment()?.getPermissions(*permissions) { listener.invoke(it) }
}


/**
 * 获取/创建 ForResultFragment
 */
private fun FragmentActivity.getFragment(): ForResultFragment {
    var fragment = supportFragmentManager.findFragmentByTag(TAG)
    if (fragment == null) {
        fragment = ForResultFragment()
        supportFragmentManager.beginTransaction().add(fragment, TAG).commitAllowingStateLoss()
        supportFragmentManager.executePendingTransactions()
    }
    return fragment as ForResultFragment
}

/**
 * ForResultFragment
 */
class ForResultFragment : Fragment(), LifecycleObserver {

    private val resultMap = mutableMapOf<Int, (Int, Intent?) -> Unit>()
    private val permissionMap = mutableMapOf<Int, (Boolean) -> Unit>()
    private var requestCode = 0
    private var resultCode = 0
    private var data: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(this)
    }

    /***
     * 跳转Activity
     */
    fun startForResult(intent: Intent, listener: (Int, Intent?) -> Unit) {
        val requestCode = resultMap.size + 1
        resultMap[requestCode] = listener
        startActivityForResult(intent, requestCode)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResume() {
        val listener = resultMap.remove(requestCode)
        listener?.invoke(resultCode, data)
    }


    /**
     * Activity 返回结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        this.requestCode = requestCode
        this.resultCode = resultCode
        this.data = data
    }

    /**
     * 是否有权限
     */
    private fun isGranted(vararg permissions: String): Boolean {
        val checkResult = permissions.map { ActivityCompat.checkSelfPermission(context!!, it) }
        return !checkResult.contains(PackageManager.PERMISSION_DENIED)
    }

    /**
     * 请求权限
     */
    fun getPermissions(vararg permissions: String, listener: (Boolean) -> Unit) {
        //已有权限
        if (isGranted(*permissions)) {
            listener.invoke(true)
            return
        }
        //申请权限
        val requestCode = permissionMap.size + 1
        permissionMap[requestCode] = listener
        requestPermissions(permissions, requestCode)
    }

    /**
     * 权限返回
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val listener = permissionMap.remove(requestCode)
        val isGrant = !grantResults.contains(PackageManager.PERMISSION_DENIED)
        listener?.invoke(isGrant)
    }


    override fun onDetach() {
        super.onDetach()
        activity?.lifecycle?.removeObserver(this)
    }

}