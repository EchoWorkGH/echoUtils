package com.echo.library.network.cache;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.echo.library.network.Net;
import com.echo.library.util.CommonUtils;
import com.echo.library.util.FileUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * author   : dongjunjie.mail@qq.com
 * time     : 2020/10/16
 * change   :
 * describe :
 */
public class RequestCache implements Runnable {

    private static final String REQUEST_CACHE_FILE = "Request";
    private static final String EXTENSION = ".cacheLog";
    private SortedSet<CacheTag> tags = new TreeSet<>();
    private File dir;
    static RequestCache instance;

    private final static String CacheTagSharedPreferences = "CacheTag";


    private SharedPreferences preference;
    Builder builder;

    public static RequestCache init(Builder builder) {
        instance = new RequestCache(builder);
        return instance;
    }

    public static boolean isInit() {
        return instance != null;
    }

    public static RequestCache getInstance() {
        if (instance == null) {
            throw new IllegalStateException("YOU MUST CALL INIT FIRST!!!!");
        }
        return instance;
    }


    RequestCache(Builder builder) {
        this.builder = builder;
        this.preference = builder.context.getSharedPreferences(CacheTagSharedPreferences, Context.MODE_PRIVATE);
        dir = new File(builder.context.getCacheDir(), REQUEST_CACHE_FILE);
        if (!dir.exists()) {
            dir.mkdir();
        }
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(EXTENSION)) {
                tags.add(new CacheTag(FileUtils.getNameWithoutExtension(file.getName())));
            }
        }
        CommonUtils.log(tags);
    }

    private File getFile(String tag) {
        return new File(dir, tag + EXTENSION);
    }

    public void store(Request request) {
        Object tag0 = request.tag();
        CommonUtils.log(tag0, request.headers());
        if (!(tag0 instanceof CacheTag)) {
            return;
        }
        SerializableRequest cacheRequest = new SerializableRequest(request);
        try {
            CacheTag tag = (CacheTag) tag0;
            File file = getFile(tag.getUuid());
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(new Gson().toJson(cacheRequest).getBytes());
            fos.close();
            CommonUtils.log("store file=" + file.getName());
            tags.add(tag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete(Object tag) {
        CommonUtils.log("delete", tag);
        if (tag instanceof CacheTag) {
            File file = getFile(((CacheTag) tag).getUuid());
            if (file.exists()) {
                CommonUtils.log("delete file=" + file.getName());
                file.delete();
            }
            ((CacheTag) tag).delete();
            tags.remove(tag);
        }
    }

    public void load() {
        run();
    }

    @SuppressLint("CheckResult")
    @Override
    public void run() {
        CommonUtils.log("RequestCache_run", tags);
        if (tags.isEmpty()) {
            return;
        }
        CacheTag[] tagList = tags.toArray(new CacheTag[0]);
        Observable.zip(Observable.interval(5, TimeUnit.SECONDS),
                Observable.fromArray(tagList),
                (aLong, cacheTag) -> cacheTag)
                .subscribe(this::doOne);

    }

    @SuppressLint("CheckResult")
    private void doOne(final CacheTag tag) {
        CommonUtils.log("doOne", tag);
        if (tag.onRequest) {
            return;
        }
        tag.onRequest = true;
        final Request request = getRequest(tag);
        if (request == null) {
            tag.onRequest = false;
            return;
        }
        CommonUtils.log("doOne Request", request);
        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
            CommonUtils.log("START", tag);
            Net.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    emitter.onNext(false);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    emitter.onNext(true);
                }
            });
        })
                .delay(tag.getRetryDelay(), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(aBoolean -> {
                    tag.onRequest = false;
                });
    }

    /**
     * 判断权限成功后，获取正确的请求
     */
    private Request getRequest(CacheTag tag) {
        if (!tag.canRetry()) {
            delete(tag);
            return null;
        }
        File file = getFile(tag.getUuid());
        if (!file.exists()) {
            CommonUtils.log(file.getName() + " is not exist.");
            delete(tag);
            return null;
        }
        final Request request;
        try {
            SerializableRequest raw = new Gson().fromJson(FileUtils.readContent(file), SerializableRequest.class);
            CommonUtils.log(raw);
            request = raw.getRequest(tag);
            CommonUtils.log("request.headers()", request.headers().toString());
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtils.log("WgCache", "Parse request failed");
            delete(tag);
            return null;
        }
        CommonUtils.log("WgCache", request.headers());
        if (builder.checkCache == null || builder.checkCache.checkRight(request)) {
            return request;
        }
        return null;

    }

    public boolean hasCache() {
        return !tags.isEmpty();
    }

    public void clear() {
        CommonUtils.log("clear");
        tags.clear();
        CacheTag.clear();
        preference.edit().clear().apply();
        // Returns code null if this abstract pathname does not denote a
        // directory, or if an I/O error occurs.
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(EXTENSION)) {
                    file.delete();
                }
            }
        }
    }


    public SharedPreferences getPreference() {
        return preference;
    }

    public boolean isNeedCache(Request request) {
        return true;
    }

    public void addFlagAfterCache(Request request, Request.Builder builder) {

    }


    public void checkResponse(Request request, String body, Response response) {
        if (request.tag() == null) {
            return;
        }
        if (builder.checkResponse == null) {
            return;
        }
        if (builder.checkResponse.checkResponse(response)) {
            RequestCache.getInstance().delete(request.tag());
        } else {
            CacheTag.requestOverOne(request.tag());
        }
    }


    public interface CheckRight {

        //判断当前request是否有效  用户是否相同，是否登录等等
        boolean checkRight(Request request);
    }

    public interface CheckNeedCache {

        //判断当前request是否有效  用户是否相同，是否登录等等
        boolean checkNeedCache(Request request);
    }

    public interface CheckResponse {

        //判断当前request是否有效  用户是否相同，是否登录等等
        boolean checkResponse(Response response);
    }

    public static Builder make(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        Context context;
        CheckRight checkCache;
        CheckResponse checkResponse;
        CheckNeedCache checkNeedCache;

        Builder(Context context) {
            this.context = context;
        }

        public Builder setCheckCache(CheckRight checkCache) {
            this.checkCache = checkCache;
            return this;
        }

        public Builder setCheckResponse(CheckResponse checkResponse) {
            this.checkResponse = checkResponse;
            return this;
        }

        public Builder setCheckNeedCache(CheckNeedCache checkNeedCache) {
            this.checkNeedCache = checkNeedCache;
            return this;
        }

    }
}
