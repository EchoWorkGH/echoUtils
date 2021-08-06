package com.echo.library.network.cache;

import android.text.TextUtils;

import com.echo.library.Data2;
import com.echo.library.network.Net;
import com.echo.library.util.CommonUtils;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/6
 * change   :
 * describe : 用于重试的拦截器
 */
public class The0CacheInterceptor implements Interceptor {
    public static boolean test = false;
    Response response;
    Request request;

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (!RequestCache.isInit()) {
            return chain.proceed(chain.request());
        }
        request = chain.request();
        if (!RequestCache.getInstance().isNeedCache(request)) {
            return chain.proceed(request);
        }
        boolean needCache = request.header(Net.Header.cache_request) != null;
        CommonUtils.logECodeT("headers", request.headers().toString(), request.header(Net.Header.cache_request), request.url(), request.tag());
        if (needCache && request.tag() == null) {
            Request.Builder builder = request.newBuilder().tag(new CacheTag());
            RequestCache.getInstance().addFlagAfterCache(request, builder);
            request = builder.build();
            RequestCache.getInstance().store(request);
        }
        response = chain.proceed(request);
        String body = null;
        try {
            body = response.body().string();
            response = response.newBuilder()
                    .body(ResponseBody.create(response.body().contentType(), body))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RequestCache.getInstance().checkResponse(request, body, response);
        return response;
    }


}


