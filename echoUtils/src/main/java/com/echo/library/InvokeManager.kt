package com.echo.library

import com.echo.library.util.CommonUtils
import java.util.*

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2021/6/29
 * change   :
 * describe :
 */
object InvokeManager {
    //请求最小间隔
    private const val MinimumIntervalTime = 500
    private val map = HashMap<String, Long>()
    fun canInvoke(): Boolean {
        val traceElement = Throwable().stackTrace
        val invokeMethod = traceElement[1].toString()
        var lastInvokeTime = map[invokeMethod]
        if (lastInvokeTime == null) {
            lastInvokeTime = 0L
        }
        val now = System.currentTimeMillis()
        val intervalTime = now - lastInvokeTime
        CommonUtils.log(invokeMethod, "lastInvokeTime", lastInvokeTime, "intervalTime", intervalTime)
        if (intervalTime > MinimumIntervalTime) {
            map[invokeMethod] = now
            return true
        }
        return false
    }
}