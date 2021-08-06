package com.echo.library.network;


import android.app.Activity;

import com.echo.library.util.DialogUtils;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/7/16
 * change   :
 * describe :
 */
public class Net {


    public static class Factory {
        public static <T> T create(Class<T> t, String baseUrl) {
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(MyGsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(getOkHttpClient())
                    .baseUrl(baseUrl)
                    .build();
            return retrofit.create(t);
        }
    }

    public Net setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        return this;
    }

    static OkHttpClient okHttpClient;

    public static OkHttpClient getOkHttpClient() {

        return okHttpClient;
    }

    private static final String lastModified = "last-modified";

    //last-modified
    public static void getLastModified(Activity activity, String url, StateCallBack<String> callBack) {
        DialogUtils.showProgressDialog(activity);
        RxUtil.execute(Observable.create(emitter -> {
            final Request request = new Request.Builder()
                    .url(url)
                    .head()
                    .build();
            Call call = getOkHttpClient().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    emitter.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    emitter.onNext(response);
                }
            });
        }), new Observer<Response>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Response response) {
                callBack.onSuccess(response.headers().get(lastModified));
                DialogUtils.hideProgressDialog();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                callBack.onError(ResponseMessage.error(e.getMessage()));
                DialogUtils.hideProgressDialog();
            }

            @Override
            public void onComplete() {

            }
        });

    }

    public interface Header {
        //缓存请求
        String cache_request = "cache_request";
        String auto_cache_request = cache_request + ":1";
        //避免同一时间多个访问（只进行第一个访问）
        String once_same_time = "once_same_time";
        String auto_once_same_time = once_same_time + ":1";
    }


}
