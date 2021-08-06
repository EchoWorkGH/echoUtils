package com.echo.library.network.interceptor;


import com.echo.library.Data2;
import com.echo.library.network.Net;
import com.echo.library.network.Utils;
import com.echo.library.util.CommonUtils;
import com.echo.library.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/6
 * change   :
 * describe : 避免一个接口同时调用。{@link Net.Header#auto_once_same_time}
 */
public class The0JustOnceInSameTimeInterceptor implements Interceptor {

    final static HashMap<String, Data2<Long, Response>> hashMap = new HashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (chain.request().header(Net.Header.once_same_time) == null) {
            return chain.proceed(chain.request());
        }
        Request request = chain.request();
        String requestLog = String.format("Request %s%n%s",
                request.url(),
                StringUtils.bodyToString(request));
        CommonUtils.log(requestLog, Thread.currentThread().getId(), Thread.currentThread().getName());
        Data2<Long, Response> data2;
        boolean first = false;
        synchronized (Data2.class) {
            data2 = hashMap.get(requestLog);
            if (data2 == null) {
                first = true;
                data2 = new Data2<>(0L, null);
                hashMap.put(requestLog, data2);
            } else {
                data2.a++;
            }
            CommonUtils.log(requestLog, data2, first);
        }
        if (!first) {
            theWait();
            CommonUtils.log(data2.a, Thread.currentThread().getId(), Thread.currentThread().getName());
            return Utils.clone(data2.b);
        }
        Response theResponse = chain.proceed(request);
        data2.b = theResponse;
        theNotify();
        hashMap.remove(requestLog);
        return theResponse;
    }


    private void theNotify() {
        synchronized (The0JustOnceInSameTimeInterceptor.class) {
            CommonUtils.log("notifyAll");
            The0JustOnceInSameTimeInterceptor.class.notifyAll();
        }
    }

    private void theWait() {
        synchronized (The0JustOnceInSameTimeInterceptor.class) {
            try {
                CommonUtils.log("wait");
                The0JustOnceInSameTimeInterceptor.class.wait();
                CommonUtils.log("wait-run");
            } catch (InterruptedException e) {
                CommonUtils.log(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}


