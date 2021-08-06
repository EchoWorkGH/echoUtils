package com.echo.library.network.cache;

import android.text.TextUtils;

import com.echo.library.util.CommonUtils;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okio.Buffer;


/**
 * 序列化Request,保存
 */
class SerializableRequest implements Serializable {

    @SerializedName("url")
    private final String url;

    @SerializedName("body")
    private final String body;

    @SerializedName("method")
    private final String method;
    @SerializedName("header")
    private final String header;

    SerializableRequest(Request request) {
        url = request.url().toString();
        body = bodyToString(request);
        method = request.method();
        header = request.headers().toString();
    }


    Request getRequest(CacheTag cacheTag) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .tag(cacheTag);
        if (!method.equalsIgnoreCase("GET")) {
            FormBody.Builder builder = new FormBody.Builder();
            for (String kv : body.split("&")) {
                String[] value = kv.split("=");
                if (value.length != 2) {
                    continue;
                }
                builder.addEncoded(value[0], value[1]);
            }
            requestBuilder.method(method, builder.build());
        }
        if (!TextUtils.isEmpty(header)) {
            String[] headers = header.split("\n");
            for (String s : headers) {
                String[] kv = s.split(": ");
                if (kv.length < 2) {
                    continue;
                }
                requestBuilder.addHeader(kv[0], kv[1]);
            }
        }
        return requestBuilder
                .build();
    }

    @Override
    public String toString() {
        return "SerializableRequest{" +
                "url='" + url + '\'' +
                ", body='" + body + '\'' +
                ", method='" + method + '\'' +
                '}';
    }

    public static String bodyToString(Request request) {
        CommonUtils.logStackTrace();
        if (request == null) {
            return "";
        }
        if (request.body() instanceof MultipartBody) {
            return "MultipartBody";
        }
        try {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (Exception e) {
            return "";
        }
    }
}
