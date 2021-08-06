package com.echo.library;


import com.echo.library.util.CommonUtils;

import java.util.HashMap;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2021/6/29
 * change   :
 * describe :
 */
public class InvokeManager {
    //请求最小间隔
    public static final int MinimumIntervalTime = 500;

    static final HashMap<String, Long> map = new HashMap<>();

    public static boolean canInvoke() {
        StackTraceElement[] traceElement = new Throwable().getStackTrace();
        String invokeMethod = traceElement[1].toString();
        Long lastInvokeTime = map.get(invokeMethod);
        if (lastInvokeTime == null) {
            lastInvokeTime = 0L;
        }
        long now = System.currentTimeMillis();
        long intervalTime = now - lastInvokeTime;
        CommonUtils.log(invokeMethod, "lastInvokeTime", lastInvokeTime, "intervalTime", intervalTime);
        if (intervalTime > MinimumIntervalTime) {
            map.put(invokeMethod, now);
            return true;
        }
        return false;
    }
}
