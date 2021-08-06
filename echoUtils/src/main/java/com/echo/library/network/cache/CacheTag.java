package com.echo.library.network.cache;

import android.content.Context;
import android.content.SharedPreferences;

import com.echo.library.util.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * 保存当前个请求（uuid） 的重试次数
 */
public class CacheTag implements Comparable<CacheTag> {


    /**
     * 重连时间间隔 单位S
     * 1分鍾，10分鍾，30分鍾，60分鍾，3小時，6小時
     */
    public final static int[] DELAY = {60, 10 * 60, 30 * 60, 60 * 60, 3 * 60 * 60, 6 * 60 * 60};
    //    public final static int[] DELAY = {2, 4, 6, 8, 10, 12};
    private final String uuid;

    ///是否正在请求
    transient boolean onRequest = false;
    //第一次重试，不等待
    boolean isFirst = true;
    RetryBean retryBean;

    //已有
    public CacheTag(String uuid) {
        this.uuid = uuid;
        retryBean = new Gson().fromJson(RequestCache.getInstance().getPreference().getString(uuid, "{}"), RetryBean.class);
    }

    //新建
    public CacheTag() {
        this(UUID.randomUUID().toString());
    }

    String getUuid() {
        return uuid;
    }


    int getRetryDelay() {
        if (isFirst) {
            return 0;
        }
        if (retryBean.retryCount < 0 || retryBean.retryCount >= DELAY.length) {
            delete();
            return 0;
        }
        int outOfTime = (int) ((System.currentTimeMillis() - retryBean.retryTimeMillis) / 1000);
        outOfTime = Math.max(0, outOfTime);
        int waitTime = Math.max(0, DELAY[retryBean.retryCount] - outOfTime);
        CommonUtils.log(waitTime, outOfTime, DELAY[retryBean.retryCount]);
        return waitTime;
    }

    private void overOneRequest() {
        CommonUtils.log("overOneRequest", this);
        retryBean.retryTimeMillis = System.currentTimeMillis();
        if (isFirst) {
            isFirst = false;
        } else {
            retryBean.retryCount++;
        }
        if (canRetry()) {
            save();
        } else {
            RequestCache.getInstance().delete(this);
        }
    }

    boolean canRetry() {
        return retryBean.retryCount < DELAY.length && retryBean.retryCount > -1;
    }


    ///主动失效避免后续重试
    public void delete() {
        retryBean.retryCount = 100;
        RequestCache.getInstance().getPreference().edit().remove(uuid).apply();
    }

    public void save() {
        CommonUtils.log(this);
        RequestCache.getInstance().getPreference().edit().putString(uuid, new Gson().toJson(retryBean)).apply();
    }


    @Override
    public int compareTo(CacheTag o) {
        return uuid.compareTo(o.uuid);
    }

    public static void clear() {
        RequestCache.getInstance().getPreference().edit().clear().apply();
    }


    public static void requestOverOne(Object tag) {
        if (!(tag instanceof CacheTag)) {
            return;
        }
        CacheTag tag1 = (CacheTag) tag;
        tag1.overOneRequest();
    }

    @Override
    public String toString() {
        return "CacheTag{" +
                ", hashCode=" + hashCode() +
                ", uuid='" + uuid + '\'' +
                ", onRequest=" + onRequest +
                ", isFirst=" + isFirst +
                ", retryBean=" + retryBean +
                '}';
    }

    public static class RetryBean {
        ///保存上一次请求时间
        @SerializedName("retryTimeMillis")
        long retryTimeMillis;
        //已重试请求次数
        @SerializedName("retryCount")
        int retryCount = 0;

        @Override
        public String toString() {
            return "RetryBean{" +
                    "retryTimeMillis=" + retryTimeMillis +
                    ", retryCount=" + retryCount +
                    '}';
        }
    }
}
