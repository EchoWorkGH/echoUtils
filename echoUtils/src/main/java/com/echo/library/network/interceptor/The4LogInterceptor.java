package com.echo.library.network.interceptor;


import com.echo.library.network.Utils;
import com.echo.library.network.cache.RequestCache;
import com.echo.library.util.CommonUtils;
import com.echo.library.util.StringUtils;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/6
 * change   :
 * describe :
 */
public class The4LogInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        String requestLog = String.format("Request %s%n%s",
                request.url(),
                StringUtils.bodyToString(request));
        CommonUtils.logECodeT(request.method(), requestLog);
        Response response = chain.proceed(request);
        String responseBody = Utils.justGetString(response);
        Response networkResponse = response.networkResponse();
        String responseLog = String.format(Locale.ENGLISH,
                "Response %s %s responseTime=%dms%n%s",
                networkResponse != null ? networkResponse.code() : response.code(),
                response.request().url(),
                response.receivedResponseAtMillis() - response.sentRequestAtMillis(),
                responseBody);
        CommonUtils.logECodeT(responseLog);
        if (request.method().equalsIgnoreCase("HEAD")) {
            CommonUtils.log(response.headers());
        }
        response = response.newBuilder()
                .body(ResponseBody.create(response.body().contentType(), responseBody))
                .build();
        RequestCache.getInstance().load();
        return response;
    }
}

